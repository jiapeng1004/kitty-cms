import {
  completeChunkSession,
  createChunkSession,
  uploadChunkWithHttpHeaders
} from '@/api/mam_chunk_upload_api'
import { normalizeObjectKey } from '@/api/mam_storage_api'

const DEFAULT_CHUNK_SIZE = 4 * 1024 * 1024

/** 与存储路由演示一致的无符号 CRC32 */
export function crc32Unsigned(buf: Uint8Array): number {
  let crc = 0xffffffff
  for (let i = 0; i < buf.length; i++) {
    crc ^= buf[i]
    for (let j = 0; j < 8; j++) {
      crc = (crc >>> 1) ^ (crc & 1 ? 0xedb88320 : 0)
    }
  }
  return (crc ^ 0xffffffff) >>> 0
}

/** 按分片读取本地文件并计算各片 CRC（与后端规划一致） */
export async function computeChunkCrc32List(file: File, chunkSize: number): Promise<number[]> {
  if (file.size === 0) {
    return []
  }
  const count = Math.ceil(file.size / chunkSize)
  const list: number[] = []
  for (let i = 0; i < count; i++) {
    const start = i * chunkSize
    const end = Math.min(start + chunkSize, file.size)
    const slice = file.slice(start, end)
    const buf = new Uint8Array(await slice.arrayBuffer())
    list.push(crc32Unsigned(buf))
  }
  return list
}

/**
 * 按扩展名推断 {@link ResourceTypeEnum}：1 视频 2 音频 3 图片 4 文本 5 Office 6 其他
 */
export function inferResourceType(fileName: string): number {
  const lower = fileName.toLowerCase()
  const ext = lower.includes('.') ? lower.slice(lower.lastIndexOf('.')) : ''
  if (['.mp4', '.mov', '.mkv', '.avi', '.webm', '.m4v'].includes(ext)) return 1
  if (['.mp3', '.wav', '.aac', '.flac', '.m4a', '.ogg'].includes(ext)) return 2
  if (['.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp', '.svg', '.ico'].includes(ext)) return 3
  if (['.txt', '.md', '.csv', '.json', '.xml', '.log'].includes(ext)) return 4
  if (['.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.wps'].includes(ext)) return 5
  return 6
}

function sanitizeFileName(name: string): string {
  return name.replace(/[/\\?%*:|"<>]/g, '_').trim() || 'file'
}

export interface MaterialUploadProgress {
  fileName: string
  phase: 'prepare' | 'upload' | 'complete'
  sentChunks: number
  totalChunks: number
}

export interface UploadMaterialFileOptions {
  catalogId: string
  /** 父资源 ID，根目录为 `0` 或不传 */
  parentId?: string
  /** 指定存储 ID；不传则后端使用主存储 */
  storageId?: string
  chunkSize?: number
  onProgress?: (p: MaterialUploadProgress) => void
}

/**
 * 单文件分片上传：创建会话 → 按 Content-Range 逐片上传 → complete
 */
export async function uploadMaterialFile(file: File, options: UploadMaterialFileOptions): Promise<void> {
  const { catalogId, parentId, chunkSize = DEFAULT_CHUNK_SIZE, storageId: optStorageId } = options
  const rawKey = `material/${catalogId}/${crypto.randomUUID()}/${sanitizeFileName(file.name)}`
  let objectKey = rawKey
  if (optStorageId) {
    const norm = await normalizeObjectKey({ storageId: optStorageId, objectKey: rawKey })
    if (norm.valid) {
      objectKey = norm.normalizedKey
    }
  }

  const type = inferResourceType(file.name)
  const chunkCrc32List = await computeChunkCrc32List(file, chunkSize)
  const totalChunks = chunkCrc32List.length

  options.onProgress?.({
    fileName: file.name,
    phase: 'prepare',
    sentChunks: 0,
    totalChunks
  })

  const sessionPayload: Parameters<typeof createChunkSession>[0] = {
    catalogId,
    title: file.name,
    type,
    parentId: parentId && parentId !== '0' ? parentId : undefined,
    objectKey,
    totalSize: file.size,
    chunkSize,
    chunkCrc32List
  }
  if (optStorageId) {
    sessionPayload.storageId = optStorageId
  }
  const session = await createChunkSession(sessionPayload)

  if (totalChunks === 0) {
    await completeChunkSession(session.id)
    options.onProgress?.({ fileName: file.name, phase: 'complete', sentChunks: 0, totalChunks: 0 })
    return
  }

  for (let i = 0; i < totalChunks; i++) {
    const start = i * chunkSize
    const end = Math.min(start + chunkSize, file.size)
    const slice = file.slice(start, end)
    const body = new Uint8Array(await slice.arrayBuffer())
    const last = start + body.byteLength - 1
    const range = `bytes ${start}-${last}/${file.size}`
    options.onProgress?.({
      fileName: file.name,
      phase: 'upload',
      sentChunks: i,
      totalChunks
    })
    await uploadChunkWithHttpHeaders(session.id, body, range, body.byteLength)
  }

  await completeChunkSession(session.id)
  options.onProgress?.({
    fileName: file.name,
    phase: 'complete',
    sentChunks: totalChunks,
    totalChunks
  })
}

/** 顺序上传多个文件（避免并发占满连接） */
export async function uploadMaterialFiles(
  files: File[],
  options: UploadMaterialFileOptions
): Promise<void> {
  for (const f of files) {
    await uploadMaterialFile(f, options)
  }
}
