import * as Cookies from "js-cookie";

/** 与 admin 共用的 localStorage 键，登录态由同一 token 表示 */
const TOKEN_KEY = 'kitty_admin_token'

let redirecting = false

export function getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY) ?? Cookies.default.get('token') ?? null
}

export function clearMamSession(): void {
    localStorage.removeItem(TOKEN_KEY)
}

/**
 * 401、业务码或文案表明需重新登录时返回 true
 */
export function isUnauthorizedError(error: unknown): boolean {
    const e = error as {
        response?: { status?: number; data?: { message?: string; code?: number | string } }
    }
    const status = e.response?.status
    if (status === 401) {
        return true
    }
    const data = e.response?.data
    if (data == null) {
        return false
    }
    const code = data.code
    if (code === 401 || code === '401' || code === 10001) {
        return true
    }
    const msg = data.message != null ? String(data.message) : ''
    if (!msg) {
        return false
    }
    if (msg === 'token.expired' || msg === 'token.invalid') {
        return true
    }
    const lower = msg.toLowerCase()
    if (lower.includes('unauthorized') || lower.includes('not login')) {
        return true
    }
    if (/未.{0,4}登[录陆]/.test(msg) || (msg.includes('登录') && (msg.includes('过期') || msg.includes('失效')))) {
        return true
    }
    if (lower.includes('token') && (lower.includes('expired') || lower.includes('invalid') || lower.includes('empty'))) {
        return true
    }
    return false
}

/**
 * 跳转到登录页。若配置了 VITE_LOGIN_URL 则使用绝对地址（多端口开发时常用）；
 * 否则当前站点 /login?redirect=当前完整 URL。
 */
export function redirectToLogin(): void {
    if (redirecting) {
        return
    }
    redirecting = true
    clearMamSession()
    const back = window.location.href
    const configured = (import.meta.env.VITE_LOGIN_URL as string | undefined)?.trim()
    if (configured) {
        try {
            const u = new URL(configured, window.location.origin)
            u.searchParams.set('redirect', back)
            window.location.replace(u.toString())
            return
        } catch {
            // 配置非法时回退
        }
    }
    window.location.replace(`/login?redirect=${encodeURIComponent(back)}`)
}
