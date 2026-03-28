import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import {fileURLToPath} from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

export default defineConfig({
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src')
        }
    },
    plugins: [vue()],
    server: {
        port: 3003,
        proxy: {
            '/kitty-mam': {
                target: 'http://localhost:9703',
                changeOrigin: true
            }
        }
    }
})
