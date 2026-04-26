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
  fileSize?: number
  chunkCrc32List?: string
  fingerprint?: string
  type: number
  /** 预览用相对路径（走 /api/material/resource/preview，带鉴权；img/video 应用此字段） */
  previewUrl?: string
  /** 对象存储原文件直链（下载/转码；列表避免用作 img 直链） */
  srcUrl?: string
  /** 封面（图片默认同 srcUrl 原图直链） */
  coverUrl?: string
  /** 视频关键帧（/keyframe；未产出为 null） */
  keyframeUrl?: string
}

/** 后端返回的 previewUrl 多为以 / 开头的相对路径，拼上 VITE_API_BASEURL 供 img/video src 使用 */
export function materialApiAbsoluteUrl(relativeOrAbsolute: string | undefined | null): string {
  if (!relativeOrAbsolute) return ''
  if (/^https?:\/\//i.test(relativeOrAbsolute)) return relativeOrAbsolute
  const base = (import.meta.env.VITE_API_BASEURL || '').replace(/\/$/, '')
  const p = relativeOrAbsolute.startsWith('/') ? relativeOrAbsolute : `/${relativeOrAbsolute}`
  return `${base}${p}`
}

/** 与 {@link icu.jiapeng.kitty.common.core.page.PageRespVo} 对齐 */
export interface PageRespVo<T> {
  size: number
  page: number
  total: number
  records: T[]
  pages?: number
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
  /** 关键词（须带 catalogId；具体走 DB 条件还是检索由后端实现） */
  keyword?: string
  /** 语义检索文本（须带 catalogId；由后端实现） */
  semanticText?: string
  limit?: number
}): Promise<MaterialResourceVO[]> {
  return api.get(`${PREFIX}/list`, { params })
}

/** 分页列表（DB 路径为真分页；带 keyword/semantic 时与 list 同检索逻辑后内存切片） */
export function pageResources(params: {
  catalogId?: string
  parentId?: string
  keyword?: string
  semanticText?: string
  page?: number
  size?: number
}): Promise<PageRespVo<MaterialResourceVO>> {
  return api.get(`${PREFIX}/page`, { params })
}

/** 回收站专用分页，与 /page 解耦 */
export function pageRecycleResources(params: {
  catalogId: string
  parentId?: string
  page?: number
  size?: number
}): Promise<PageRespVo<MaterialResourceVO>> {
  return api.get(`${PREFIX}/recycle/page`, { params })
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

export interface MaterialResourceDerivativeVO {
  destinationType: string
  available?: boolean
  accessUrl?: string
  fileSize?: number
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
  /** 衍生产物（多码率/封面/雪碧等） */
  derivatives?: MaterialResourceDerivativeVO[]
}

export interface MaterialDownloadUrlVO {
  resourceId: string
  destinationType: string
  actualDestinationType: string
  url: string
  expiresInSec?: number
}

export interface MaterialDownloadReportItemDTO {
  resourceId: string
  destinationType: string
  resourceTitle?: string
  actualDestinationType?: string
}

export interface MaterialDownloadReportBatchDTO {
  items: MaterialDownloadReportItemDTO[]
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

/** 按分级解析可下载直链（缺码率时非 COVER/SPRITE 可能降级为 SOURCE） */
export function getResourceDownloadUrl(params: {
  resourceId: string
  destinationType: string
}): Promise<MaterialDownloadUrlVO> {
  return api.get(`${PREFIX}/download-url`, { params })
}

export function reportResourceDownload(body: MaterialDownloadReportItemDTO): Promise<void> {
  return api.post(`${PREFIX}/download/report`, body)
}

export function reportResourceDownloadBatch(body: MaterialDownloadReportBatchDTO): Promise<void> {
  return api.post(`${PREFIX}/download/report/batch`, body)
}

export function recycleResourcesToBin(body: { resourceIds: string[] }): Promise<void> {
  return api.post(`${PREFIX}/recycle`, body)
}
