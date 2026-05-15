import type { MaterialStorageInstanceOptionVO } from '@/mam/api/mam_storage_api'

/**
 * 与后端 FileStorageInstanceCodeEnum 同步。
 * listStorageInstanceOptions 失败时作为下拉数据源，避免空白或误报。
 */
export const FALLBACK_STORAGE_INSTANCE_OPTIONS: MaterialStorageInstanceOptionVO[] = [
  { storageType: 's3', code: 'default-s3', label: '默认对象存储' },
  { storageType: 's3', code: 'minio-dev', label: 'MinIO 开发' },
  { storageType: 'disk', code: 'default-disk', label: '默认本地磁盘' }
]
