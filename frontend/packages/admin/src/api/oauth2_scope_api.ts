import api from '../utils/api'
import { USER_SERVICE_PATH } from './constants'

const PREFIX = `${USER_SERVICE_PATH}/api/oauth2-scope`

export interface Oauth2ScopeItem {
  id: string
  scopeName: string
  scopeCode: string
  scopeDesc?: string
}

export function getOauth2ScopeList(): Promise<Oauth2ScopeItem[]> {
  return api.get(`${PREFIX}/list`)
}

