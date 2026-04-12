import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import monacoEditorPluginRaw from 'vite-plugin-monaco-editor'

const monacoEditorPlugin =
  typeof monacoEditorPluginRaw === 'function'
    ? monacoEditorPluginRaw
    : (monacoEditorPluginRaw as { default: (opts: unknown) => unknown }).default

export default defineConfig({
  plugins: [react(), monacoEditorPlugin({})],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
  },
})
