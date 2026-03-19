import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

import { initAuthFromUrl } from './utils/auth'

// token/provider 从 URL 解析后写入 localStorage（符合你的 `?token=` 流程）
initAuthFromUrl()

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(Antd)

app.mount('#app')

