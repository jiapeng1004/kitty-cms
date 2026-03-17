# 👤 Kitty User

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk" alt="Java 25"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=flat-square&logo=springboot" alt="Spring Boot 4"/>
  <img src="https://img.shields.io/badge/Sa--Token-1.37-FF6B6B?style=flat-square" alt="Sa-Token"/>
  <img src="https://img.shields.io/badge/gRPC-9801-244C5A?style=flat-square&logo=grpc" alt="gRPC"/>
</p>

<p align="center">
  <strong>User Center Service</strong> · 用户中心服务
</p>

---

## 📖 简介 | Introduction

**Kitty User** 是 Kitty CMS 的用户中心微服务，提供用户、角色、租户、权限、配置等管理能力，支持 REST API 与 gRPC 双协议。

**Kitty User** is the user center microservice for Kitty CMS, providing user, role, tenant, permission, and config management with both REST and gRPC support.

---

## 🏗️ 结构 | Structure

```
kitty-user/
├── kitty-user-api/           # DTO、接口定义
├── kitty-user-grpc/           # gRPC proto 与生成代码
├── kitty-user-plugin-grpc-sa/ # gRPC 与 Sa-Token 集成插件
├── kitty-user-func/           # 业务逻辑
└── kitty-user-server/         # Spring Boot 入口
```

### 模块说明 | Module Overview

| 模块 | 职责 |
|------|------|
| [kitty-user-api](kitty-user-api) | DTO、接口、常量 |
| [kitty-user-grpc](kitty-user-grpc) | gRPC proto、生成代码 |
| [kitty-user-plugin-grpc-sa](kitty-user-plugin-grpc-sa) | gRPC 调用与 Sa-Token 认证集成 |
| [kitty-user-func](kitty-user-func) | 用户、角色、租户、权限、配置、SQL 等业务 |
| [kitty-user-server](kitty-user-server) | Spring Boot 应用、配置、入口 |

---

## ✨ 功能 | Features

| 功能 | 说明 |
|------|------|
| 👤 用户 | 用户 CRUD、登录认证 |
| 🎭 角色 | 角色管理 |
| 🏢 租户 | 多租户支持 |
| 🔐 权限 | 权限管理 |
| ⚙️ 配置 | 系统配置、配置类 |
| 📡 双协议 | REST API + gRPC |

---

## 🚀 构建 | Build

```bash
# 从仓库根目录
./mvnw -B -pl kitty-user/kitty-user-server -am clean package -DskipTests
```

---

## ⚙️ 配置 | Configuration

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 9701 | HTTP 端口 |
| `spring.grpc.server.port` | 9801 | gRPC 端口 |
| `spring.data.redis.*` | localhost:6379 | Redis |

配置文件：`kitty-user-server/src/main/resources/application.yml`

---

## 🏃 运行 | Run

```bash
java -jar kitty-user/kitty-user-server/target/*.jar
```

访问：`http://localhost:9701` · Swagger: `http://localhost:9701/swagger-ui.html`

---

## 📄 许可证 | License

[Apache-2.0](../LICENSE)
