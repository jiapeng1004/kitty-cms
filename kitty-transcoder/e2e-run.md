# 转码服务完整 E2E 测试说明

## 1. 环境准备

- JDK 25+、Node 18+、Redis、Maven
- 后端：`kitty-transcoder-server`（端口 9703，context-path `/kitty-transcoder`）
- 前端：`frontend/packages/transcoder`（端口 3002，代理 `/api` 到后端）

## 2. 启动顺序

**终端 1 - Redis**（若未常驻）：

```bash
redis-server
```

**终端 2 - 后端**：

```bash
cd kitty-transcoder/kitty-transcoder-server
mvn spring-boot:run
```

等待出现 “Started KittyTranscoderApplication”。

**终端 3 - 前端**：

```bash
cd frontend
npm install
npm run dev:transcoder
```

**终端 4 - E2E**：

```bash
cd frontend/packages/transcoder
npm install
npx playwright install chromium
npm run test:e2e
```

## 3. Chrome 有头模式（便于排查）

```bash
cd frontend/packages/transcoder
HEADLESS=0 npm run test:e2e
```

或使用 UI 模式：

```bash
npm run test:e2e:ui
```

## 4. 测试覆盖

- 登录页展示与 AK/SK 登录
- 策略列表、新建策略（名称、目标格式）
- 任务列表、新建任务（输入类型 DISK、路径、策略选择）、任务详情（状态、输出路径、输出 HTTP）
- Access Key 列表页

## 5. 自定义地址

```bash
TRANSCODER_UI_URL=http://127.0.0.1:3002 TRANSCODER_API_URL=http://127.0.0.1:9703/kitty-transcoder npm run test:e2e
```
