import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

export default defineConfig({
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src')
        }
    },
    plugins: [vue()],
    server: {
        port: 3004,
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:9704',
                changeOrigin: true,
                rewrite: (path) => path
            }
        }
    }
})

