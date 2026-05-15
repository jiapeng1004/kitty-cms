import api, {getBaseURL} from '../utils/api'

export interface LoginParams {
    username: string
    password: string
    captcha: string
}

export interface LoginResult {
    token: string
    timeout: number
}

export function login(data: LoginParams): Promise<LoginResult> {
    return api.post('/api/user/login', data)
}

/** 退出登录：使服务端使当前 token 失效 */
export function logout(): Promise<void> {
    return api.post('/api/user/logout')
}

/** 与后端 UserRegister 一致：nickName、pwd、captcha 必填，其余可选 */
export interface RegisterParams {
    nickName: string
    pwd: string
    captcha: string
    phone?: string
    email?: string
    realName?: string
}

export function register(data: RegisterParams): Promise<unknown> {
    return api.post('/api/user/register', data)
}

export function getCaptchaUrl(): string {
    return `${getBaseURL()}/api/user/captcha?t=${Date.now()}`
}

/**
 * 通过 axios 拉取验证码图片（会带上 X-Tenant-Id 等请求头），返回可展示的 Blob URL。
 * 调用方在刷新或卸载时需对返回值执行 URL.revokeObjectURL 释放。
 */
export async function fetchCaptchaImageUrl(): Promise<string> {
    const blob = await api.get(getCaptchaUrl(), { responseType: 'blob' }) as Blob
    return URL.createObjectURL(blob)
}

/** 预检：检查该 source 是否已配置并就绪，再决定是否展示或跳转 render */
export function checkOpenAuthReady(source: string): Promise<boolean> {
    return api.get(`/open/oauth2/auth/check/${encodeURIComponent(source)}`)
}

/** 第三方登录跳转地址（飞书、钉钉等），前端先预检通过后再用此 URL 跳转或 a 标签 href */
export function getOAuth2RenderUrl(source: string): string {
    return `${getBaseURL()}/open/oauth2/auth/render/${encodeURIComponent(source)}`
}
