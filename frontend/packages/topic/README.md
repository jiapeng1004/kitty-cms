# topic（选题独立前端）

Vue 3 + Vite + Ant Design Vue。开发时通过 Vite 将 `/api` 代理到本地 `kitty-topic`（默认 `9704`）。

## Token

首次可从 URL 注入并写入本地存储，例如：

`http://localhost:3004/?token=YOUR_TOKEN&provider=github`

- `token`：写入 `localStorage` 作为 Bearer
- `provider`（可选）：写入后作为请求头 `X-OAuth-Provider`

生产环境可设置 `VITE_API_BASEURL` 指向选题服务公网地址。

## 脚本

```bash
npm run dev   # 在 packages/topic 内，或从 frontend 根执行 npm run dev:topic
npm run build
```
