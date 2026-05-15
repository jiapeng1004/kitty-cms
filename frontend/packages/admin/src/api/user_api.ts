import api from '../utils/api'

export function getUserPage(params?: { page?: number; size?: number; searchKey?: string }): Promise<{ records: unknown[]; total: number; page?: number; size?: number }> {
  return api.get('/api/user/query', { params })
}

export function getUserById(id: string): Promise<unknown> {
  return api.get(`/api/user/${id}`)
}

export function updateUser(id: string, data: Record<string, unknown>): Promise<unknown> {
  return api.put(`/api/user/${id}`, data)
}
