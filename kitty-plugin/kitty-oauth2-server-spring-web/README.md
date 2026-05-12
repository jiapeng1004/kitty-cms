# kitty-oauth2-server-spring-web

基于 **Spring Web（MVC）** 的 **OAuth 2.0 授权服务器**实现，遵循 **RFC 6749**（令牌/授权端点）、**RFC 6750**（Bearer）、**RFC 7636**（PKCE）、**RFC 7662**（令牌自省）、**RFC 8414**（授权服务器元数据）。  
**不依赖** Sa-Token OAuth2、Spring Authorization Server 等重型栈；核心逻辑仅依赖 **SPI（端口）**，持久化、客户端注册、用户会话等由宿主应用以 **DDD 外围适配器** 注入。

## 文档目录

| 章节 | 内容 |
|------|------|
| [1](#1-模块定位) | 模块定位、`redirect_uri` 策略、RFC 7662 与资源侧关系 |
| [2](#2-maven-依赖) | Maven 依赖 |
| [3](#3-spring-boot-装配starter-自动配置) | Starter 与显式 `@Import` |
| [4](#4-配置属性插件自有不属于-kittyuser) | `kitty.oauth2.authorization-server` |
| [5](#5-extras可选默认-spisa-token--spring-security--redis--mybatis-plus) | extras（Sa-Token / Security / Redis / MyBatis-Plus） |
| [6](#6-spi宿主必须实现的端口或由第-5-节-extras-提供默认) | SPI 端口一览 |
| [7](#7-参考实现本仓库) | 本仓库参考实现 |
| [8](#8-http-契约与请求-dto) | HTTP 契约与 DTO |
| [9](#9-端点协议摘要) | 端点协议摘要 |
| [10](#10-安全与网关) | 安全与网关 |
| [11](#11-与-spring-authorization-server-迁移) | 与 Spring Authorization Server 迁移 |
| [12](#12-相关类索引) | 相关类索引 |

---

## 1. 模块定位

| 项目 | 说明 |
|------|------|
| **运行时依赖** | 主要为 `spring-boot-starter-web`（Servlet + MVC） |
| **契约** | `OAuth2AuthorizationServerApi` 使用 Spring 6 **HttpExchange** 注解声明 HTTP 契约，便于 **OpenAPI** 与 **声明式 HTTP 客户端**（`RestClient` / `HttpServiceProxyFactory`）对齐 |
| **扩展** | 通过 **6 个 SPI 接口** 对接 ORM、Redis、会话、登录跳转等；**extras**（第 5 节）可对 Sa-Token / Security / Redis 提供可选默认实现；另有 **配置属性** 与 **Properties 适配器**（可覆盖） |
| **Starter** | 提供 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 与兼容用 `META-INF/spring.factories`，Servlet Web 应用下 **自动启用** |

### 1.1 授权码与 `redirect_uri`（相对 RFC 6749 的说明）

**RFC 6749** 摘要：

- **4.1.1 节**：若客户端只登记了一个重定向 URI，授权请求中的 `redirect_uri` 可为 **OPTIONAL**；若登记多个，则 **必填**。
- **4.1.3 节**：若授权请求里**带了** `redirect_uri`，换 token 时须再带**且与授权请求一致**；若授权阶段未带（在规范允许省略的情形下），换 token 侧的要求随之不同。

**本模块策略（刻意加严，非 RFC 另有独立条文）：**

- **`GET /oauth2/authorize`**：**始终要求** query 中带 **`redirect_uri`**（且须在客户端已登记集合内）。
- **`POST /oauth2/token`**（`grant_type=authorization_code`）：**始终要求**表单中带 **`redirect_uri`**，并与发码时绑定的 URI **一致**。

即：**未实现**「仅单回调时授权请求可省略 `redirect_uri`」这一 OPTIONAL 分支；在已支持的真实调用路径上，与 4.1.3 节「授权时带了则换 token 须带且一致」**一致**，整体属于对 **`redirect_uri` 显式绑定的局部加严**（减少歧义与错误配置）。对接客户端时请 **始终在授权与换 token 两处传递相同的 `redirect_uri`**；若需兼容「省略授权端 `redirect_uri`」的第三方客户端，须另行扩展授权端与换 token 校验逻辑。

### 1.2 RFC 7662 令牌自省（`POST /oauth2/introspect`）

**目的**：资源服务可**不**在本地部署与授权服务器相同的**本地令牌持久化**（如 MyBatis/Redis 上的 access/refresh 存储），改为向授权服务器**中心化**查询某个 `access_token` / `refresh_token` 是否仍有效，并读取 `scope`、`client_id`、`sub`（与持久化中 `OAuth2TokenSnapshot` 一致）等元数据。

| 项目 | 说明 |
|------|------|
| **路径** | `POST {issuer}/oauth2/introspect`，`Content-Type: application/x-www-form-urlencoded` |
| **调用方认证** | 与令牌端点相同：**RFC 6749 §2.3**（`Authorization: Basic …` 或表单 `client_id` + `client_secret`）；须为已登记且校验通过的**机密客户端** |
| **请求体** | **`token`**（必填）、**`token_type_hint`**（可选：`access_token` / `refresh_token`） |
| **成功响应** | JSON：`active`（boolean）；若 `active=true`，含 `scope`、`client_id`、`username`/`sub`（来自 subject）、`token_type` 等（**当前实现不填 `exp`/`iat`**，因各令牌持久化端口的读取 API 未统一暴露绝对过期时刻，见 `OAuth2AuthorizationServerCoreService#processTokenIntrospection` 注释） |
| **失败** | 客户端认证失败：`401` + `invalid_client`（与 `/oauth2/token` 一致）；缺少 `token`：`400` + `invalid_request` |
| **元数据** | RFC 8414 JSON 中增加 **`introspection_endpoint`**，值为 `{issuer}/oauth2/introspect` |

**资源侧（`kitty-oauth2-resource-spring-web`）** 在业务进程内通过 **`OAuth2AccessTokenValidationPort`** 校验 Bearer：可与 AS **共享 Redis / MyBatis 表**（优先 **`extras.access-token-persistence`**，未设置则回退 **`extras.token-persistence`**，为 `redis`、`auto` 或 `mybatis-plus`），或 **`center`** 时用 **`RestTemplate`** 调用 AS 的 `POST /oauth2/introspect`（见 `OAuth2IntrospectionAccessTokenValidationAdapter`、默认可替换的 **`oauth2TokenIntrospectionRestTemplate`** Bean 与该模块 README）。与 **本地持久化 + `@CheckScope`** 的关系：**二选一或组合策略由业务决定**——中心化自省适合多实例资源服务、与 AS 异构部署；共享存储适合与 AS **同库/同 Redis** 且追求低延迟。

---

## 2. Maven 依赖

在宿主应用（如业务 `*-func` 或 `*-server`）中引入：

```xml
<dependency>
    <groupId>icu.jiapeng</groupId>
    <artifactId>kitty-oauth2-server-spring-web</artifactId>
    <version>${kittycms.version}</version>
</dependency>
```

---

## 3. Spring Boot 装配（Starter 自动配置）

引入依赖后，**默认无需** `@Import`：由 `OAuth2AuthorizationServerSpringWebAutoConfiguration` 通过 Spring Boot 自动配置导入 `OAuth2AuthorizationServerSpringWebConfiguration`（Servlet Web 环境生效）。

- **Boot 3+** 注册文件：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- **兼容旧式**（如 Boot 2.x 或部分工具链）：`META-INF/spring.factories` 中的 `EnableAutoConfiguration`

若希望 **显式** 装配（与自动配置二选一，不要重复导入同一配置类）：

```java
import icu.jiapeng.kitty.oauth2.server.springweb.config.OAuth2AuthorizationServerSpringWebConfiguration;

@Import(OAuth2AuthorizationServerSpringWebConfiguration.class)
public class YourApplicationConfiguration {
}
```

装配完成后会注册：

- `OAuth2AuthorizationServerController`（`@RestController`，映射 `/oauth2/token`、`/oauth2/introspect`、`/oauth2/authorize`、元数据等路径）
- `OAuth2AuthorizationServerService`（核心实现 `OAuth2AuthorizationServerCoreService`）
- 若容器中 **不存在** `OAuth2AuthorizationServerPropertiesPort` Bean：注册默认实现（绑定 `kitty.oauth2.authorization-server` 配置）

**注意**：除 **第 5 节 extras** 可按条件提供默认实现的端口外，其余 SPI 须由宿主提供 `@Component` / `@Bean`（见 **第 6 节**）。

---

## 4. 配置属性（插件自有，不属于 `kitty.user`）

前缀：**`kitty.oauth2.authorization-server`**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `issuer` | `String` | `""` | 授权服务器 **issuer**（RFC 8414）。**空** 时由运行时根据当前请求推导：`scheme + host:port + context-path`。生产环境建议显式配置为对外可达的基地址（含 context-path）。 |
| `login-page-url` | `String` | `/login` | 浏览器未登录访问 `/oauth2/authorize` 时 **302** 跳转的登录页。可为相对路径（相对当前请求的 context-path）或完整 `http(s)` URL。 |
| `extras.end-user-session` | 枚举 | `auto` | 授权页「是否已登录」：`auto` / `satoken` / `spring-security` / `none`。自动配置里 **`@ConditionalOnProperty#havingValue`** 与上述字符串一致；**`auto`** 亦为字面量（**`matchIfMissing = true`** 与未写配置时的默认一致）。与 **令牌 / consent** 无关。 |
| `extras.token-persistence` | 枚举 | `auto` | **总开关**：授权码 / access_token / refresh_token 在**未配置分项键**时均回退到此值。取值：`auto` / `redis` / `mybatis-plus` / **`center`** / `none`。`redis` / `mybatis-plus` 由 extras 按**各分项解析后的有效值**注册对应 Port；**`center`** 表示本模块不注册上述 extras（典型为纯资源侧走 RFC 7662；**AS 若选此项须自行提供**三类端口实现）。 |
| `extras.access-token-persistence` | 枚举 | — | **可选分项**：仅覆盖 **access_token**；未配置时继承 **`extras.token-persistence`**（运行时由 `OAuth2ExtrasPersistenceEffective` 解析）。 |
| `extras.refresh-token-persistence` | 枚举 | — | **可选分项**：仅覆盖 **refresh_token**；未配置时继承总开关。 |
| `extras.authorization-code-persistence` | 枚举 | — | **可选分项**：仅覆盖 **authorization code**；未配置时继承总开关。 |
| `extras.consent-storage` | 枚举 | `auto` | consent：`auto`（表存储）或 `in-memory`。注册 consent Bean 时 **`havingValue = "auto"`**。 |
| `extras.satoken.*` | — | — | 仅 Sa-Token **细项**（Cookie 名、请求头等），**无** `enabled=true` 式开关。 |
| `extras.spring-security.*` | — | — | 仅 Spring Security **细项**（如网关注入主体请求头），**无** `enabled=true` 式开关。 |

**示例（`application.yml`）**：

```yaml
kitty:
  oauth2:
    authorization-server:
      issuer: "https://api.example.com/kitty-user"
      login-page-url: "/login"
      extras:
        end-user-session: satoken
        token-persistence: redis
        consent-storage: auto
        satoken:
          token-header-name: ""
```

未登录授权时，实际重定向形如：

`{resolveLoginPageUrl 结果}?oauth2_redirect={URLEncode(完整 authorize URL 含 query)}`

前端需在登录成功后 **再跳回** `oauth2_redirect` 指向的地址以继续授权码流程。

---

## 5. extras：可选默认 SPI（Sa-Token / Spring Security / Redis / MyBatis-Plus）

本模块在包 `icu.jiapeng.kitty.oauth2.server.springweb.extras` 下提供若干 **可选** 适配器。选型由 **`extras.end-user-session`**、**`extras.token-persistence`**（总开关）、**可选分项** `access-token-persistence` / `refresh-token-persistence` / `authorization-code-persistence`、**`extras.consent-storage`** 完成；令牌类分项 **未设置** 时回退到 **`token-persistence`**。自动配置中条件类按 **分项有效值** 与 **`redis` / `auto` / `mybatis-plus` 等** 匹配，**不再**使用 `extras.*.enabled=true`。**(1)** 在 **自己的** `pom.xml` 中显式引入对应依赖且 classpath 具备触发类；**(2)** 容器中尚无同类型 Port 的自定义 Bean（`@ConditionalOnMissingBean`）。

### 5.1 设计目标与 Maven 行为

| 项目 | 说明 |
|------|------|
| **配置开关（首要）** | **`extras.end-user-session`**、**`extras.token-persistence`**（及可选分项）、**`extras.consent-storage`**：总开关与 consent 默认为 **`auto`**；令牌分项未写时继承总开关（见 **5.2**）。 |
| **外置集成** | 核心 starter 仅强依赖 `spring-boot-starter-web`（及 JSON 等）；Sa-Token、Spring Security、Redis 在 **本模块 `pom.xml`** 中声明为 **`optional=true`**，**不向下游传递**。 |
| **显式依赖** | 须在 **自己的** `pom.xml` 中声明对应坐标，否则缺少类或 Bean，该 extras 不会生效（通常表现为对应 Port 仍缺 Bean）。 |
| **覆盖默认** | 各 extras 注册 Bean 时使用 `@ConditionalOnMissingBean(对应 Port)`。宿主只要自行声明 `@Bean OAuth2EndUserSessionPort` 或任一 **`OAuth2*PersistencePort`**（授权码 / access / refresh），即可 **完全替换** 该条链路的默认实现，无需 `spring.autoconfigure.exclude`。 |

### 5.2 自动配置类与顺序

| 自动配置类 | 注册的 Bean（默认） | 触发条件（摘要） |
|------------|---------------------|------------------|
| `OAuth2ExtrasSaTokenAutoConfiguration` | `OAuth2EndUserSessionPort` → `SaTokenOAuth2EndUserSessionAdapter` | **`havingValue = "satoken"`** 或 **`"auto"`**（**`matchIfMissing`**）；存在 `StpUtil`；且无自定义 Port |
| `OAuth2ExtrasSpringSecurityAutoConfiguration` | `OAuth2EndUserSessionPort` → `SpringSecurityOAuth2EndUserSessionAdapter` | **`havingValue = "spring-security"`** 或 **`"auto"`**（**`matchIfMissing`**）；存在 `SecurityContextHolder`；且无自定义 Port |
| `OAuth2ServerExtrasRedisTokenPersistenceAutoConfiguration`（**本 server 模块**） | 三个端口 → `RedisOAuth2AuthorizationCodePersistenceAdapter` / `RedisOAuth2AccessTokenPersistenceAdapter` / `RedisOAuth2RefreshTokenPersistenceAdapter` | 各端口按 **分项有效值** 为 **`redis`** 或 **`auto`**（且存在 `StringRedisTemplate`）；**`@ConditionalOnMissingBean` 按端口独立** |
| `OAuth2ServerExtrasMybatisPlusTokenPersistenceAutoConfiguration`（**本 server 模块**） | 三个端口 → `MybatisPlusOAuth2AuthorizationCodePersistenceAdapter` / `MybatisPlusOAuth2AccessTokenPersistenceAdapter` / `MybatisPlusOAuth2RefreshTokenPersistenceAdapter` | 各端口分项有效值为 **`auto`** / **`mybatis-plus`**；`DataSource` + MyBatis-Plus（**`@MapperScan`** 在任一分项需要 MyBatis 时启用） |
| `OAuth2ExtrasMybatisPlusAutoConfiguration`（本 server 模块） | **仅** `OAuth2ConsentStoragePort` → `MybatisPlusOAuth2ConsentStorageAdapter` | `consent-storage` 为 **`auto`** / **`mybatis-plus`**；`DataSource` + MyBatis-Plus |

注册顺序见 **`kitty-oauth2-server-spring-web`** 的 `AutoConfiguration.imports`：**Sa-Token → Spring Security → Redis 令牌 → MyBatis 令牌 → consent MyBatis → 主 OAuth2**。主入口 `OAuth2AuthorizationServerSpringWebAutoConfiguration` 的 `@AutoConfiguration(after = …)` 与上述一致。

#### 5.2.1 配置项与端口（流程）对照

会话与 consent 读 **`end-user-session`**、**`consent-storage`**；令牌三类读 **`OAuth2ExtrasPersistenceEffective`** 对每个 **分项**（`AUTHORIZATION_CODE` / `ACCESS_TOKEN` / `REFRESH_TOKEN`）解析出的有效值（分项键未设 → **`token-persistence`**）。

| 关心的能力 | 配置键 | 实现的 Port | 说明 |
|------------|--------|-------------|------|
| 浏览器会话 | **`extras.end-user-session`** | `OAuth2EndUserSessionPort` | **`satoken` / `spring-security`**：与条件匹配；**`auto`**：导入顺序下 Sa-Token 先于 Spring Security；**`none`**：须自建 Port。 |
| 授权码 | **`extras.authorization-code-persistence`**（或总开关 **`token-persistence`**） | `OAuth2AuthorizationCodePersistencePort` | 有效值为 **`redis` / `mybatis-plus`** 时注册对应适配器；**`auto`**：Redis 先于 MyBatis；**`center`**：extras 不注册该端口（须自建）；**`none`**：须自建。 |
| access_token | **`extras.access-token-persistence`**（或总开关） | `OAuth2AccessTokenPersistencePort` | 同上，**按端口独立**；可与 refresh / code 分项混用（例如 code=redis、access=mybatis-plus）。 |
| refresh_token | **`extras.refresh-token-persistence`**（或总开关） | `OAuth2RefreshTokenPersistencePort` | 同上。 |
| consent | **`extras.consent-storage`** | `OAuth2ConsentStoragePort` | **`auto`**：表存储；**`in-memory`**：核心内存；与令牌分项独立。 |

**示例（会话走 Sa-Token、令牌与 consent 走库、Redis 仍给业务用）：**

```yaml
kitty:
  oauth2:
    authorization-server:
      extras:
        end-user-session: satoken
        token-persistence: mybatis-plus
        consent-storage: auto
```

### 5.3 Sa-Token（`extras.satoken`）

- **配置**：**`extras.end-user-session=satoken`**（**`havingValue = "satoken"`**）或 **`auto`**；`extras.satoken.*` 仅影响 Cookie/请求头等细项。  
- **实现类**：`SaTokenOAuth2EndUserSessionAdapter`  
- **作用**：授权端点通过 Sa-Token `StpUtil.isLogin()` / `getLoginId()` 判断浏览器是否已登录并取主体 id；与 OAuth2 **Bearer 访问令牌** 无直接耦合，会话仍走 Sa-Token。  
- **依赖示例**：`cn.dev33:sa-token-core`（或你们项目已用的 Sa-Token starter）。  

### 5.4 Spring Security（`extras.springsecurity`）

- **配置**：**`extras.end-user-session=spring-security`**（**`havingValue = "spring-security"`**）或 **`auto`**。  
- **实现类**：`SpringSecurityOAuth2EndUserSessionAdapter`  
- **作用**：从 `SecurityContextHolder` 取当前 `Authentication`；已认证且非匿名时使用 `getName()` 作为用户 id。  
- **与 Sa-Token 同时启用**：**`end-user-session=auto`** 时 **仅 Sa-Token 适配器生效**（imports 顺序 + `MissingBean`）。**只想用 Security** 时设 **`end-user-session: spring-security`**，或自行声明 `OAuth2EndUserSessionPort` Bean。  
- **依赖示例**：`org.springframework.security:spring-security-core`（完整安全链通常另有 `spring-boot-starter-security` 等）。  

### 5.5 Redis（分项有效值为 `redis` / `auto`，**本 server 模块**）

- **配置**：各 **分项**（`authorization-code-persistence` / `access-token-persistence` / `refresh-token-persistence`）经回退后的有效值为 **`redis`** 或 **`auto`** 时，对应端口尝试注册 Redis 适配器；存在 `StringRedisTemplate`。  
- **实现类**：`RedisOAuth2AuthorizationCodePersistenceAdapter`、`RedisOAuth2AccessTokenPersistenceAdapter`、`RedisOAuth2RefreshTokenPersistenceAdapter`（**`OAuth2ServerExtrasRedisTokenPersistenceAutoConfiguration`**）  
- **作用**：授权码 / access / refresh 的领域快照以 **JSON** 存 Redis；授权码 **读取后删除**（一次性）。序列化使用 Spring Boot 注入的 **`ObjectMapper`**（Spring Boot 4 为 **Jackson 3**，`tools.jackson.databind.ObjectMapper`）。  
- **键前缀**：见 `OAuth2RedisTokenKeys`（默认 `oauth2:server:code:`、`oauth2:server:at:`、`oauth2:server:rt:`）。多实例共用同一 Redis 时注意隔离策略或按需替换对应端口 Bean。  
- **前提**：**本模块** `pom.xml` 将 `spring-boot-starter-data-redis` 标为 **optional**；应用需显式引入 Redis 且能创建 **`StringRedisTemplate`** Bean。  

### 5.6 MyBatis-Plus（`extras.mybatis-plus`）

- **Consent（本 server 模块）**：**`consent-storage=auto`** 或 **`mybatis-plus`** 时注册 `MybatisPlusOAuth2ConsentStorageAdapter`；`@MapperScan` 仅扫描 `...server.springweb.extras.mybatisplus.mapper`（consent Mapper）。  
- **令牌（本 server 模块）**：各分项有效值为 **`auto`** 或 **`mybatis-plus`** 时，对应端口注册 `MybatisPlusOAuth2…` 适配器；**`OAuth2ServerExtrasMybatisPlusTokenPersistenceAutoConfiguration`**。  
- **依赖**：MyBatis-Plus 在 **server** 与 **resource** 中均为 **optional**；应用须自行声明 `mybatis-plus-spring-boot4-starter` 并具备 **`DataSource`**。  
- **与 Redis 同时启用**：某端口分项为 **`redis`** 或 **`mybatis-plus`** 时明确落点；**`auto`** 时 Redis 优先（该端口上 **`@ConditionalOnMissingBean`**，Redis 已注册则 MyBatis 不注册）。  
- **access_token / refresh_token 主键**：SHA-256 十六进制（64 字符），与适配器一致。  
- **建表**：consent → **`classpath:schema/kitty-oauth2-mybatis-plus.mysql.sql`**（本模块）；令牌表 → **`classpath:schema/kt-oauth2-token-tables.mysql.sql`**（本模块 resources）。

```sql
-- 以下为 consent 表示例（完整文件见 classpath:schema/kitty-oauth2-mybatis-plus.mysql.sql）

CREATE TABLE IF NOT EXISTS `kt_oauth2_user_client_consent` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    VARCHAR(191) NOT NULL,
    `client_id`  VARCHAR(191) NOT NULL,
    `scopes`     TEXT         NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_oauth2_consent_user_client` (`user_id`, `client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 5.7 与文档其它章节的关系

- **第 10 节「安全与网关」**仍适用：OAuth2 Bearer 与 Sa-Token 会话 token 是不同体系；extras 只解决「授权端点如何识别已登录用户」与「令牌落库」的**常见接线**。  
- **更细的 Javadoc**：各 `extras` 子包提供 **`package-info.java`**（IDE 与 `mvn javadoc` 可见）。  

---

## 6. SPI（宿主必须实现的端口，或由第 5 节 extras 提供默认）

**`OAuth2AuthorizationCodePersistencePort`**、**`OAuth2AccessTokenPersistencePort`**、**`OAuth2RefreshTokenPersistencePort`** 与 **`OAuth2AuthorizationCodeSnapshot` / `OAuth2TokenSnapshot`（`...server.springweb.model`）** 均定义于 **本 server 模块**；**Redis / MyBatis 的默认实现** 由 **第 5 节** extras 分别注册为 **三个 Bean**。资源侧校验用的 **`OAuth2AccessTokenValidationPort`** 与快照类型在 **`kitty-oauth2-resource-spring-web`** 中，字段与授权服务器写入约定对齐，**两模块无 Maven 依赖**。**仅作资源服务器**时可只引 **`kitty-oauth2-resource-spring-web`**；若同时部署授权服务器，再引入本模块。`OAuth2EndUserSessionPort` 的 extras 仅由 **本 server 模块** 提供。若未引入对应依赖或已自定义同名 Port Bean，则仍须自行实现。其余端口 **必须** 由宿主提供（除非表中注明可选）。

| 接口 | 职责 |
|------|------|
| `OAuth2RegisteredClientRegistryPort` | 按 `client_id` 查询 **已启用** 的客户端，返回 `OAuth2RegisteredClientSnapshot`（含密钥、`allowedGrantTypes`、`allowedScopes`、`allowedRedirectUris`、`allowAuthenticationMethods`、令牌 TTL 等）。 |
| `OAuth2AuthorizationCodePersistencePort` | 授权码存储与读取；`consumeAuthorizationCode` 应 **读取并删除**（一次性）。 |
| `OAuth2AccessTokenPersistencePort` | 访问令牌存储、读取与撤销（`removeAccessToken`）。 |
| `OAuth2RefreshTokenPersistencePort` | 刷新令牌存储、读取与删除。 |
| `OAuth2ResourceOwnerPasswordPort` | 密码模式（`grant_type=password`）下校验用户名密码，返回资源所有者主体 id（如用户 id）。 |
| `OAuth2EndUserSessionPort` | 授权端点判断 **当前浏览器会话** 是否已登录；已登录则返回主体 id（用于签发授权码）。 |
| `OAuth2LoginNavigationPort` | 未登录时执行 **302** 到登录页（通常结合配置中的 `login-page-url` 与 `oauth2_redirect`）。 |
| `OAuth2AuthorizationServerPropertiesPort` | 解析 **issuer**、**登录页绝对 URL**（供元数据与跳转）。**可选**：不声明 Bean 时使用插件默认实现（绑定第 4 节配置）。若需多租户/动态 issuer，可自实现并注册同名 Bean **覆盖** 默认。 |

**领域快照模型**：

- `OAuth2RegisteredClientSnapshot`（`icu.jiapeng.kitty.oauth2.server.springweb.model`）
- `OAuth2AuthorizationCodeSnapshot`、`OAuth2TokenSnapshot`（`icu.jiapeng.kitty.oauth2.server.springweb.model`，供上述三类持久化端口与授权服务器核心使用；资源模块另有结构对齐的 `OAuth2TokenSnapshot` 供 `OAuth2AccessTokenValidationPort`）

---

## 7. 参考实现（本仓库）

- **extras 默认适配（本模块）**：`icu.jiapeng.kitty.oauth2.server.springweb.extras` 下 Sa-Token / Spring Security / Redis / MyBatis-Plus 实现见 **第 5 节**；源码与 **`package-info.java`** 说明一致。
- **业务侧示例（`kitty-user-func`）**：包 `icu.jiapeng.kitty.user.oauth2.adapter.server` 仍提供与业务系统绑定的适配，例如：
  - `KtOauth2RegisteredClientRegistryAdapter` → `KtOauth2ClientService`
  - `KtUserOAuth2ResourceOwnerPasswordAdapter` → `KtUserService.authenticateForOAuth2PasswordGrant`
  - `UserConfigOAuth2LoginNavigationAdapter` → 基于已注入的 `OAuth2AuthorizationServerPropertiesPort` 拼登录跳转

复制到新项目时，将注册表、密码校验、登录跳转等替换为你们的实现；会话与 Redis 持久化可优先复用 **第 5 节 extras**，再按需覆盖。

---

## 8. HTTP 契约与请求 DTO

### 8.1 请求 DTO（协议参数）

包 **`icu.jiapeng.kitty.oauth2.server.springweb.dto`**：

| 类 | 绑定方式 | 说明 |
|----|----------|------|
| **`OAuth2TokenRequest`** | `POST /oauth2/token` 表单（`application/x-www-form-urlencoded`） | 汇总各 grant 可能用到的字段：`grantType`、`scope`、`clientId`、`clientSecret`、`username`、`password`、`refreshToken`、`code`、`redirectUri`、`codeVerifier`。不同 grant 只使用其中一部分；**合法性在 `OAuth2AuthorizationServerCoreService` 内按 grant 分别校验**。可选请求头 **`Authorization`**（如 `client_secret_basic`）由 Controller 单独传入核心服务，不写入 DTO。 |
| **`OAuth2AuthorizeRequest`** | `GET /oauth2/authorize` 查询参数 | `responseType`、`clientId`、`redirectUri`、`scope`、`state`、`codeChallenge`、`codeChallengeMethod`。 |

元数据端点 **`GET /.well-known/oauth-authorization-server`** 无协议体；issuer 推导仍依赖当前 **`HttpServletRequest`**（与配置中的 `issuer` 为空时的行为一致）。

### 8.2 HTTP 契约声明（HttpExchange）

接口：**`icu.jiapeng.kitty.oauth2.server.springweb.api.OAuth2AuthorizationServerApi`**

用于：

- **OpenAPI / Swagger**：接口上已标注 `@Tag`、`@Operation`
- **声明式客户端**：与 Spring `RestClient` + `HttpServiceProxyFactory` 或同类机制配合（**注意**：实际请求需带宿主 `context-path`，例如 `/kitty-user`）

### 8.3 方法一览

| 方法 | HttpExchange 注解 | 路径 | 说明 |
|------|-------------------|------|------|
| `token` | `@PostMapping`，`contentType = application/x-www-form-urlencoded` | `/oauth2/token` | 参数：`Authorization`（可选）、`@ModelAttribute OAuth2TokenRequest` |
| `authorize` | `@GetMapping` | `/oauth2/authorize` | 参数：`@ModelAttribute OAuth2AuthorizeRequest`，以及 **`HttpServletRequest` / `HttpServletResponse`**（前者用于拼接登录回跳 URL、供 `OAuth2LoginNavigationPort` 解析登录页等，**非 OAuth 查询字段**） |
| `authorizationServerMetadata` | `@GetMapping` | `/.well-known/oauth-authorization-server` | 参数：`HttpServletRequest`（issuer 推导） |

实现类 **`OAuth2AuthorizationServerController`** 在 MVC 层声明 `@PostMapping` / `@GetMapping` 与 `consumes` / `produces`，与项目内其它 `*Api` + `*Controller` 模式一致。

### 8.4 声明式 HTTP 客户端与直接 HTTP 调用

- **令牌端点**：表单字段名仍为 RFC 蛇形（如 `grant_type`）；Spring MVC 对 `@ModelAttribute` 使用宽松绑定，可绑定到 `OAuth2TokenRequest` 的驼峰字段。
- **集成测试 / 脚本 / 外部服务** 调用时，也可使用 **`RestClient`** 按路径与 Content-Type 手写请求，**baseUrl** 须含 **context-path**（若有）。
- **同进程内** 可注入 **`OAuth2AuthorizationServerService`**：`processTokenRequest` 返回 `ResponseEntity<?>`（成功体为 `OAuth2TokenSuccessResponse`，失败为 `OAuth2ErrorResponse`）；`buildAuthorizationServerMetadata` 返回 `OAuth2AuthorizationServerMetadataResponse`。

### 8.5 响应 DTO 与规范符合性

包 **`icu.jiapeng.kitty.oauth2.server.springweb.dto.response`**：

| 类 | 对应端点 / RFC | 说明 |
|----|------------------|------|
| **`OAuth2TokenSuccessResponse`** | RFC 6749 §5.1 `POST /oauth2/token` 成功 | `access_token`、`token_type`（`Bearer`）、`expires_in`；按需含 `refresh_token`、`scope`（空 scope 不输出）。 |
| **`OAuth2ErrorResponse`** | RFC 6749 §5.2 令牌错误 | `error`、可选 `error_description`、`error_uri`（预留，当前未填）。HTTP：`invalid_client` 为 **401** 且带 `WWW-Authenticate: Basic realm="oauth2"`，其余错误多为 **400**。 |
| **`OAuth2AuthorizationServerMetadataResponse`** | RFC 8414 `GET /.well-known/oauth-authorization-server` | `issuer`、`authorization_endpoint`、`token_endpoint`、各 `*_supported` 列表等。 |

**授权端点 `GET /oauth2/authorize`**：成功为 **302** 重定向至 `redirect_uri` 并带 `code` / `state`（RFC 6749 §4.1.2）；可重定向的错误（如 `invalid_scope`）在 query 中带 `error`、`error_description`、`state`。若 **`client_id` / `redirect_uri` 等无法安全重定向**（RFC 要求不 redirect），当前实现使用 **`HttpServletResponse.sendError(400)`**（容器默认错误页，**非** OAuth JSON），与纯浏览器交互常见做法一致；若需 **JSON 错误体**，需在宿主侧另做内容协商或网关层扩展。

**示例：拉取 RFC 8414 元数据（GET）**

```java
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.response.OAuth2AuthorizationServerMetadataResponse;

RestClient restClient = RestClient.builder()
        .baseUrl("http://localhost:8080/kitty-user")
        .build();

OAuth2AuthorizationServerMetadataResponse metadata = restClient.get()
        .uri("/.well-known/oauth-authorization-server")
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
```

**示例：令牌端点（POST，`application/x-www-form-urlencoded`）**

```java
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import icu.jiapeng.kitty.oauth2.server.springweb.dto.response.OAuth2TokenSuccessResponse;

var form = new LinkedMultiValueMap<String, String>();
form.add("grant_type", "client_credentials");
form.add("client_id", "my-client");
form.add("client_secret", "secret");

OAuth2TokenSuccessResponse token = restClient.post()
        .uri("/oauth2/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
```

（失败时响应体为 `OAuth2ErrorResponse`，可用 `ParameterizedTypeReference` 的父类型或 `Object` / `Map` 按集成需要解析。）

---

## 9. 端点协议摘要

### 9.1 `POST /oauth2/token`

- **Content-Type**：`application/x-www-form-urlencoded`
- **成功**：`200`，`Content-Type: application/json`，Body 为 OAuth 2.0 令牌响应（含 `access_token`、`token_type`、`expires_in` 等）。
- **错误**：`400` / `401`（`invalid_client` 时），Body 为 RFC 6749 风格：`{"error":"...","error_description":"..."}`。
- **客户端认证**：支持 **HTTP Basic**（`Authorization: Basic`）或 **表单** `client_id` + `client_secret`（与客户端快照中的 `allowAuthenticationMethods` 一致）。

**支持的 `grant_type`**：

| 值 | 说明 |
|----|------|
| `client_credentials` | 客户端凭证模式；仅返回 `access_token`（无 refresh）。 |
| `password` | 资源所有者密码模式；需 `username`、`password`。 |
| `refresh_token` | 刷新；需 `refresh_token`；旧 refresh 在成功后轮换删除。 |
| `authorization_code` | 授权码换令牌；需 `code`、`redirect_uri`，PKCE 时需 `code_verifier`。 |

### 9.2 `GET /oauth2/authorize`

- **用途**：授权码模式第一步；需 **已登录**（由 `OAuth2EndUserSessionPort` 判断）。
- **典型查询参数**：`response_type=code`、`client_id`、`redirect_uri`、`scope`、`state`；PKCE：`code_challenge`、`code_challenge_method`。
- **成功**：`302` 到 `redirect_uri?code=...&state=...`。
- **未登录**：`302` 到登录页（见第 4 节）。

### 9.3 `GET /.well-known/oauth-authorization-server`

- **用途**：RFC 8414；返回 JSON，含 `issuer`、`authorization_endpoint`、`token_endpoint`、`grant_types_supported`、`code_challenge_methods_supported` 等。

---

## 10. 安全与网关

- 本插件 **不** 用 Filter 挂载 OAuth2 端点；由 **Controller** 声明映射。
- 若使用 Sa-Token、Spring Security 等全局拦截，请将 **`/oauth2/**`**、**`/.well-known/**`** 加入匿名/白名单（以宿主配置为准）。
- 插件颁发的 **OAuth2 Bearer** 与 Sa-Token **会话 token** 为不同体系；保护业务 API 时需单独校验 OAuth2 令牌或统一接入资源服务器策略。

---

## 11. 与 Spring Authorization Server 迁移

核心服务仅依赖 SPI 与 Servlet API，持久化与客户端模型在宿主侧；迁移到 **Spring Authorization Server** 时，可逐步用其 `RegisteredClientRepository`、`AuthorizationServerSettings` 替换适配实现，HTTP 路径可保持兼容。

---

## 12. 相关类索引

| 类 | 包 |
|----|-----|
| `OAuth2AuthorizationServerApi` | `...springweb.api` |
| `OAuth2AuthorizationServerService` | `...springweb.api` |
| `OAuth2AuthorizationServerCoreService` | `...springweb.internal` |
| `OAuth2AuthorizationServerController` | `...springweb.web` |
| `OAuth2AuthorizationServerSpringWebAutoConfiguration` | `...springweb.autoconfigure` |
| `OAuth2ExtrasSaTokenAutoConfiguration` / `OAuth2ExtrasSpringSecurityAutoConfiguration` / `OAuth2ExtrasMybatisPlusAutoConfiguration` | `...springweb.autoconfigure` |
| `OAuth2ServerExtrasRedisTokenPersistenceAutoConfiguration` / `OAuth2ServerExtrasMybatisPlusTokenPersistenceAutoConfiguration` | `...springweb.autoconfigure`（**本模块**） |
| `OAuth2ExtrasPropertyPrefix` | `icu.jiapeng.kitty.oauth2.resource.springweb.autoconfigure` |
| `SaTokenOAuth2EndUserSessionAdapter` / `SpringSecurityOAuth2EndUserSessionAdapter` | `...springweb.extras.satoken` / `...extras.springsecurity` |
| `RedisOAuth2AuthorizationCodePersistenceAdapter` 等 / `OAuth2RedisTokenKeys` | `...server.springweb.extras.token.redis` |
| `MybatisPlusOAuth2AuthorizationCodePersistenceAdapter` 等 | `...server.springweb.extras.token.mybatisplus` |
| `MybatisPlusOAuth2ConsentStorageAdapter` | `...springweb.extras.mybatisplus` |
| `OAuth2AuthorizationServerSpringWebConfiguration` | `...springweb.config` |
| `KittyOAuth2AuthorizationServerProperties` / `OAuth2ExtrasTokenPersistence` / `OAuth2ExtrasPersistenceEffective` / `OAuth2ExtrasEndUserSession` / `OAuth2ExtrasConsentStorage` | `...springweb.properties` |
| `OAuth2TokenRequest` / `OAuth2AuthorizeRequest` / `OAuth2TokenIntrospectionRequest` | `...springweb.dto` |
| `OAuth2TokenSuccessResponse` / `OAuth2ErrorResponse` / `OAuth2AuthorizationServerMetadataResponse` | `...springweb.dto.response` |
| `OAuth2TokenIntrospectionResponse` | `...springweb.dto`（**本模块**；资源模块另有结构兼容的同名 record，供 RFC 7662 JSON 反序列化） |
| `OAuth2IntrospectionAccessTokenValidationAdapter` | `icu.jiapeng.kitty.oauth2.resource.springweb.extras.introspection`（**`kitty-oauth2-resource-spring-web`**，**`access-token-persistence=center`** 或回退的 **`token-persistence=center`** 时注册；Bean 名见该模块 README） |
| `OAuth2Rfc` / `OAuth2Pkce` / `OAuth2StandardGrantType` | `...springweb.protocol` |

---

文档版本随模块迭代更新；若行为与代码不一致，以 **源码与 OpenAPI** 为准。
