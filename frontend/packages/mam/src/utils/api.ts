import axios, {type AxiosInstance} from 'axios'
import {isUnauthorizedError, redirectToLogin} from '@/utils/authRedirect'

const baseURL = import.meta.env.VITE_API_BASEURL || ''

const TOKEN_KEY = 'kitty_admin_token'

const baseConfig = {
    baseURL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
}

function attachAuthInterceptor(instance: AxiosInstance) {
    instance.interceptors.request.use((config) => {
        const token = localStorage.getItem(TOKEN_KEY)
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    })
    return instance
}

/** 未解包 body，用于需要 HTTP 状态码（如 204）的场景 */
export const httpClient = attachAuthInterceptor(axios.create(baseConfig))
httpClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (isUnauthorizedError(error)) {
            redirectToLogin()
        }
        return Promise.reject(error)
    }
)

/** 默认：成功时返回 response.data，与后端直出 DTO 约定一致 */
const api = attachAuthInterceptor(axios.create(baseConfig))
api.interceptors.response.use(
    (response) => response.data,
    (error) => {
        if (isUnauthorizedError(error)) {
            redirectToLogin()
        }
        return Promise.reject(error)
    }
)

export default api
