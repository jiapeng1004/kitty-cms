import api from '@/utils/api'

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
  return api.get(`/api/material/storage/list`)
}

export function previewStorageRoute(data: {
  storageId: string
  objectKey: string
}): Promise<StorageRoutePreviewResult> {
  return api.post(`/api/material/storage/route/preview`, data)
}

export function normalizeObjectKey(data: {
  storageId: string
  objectKey: string
}): Promise<StorageObjectKeyNormalizeResult> {
  return api.post(`/api/material/storage/object-key/normalize`, data)
}

export function checkStorageConnectivity(data: {
  storageId: string
  objectKey?: string
}): Promise<StorageConnectivityResult> {
  return api.post(`/api/material/storage/connectivity/check`, data)
}

/** 与后端 FileStorageInstanceCodeEnum 对应的实例编码选项 */
export interface MaterialStorageInstanceOptionVO {
  storageType: string
  code: string
  label: string
}

export function listStorageInstanceOptions(): Promise<MaterialStorageInstanceOptionVO[]> {
  return api.get(`/api/material/storage/config/instance-options`)
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
  return api.get(`/api/material/storage/config/list`)
}

export function createFileStorage(
  data: MaterialFileStorageUpsertDTO
): Promise<MaterialFileStorageVO> {
  return api.post(`/api/material/storage/config`, data)
}

export function updateFileStorage(
  data: MaterialFileStorageUpsertDTO
): Promise<MaterialFileStorageVO> {
  return api.put(`/api/material/storage/config`, data)
}

export function deleteFileStorage(storageId: string): Promise<void> {
  return api.delete(`/api/material/storage/config/${storageId}`)
}
