import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASEURL || ''

const instance = axios.create({
    baseURL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

const TOKEN_KEY = 'kitty_admin_token'
const TENANT_ID_KEY = 'kitty_admin_tenant_id'
export const USER_NAME_KEY = 'kitty_admin_user_name'

export function getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string | null): void {
    if (token) localStorage.setItem(TOKEN_KEY, token)
    else localStorage.removeItem(TOKEN_KEY)
}

export function getTenantId(): string {
    return localStorage.getItem(TENANT_ID_KEY) || ''
}

export function setTenantId(tenantId: string): void {
    if (tenantId) localStorage.setItem(TENANT_ID_KEY, tenantId)
    else localStorage.removeItem(TENANT_ID_KEY)
}

export function setUserName(name: string | null): void {
    if (name) localStorage.setItem(USER_NAME_KEY, name)
    else localStorage.removeItem(USER_NAME_KEY)
}

export function getBaseURL(): string {
    return baseURL
}

/**
 * 从请求异常中取优先展示的文案：若有后端返回的 body.message 则用，否则用 error.message。
 */
export function getResponseMessage(error: unknown): string {
    const err = error as { response?: { data?: { message?: string } }; message?: string }
    const bodyMessage = err.response?.data?.message
    if (bodyMessage != null && String(bodyMessage).trim() !== '') {
        return String(bodyMessage).trim()
    }
    return err.message != null ? String(err.message) : '请求失败'
}

instance.interceptors.request.use(
    (config) => {
        const token = getToken()
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        const tenantId = getTenantId()
        if (tenantId) {
            config.headers['X-Tenant-Id'] = tenantId
        }
        return config
    },
    (error) => Promise.reject(error)
)

instance.interceptors.response.use(
    (response) => response.data,
    (error) => {
        const status = error.response?.status as number | undefined
        const msg: string | undefined = error.response?.data?.message

        // 未登录 / 登录过期 / token 无效等场景统一视为需重新登录
        if (status === 401 || msg === 'token.expired' || msg === 'token.invalid') {
            setToken(null)
            setUserName(null)
            const current = window.location.pathname + window.location.search
            if (!current.startsWith('/login') && !current.startsWith('/register')) {
                const redirect = encodeURIComponent(current)
                window.location.href = `/login?redirect=${redirect}`
            }
        }
        return Promise.reject(error)
    }
)

export default instance
