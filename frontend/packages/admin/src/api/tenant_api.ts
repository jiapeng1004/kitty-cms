import api from '../utils/api'
import { USER_SERVICE_PATH } from './constants'

const PREFIX = `${USER_SERVICE_PATH}/api/tenant`

/** 开放接口：登录页租户下拉列表，无需鉴权 */
export interface TenantOption {
  id: string
  name: string
}

export function getTenantList(): Promise<TenantOption[]> {
  return api.get(`${PREFIX}/list`)
}

export function getTenantPage(params?: Record<string, unknown>): Promise<{ records: unknown[]; total: number }> {
  return api.get(`${PREFIX}/query`, { params })
}

export function getTenantById(id: string): Promise<unknown> {
  return api.get(`${PREFIX}/${id}`)
}

export function createTenant(data: Record<string, unknown>): Promise<unknown> {
  return api.post(PREFIX, data)
}

export function updateTenant(id: string, data: Record<string, unknown>): Promise<unknown> {
  return api.put(`${PREFIX}/${id}`, data)
}

export function deleteTenant(id: string): Promise<unknown> {
  return api.delete(`${PREFIX}/${id}`)
}
