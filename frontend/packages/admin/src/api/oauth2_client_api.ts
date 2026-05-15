import api from '../utils/api'

export interface Oauth2ClientItem {
    id: string
    clientName: string
    clientId: string
    clientSecret: string
    allowedScopes?: string
    allowedGrantTypes?: string
    allowAuthenticationMethods?: string
    allowedRedirectUris?: string
    accessTokenTimeout?: number
    refreshTokenTimeout?: number
    blackListExemption?: string
    status?: number
    requireAuthorizationConsent?: number
    createTime?: string
}

export function getOauth2ClientPage(params?: Record<string, unknown>): Promise<{
    records: Oauth2ClientItem[];
    total: number;
    page?: number;
    size?: number
}> {
    return api.get('/api/user/oauth2-client/query', {params})
}

export function getOauth2ClientById(id: string): Promise<Oauth2ClientItem> {
    return api.get(`/api/user/oauth2-client/${id}`)
}

export function createOauth2Client(data: Record<string, unknown>): Promise<Oauth2ClientItem> {
    return api.post('/api/user/oauth2-client', data)
}

export function updateOauth2Client(id: string, data: Record<string, unknown>): Promise<boolean> {
    return api.put(`/api/user/oauth2-client/${id}`, data)
}

export function deleteOauth2Client(id: string): Promise<boolean> {
    return api.delete(`/api/user/oauth2-client/${id}`)
}
