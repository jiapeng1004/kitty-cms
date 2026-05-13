# 🎬 Transcoder Console

<p align="center">
  <img src="https://img.shields.io/badge/Vue-3-42B883?style=flat-square&logo=vuedotjs" alt="Vue 3"/>
  <img src="https://img.shields.io/badge/Vite-5-646CFF?style=flat-square&logo=vite" alt="Vite 5"/>
  <img src="https://img.shields.io/badge/Ant%20Design%20Vue-4-0170FE?style=flat-square" alt="Ant Design Vue"/>
  <img src="https://img.shields.io/badge/Playwright-E2E-2EAD4B?style=flat-square&logo=playwright" alt="Playwright"/>
</p>

<p align="center">
  <strong>Transcoder Service Console</strong> · 转码服务控制台
</p>

---

## 📖 简介 | Introduction

**Transcoder Console** 是 Kitty Transcoder 转码服务的 Web 控制台，基于 Vue 3 + Ant Design Vue + Vite 构建，对接转码后端 API（默认 `http://localhost:9703/kitty-transcoder`）。

**Transcoder Console** is the web UI for Kitty Transcoder, built with Vue 3, Ant Design Vue, and Vite, connecting to the transcoder backend API.

---

## 🚀 开发 | Development

```bash
# 在 frontend 根目录
npm run dev:transcoder
```

| 地址 | 说明 |
|------|------|
| 前端 | http://localhost:3002（以 Vite 配置为准） |
| 后端 API | http://localhost:9703/kitty-transcoder |

**前置**：需先启动转码后端；创建 Access Key 后使用 AK/SK 登录。

---

## 📦 构建 | Build

```bash
npm run build:transcoder
```

---

## 🧪 E2E 测试 | Playwright E2E

**前置**：Redis、转码后端（9703）、前端（3002）已启动。

```bash
cd frontend/packages/transcoder
npm install
npx playwright install chromium
npm run test:e2e
```

带界面调试：`npm run test:e2e:ui`

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `TRANSCODER_UI_URL` | http://localhost:3002 | 前端地址 |
| `TRANSCODER_API_URL` | http://localhost:9703/kitty-transcoder | 后端 API |
| `HEADLESS` | 1 | 0=有头模式 |

测试流程：创建 AK → 登录 → 创建策略 → 创建任务 → 查看任务详情。

---

## 📄 许可证 | License

[Apache-2.0](../../../LICENSE)
