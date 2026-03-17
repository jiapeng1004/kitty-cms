import api from '../utils/api'
import { USER_SERVICE_PATH } from './constants'

export interface Oauth2GrantTypeItem {
  code: string
  desc: string
}

export function getOpenOauth2GrantTypeList(): Promise<Oauth2GrantTypeItem[]> {
  return api.get(`${USER_SERVICE_PATH}/open/oauth2/grant-types`)
}

