# Transcode Authentication 规范

## 1. 概述

本规范定义了转码服务的认证和授权机制，使用 Access Key（AK）和 Secret Key（SK）实现服务间和前端的认证。支持 Cookie 登录和 API 签名两种认证方式，确保 API 的安全性和可靠性。

## 2. 功能需求

### 2.1 Access Key 管理
- **创建 Access Key**：用户可以创建新的 Access Key 和 Secret Key
- **查询 Access Key**：用户可以查询自己的 Access Key 列表
- **禁用 Access Key**：用户可以禁用不再使用的 Access Key
- **删除 Access Key**：用户可以删除不再需要的 Access Key
- **Access Key 过期**：支持设置 Access Key 的过期时间

### 2.2 Cookie 登录方式
- **登录请求**：使用 AK/SK 进行登录
- **Cookie 返回**：登录成功后返回 Cookie
- **Cookie 验证**：后续请求使用 Cookie 进行认证
- **Cookie 过期**：Cookie 设置过期时间
- **Cookie 刷新**：支持 Cookie 刷新机制

### 2.3 API 签名方式
- **签名生成**：使用 AK/SK 生成请求签名
- **签名验证**：服务端验证请求签名的正确性
- **签名参数**：包含 Client ID、Timestamp、Signature
- **签名算法**：使用 HMAC-SHA1 算法
- **参数排序**：参数按 ASCII 正序拼接

### 2.4 认证流程
- **Cookie 登录流程**：AK/SK 登录 → 返回 Cookie → 使用 Cookie 访问 API
- **API 签名流程**：生成签名 → 发送请求 → 验证签名 → 返回结果

## 3. 非功能需求

### 3.1 安全性
- Secret Key 永不返回给客户端
- 签名算法使用 HMAC-SHA1
- 签名有效期限制（默认 5 分钟）
- 防止重放攻击（时间戳验证）
- 防止中间人攻击（HTTPS）

### 3.2 性能
- 签名生成时间：< 10ms
- 签名验证时间：< 10ms
- Cookie 验证时间：< 5ms

### 3.3 可靠性
- 认证成功率：> 99.9%
- 签名验证准确率：100%

### 3.4 可扩展性
- 支持新增认证方式
- 支持自定义签名算法
- 支持多租户隔离

## 4. 数据模型

### 4.1 数据库表

#### 4.1.1 transcode_access_key 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | VARCHAR(36) | PRIMARY KEY | Access Key ID |
| access_key | VARCHAR(64) | NOT NULL UNIQUE | Access Key |
| secret_key | VARCHAR(128) | NOT NULL | Secret Key（加密存储） |
| client_id | VARCHAR(64) | NOT NULL | 客户端 ID |
| user_id | VARCHAR(50) | NOT NULL | 用户 ID |
| tenant_id | VARCHAR(50) | NULL | 租户 ID |
| status | VARCHAR(20) | NOT NULL | 状态（ACTIVE、DISABLED、EXPIRED） |
| expires_at | DATETIME | NULL | 过期时间 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| creator | VARCHAR(50) | NOT NULL | 创建者 |
| last_used_at | DATETIME | NULL | 最后使用时间 |
| description | VARCHAR(500) | NULL | 描述 |

### 4.2 Redis 数据结构

#### 4.2.1 Access Key 缓存
- **名称**：`transcode:access_key:{accessKey}`
- **类型**：Redis Hash
- **结构**：
  - `accessKey`：Access Key
  - `secretKey`：Secret Key（加密）
  - `clientId`：客户端 ID
  - `userId`：用户 ID
  - `tenantId`：租户 ID
  - `status`：状态
  - `expiresAt`：过期时间
- **TTL**：根据过期时间设置

#### 4.2.2 Session 缓存
- **名称**：`transcode:session:{sessionId}`
- **类型**：Redis Hash
- **结构**：
  - `userId`：用户 ID
  - `tenantId`：租户 ID
  - `accessKey`：Access Key
  - `createdAt`：创建时间
  - `expiresAt`：过期时间
- **TTL**：Session 过期时间（默认 24 小时）

## 5. API 接口

### 5.1 认证接口

#### 5.1.1 Cookie 登录
- **路径**：`/api/auth/login`
- **方法**：POST
- **请求体**：
  ```json
  {
    "accessKey": "string",
    "secretKey": "string"
  }
  ```
- **响应**：
  ```json
  {
    "userId": "string",
    "tenantId": "string",
    "accessToken": "string",
    "expiresAt": "timestamp"
  }
  ```
- **响应头**：
  ```
  Set-Cookie: transcode_session=xxx; Path=/; HttpOnly; Secure; SameSite=Strict
  ```

#### 5.1.2 登出
- **路径**：`/api/auth/logout`
- **方法**：POST
- **响应**：
  ```json
  {
    "status": "SUCCESS",
    "loggedOutAt": "timestamp"
  }
  ```

### 5.2 Access Key 管理接口

#### 5.2.1 创建 Access Key
- **路径**：`/api/auth/access-key`
- **方法**：POST
- **请求体**：
  ```json
  {
    "description": "用于服务间调用",
    "expiresAt": "timestamp"
  }
  ```
- **响应**：
  ```json
  {
    "accessKeyId": "string",
    "accessKey": "string",
    "secretKey": "string",
    "clientId": "string",
    "status": "ACTIVE",
    "createdAt": "timestamp",
    "expiresAt": "timestamp"
  }
  ```

#### 5.2.2 查询 Access Key 列表
- **路径**：`/api/auth/access-key`
- **方法**：GET
- **参数**：
  - `status`：状态（可选）
  - `page`：页码（默认1）
  - `size`：每页数量（默认10）
- **响应**：
  ```json
  {
    "total": 100,
    "page": 1,
    "size": 10,
    "items": [
      {
        "accessKeyId": "string",
        "accessKey": "string",
        "clientId": "string",
        "status": "ACTIVE",
        "description": "用于服务间调用",
        "createdAt": "timestamp",
        "expiresAt": "timestamp",
        "lastUsedAt": "timestamp"
      }
    ]
  }
  ```

#### 5.2.3 禁用 Access Key
- **路径**：`/api/auth/access-key/{id}/disable`
- **方法**：POST
- **响应**：
  ```json
  {
    "accessKeyId": "string",
    "status": "DISABLED",
    "disabledAt": "timestamp"
  }
  ```

#### 5.2.4 删除 Access Key
- **路径**：`/api/auth/access-key/{id}`
- **方法**：DELETE
- **响应**：
  ```json
  {
    "accessKeyId": "string",
    "status": "DELETED",
    "deletedAt": "timestamp"
  }
  ```

## 6. 签名算法

### 6.1 签名生成

#### 6.1.1 签名步骤
1. 获取请求参数
2. 按参数名的 ASCII 正序排序
3. 拼接参数：`key1=value1&key2=value2&...`
4. 添加时间戳：`{参数}&timestamp={timestamp}`
5. 使用 Secret Key 进行 HMAC-SHA1 签名
6. 将签名进行 Base64 编码
7. 将签名添加到请求头：`X-Signature: {signature}`

#### 6.1.2 签名示例
```
参数：
{
  "inputFile": "/path/to/video.mp4",
  "strategyId": "strategy_001"
}

排序后拼接：
inputFile=/path/to/video.mp4&strategyId=strategy_001&timestamp=1739457045

HMAC-SHA1 签名（使用 Secret Key）：
signature = Base64(HMAC-SHA1(secretKey, "inputFile=/path/to/video.mp4&strategyId=strategy_001&timestamp=1739457045"))

请求头：
X-Client-Id: client_001
X-Timestamp: 1739457045
X-Signature: {signature}
```

### 6.2 签名验证

#### 6.2.1 验证步骤
1. 从请求头获取 Client ID、Timestamp、Signature
2. 根据 Client ID 查询 Access Key 和 Secret Key
3. 验证 Access Key 是否有效
4. 验证时间戳是否在有效期内（默认 5 分钟）
5. 获取请求参数
6. 按相同方式重新生成签名
7. 比对生成的签名与请求的签名是否一致
8. 返回验证结果

## 7. HTTP Exchange 客户端

### 7.1 客户端定义
所有 API 接口使用 HTTP Exchange 客户端定义，其他服务可以直接引入 jar 包调用。

### 7.2 接口示例
```java
public interface TranscodeApi {
    
    @PostMapping("/api/transcode/task")
    String createTask(@RequestBody CreateTaskRequest request);
    
    @GetMapping("/api/transcode/task/{id}")
    TaskVO getTask(@PathVariable String id);
    
    @DeleteMapping("/api/transcode/task/{id}")
    Boolean cancelTask(@PathVariable String id);
}
```

### 7.3 客户端配置
```java
@Configuration
public class TranscodeClientConfig {
    
    @Bean
    public TranscodeApi transcodeApi() {
        return HttpServiceProxyFactory.builder()
            .baseUrl("http://localhost:8080")
            .build()
            .createClient(TranscodeApi.class);
    }
}
```

### 7.4 认证拦截器
```java
@Component
public class AuthInterceptor implements ClientExchangeFilterFunction {
    
    private final AccessKeyProvider accessKeyProvider;
    
    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        String accessKey = accessKeyProvider.getAccessKey();
        String secretKey = accessKeyProvider.getSecretKey();
        
        String signature = generateSignature(request, secretKey);
        
        ClientRequest authRequest = ClientRequest.from(request)
            .header("X-Client-Id", accessKey)
            .header("X-Timestamp", String.valueOf(System.currentTimeMillis()))
            .header("X-Signature", signature)
            .build();
        
        return next.exchange(authRequest);
    }
}
```

## 8. 实现方案

### 8.1 技术选型
- **Web 框架**：Spring Web（Spring Boot 4.x）
- **安全框架**：Spring Security
- **签名算法**：HMAC-SHA1
- **HTTP 客户端**：Spring HTTP Exchange
- **缓存**：Redis

### 8.2 核心组件
- **AuthenticationService**：认证服务
- **AccessKeyService**：Access Key 管理服务
- **SignatureValidator**：签名验证器
- **SignatureGenerator**：签名生成器
- **SessionManager**：Session 管理器
- **AuthInterceptor**：认证拦截器

### 8.3 关键流程

#### 8.3.1 Cookie 登录流程
1. 接收登录请求（AK/SK）
2. 验证 Access Key 和 Secret Key
3. 检查 Access Key 状态和过期时间
4. 生成 Session ID
5. 保存 Session 信息到 Redis
6. 返回 Cookie
7. 后续请求使用 Cookie 认证

#### 8.3.2 API 签名流程
1. 客户端获取请求参数
2. 按参数名 ASCII 正序排序
3. 拼接参数并添加时间戳
4. 使用 Secret Key 进行 HMAC-SHA1 签名
5. 将签名添加到请求头
6. 发送请求到服务端
7. 服务端验证签名
8. 返回结果

#### 8.3.3 签名验证流程
1. 从请求头获取 Client ID、Timestamp、Signature
2. 根据 Client ID 查询 Access Key
3. 验证 Access Key 是否有效
4. 验证时间戳是否在有效期内
5. 获取请求参数
6. 重新生成签名
7. 比对签名是否一致
8. 返回验证结果

## 9. 监控与告警

### 9.1 监控指标
- 登录成功率
- 签名验证成功率
- Access Key 使用统计
- 认证失败原因统计

### 9.2 告警机制
- 认证失败率过高
- 异常登录行为
- Access Key 过期提醒

## 10. 部署与运维

### 10.1 依赖
- Redis 6.0+
- JDK 25+
- Spring Boot 4.x
- Spring Security

### 10.2 配置
- 签名有效期（默认 5 分钟）
- Session 过期时间（默认 24 小时）
- Access Key 默认过期时间
- 最大 Access Key 数量

### 10.3 运维操作
- 查看 Access Key 列表
- 禁用异常 Access Key
- 查看认证日志
- 查看签名验证统计

## 11. 测试计划

### 11.1 单元测试
- 签名生成测试
- 签名验证测试
- 认证流程测试
- Session 管理测试

### 11.2 集成测试
- Cookie 登录集成测试
- API 签名集成测试
- HTTP Exchange 集成测试
- Redis 集成测试

### 11.3 安全测试
- 签名伪造测试
- 重放攻击测试
- 中间人攻击测试
- 暴力破解测试

### 11.4 性能测试
- 签名生成性能测试
- 签名验证性能测试
- 并发认证测试

## 12. 风险评估

### 12.1 潜在风险
- **Secret Key 泄露**：Secret Key 泄露导致安全问题
- **签名重放**：攻击者重放已签名的请求
- **时间戳伪造**：攻击者伪造时间戳绕过验证
- **中间人攻击**：攻击者拦截和篡改请求
- **Cookie 劫持**：Cookie 被劫持导致会话劫持

### 12.2 缓解措施
- **Secret Key 加密**：Secret Key 加密存储，永不返回
- **时间戳验证**：验证时间戳的有效性，防止重放
- **HTTPS 加密**：使用 HTTPS 加密传输
- **Cookie 安全**：设置 HttpOnly、Secure、SameSite 属性
- **签名算法**：使用 HMAC-SHA1 签名算法
- **访问日志**：记录所有认证请求，便于审计
- **异常检测**：检测异常登录行为，及时告警