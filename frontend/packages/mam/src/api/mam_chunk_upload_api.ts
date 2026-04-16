import api from '@/utils/api'
import { MATERIAL_SERVICE_PATH } from './constants'

const PREFIX = `${MATERIAL_SERVICE_PATH}/api/material/upload/chunk/session`

/** 分片上传耗时较长，避免默认 10s 超时 */
const UPLOAD_TIMEOUT_MS = 300_000

export interface MaterialChunkUploadPartVO {
  chunkIndex: number
  byteSize: number
}

export interface MaterialChunkUploadSessionVO {
  id: string
  resourceId: string
  catalogId?: string
  parentResourceId?: string
  title?: string
  resourceType?: number
  storageId: string
  objectKey: string
  totalSize: number
  chunkSize: number
  chunkCount: number
  status: string
  parts: MaterialChunkUploadPartVO[]
}

/** 与创建资源时的预编目载荷一致，合并成功后会写入元数据实例 */
export interface MaterialPrecatalogPayloadDTO {
  templateId: string
  fieldValues: Record<string, string>
}

export interface MaterialChunkUploadSessionCreateDTO {
  /** 与下面 MAM 字段二选一：有值则沿用「已有资源 + 已保存指纹」旧流程 */
  resourceId?: string
  catalogId?: string
  parentId?: string
  title?: string
  /** ResourceTypeEnum，非文件夹 */
  type?: number
  /** 各分片 CRC32（无符号），条数须等于分片规划；totalSize=0 时不传 */
  chunkCrc32List?: number[]
  precatalog?: MaterialPrecatalogPayloadDTO
  /** 不传则后端使用主存储 */
  storageId?: string
  objectKey: string
  totalSize: number
  chunkSize: number
}

export function createChunkSession(data: MaterialChunkUploadSessionCreateDTO): Promise<MaterialChunkUploadSessionVO> {
  return api.post(PREFIX, data, { timeout: UPLOAD_TIMEOUT_MS })
}

export function getChunkSession(sessionId: string): Promise<MaterialChunkUploadSessionVO> {
  return api.get(`${PREFIX}/${encodeURIComponent(sessionId)}`)
}

export function reportChunkPart(
  sessionId: string,
  data: { chunkIndex: number; byteSize: number }
): Promise<MaterialChunkUploadSessionVO> {
  return api.post(`${PREFIX}/${encodeURIComponent(sessionId)}/part`, data, { timeout: UPLOAD_TIMEOUT_MS })
}

export function completeChunkSession(sessionId: string): Promise<MaterialChunkUploadSessionVO> {
  return api.post(`${PREFIX}/${encodeURIComponent(sessionId)}/complete`, {}, { timeout: UPLOAD_TIMEOUT_MS })
}

export function cancelChunkSession(sessionId: string): Promise<MaterialChunkUploadSessionVO> {
  return api.post(`${PREFIX}/${encodeURIComponent(sessionId)}/cancel`)
}

/**
 * 5.6：二进制分片，须携带 Content-Range: bytes first-last/total；
 * Content-Length 可省略（则按 body 长度），若带 header 必须与 body 一致。
 */
export function uploadChunkWithHttpHeaders(
  sessionId: string,
  body: ArrayBuffer | Uint8Array | string,
  contentRange: string,
  contentLength?: number
): Promise<MaterialChunkUploadSessionVO> {
  const payload =
    typeof body === 'string' ? new TextEncoder().encode(body) : body instanceof Uint8Array ? body : new Uint8Array(body)
  const headers: Record<string, string | number> = {
    'Content-Type': 'application/octet-stream',
    'Content-Range': contentRange
  }
  if (contentLength !== undefined) {
    headers['Content-Length'] = contentLength
  } else {
    headers['Content-Length'] = payload.byteLength
  }
  return api.post(`${PREFIX}/${encodeURIComponent(sessionId)}/chunk`, payload, {
    headers,
    timeout: UPLOAD_TIMEOUT_MS
  })
}
