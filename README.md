# 🐱 Kitty CMS

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk" alt="Java 25"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=flat-square&logo=springboot" alt="Spring Boot 4"/>
  <img src="https://img.shields.io/badge/Vue-3-42B883?style=flat-square&logo=vuedotjs" alt="Vue 3"/>
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?style=flat-square&logo=mysql" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Redis-6-DC382D?style=flat-square&logo=redis" alt="Redis"/>
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue?style=flat-square" alt="License"/>
</p>

<p align="center">
  <strong>Content Management System Monorepo</strong> · 内容管理系统一体化仓库
</p>

---

## 📖 简介 | Introduction

**Kitty CMS** 是一个基于 Spring Boot 4 与 Vue 3 的模块化内容管理系统，采用 Monorepo 结构，包含用户中心、CMS、素材管理、转码服务等微服务及对应前端应用。

**Kitty CMS** is a modular content management system built on Spring Boot 4 and Vue 3. It uses a monorepo structure with microservices for user center, CMS, material management, transcoding, and their corresponding frontend applications.

---

## 🏗️ 仓库结构 | Repository Structure

```
kitty-cms/
├── kitty-common/          # 公共模块 Common
├── kitty-user/            # 用户中心 User Center (9701, 9801)
├── kitty-cms/              # CMS 服务 CMS Service (9702, 9802)
├── kitty-material/         # 素材管理 Material Management
├── kitty-transcoder/       # 转码服务 Transcoder (9703, 9803)
├── frontend/               # 前端应用 Frontend
│   └── packages/
│       ├── admin/          # 管理后台 Admin
│       ├── mobile/         # 移动端 Mobile
│       └── transcoder/     # 转码控制台 Transcoder Console
└── .github/workflows/      # CI/CD
```

### 模块导航 | Module Index

| 模块 Module | 说明 Description | 文档 Doc |
|-------------|------------------|----------|
| [kitty-common](kitty-common) | 公共工具、DTO、基础能力 | [README](kitty-common/README.md) |
| [kitty-user](kitty-user) | 用户、角色、租户、权限、配置 | [README](kitty-user/README.md) |
| [kitty-cms](kitty-cms) | 内容管理核心服务 | [README](kitty-cms/README.md) |
| [kitty-material](kitty-material) | 素材（图片/视频等）管理 | [README](kitty-material/README.md) |
| [kitty-transcoder](kitty-transcoder) | 音视频转码、预览、任务队列 | [README](kitty-transcoder/README.md) |
| [frontend](frontend) | Vue 3 前端应用集合 | [README](frontend/README.md) |

---

## ✨ 技术栈 | Tech Stack

| 后端 Backend | 前端 Frontend |
|--------------|---------------|
| Java 25 | Vue 3 |
| Spring Boot 4.0.2 | Vite 5 |
| MyBatis-Plus | Ant Design Vue |
| MySQL 8 / R2DBC | Pinia |
| Redis / Redisson | Axios |
| Sa-Token | - |
| gRPC | - |

---

## 🚀 快速开始 | Quick Start

### 环境要求 | Prerequisites

| 依赖 | 版本 |
|------|------|
| [JDK](https://adoptium.net/) | 25 |
| [Node.js](https://nodejs.org/) | 20+ |
| [Maven](https://maven.apache.org/) | 3.9+ |
| [MySQL](https://www.mysql.com/) | 8.0+ |
| [Redis](https://redis.io/) | 6+ |

### 构建后端 | Build Backend

```bash
./mvnw -B clean package -DskipTests
```

### 构建前端 | Build Frontend

```bash
cd frontend
npm ci
npm run build:admin      # 管理后台
npm run build:mobile    # 移动端
npm run build:transcoder # 转码控制台
npm run build:all       # 全部
```

### 运行服务 | Run Services

各服务独立启动，需先启动 MySQL、Redis：

```bash
# 用户中心
java -jar kitty-user/kitty-user-server/target/*.jar

# CMS
java -jar kitty-cms/kitty-cms-server/target/*.jar

# 素材管理
java -jar kitty-material/kitty-material-server/target/*.jar

# 转码服务（需 -Djavacpp.platform=linux-x86_64 生产环境）
java -jar kitty-transcoder/kitty-transcoder-server/target/app.jar
```

---

## ⚙️ 端口一览 | Port Overview

| 服务 Service | HTTP | gRPC |
|--------------|------|------|
| kitty-user | 9701 | 9801 |
| kitty-cms | 9702 | 9802 |
| kitty-transcoder | 9703 | 9803 |
| kitty-material | 见模块文档 | - |

---

## 📐 项目规范 | Project Conventions

**以下规范所有模块与后续代码生成必须严格遵守。**

### 1. 接口响应形式

- **正常响应**：接口成功时**直接返回业务数据**（裸响应），不做统一包装。例如返回 `UserVO`、`PageRespVo<T>`、`String`、`boolean` 等，不要包一层 `{ code, data, message }`。
- **错误响应**：不通过返回值里的 `code` 表示失败。业务异常时**抛出异常**（如 `BizException`），由项目内已实现的 **全局异常处理器**（如 `GlobalExceptionHandler`）统一捕获并封装为错误响应（如 `CommonErrorResult`）。调用方通过 HTTP 状态码与异常响应体判断错误。

### 2. API 契约与 DTO/VO 归属

- **除非明确仅用于内部、不对外暴露的接口**，其余所有对外的 HTTP API：
  - **DTO、VO** 必须放在对应模块的 **`-api` 包**（如 `kitty-user-api`、`kitty-cms-api`），不得放在 `-func` 或实现模块里。
  - **Controller** 必须实现以 **HttpExchange**（如 `@GetExchange`、`@PostExchange`）声明在 **`-api` 包** 中的 **Api 接口**，契约与实现分离。
- 入参、出参禁止使用裸 `Map`，必须使用 DTO/VO 类型（参见 `.cursor/rules/controller-request-response.mdc`）。

### 3. 前端调用后端：RPC 风格

- **禁止**在 Vue 或业务组件中直接写 `api.get/post/put/delete` 请求后端。
- **必须**按业务域建立 `*_api.js`（或 `*_api.ts`）模块：在模块内 `import api from '../utils/api'`、定义 `PREFIX`、用 `api.get/post/put/delete` 封装并**导出具名函数**（如 `login(data)`、`getUserPage(params)`）；组件内只 `import { login, getUserPage } from '@/api/xxx_api'` 并调用这些函数。详见 **`.cursor/rules/frontend-api-rpc-style.mdc`**。

### 4. 约束文件

- **前端静态资源**：图标、图片等路径**仅允许在 `assets/index.ts`（或项目约定的唯一入口）中书写一次**，其余通过 `import { IMG_LOGO } from '@/assets'` 等引用，禁止硬编码路径。详见 **`.cursor/rules/frontend-assets-centralized.mdc`**。
- 详细约束与示例见 **`.cursor/rules/`** 下规则文件（如 `api-response-and-contract.mdc`、`frontend-api-rpc-style.mdc`、`frontend-assets-centralized.mdc`、`controller-request-response.mdc`、`no-hardcoded-map-keys.mdc`、`type-registry-factory.mdc`），**代码生成与人工编码均需遵守**。

---

## 📁 配置 | Configuration

各服务配置位于 `*/src/main/resources/application.yml`，可配置数据源、Redis、端口等。

---

## 🔧 CI/CD | GitHub Actions

| 工作流 | 路径 | 说明 |
|--------|------|------|
| Transcoder 构建 | [.github/workflows/transcoder-build.yml](.github/workflows/transcoder-build.yml) | 后端 + 前端 + Docker 镜像推送 |

触发条件：推送 `main`/`develop` 或 commit/tag 以 `_transcoder` 结尾。

---

## 📄 许可证 | License

[Apache-2.0](LICENSE)

---

<p align="center">
  <sub>Built with ❤️ by Jia Peng</sub>
</p>
