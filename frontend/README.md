# 🖥️ Kitty CMS Frontend

<p align="center">
  <img src="https://img.shields.io/badge/Vue-3-42B883?style=flat-square&logo=vuedotjs" alt="Vue 3"/>
  <img src="https://img.shields.io/badge/Vite-5-646CFF?style=flat-square&logo=vite" alt="Vite 5"/>
  <img src="https://img.shields.io/badge/Ant%20Design%20Vue-4-0170FE?style=flat-square" alt="Ant Design Vue"/>
  <img src="https://img.shields.io/badge/Node-20+-339933?style=flat-square&logo=nodedotjs" alt="Node 20"/>
</p>

<p align="center">
  <strong>Frontend Applications for Kitty CMS</strong> · Kitty CMS 前端应用集合
</p>

---

## 📖 简介 | Introduction

**Kitty CMS Frontend** 是 Kitty CMS 的前端应用 Monorepo，采用 npm workspaces 管理多个子包，包含管理后台、移动端、转码控制台等。

**Kitty CMS Frontend** is the frontend monorepo for Kitty CMS, using npm workspaces to manage admin panel, mobile app, transcoder console, and more.

---

## 🏗️ 结构 | Structure

```
frontend/
├── packages/
│   ├── admin/        # 管理后台 Admin Panel
│   ├── mobile/       # 移动端 (uni-app) Mobile
│   └── transcoder/   # 转码控制台 Transcoder Console
└── package.json      # Workspace 根配置
```

### 子包导航 | Package Index

| 子包 Package | 说明 Description | 文档 Doc |
|--------------|------------------|----------|
| [admin](packages/admin) | 管理后台，Vue 3 + Ant Design Vue | [README](packages/admin/README.md) |
| [mobile](packages/mobile) | 移动端，uni-app 多端 | [README](packages/mobile/README.md) |
| [transcoder](packages/transcoder) | 转码服务控制台 | [README](packages/transcoder/README.md) |

---

## 🚀 快速开始 | Quick Start

### 安装依赖 | Install

```bash
npm ci
```

### 开发 | Development

```bash
npm run dev:admin      # 管理后台
npm run dev:mobile     # 移动端 (uni-app)
npm run dev:transcoder # 转码控制台
```

### 构建 | Build

```bash
npm run build:admin
npm run build:mobile
npm run build:transcoder
npm run build:all      # 全部构建
```

---

## ⚙️ 技术栈 | Tech Stack

| 子包 | 框架 | UI |
|------|------|-----|
| admin | Vue 3 + Vite | Ant Design Vue |
| mobile | uni-app (Vue 3) | uni-ui |
| transcoder | Vue 3 + Vite | Ant Design Vue |

---

## 📄 许可证 | License

[Apache-2.0](../LICENSE)
