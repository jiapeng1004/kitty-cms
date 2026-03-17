import api from '../utils/api'
import { USER_SERVICE_PATH } from './constants'

const PREFIX = `${USER_SERVICE_PATH}/api/oauth2-client`

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

export function getOauth2ClientPage(params?: Record<string, unknown>): Promise<{ records: Oauth2ClientItem[]; total: number; page?: number; size?: number }> {
  return api.get(`${PREFIX}/query`, { params })
}

export function getOauth2ClientById(id: string): Promise<Oauth2ClientItem> {
  return api.get(`${PREFIX}/${id}`)
}

export function createOauth2Client(data: Record<string, unknown>): Promise<Oauth2ClientItem> {
  return api.post(PREFIX, data)
}

export function updateOauth2Client(id: string, data: Record<string, unknown>): Promise<boolean> {
  return api.put(`${PREFIX}/${id}`, data)
}

export function deleteOauth2Client(id: string): Promise<boolean> {
  return api.delete(`${PREFIX}/${id}`)
}

