# API 层说明

组件中**不要**直接写 API 路径或 `api.get/post(...)` + 路径，避免多处硬编码同一路径。

- 按功能拆分：`auth_api.js`、`transcoder_api.js`、`strategy_api.js`
- 路径与请求方式集中在本目录下的 `*_api.js` 中定义
- 组件只引用这些模块的方法，例如：`import { login, listAccessKeys } from '../api/auth_api'`
