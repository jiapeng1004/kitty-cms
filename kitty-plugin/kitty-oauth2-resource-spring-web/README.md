# kitty-oauth2-resource-spring-web

**OAuth 2.0 资源服务器侧**能力：解析并校验 **Bearer `access_token`**（RFC 6750），对 Controller 提供 **`@CheckScope`** 与 **`OAuth2ScopeCheck`**；与 **`kitty-oauth2-server-spring-web`** 通过 **Redis 键约定 / 数据库表结构 / RFC 7662 自省** 对齐，**两模块无 Maven 依赖**，仅靠配置与领域快照模型约定一致。

---

## 1. 模块定位

| 项目 | 说明 |
|------|------|
| **核心端口** | `OAuth2AccessTokenValidationPort`：按 token 原文返回 `Optional<OAuth2TokenSnapshot>` |
| **Web 层** | `OAuth2ScopeCheck` + `@CheckScope` 拦截器；失败由 `OAuth2ScopeCheckExceptionAdvice` 输出 HTTP 响应 |
| **令牌来源** | **共享 Redis**、**共享 MyBatis 表（只读）**，或 **`center`**：`RestTemplate` 调授权服务器 **`POST /oauth2/introspect`**（**默认可替换**，见第 5.1 节） |
| **默认策略** | 若 classpath **无** 授权服务器自动配置类，且用户**未**配置 **`access-token-persistence`** 与 **`token-persistence`**，则通过 **`OAuth2ResourceTokenPersistenceDefaultEnvironmentPostProcessor`** 注入 **`access-token-persistence=center`**（偏好多实例、与 AS 解耦；**不**再写入总开关，以免干扰 AS 侧 refresh/code 语义） |

---

## 2. Maven 依赖

```xml
<dependency>
    <groupId>icu.jiapeng</groupId>
    <artifactId>kitty-oauth2-resource-spring-web</artifactId>
    <version>${kittycms.version}</version>
</dependency>
```

- **`spring-boot-starter-web`**：标记为 optional，典型 Spring MVC 应用需自行引入 Web。
- **`spring-boot-starter-data-redis`**、**`mybatis-plus-spring-boot4-starter`**：optional；仅在使用对应校验实现时引入。

---

## 3. 自动配置与注册顺序

`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（及兼容用 `spring.factories`）注册：

1. `OAuth2ResourceExtrasRedisAccessTokenValidationAutoConfiguration`
2. `OAuth2ResourceExtrasMybatisPlusAccessTokenValidationAutoConfiguration`
3. `OAuth2ResourceExtrasCenterAccessTokenValidationAutoConfiguration`
4. `OAuth2ScopeCheckAutoConfiguration`（依赖容器中已存在 `OAuth2AccessTokenValidationPort`）

`EnvironmentPostProcessor`：`OAuth2ResourceTokenPersistenceDefaultEnvironmentPostProcessor`（见第 1 节）。

---

## 4. `OAuth2AccessTokenValidationPort` 实现选型

配置键与授权服务器 **access 分项** 对齐：**优先** **`kitty.oauth2.authorization-server.extras.access-token-persistence`**，**未设置**时回退 **`…extras.token-persistence`**（`OAuth2ResourceAccessTokenPersistenceEffective`，与 server 的 `OAuth2ExtrasPersistenceEffective` 中 `ACCESS_TOKEN` 分项一致）。

| 取值（**有效值**） | 条件摘要 | 实现类 |
|------|----------|--------|
| **`redis`** 或 **`auto`**（且存在 `StringRedisTemplate`、无自定义 Port） | Redis 分支在 **`auto`** 下与 MyBatis 竞争：`@ConditionalOnMissingBean(OAuth2AccessTokenValidationPort)` 先满足的生效 | `RedisOAuth2AccessTokenValidationAdapter` |
| **`auto`** 或 **`mybatis-plus`**（且存在 `DataSource`、无自定义 Port） | `@MapperScan` 扫描 `...extras.mybatisplus.mapper` | `MybatisPlusOAuth2AccessTokenValidationAdapter` |
| **`center`** | 注册 `OAuth2IntrospectionAccessTokenValidationAdapter`（**`RestTemplate`**；默认 Bean 见第 5.1 节） | 见第 5 节 |

**覆盖默认**：任一实现均以 `@ConditionalOnMissingBean(OAuth2AccessTokenValidationPort.class)` 注册；宿主自定义 `@Bean OAuth2AccessTokenValidationPort` 可完全替换。

---

## 5. 中心化自省（`access-token-persistence=center`，或回退的 `token-persistence=center`）

前缀：**`kitty.oauth2.resource.introspection`**

| 属性 | 说明 |
|------|------|
| `endpoint` | RFC 7662 自省端点 **绝对 URL**（通常取授权服务器元数据中的 `introspection_endpoint`） |
| `client-id` / `client-secret` | 用于 **RFC 6749 §2.3** 客户端认证，调用自省端点 |

与授权服务器 **`kitty-oauth2-server-spring-web`** 的 **`POST /oauth2/introspect`** 协议一致；响应 DTO 为 **`OAuth2TokenIntrospectionResponse`**（`...resource.springweb.dto`）。

### 5.1 自定义 `RestTemplate`（可选，更高性能 HTTP 实现）

中心化自省通过 **`OAuth2IntrospectionAccessTokenValidationAdapter`** 发起 HTTP 调用，底层使用 **`RestTemplate`**。自动配置会注册 **默认** `RestTemplate`（JDK `HttpURLConnection` 的 `SimpleClientHttpRequestFactory`），并设置 **`ResponseErrorHandler`**，使 **4xx/5xx 不抛异常**，由适配器根据状态码与响应体判断令牌是否有效。

| 项目 | 说明 |
|------|------|
| **Bean 名称** | 固定为 **`oauth2TokenIntrospectionRestTemplate`**，与常量 **`OAuth2ResourceExtrasCenterAccessTokenValidationAutoConfiguration#OAUTH2_TOKEN_INTROSPECTION_REST_TEMPLATE_BEAN_NAME`** 一致 |
| **覆盖方式** | 默认 `RestTemplate` 在自动配置中带 **`@ConditionalOnMissingBean(name = "oauth2TokenIntrospectionRestTemplate")`**：仅当容器中**尚不存在该名称**的 Bean 时才注册；宿主声明 **`@Bean(name = "oauth2TokenIntrospectionRestTemplate")`** 即可替换，自省适配器通过 **`@Qualifier`** 注入同名 Bean |
| **典型用途** | 使用 Spring 提供的 **`OkHttp3ClientHttpRequestFactory`**（`spring-web`，需 classpath 存在 **OkHttp 3**）包装自定义 **`OkHttpClient`**，以连接池、HTTP/2 等获得更好吞吐与延迟（通常需额外依赖 **`com.squareup.okhttp3:okhttp`**，版本与 Spring Boot 依赖管理对齐） |
| **建议** | 自定义 `RestTemplate` 时，若希望与默认行为一致（非 2xx 仍走适配器解析逻辑），可同样设置 **`hasError` 恒为 `false`** 的 **`DefaultResponseErrorHandler`**；若保留默认 **`ResponseErrorHandler`**，非 2xx 会抛 **`RestClientException`**，适配器会捕获并视为令牌无效，一般也可接受 |

示例（OkHttp，需按项目补齐依赖与 import）：

```java
@Bean(name = OAuth2ResourceExtrasCenterAccessTokenValidationAutoConfiguration.OAUTH2_TOKEN_INTROSPECTION_REST_TEMPLATE_BEAN_NAME)
RestTemplate oauth2TokenIntrospectionRestTemplate() {
    OkHttpClient ok = new OkHttpClient();
    RestTemplate t = new RestTemplate(new OkHttp3ClientHttpRequestFactory(ok));
    t.setErrorHandler(new DefaultResponseErrorHandler() {
        @Override
        public boolean hasError(ClientHttpResponse response) throws java.io.IOException {
            return false;
        }
    });
    return t;
}
```

仅替换 **`OAuth2AccessTokenValidationPort`** 而不动 `RestTemplate` 时，仍可使用 **`@ConditionalOnMissingBean(OAuth2AccessTokenValidationPort.class)`** 自行注册完整适配；仅替换 **`RestTemplate`** 时，保持上述 **Bean 名**即可复用 **`OAuth2IntrospectionAccessTokenValidationAdapter`**。

---

## 6. Scope 校验

前缀：**`kitty.oauth2.authorization-server.scope-check`**（与 server 文档中的命名空间一致，便于单一配置源）

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `enabled` | `true` | 为 `true`（或未配置，**`matchIfMissing = true`**）时注册 **`OAuth2CheckScopeHandlerInterceptor`**（全局 `/**`，顺序 `Ordered.LOWEST_PRECEDENCE`） |
| `bearer-header-name` | `""` | 空表示使用 **`Authorization`**（RFC 6750 常见 `Bearer` 形态）；非空则使用该请求头承载原始 token |

在 Controller 上使用 **`@CheckScope`**（见类 Javadoc）：可仅校验令牌可解析，或声明所需 `scope` 及 `OAuth2ScopeCheckMode`（ALL / ANY）。

---

## 7. 与授权服务器、持久化的对齐

- **Redis**：键前缀与 server 侧写入一致，见 **`OAuth2RedisTokenKeys`**（与 server 模块 `RedisOAuth2AccessTokenPersistenceAdapter` 等相同约定）。
- **MyBatis-Plus**：只读访问 access_token 表；建表脚本与授权服务器共用，见 **`kitty-oauth2-server-spring-web`** 资源下的 **`classpath:schema/kt-oauth2-token-tables.mysql.sql`**。
- **仅资源、不引入 server starter**：默认 **`access-token-persistence=center`**（除非显式配置 access 或总开关为 `redis` / `mybatis-plus` 等）；**与 AS 同进程**时 classpath 上存在授权服务器自动配置类，则 **不** 注入该默认，以免覆盖宿主对 AS 侧 `auto` 的意图。

---

## 8. 相关类索引

| 类 | 包 |
|----|-----|
| `OAuth2AccessTokenValidationPort` | `...resource.springweb.port` |
| `OAuth2ScopeCheck` / `@CheckScope` / `OAuth2CheckScopeHandlerInterceptor` | `...resource.springweb.security` |
| `OAuth2ResourceIntrospectionProperties` | `...resource.springweb.properties` |
| `OAuth2ScopeCheckProperties` | `...resource.springweb.properties` |
| `OAuth2IntrospectionAccessTokenValidationAdapter` | `...resource.springweb.extras.introspection` |
| `OAuth2ResourceExtrasCenterAccessTokenValidationAutoConfiguration` | `...resource.springweb.autoconfigure`（`oauth2TokenIntrospectionRestTemplate` 默认 Bean） |
| `RedisOAuth2AccessTokenValidationAdapter` | `...resource.springweb.extras.redis` |
| `MybatisPlusOAuth2AccessTokenValidationAdapter` | `...resource.springweb.extras.mybatisplus` |
| `OAuth2ResourceTokenPersistenceDefaultEnvironmentPostProcessor` | `...resource.springweb.env` |

更完整的授权协议、端点与 SPI 说明见 **`kitty-oauth2-server-spring-web`** 的 README。
