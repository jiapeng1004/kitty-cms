import api from '@/utils/api'
import { MATERIAL_SERVICE_PATH } from './constants'

const PREFIX = `${MATERIAL_SERVICE_PATH}/api/material/transcode`

export interface MaterialTranscodeStrategyVO {
  id: string
  name: string
  platformCode: string
  externalStrategyId: string
  paramsJson?: string
  enabled?: number
}

export interface MaterialTranscodeStrategyUpsertDTO {
  id?: string
  name: string
  platformCode: string
  externalStrategyId: string
  paramsJson?: string
  enabled?: number
}

export interface CatalogTranscodeBindVO {
  id: string
  catalogId: string
  strategyId: string
  resourceType?: number
  sortNum?: number
}

export interface CatalogTranscodeBindCreateDTO {
  catalogId: string
  strategyId: string
  resourceType?: number
  sortNum?: number
}

export function listStrategies(): Promise<MaterialTranscodeStrategyVO[]> {
  return api.get(`${PREFIX}/strategy/list`)
}

export function createStrategy(data: MaterialTranscodeStrategyUpsertDTO): Promise<MaterialTranscodeStrategyVO> {
  return api.post(`${PREFIX}/strategy`, data)
}

export function updateStrategy(data: MaterialTranscodeStrategyUpsertDTO): Promise<MaterialTranscodeStrategyVO> {
  return api.put(`${PREFIX}/strategy`, data)
}

export function deleteStrategy(id: string): Promise<void> {
  return api.delete(`${PREFIX}/strategy`, { params: { id } })
}

export function listBinds(catalogId: string): Promise<CatalogTranscodeBindVO[]> {
  return api.get(`${PREFIX}/bind/list`, { params: { catalogId } })
}

export function createBind(data: CatalogTranscodeBindCreateDTO): Promise<CatalogTranscodeBindVO> {
  return api.post(`${PREFIX}/bind`, data)
}

export function deleteBind(id: string): Promise<void> {
  return api.delete(`${PREFIX}/bind`, { params: { id } })
}
