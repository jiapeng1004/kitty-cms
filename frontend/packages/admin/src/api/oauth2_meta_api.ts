import api from '../utils/api'

export interface Oauth2GrantTypeItem {
    code: string
    desc: string
}

export function getOpenOauth2GrantTypeList(): Promise<Oauth2GrantTypeItem[]> {
    return api.get('/open/oauth2/grant-types')
}

export interface Oauth2ClientAuthenticationMethodItem {
    code: string
    desc: string
}

export function getOpenOauth2ClientAuthenticationMethodList(): Promise<Oauth2ClientAuthenticationMethodItem[]> {
    return api.get('/open/oauth2/client-authentication-methods')
}
