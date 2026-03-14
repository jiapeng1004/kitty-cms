import api from '../utils/api'
import { USER_SERVICE_PATH } from './constants'

const PREFIX = `${USER_SERVICE_PATH}/api/configClass`

export function getConfigClassPage(params?: Record<string, unknown>): Promise<{ records: unknown[]; total: number }> {
  return api.get(`${PREFIX}/query`, { params })
}

export function getConfigClassById(id: string): Promise<unknown> {
  return api.get(`${PREFIX}/${id}`)
}

export function createConfigClass(data: Record<string, unknown>): Promise<unknown> {
  return api.post(PREFIX, data)
}

export function updateConfigClass(id: string, data: Record<string, unknown>): Promise<unknown> {
  return api.put(`${PREFIX}/${id}`, data)
}

export function removeConfigClass(id: string): Promise<unknown> {
  return api.delete(`${PREFIX}/${id}`)
}
