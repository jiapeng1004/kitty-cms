# kitty-user-plugin-grpc-sa

在 **Sa-Token** 与 **kitty-user gRPC 服务**之间搭桥：**角色/权限列表**通过 gRPC 拉取；**会话存储（SaTokenDao）** 的读路径走 **Auth** gRPC，便于网关或业务服务不落地 Redis 也能校验登录态（写路径见下文限制）。

---

## 1. 模块定位

| 项目 | 说明 |
|------|------|
| **`StpInterface`** | `SaGrpcInterface`：实现 `getRoleList` / `getPermissionList`，分别调用 `KtRoleService`、`KtPermissionSvc` |
| **`SaTokenDao`** | `SaTokenDaoKittyUser`：基于 `AuthServiceGrpc.AuthServiceBlockingStub` 的 **`get` / `getTimeout`**；其余方法当前 **未实现**（抛出 `UnsupportedOperationException`） |
| **gRPC 客户端** | 使用 **`@GrpcClient("kitty-user")`**（`grpc-spring-boot-starter`），需为名为 **`kitty-user`** 的 channel 配置地址 |

---

## 2. Maven 依赖

```xml
<dependency>
    <groupId>icu.jiapeng</groupId>
    <artifactId>kitty-user-plugin-grpc-sa</artifactId>
    <version>${kittycms.version}</version>
</dependency>
```

传递依赖主要包括：**`sa-token-spring-boot3-starter`**、**`kitty-user-grpc`**（含 gRPC stub 与 `grpc-client-spring-boot-starter` 等）。

---

## 3. 装配方式

通过 **`META-INF/spring.factories`** 注册：

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
icu.jiapeng.kitty.plugin.sa.config.SaGrpcConfig
```

**`SaGrpcConfig`** 会：

- `@Import(SaGrpcInterface.class)`
- 声明 **`@GrpcClientBean`**，注册 **`kitty-user`** 通道上的 `AuthServiceBlockingStub`、`KtRoleServiceBlockingStub`
- 注册 **`SaTokenDaoKittyUser`**、**`SaGrpcInterface`**（构造需要 **`KtPermissionSvcBlockingStub`**；若 classpath 上未由其它 `@GrpcClientBean` / 扫描注册，请在宿主侧为 `KtPermissionSvcGrpc.KtPermissionSvcBlockingStub` 补充与 **`kitty-user`** 的客户端声明）

宿主需配置 gRPC 客户端，例如（具体键名以项目 **`grpc-spring-boot-starter`** 版本为准）：

```yaml
grpc:
  client:
    kitty-user:
      address: "static://127.0.0.1:9090"
```

---

## 4. 使用注意

- **`SaTokenDao` 写操作**：`set`、`delete`、`updateTimeout` 等 **未实现**；若 Sa-Token 运行时需要在本进程写入会话存储，需改用完整 **`SaTokenDao`** 实现或调整 Sa-Token 存储策略。
- **与用户服务契约**：Proto 方法需与 **`kitty-user`** 服务端一致（`saTokenDaoGet`、`saTokenDaoTimeOut`、`getRoleIdsByUserId`、`getPCodesByUserId` 等）。

---

## 5. 相关类

| 类 | 职责 |
|----|------|
| `SaGrpcConfig` | Spring 配置与 Bean 注册 |
| `SaGrpcInterface` | `StpInterface` 实现 |
| `SaTokenDaoKittyUser` | `SaTokenDao` 读路径 gRPC 适配 |
