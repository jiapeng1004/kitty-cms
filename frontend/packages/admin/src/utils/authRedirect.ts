/** 与 api.ts 保持一致的键，避免与 api 互相 import 造成循环依赖 */
const TOKEN_KEY = 'kitty_admin_token'

/**
 * 与 kitty-user Sa-Token 一致：请求头为 Authorization、前缀 Bearer，持久化层只存 raw token（uuid）。
 * 若历史数据或回调 URL 误带了 "Bearer " 前缀，读入时剥掉，避免重复拼接成非法头。
 */
export function normalizeAccessToken(raw: string | null | undefined): string | null {
  if (raw == null) {
    return null
  }
  let t = String(raw).trim()
  if (!t) {
    return null
  }
  if (/^bearer\s+/i.test(t)) {
    t = t.replace(/^bearer\s+/i, '').trim()
  }
  return t || null
}
const TENANT_ID_KEY = 'kitty_admin_tenant_id'
const USER_NAME_KEY = 'kitty_admin_user_name'

let redirecting = false

function clearAdminSessionForLogin(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(TENANT_ID_KEY)
  localStorage.removeItem(USER_NAME_KEY)
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
 * 跳转到登录并带上 redirect（当前除登录/注册外的地址）
 */
export function redirectToLogin(): void {
  if (redirecting) {
    return
  }
  redirecting = true
  clearAdminSessionForLogin()
  const current = window.location.pathname + window.location.search + window.location.hash
  if (current.startsWith('/login') || current.startsWith('/register')) {
    return
  }
  const backParam = encodeURIComponent(current)
  const configured = (import.meta.env.VITE_LOGIN_URL as string | undefined)?.trim()
  if (configured) {
    try {
      const u = new URL(configured, window.location.origin)
      u.searchParams.set('redirect', current)
      window.location.replace(u.toString())
      return
    } catch {
      // 回退
    }
  }
  window.location.replace(`/login?redirect=${backParam}`)
}
