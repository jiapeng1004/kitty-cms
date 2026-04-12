import client from './client'
import { pathsV1, sourceById } from './v1Paths'

export type ListSourcesParams = {
  page?: number
  page_size?: number
  enabled?: string
  type?: string
}

/** 下拉/管理页默认一次拉全量信息源 */
export const DEFAULT_SOURCE_LIST_PARAMS: ListSourcesParams = {
  page: 1,
  page_size: 500,
}

export function listSources(params: ListSourcesParams = DEFAULT_SOURCE_LIST_PARAMS) {
  return client.get(pathsV1.sources, { params })
}

/** 信息源 type 字段中已有标签的词频 Top N（最多 5） */
export function getSourceTypeTagSuggestions(limit = 5) {
  return client.get<{ items: { tag: string; count: number }[] }>(
    pathsV1.sourcesTypeTagSuggestions,
    { params: { limit } },
  )
}

/** Spider 名 = name；config 内不含 spiderName */
export function validateSourceSpider(payload: {
  name: string
  config: Record<string, unknown>
}) {
  return client.post(pathsV1.sourcesValidateSpider, payload)
}

export function createSource(payload: Record<string, unknown>) {
  return client.post(pathsV1.sources, payload)
}

export function updateSource(sourceId: number, payload: Record<string, unknown>) {
  return client.put(sourceById(sourceId), payload)
}

export function deleteSource(sourceId: number) {
  return client.delete(sourceById(sourceId))
}
