# 📝 Kitty CMS

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=flat-square&logo=springboot" alt="Spring Boot 4"/>
  <img src="https://img.shields.io/badge/gRPC-9802-244C5A?style=flat-square&logo=grpc" alt="gRPC"/>
</p>

<p align="center">
  <strong>Content Management Core Service</strong> · 内容管理核心服务
</p>

---

## 📖 简介 | Introduction

**Kitty CMS** 是 Kitty CMS 生态的内容管理核心微服务，提供内容编排、运行时信息等能力，支持 REST API 与 gRPC。

**Kitty CMS** is the content management core microservice, providing content orchestration, runtime info, and related features with REST and gRPC support.

---

## 🏗️ 结构 | Structure

```
kitty-cms/
├── kitty-cms-api/     # DTO、接口定义
├── kitty-cms-func/    # 业务逻辑
└── kitty-cms-server/  # Spring Boot 入口
```

### 模块说明 | Module Overview

| 模块 | 职责 |
|------|------|
| [kitty-cms-api](kitty-cms-api) | DTO、接口、常量 |
| [kitty-cms-func](kitty-cms-func) | 业务逻辑、Controller、AOP |
| [kitty-cms-server](kitty-cms-server) | Spring Boot 应用、配置、入口 |

---

## 🚀 构建 | Build

```bash
# 从仓库根目录
./mvnw -B -pl kitty-cms/kitty-cms-server -am clean package -DskipTests
```

---

## ⚙️ 配置 | Configuration

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 9702 | HTTP 端口 |
| `spring.grpc.server.port` | 9802 | gRPC 端口 |
| `spring.data.redis.*` | localhost:6379 | Redis |

配置文件：`kitty-cms-server/src/main/resources/application.yml`

---

## 🏃 运行 | Run

```bash
java -jar kitty-cms/kitty-cms-server/target/*.jar
```

访问：`http://localhost:9702` · Swagger: `http://localhost:9702/swagger-ui.html`

---

## 📄 许可证 | License

[Apache-2.0](../LICENSE)
