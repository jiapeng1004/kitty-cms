# kitty-topic

选题（Topic）微服务：Gin + GORM + Swagger（占位 OpenAPI）+ 原生 gRPC + Zap + Viper。

## 运行

在 `kitty-topic/` 目录：

```bash
go run .
```

默认：

- HTTP：`http://127.0.0.1:9704`
- gRPC：`127.0.0.1:9091`
- 配置：`application.yml`；也可通过环境变量 `KITTY_TOPIC_*` 覆盖（Viper `SetEnvPrefix`）

## 鉴权

- `Authorization: Bearer <token>`
- kitty-user token：连接 `auth.kittyUserGrpcAddress` 调用 gRPC `TokenIntrospection`
- 否则需 `X-OAuth-Provider`，并在 `topic.providers.<name>.userinfoUrl` 配置三方 userinfo

## 重新生成 gRPC 代码

```bash
go generate
```
