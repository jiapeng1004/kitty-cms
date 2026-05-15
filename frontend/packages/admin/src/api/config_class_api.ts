import api from '../utils/api'

export function getConfigClassPage(params?: Record<string, unknown>): Promise<{ records: unknown[]; total: number }> {
  return api.get('/api/config/class/query', { params })
}

export function getConfigClassById(id: string): Promise<unknown> {
  return api.get(`/api/config/class/${id}`)
}

export function createConfigClass(data: Record<string, unknown>): Promise<unknown> {
  return api.post('/api/config/class', data)
}

export function updateConfigClass(id: string, data: Record<string, unknown>): Promise<unknown> {
  return api.put(`/api/config/class/${id}`, data)
}

export function removeConfigClass(id: string): Promise<unknown> {
  return api.delete(`/api/config/class/${id}`)
}
