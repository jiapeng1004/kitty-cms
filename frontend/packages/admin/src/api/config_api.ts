import api from '../utils/api'
import { USER_SERVICE_PATH } from './constants'

const PREFIX = `${USER_SERVICE_PATH}/api/config`

export function getConfigPage(params?: Record<string, unknown>): Promise<{ records: unknown[]; total: number }> {
  return api.get(`${PREFIX}/query`, { params })
}

export function getConfigById(id: string): Promise<unknown> {
  return api.get(`${PREFIX}/${id}`)
}

export function createConfig(data: Record<string, unknown>): Promise<unknown> {
  return api.post(PREFIX, data)
}

export function updateConfig(id: string, data: Record<string, unknown>): Promise<unknown> {
  return api.put(`${PREFIX}/${id}`, data)
}

export function removeConfig(id: string): Promise<unknown> {
  return api.delete(`${PREFIX}/${id}`)
}

/** 仅设置配置值（id 或 configKey 二选一 + configValue） */
export function setConfigVal(data: { id?: string; configKey?: string; configValue: string }): Promise<string> {
  return api.post(`${PREFIX}/setVal`, data)
}
