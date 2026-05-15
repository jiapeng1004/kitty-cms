import api from '../utils/api'

/** 开放接口：登录页租户下拉列表，无需鉴权 */
export interface TenantOption {
  id: string
  name: string
}

export function getTenantList(): Promise<TenantOption[]> {
  return api.get('/api/tenant/list')
}

export function getTenantPage(params?: Record<string, unknown>): Promise<{ records: unknown[]; total: number }> {
  return api.get('/api/tenant/query', { params })
}

export function getTenantById(id: string): Promise<unknown> {
  return api.get(`/api/tenant/${id}`)
}

export function createTenant(data: Record<string, unknown>): Promise<unknown> {
  return api.post('/api/tenant', data)
}

export function updateTenant(id: string, data: Record<string, unknown>): Promise<unknown> {
  return api.put(`/api/tenant/${id}`, data)
}

export function deleteTenant(id: string): Promise<unknown> {
  return api.delete(`/api/tenant/${id}`)
}
