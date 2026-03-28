/// <reference types="vite/client" />

// 仅为当前包补齐 Vite env 的类型声明，避免 Cursor/TS 提示
// `Property 'VITE_API_BASEURL' does not exist on type 'ImportMetaEnv'`。
interface ImportMetaEnv {
  readonly VITE_API_BASEURL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

