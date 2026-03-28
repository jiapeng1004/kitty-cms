import api from '@/utils/api'
import { httpClient } from '@/utils/api'
import { MATERIAL_SERVICE_PATH } from './constants'
import type { MaterialReviewTaskVO } from './mam_review_api'

export type { MaterialReviewTaskVO }

const PREFIX = `${MATERIAL_SERVICE_PATH}/api/material/resource`

export interface MaterialResourceVO {
  id: string
  title: string
  catalogId: string
  parentId: string
  path: string
  fileSize?: number
  chunkCrc32List?: string
  fingerprint?: string
  type: number
}

export interface FingerprintPrecheckResult {
  hit: boolean
  matchedResources: MaterialResourceVO[]
}

export interface MaterialResourceUpsertDTO {
  id?: string
  title: string
  catalogId: string
  parentId?: string
  type: number
}

export function listResources(params: {
  catalogId?: string
  parentId?: string
  /** 全文检索（走 ES，须带 catalogId） */
  keyword?: string
  /** 语义检索（走 ES kNN，须带 catalogId） */
  semanticText?: string
  limit?: number
}): Promise<MaterialResourceVO[]> {
  return api.get(`${PREFIX}/list`, { params })
}

export function createResource(data: MaterialResourceUpsertDTO): Promise<MaterialResourceVO> {
  return api.post(`${PREFIX}`, data)
}

export function updateResource(data: MaterialResourceUpsertDTO): Promise<MaterialResourceVO> {
  return api.put(`${PREFIX}`, data)
}

export function createFolder(data: { title: string; catalogId: string; parentId?: string }): Promise<MaterialResourceVO> {
  return api.post(`${PREFIX}/folder`, data)
}

export function rebuildPaths(data: { catalogId: string }): Promise<void> {
  return api.post(`${PREFIX}/path/rebuild`, data)
}

export function planFolderUpload(data: {
  catalogId: string
  parentId?: string
  relativePaths: string[]
}): Promise<MaterialResourceVO[]> {
  return api.post(`${PREFIX}/folder/plan-upload`, data)
}

export function saveFingerprint(data: {
  resourceId: string
  fileSize: number
  chunkCrc32List: number[]
}): Promise<MaterialResourceVO> {
  return api.post(`${PREFIX}/fingerprint`, data)
}

export function precheckFingerprint(data: {
  fileSize: number
  chunkCrc32List: number[]
}): Promise<FingerprintPrecheckResult> {
  return api.post(`${PREFIX}/fingerprint/precheck`, data)
}

export interface MaterialMetaFileVO {
  id: string
  resourceId: string
  name: string
  size?: number
  relaPath?: string
  storageId: string
  objectKey: string
}

export interface MaterialMetaFileBindDTO {
  resourceId: string
  storageId: string
  objectKey: string
  name?: string
}

export interface MaterialMetadataInstanceEntryVO {
  fieldId: string
  fieldCode: string
  fieldName: string
  fieldValue: string
  version?: number
  lastVersion?: number
  templateId?: string
}

export interface MaterialMetadataSnapshotVO {
  templateId: string
  templateName: string
  version: number | null
  entries: MaterialMetadataInstanceEntryVO[]
}

export interface MaterialResourceTaskVO {
  id: string
  resourceId: string
  resourceTitle: string
  taskType: string
  thirdTaskId?: string
  progress?: number
  status?: string
  inputType?: string
  inputPath?: string
  materialStrategyId?: string
  strategyName?: string
}

export interface MaterialResourceDetailVO {
  resource: MaterialResourceVO
  metadata: MaterialMetadataSnapshotVO[]
  tasks?: MaterialResourceTaskVO[]
  /** 转码类任务分区 */
  transcodeTasks?: MaterialResourceTaskVO[]
  /** 标签/向量等启发式筛选 */
  taggingTasks?: MaterialResourceTaskVO[]
  /** 通用审核任务 */
  reviewTasks?: MaterialReviewTaskVO[]
}

/** 无记录时返回 null（HTTP 204） */
export async function getMetaFile(resourceId: string): Promise<MaterialMetaFileVO | null> {
  const res = await httpClient.get<MaterialMetaFileVO>(`${PREFIX}/meta-file`, {
    params: { resourceId },
    validateStatus: (s) => s >= 200 && s < 300
  })
  if (res.status === 204) return null
  const data = res.data
  if (data === '' || data == null) return null
  return data as MaterialMetaFileVO
}

export function bindMetaFile(data: MaterialMetaFileBindDTO): Promise<MaterialMetaFileVO> {
  return api.post(`${PREFIX}/meta-file/bind`, data)
}

export function getResourceDetail(resourceId: string): Promise<MaterialResourceDetailVO> {
  return api.get(`${PREFIX}/detail`, { params: { resourceId } })
}
