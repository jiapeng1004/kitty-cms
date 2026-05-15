import api from '../utils/api'

export interface Oauth2ScopeItem {
    id: string
    scopeName: string
    scopeCode: string
    scopeDesc?: string
}

export function getOauth2ScopeList(): Promise<Oauth2ScopeItem[]> {
    return api.get('/open/oauth2/scope/list')
}
