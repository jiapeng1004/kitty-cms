# 基于Schema的多租户隔离方案

## 1. 方案概述

本方案旨在为Kitty CMS系统实现基于数据库Schema的多租户隔离机制，使系统能够在单个应用实例中安全、高效地服务多个租户，每个租户的数据完全隔离在独立的数据库Schema中。

## 2. 技术背景

当前系统使用MySQL数据库，通过R2DBC进行连接，数据库配置如下：

```yaml
spring:
  r2dbc:
    url: r2dbc:mysql://localhost:3306/mediaboot
    username: root
    password: root
```

## 3. 方案设计

### 3.1 核心概念

- **租户(Tenant)**：系统的独立使用方，拥有自己的数据和配置
- **Schema**：在MySQL中对应数据库(database)，用于隔离不同租户的数据
- **租户标识符(Tenant ID)**：每个租户的唯一标识，用于确定数据存储的Schema

### 3.2 架构设计

1. **租户管理模块**：负责租户的创建、查询、更新和删除
2. **Schema管理模块**：负责为新租户创建数据库Schema，以及Schema的生命周期管理
3. **数据源路由模块**：根据租户ID动态路由到对应的数据库Schema
4. **权限控制模块**：确保租户只能访问自己的数据

### 3.3 数据库设计

#### 3.3.1 共享Schema

创建一个共享的`kitty_cms` Schema，用于存储系统级数据，如：

- `tenant`表：存储租户信息
- `user`表：存储系统用户信息（与租户关联）

#### 3.3.2 租户专用Schema

为每个租户创建独立的Schema，命名规则：`tenant_{tenant_id}`，包含：

- 业务相关表（如配置、内容等）
- 租户专用的用户表（如果需要）

## 4. 技术实现

### 4.1 数据源配置

修改`application.yml`配置，使用多数据源配置：

```yaml
spring:
  r2dbc:
    url: r2dbc:mysql://localhost:3306/kitty_cms
    username: root
    password: root
  data:
    r2dbc:
      repositories:
        enabled: true
```

### 4.2 租户管理

#### 4.2.1 租户实体

```java
@Entity
@Table(name = "tenant")
public class Tenant {
    @Id
    private Long id;
    private String name;
    private String schemaName;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    // getter, setter
}
```

#### 4.2.2 租户服务

```java
@Service
public class TenantService {
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private SchemaManager schemaManager;
    
    public Tenant createTenant(String name) {
        Tenant tenant = new Tenant();
        tenant.setName(name);
        String schemaName = "tenant_" + System.currentTimeMillis();
        tenant.setSchemaName(schemaName);
        tenant.setStatus("ACTIVE");
        tenant.setCreatedTime(LocalDateTime.now());
        tenant.setUpdatedTime(LocalDateTime.now());
        
        Tenant savedTenant = tenantRepository.save(tenant);
        schemaManager.createSchema(schemaName);
        schemaManager.initSchema(schemaName);
        
        return savedTenant;
    }
    
    // 其他方法：getTenant, updateTenant, deleteTenant
}
```

### 4.3 Schema管理

```java
@Service
public class SchemaManager {
    @Autowired
    private ConnectionFactory connectionFactory;
    
    public void createSchema(String schemaName) {
        Mono.from(connectionFactory.create())
            .flatMap(connection -> Mono.from(connection.createStatement("CREATE DATABASE IF NOT EXISTS " + schemaName).execute()))
            .block();
    }
    
    public void initSchema(String schemaName) {
        // 执行初始化SQL脚本，创建表结构
        // 可以从资源文件读取SQL脚本并执行
    }
    
    public void dropSchema(String schemaName) {
        Mono.from(connectionFactory.create())
            .flatMap(connection -> Mono.from(connection.createStatement("DROP DATABASE IF EXISTS " + schemaName).execute()))
            .block();
    }
}
```

### 4.4 数据源路由

#### 4.4.1 租户上下文

```java
public class TenantContext {
    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();
    
    public static void setCurrentTenant(Long tenantId) {
        currentTenant.set(tenantId);
    }
    
    public static Long getCurrentTenant() {
        return currentTenant.get();
    }
    
    public static void clear() {
        currentTenant.remove();
    }
}
```

#### 4.4.2 租户过滤器

```java
@Component
public class TenantFilter implements WebFilter {
    @Autowired
    private TenantService tenantService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 从请求头或Token中获取租户ID
        String tenantIdStr = exchange.getRequest().getHeaders().getFirst("X-Tenant-ID");
        if (tenantIdStr != null) {
            try {
                Long tenantId = Long.parseLong(tenantIdStr);
                TenantContext.setCurrentTenant(tenantId);
            } catch (NumberFormatException e) {
                // 处理异常
            }
        }
        
        return chain.filter(exchange).doFinally(signalType -> TenantContext.clear());
    }
}
```

#### 4.4.3 动态数据源路由

```java
@Component
public class TenantConnectionFactory implements ConnectionFactory {
    @Autowired
    private ConnectionFactory targetConnectionFactory;
    @Autowired
    private TenantRepository tenantRepository;
    
    @Override
    public ConnectionFactoryMetadata getMetadata() {
        return targetConnectionFactory.getMetadata();
    }
    
    @Override
    public Mono<Connection> create() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Tenant tenant = tenantRepository.findById(tenantId).block();
            if (tenant != null) {
                // 修改连接的Schema
                return targetConnectionFactory.create()
                    .flatMap(connection -> Mono.from(connection.createStatement("USE " + tenant.getSchemaName()).execute())
                    .then(Mono.just(connection)));
            }
        }
        return targetConnectionFactory.create();
    }
}
```

### 4.5 权限控制

#### 4.5.1 基于租户的权限检查

```java
@Aspect
@Component
public class TenantPermissionAspect {
    @Around("execution(* icu.jiapeng.kitty.cms.service.*.*(..))")
    public Object checkTenantPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Long currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            throw new PermissionDeniedException("Tenant ID is required");
        }
        // 执行方法
        return joinPoint.proceed();
    }
}
```

## 5. 代码修改建议

### 5.1 核心模块修改

1. **kitty-common-core**：添加租户上下文和工具类
2. **kitty-cms-api**：添加租户管理相关的API
3. **kitty-cms-func**：实现租户管理、Schema管理和数据源路由功能
4. **kitty-cms-server**：配置租户过滤器和动态数据源

### 5.2 数据库脚本

1. 创建共享Schema：`kitty_cms`
2. 创建租户表：`tenant`
3. 为每个租户创建独立的Schema和初始化表结构

## 6. 优缺点分析

### 6.1 优点

1. **数据隔离性强**：不同租户的数据存储在独立的Schema中，物理隔离，安全性高
2. **扩展性好**：可以根据租户的需求独立扩展其Schema
3. **维护成本低**：每个租户的Schema结构相同，便于统一管理和维护
4. **性能优化**：可以针对不同租户的Schema进行独立的性能优化

### 6.2 缺点

1. **初始设置复杂**：需要为每个租户创建Schema和初始化表结构
2. **数据库连接管理复杂**：需要动态管理不同Schema的连接
3. **跨租户查询困难**：需要特殊处理跨租户的数据查询
4. **资源消耗较大**：每个租户都需要独立的Schema，可能会消耗较多的数据库资源

## 7. 实施计划

### 7.1 阶段一：基础架构搭建

1. 创建共享Schema和租户表
2. 实现租户管理和Schema管理功能
3. 配置动态数据源路由

### 7.2 阶段二：核心功能改造

1. 修改现有API，支持多租户
2. 添加租户过滤器和权限控制
3. 测试核心功能

### 7.3 阶段三：扩展和优化

1. 添加租户管理界面
2. 优化数据库连接管理
3. 完善监控和日志

## 8. 结论

基于Schema的多租户隔离方案是一种成熟、可靠的多租户实现方式，适合Kitty CMS系统的需求。通过本方案的实施，可以实现不同租户数据的有效隔离，提高系统的安全性和可扩展性，同时保持代码的可维护性。

虽然本方案在初始设置和数据库连接管理方面较为复杂，但这些复杂性可以通过良好的设计和实现来克服。在实施过程中，需要注意数据库资源的合理使用，避免过度消耗数据库资源。