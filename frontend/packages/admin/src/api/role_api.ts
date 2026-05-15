import api from '../utils/api'

export interface RoleItem {
  id: string
  roleName: string
  createTime?: string
}

export interface RolePageResp {
  records: RoleItem[]
}

export function getRolePage(): Promise<RolePageResp> {
  return api.get('/api/role/page')
}
