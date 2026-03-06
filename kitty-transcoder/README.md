# 🎬 Kitty Transcoder

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk" alt="Java 25"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=flat-square&logo=springboot" alt="Spring Boot 4"/>
  <img src="https://img.shields.io/badge/Vue-3-42B883?style=flat-square&logo=vuedotjs" alt="Vue 3"/>
  <img src="https://img.shields.io/badge/FFmpeg-JavaCV-0078D4?style=flat-square&logo=ffmpeg" alt="FFmpeg"/>
  <img src="https://img.shields.io/badge/gRPC-9803-244C5A?style=flat-square&logo=grpc" alt="gRPC"/>
</p>

<p align="center">
  <strong>Media transcoding service</strong> · 媒体转码服务
</p>

---

## 📖 简介 | Introduction

**Kitty Transcoder** 是 [Kitty CMS](https://github.com/jiapengaoa/kitty-cms) 生态中的媒体转码微服务，基于 JavaCV/FFmpeg 提供音视频转码、预览、任务队列与策略管理能力，支持 HTTP REST 与 gRPC 双协议。

**Kitty Transcoder** is a media transcoding microservice in the [Kitty CMS](https://github.com/jiapengaoa/kitty-cms) ecosystem. Built on JavaCV/FFmpeg, it provides video/audio transcoding, preview, task queue, and strategy management, with both HTTP REST and gRPC support.

---

## ✨ 特性 | Features

| 特性 Feature | 说明 Description |
|-------------|------------------|
| 🎞️ **转码任务** | 提交、查询、进度追踪、SSE 实时推送 |
| 📺 **预览** | 视频帧预览、元信息查询 |
| ⚙️ **策略** | 转码策略 CRUD、预设模板 |
| 🔐 **认证** | 登录、AccessKey 管理、签名校验 |
| 📡 **双协议** | REST API + gRPC |
| 📞 **回调** | HTTP / gRPC 进度回调（调用方实现 TranscodeListenerService） |

---

## 🏗️ 项目结构 | Project Structure

```
kitty-transcoder/
├── kitty-transcoder-api/     # DTO、接口定义
├── kitty-transcoder-grpc/    # gRPC proto 与生成代码
├── kitty-transcoder-func/    # 业务逻辑（转码、Redis、MyBatis-Plus）
├── kitty-transcoder-server/  # Spring Boot 入口
├── Dockerfile                # CI 多阶段构建
└── Dockerfile-single         # 单机构建（含 JavaCV linux-x86_64）

frontend/packages/transcoder/ # Vue 3 + Vite + Ant Design Vue
├── src/
│   ├── api/                  # API 封装
│   ├── views/                # 页面
│   └── stores/               # Pinia 状态
└── vite.config.ts
```

### 模块说明 | Module Overview

| 模块 Module | 职责 Responsibility |
|------------|---------------------|
| [kitty-transcoder-api](kitty-transcoder-api) | DTO、接口、常量 |
| [kitty-transcoder-grpc](kitty-transcoder-grpc) | gRPC proto、生成代码 |
| [kitty-transcoder-func](kitty-transcoder-func) | 转码引擎、任务队列、策略、Redis、MyBatis-Plus |
| [kitty-transcoder-server](kitty-transcoder-server) | Spring Boot 应用、配置、入口 |

---

## 🚀 构建 | Build

### 环境要求 | Prerequisites

| 依赖 | 版本 |
|------|------|
| [JDK](https://adoptium.net/) | 25 |
| [Node.js](https://nodejs.org/) | 20+ |
| [Maven](https://maven.apache.org/) | 3.9+ |
| [MySQL](https://www.mysql.com/) | 8.0+ |
| [Redis](https://redis.io/) | 6+ |

### 后端构建 | Backend

```bash
# 从仓库根目录执行
./mvnw -B "-Djavacpp.platform=linux-x86_64" \
  -pl kitty-transcoder/kitty-transcoder-server -am \
  -DskipTests clean package
```

> 生产环境需指定 `-Djavacpp.platform=linux-x86_64` 以使用预编译 JavaCV 平台库；本地开发可省略。

### 前端构建 | Frontend

```bash
cd frontend
npm ci
npm run build:transcoder
```

### Docker 构建 | Docker

```bash
# 需先完成后端、前端构建
docker build -f kitty-transcoder/Dockerfile -t kitty-transcoder .
```

---

## ⚙️ 配置 | Configuration

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 9703 | HTTP 端口 |
| `spring.grpc.server.port` | 9803 | gRPC 端口 |
| `spring.data.redis.*` | localhost:6379 | Redis |
| `spring.datasource.*` | MySQL | 数据源 |
| `transcoder.temp-dir` | `${java.io.tmpdir}/transcode` | 临时目录 |
| `transcoder.output.http-prefix` | 空 | 输出 URL 前缀 |

配置文件：`kitty-transcoder-server/src/main/resources/application.yml`

---

## 运行 | Run

```bash
# 本地启动（需 MySQL、Redis）
java -jar kitty-transcoder-server/target/app.jar

# 访问
# HTTP: http://localhost:9703
# Swagger UI: http://localhost:9703/swagger-ui.html
# OpenAPI JSON: http://localhost:9703/v3/api-docs
# gRPC: localhost:9803
```

---

## 回调通知 | Notification Callback

创建任务时可配置 `notifications`，支持两种方式，**统一载荷** `TranscodeProgressNotifyVO`：

| method | target | 说明 |
|--------|--------|------|
| HTTP | 回调 URL | POST JSON，载荷同上 |
| GRPC | host:port | 调用方需实现 [TranscodeListenerService](kitty-transcoder-grpc/src/main/proto/transcode_listener.proto)，转码服务作为 gRPC 客户端回调 `OnProgress` |

**统一载荷字段**：`taskId`, `status`, `progress`, `outputPath`, `outputHttpUrl`, `errorMessage`

### 自省观察员 | Self-Introspection（开发阶段）

无外部调用方时，可将通知目标指向转码服务自身，仅打日志便于观察：

| method | target | 说明 |
|--------|--------|------|
| HTTP | `http://localhost:9703/api/transcode/introspection/notification` | 本服务内置 HTTP 回调端点，仅 log |
| GRPC | `localhost:9803` | 本服务内置 TranscodeListenerService 实现，仅 log |

---

## 主要 API | REST API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/task` | 提交转码任务 |
| GET | `/task/{id}` | 查询任务 |
| GET | `/tasks` | 任务列表 |
| GET | `/progress/{id}` | 查询进度 |
| GET | `/sse/{id}` | SSE 进度流 |
| POST | `/strategy` | 创建策略 |
| GET | `/strategy` | 策略列表 |
| GET | `/preview` | 视频帧预览 |
| POST | `/login` | 登录 |
| POST | `/access-key` | 创建 AccessKey |

---

## CI/CD | GitHub Actions

| 工作流 | 路径 | 触发条件 |
|--------|------|----------|
| 构建 | [.github/workflows/transcoder-build.yml](../.github/workflows/transcoder-build.yml) | 推送 `main`/`develop` 或 commit/tag 以 `_transcoder` 结尾 |

### 流程概览

```
[build-backend]  → JDK 25, Maven 构建, 上传 app.jar
       ↓
[docker] ← [build-frontend] → Node 20, npm ci, build:transcoder
       ↓
推送镜像 → registry.cn-hangzhou.aliyuncs.com/jp-java/kitty-transcoder
```

### 使用 Secrets

- `ALIYUN_ACR_PASSWORD`：阿里云 ACR 访问凭证

---

## 技术栈 | Tech Stack

| 后端 | 前端 |
|------|------|
| Java 25 | Vue 3 |
| Spring Boot 4 | Vite 5 |
| MyBatis-Plus | Ant Design Vue |
| Redis / Redisson | Pinia |
| JavaCV (FFmpeg) | Axios |
| gRPC | - |

---

## 许可证 | License

[Apache-2.0](../LICENSE)

---

<p align="center">
  <sub>Built with ❤️ for</sub>
  <a href="https://github.com/jiapengaoa/kitty-cms">Kitty CMS</a>
</p>
