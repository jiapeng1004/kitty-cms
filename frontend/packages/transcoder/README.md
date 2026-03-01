# 转码服务前端

Vue 3 + Ant Design Vue + Vite。对接转码后端（默认 `http://localhost:9703/kitty-transcoder`）。

## 开发

```bash
# 安装依赖（在 frontend 根目录）
npm install

# 启动后端（在项目根目录）
cd kitty-transcoder/kitty-transcoder-server && mvn spring-boot:run

# 启动前端（在 frontend 根目录）
npm run dev:transcoder
```

前端: http://localhost:3002  
后端 API: http://localhost:9703/kitty-transcoder  
需先在后端创建 Access Key（或通过本前端的 Access Key 页创建），再用 AK/SK 登录。

## Chrome E2E 测试（Playwright）

**前置**：Redis 已启动；后端已启动（9703）；前端已启动（3002）。

```bash
cd frontend/packages/transcoder
npm install
npx playwright install chromium
npm run test:e2e
```

带界面调试：

```bash
npm run test:e2e:ui
```

环境变量（可选）：

- `TRANSCODER_UI_URL`：前端地址，默认 `http://localhost:3002`
- `TRANSCODER_API_URL`：后端 API 根地址，默认 `http://localhost:9703/kitty-transcoder`
- `HEADLESS=0`：有头模式运行浏览器

测试流程：创建 AK -> 登录 -> 创建策略 -> 创建任务 -> 查看任务详情。
