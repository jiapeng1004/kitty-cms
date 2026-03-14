import api from '../utils/api'
import { USER_SERVICE_PATH } from './constants'

const PREFIX = `${USER_SERVICE_PATH}/api/user`

export function getUserPage(params?: { page?: number; size?: number; searchKey?: string }): Promise<{ records: unknown[]; total: number; page?: number; size?: number }> {
  return api.get(`${PREFIX}/query`, { params })
}

export function getUserById(id: string): Promise<unknown> {
  return api.get(`${PREFIX}/${id}`)
}

export function updateUser(id: string, data: Record<string, unknown>): Promise<unknown> {
  return api.put(`${PREFIX}/${id}`, data)
}
