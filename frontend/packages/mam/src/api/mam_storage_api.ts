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
