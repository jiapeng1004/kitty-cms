# kitty-local-dns-starter

为基于 **Spring `RestClient` + `HttpServiceProxyFactory`** 的 **声明式 HTTP 客户端**（`@HttpExchange`）提供可选的 **本地 host 别名解析**：把请求 URI 中的 **主机名** 按配置重写为 **`host:port`**，便于本地或联调时把 `http://kitty-user/...` 这类别名转发到真实地址，而**不修改**业务接口上的路径与 query。

---

## 1. 适用场景

- `@HttpExchange(url = "http://kitty-user")` 等使用 **短主机名** 作为逻辑服务名；本地没有 DNS 时，通过 YAML 将 `kitty-user` 映射到 `host.example.com:9701`。
- 与 **Spring 6+** `RestClient`、`HttpServiceProxyFactory` 配合（本模块注册的 **`HttpServiceProxyFactory`** 会安装 **`LocalDnsClientHttpRequestInterceptor`**）。

---

## 2. Maven 依赖

```xml
<dependency>
    <groupId>icu.jiapeng</groupId>
    <artifactId>kitty-local-dns-starter</artifactId>
    <version>${kittycms.version}</version>
</dependency>
```

依赖：`spring-web`、`spring-boot`（无 `spring-boot-starter-web` 强绑定，按宿主应用补齐）。

---

## 3. 配置

配置类：**`LocalDnsProperties`**，绑定前缀 **`kitty`**，实际映射写在 **`kitty.local-dns`** 下。

```yaml
kitty:
  local-dns:
    enabled: "true"          # 可选；不填时：存在至少一条 alias 映射则视为启用
    kitty-user: "kitty-user.jiapeng.asia:9701"
```

| 键 | 含义 |
|----|------|
| **`enabled`** | 可选。为 `true`/`false` 时显式开关；未配置时若存在除预留键外的映射则 **启用** |
| **其它键** | **alias → `host:port` 字符串**；`host:port` 支持 `host:9701` 或仅 `host`（端口默认 **80**） |

预留键 **`enabled`** 不会进入映射表；解析时过滤，见 **`LocalDnsProperties#getMappings()`**。

---

## 4. 行为说明

- **`LocalDnsEnabledCondition`**：仅当 **`kitty.local-dns` 启用**（显式 `enabled=true` 或存在有效映射）时，装配本模块 Bean。
- **`LocalDnsAutoConfiguration`**：条件满足时注册 **`@Primary`** 的 **`HttpServiceProxyFactory`**，内部 **`RestClient`** 带有 **`LocalDnsClientHttpRequestInterceptor`**。
- **拦截器**：若当前请求 URI 的 **host** 命中映射表中的 **alias**，则把 **scheme、host、port** 替换为目标，**path、query、fragment** 不变。

若应用中已有自定义 **`HttpServiceProxyFactory`** Bean，请注意 **`@Primary`** 与条件装配的交互，避免重复或冲突。

---

## 5. 相关类

| 类 | 说明 |
|----|------|
| `LocalDnsProperties` | `kitty.local-dns` 绑定 |
| `LocalDnsAutoConfiguration` | `HttpServiceProxyFactory` + `RestClient` |
| `LocalDnsClientHttpRequestInterceptor` | URI host/port 重写 |
| `LocalDnsEnabledCondition` | 启用条件 |

自动配置入口：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` → **`icu.jiapeng.kitty.dns.LocalDnsAutoConfiguration`**。
