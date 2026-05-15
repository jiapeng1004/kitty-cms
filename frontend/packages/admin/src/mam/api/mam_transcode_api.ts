import api from '@/utils/api'

export interface MaterialTranscodeStrategyVO {
  id: string
  name: string
  platformCode: string
  externalStrategyId: string
  paramsJson?: string
  enabled?: number
  /** ResourceTypeEnum，与全局默认、解析链一致 */
  resourceType?: number
  /** 1=该资源类型下全局默认 */
  isGlobalDefault?: number
}

export interface MaterialTranscodeStrategyUpsertDTO {
  id?: string
  name: string
  platformCode: string
  externalStrategyId: string
  paramsJson?: string
  enabled?: number
  resourceType?: number
  isGlobalDefault?: number
}

export interface CatalogTranscodeBindVO {
  id: string
  catalogId: string
  strategyId: string
  strategyName?: string
  resourceType?: number
  sortNum?: number
}

export interface CatalogTranscodeBindCreateDTO {
  catalogId: string
  strategyId: string
  resourceType?: number
  sortNum?: number
}

export function listStrategies(resourceType?: number): Promise<MaterialTranscodeStrategyVO[]> {
  return api.get(`/api/material/transcode/strategy/list`, { params: resourceType != null ? { resourceType } : {} })
}

export function createStrategy(data: MaterialTranscodeStrategyUpsertDTO): Promise<MaterialTranscodeStrategyVO> {
  return api.post(`/api/material/transcode/strategy`, data)
}

export function updateStrategy(data: MaterialTranscodeStrategyUpsertDTO): Promise<MaterialTranscodeStrategyVO> {
  return api.put(`/api/material/transcode/strategy`, data)
}

export function deleteStrategy(id: string): Promise<void> {
  return api.delete(`/api/material/transcode/strategy`, { params: { id } })
}

export function listBinds(catalogId: string): Promise<CatalogTranscodeBindVO[]> {
  return api.get(`/api/material/transcode/bind/list`, { params: { catalogId } })
}

export function createBind(data: CatalogTranscodeBindCreateDTO): Promise<CatalogTranscodeBindVO> {
  return api.post(`/api/material/transcode/bind`, data)
}

export function deleteBind(id: string): Promise<void> {
  return api.delete(`/api/material/transcode/bind`, { params: { id } })
}
