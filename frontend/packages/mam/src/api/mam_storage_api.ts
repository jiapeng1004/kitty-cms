import api from '@/utils/api'
import { MATERIAL_SERVICE_PATH } from './constants'

const PREFIX = `${MATERIAL_SERVICE_PATH}/api/material/storage`

export interface StorageRoutePreviewResult {
  storageId: string
  engineType: number
  driverName: string
  objectKey: string
  routeTarget: string
}

export interface StorageObjectKeyNormalizeResult {
  originalKey: string
  normalizedKey: string
  valid: boolean
  driverName: string
}

export interface StorageConnectivityResult {
  storageId: string
  engineType: number
  driverName: string
  reachable: boolean
  detail: string
}

export function listStorageIds(): Promise<string[]> {
  return api.get(`${PREFIX}/list`)
}

export function previewStorageRoute(data: {
  storageId: string
  objectKey: string
}): Promise<StorageRoutePreviewResult> {
  return api.post(`${PREFIX}/route/preview`, data)
}

export function normalizeObjectKey(data: {
  storageId: string
  objectKey: string
}): Promise<StorageObjectKeyNormalizeResult> {
  return api.post(`${PREFIX}/object-key/normalize`, data)
}

export function checkStorageConnectivity(data: {
  storageId: string
  objectKey?: string
}): Promise<StorageConnectivityResult> {
  return api.post(`${PREFIX}/connectivity/check`, data)
}

/** 与后端 FileStorageInstanceCodeEnum 对应的实例编码选项 */
export interface MaterialStorageInstanceOptionVO {
  storageType: string
  code: string
  label: string
}

export function listStorageInstanceOptions(): Promise<MaterialStorageInstanceOptionVO[]> {
  return api.get(`${PREFIX}/config/instance-options`)
}

/** 管理端：存储配置列表项（需 material:storage:manage，AK/SK 明文便于运维） */
export interface MaterialFileStorageVO {
  /** 存储主键，即对外 storageId */
  id: string
  storageType: string
  bucket?: string | null
  internalEndpoint?: string | null
  externalEndpoint?: string | null
  accessKey?: string | null
  secretKey?: string | null
  secretConfigured?: boolean | null
  primaryStorage?: boolean | null
}

/** 管理端：创建/更新存储配置 */
export interface MaterialFileStorageUpsertDTO {
  /** 新建可选：有则作为存储主键；留空则服务端 SecureRandom 生成 */
  id?: string
  storageType: string
  bucket?: string
  internalEndpoint?: string
  externalEndpoint?: string
  accessKey?: string
  secretKey?: string
  /** 是否设为主存储（全局唯一） */
  primaryStorage?: boolean
}

export function listFileStorageConfigs(): Promise<MaterialFileStorageVO[]> {
  return api.get(`${PREFIX}/config/list`)
}

export function createFileStorage(
  data: MaterialFileStorageUpsertDTO
): Promise<MaterialFileStorageVO> {
  return api.post(`${PREFIX}/config`, data)
}

export function updateFileStorage(
  data: MaterialFileStorageUpsertDTO
): Promise<MaterialFileStorageVO> {
  return api.put(`${PREFIX}/config`, data)
}

export function deleteFileStorage(storageId: string): Promise<void> {
  return api.delete(`${PREFIX}/config/${storageId}`)
}
