import api from '../utils/api'
import { USER_SERVICE_PATH } from './constants'

const PREFIX = `${USER_SERVICE_PATH}/api/role`

export interface RoleItem {
  id: string
  roleName: string
  createTime?: string
}

export interface RolePageResp {
  records: RoleItem[]
}

export function getRolePage(): Promise<RolePageResp> {
  return api.get(`${PREFIX}/page`)
}

