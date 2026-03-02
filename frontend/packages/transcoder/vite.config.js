import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3002,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:9703',
        changeOrigin: true,
        rewrite: (path) => '/kitty-transcoder' + path
      }
    }
  }
})
