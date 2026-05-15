import api from '../utils/api'

export function getConfigPage(params?: Record<string, unknown>): Promise<{ records: unknown[]; total: number }> {
  return api.get('/api/config/query', { params })
}

export function getConfigById(id: string): Promise<unknown> {
  return api.get(`/api/config/${id}`)
}

export function createConfig(data: Record<string, unknown>): Promise<unknown> {
  return api.post('/api/config', data)
}

export function updateConfig(id: string, data: Record<string, unknown>): Promise<unknown> {
  return api.put(`/api/config/${id}`, data)
}

export function removeConfig(id: string): Promise<unknown> {
  return api.delete(`/api/config/${id}`)
}

/** 仅设置配置值（id 或 configKey 二选一 + configValue） */
export function setConfigVal(data: { id?: string; configKey?: string; configValue: string }): Promise<string> {
  return api.post('/api/config/setVal', data)
}
