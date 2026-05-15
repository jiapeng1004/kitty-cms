/// <reference types="vite/client" />

// 仅为当前包补齐 Vite env 的类型声明，避免 Cursor/TS 提示
// `Property 'VITE_API_BASEURL' does not exist on type 'ImportMetaEnv'`。
interface ImportMetaEnv {
  readonly VITE_API_BASEURL?: string
  /** 未登录时跳转的登录页绝对地址，例如 http://localhost:5173/login（MAM 与 admin 分端口时） */
  readonly VITE_LOGIN_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

