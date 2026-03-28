<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  cancelChunkSession,
  completeChunkSession,
  createChunkSession,
  getChunkSession,
  reportChunkPart,
  uploadChunkWithHttpHeaders,
  type MaterialChunkUploadSessionVO
} from '@/api/mam_chunk_upload_api'
import {
  checkStorageConnectivity,
  listStorageIds,
  normalizeObjectKey,
  previewStorageRoute,
  type StorageConnectivityResult,
  type StorageObjectKeyNormalizeResult,
  type StorageRoutePreviewResult
} from '@/api/mam_storage_api'

const storageIds = ref<string[]>([])
const storageId = ref('')
const objectKey = ref('demo/path/file.bin')
const loading = ref(false)
const result = ref<StorageRoutePreviewResult | null>(null)
const normalizeResult = ref<StorageObjectKeyNormalizeResult | null>(null)
const connectivityResult = ref<StorageConnectivityResult | null>(null)

/** 关闭则使用「已有资源 ID」旧流程；开启则 MAM 建档（栏目/标题/类型 + 分片 CRC 规划） */
const chunkUseMam = ref(false)
const chunkResourceId = ref('')
const chunkCatalogId = ref('')
const chunkTitle = ref('demo-upload.bin')
const chunkResourceType = ref(6)
const chunkParentId = ref('')
const chunkTotalSize = ref(10)
const chunkSize = ref(4)
const chunkSessionId = ref('')
const chunkPartIndex = ref(0)
const chunkPartByteSize = ref(4)
const chunkSession = ref<MaterialChunkUploadSessionVO | null>(null)
const chunkBusy = ref(false)

async function loadStorageIds() {
  try {
    storageIds.value = await listStorageIds()
    if (!storageId.value && storageIds.value.length > 0) {
      storageId.value = storageIds.value[0]
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '加载存储列表失败')
  }
}

async function preview() {
  if (!storageId.value || !objectKey.value) {
    message.warning('请先选择存储ID并输入对象键')
    return
  }
  loading.value = true
  try {
    result.value = await previewStorageRoute({
      storageId: storageId.value,
      objectKey: objectKey.value
    })
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '路由预览失败')
  } finally {
    loading.value = false
  }
}

async function runNormalize() {
  if (!storageId.value || !objectKey.value) {
    return
  }
  try {
    normalizeResult.value = await normalizeObjectKey({
      storageId: storageId.value,
      objectKey: objectKey.value
    })
    if (normalizeResult.value.normalizedKey !== objectKey.value) {
      objectKey.value = normalizeResult.value.normalizedKey
    }
  } catch {
    // ignore validation preview errors, keep manual preview as final action
  }
}

async function runConnectivityCheck() {
  if (!storageId.value) {
    message.warning('请先选择存储ID')
    return
  }
  try {
    connectivityResult.value = await checkStorageConnectivity({
      storageId: storageId.value,
      objectKey: objectKey.value
    })
    if (connectivityResult.value.reachable) {
      message.success('连通性检查通过')
    } else {
      message.warning('连通性检查未通过')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '连通性检查失败')
  }
}

/** 与演示首片内容一致：各分片填充字节 0x61，用于生成与合并校验一致的 CRC 列表 */
function crc32Unsigned(buf: Uint8Array): number {
  let crc = 0xffffffff
  for (let i = 0; i < buf.length; i++) {
    crc ^= buf[i]
    for (let j = 0; j < 8; j++) {
      crc = (crc >>> 1) ^ (crc & 1 ? 0xedb88320 : 0)
    }
  }
  return (crc ^ 0xffffffff) >>> 0
}

function plannedChunkCrcList(total: number, csize: number): number[] {
  if (total === 0) return []
  const count = Math.ceil(total / csize)
  const list: number[] = []
  for (let i = 0; i < count; i++) {
    const start = i * csize
    const len = i === count - 1 ? total - start : csize
    const buf = new Uint8Array(len).fill(97)
    list.push(crc32Unsigned(buf))
  }
  return list
}

async function submitCreateChunkSession() {
  if (!storageId.value || !objectKey.value) {
    message.warning('请选择存储并填写对象键')
    return
  }
  if (!chunkUseMam.value && !chunkResourceId.value?.trim()) {
    message.warning('请填写资源ID，或开启 MAM 建档')
    return
  }
  if (chunkUseMam.value && (!chunkCatalogId.value?.trim() || !chunkTitle.value?.trim())) {
    message.warning('MAM 建档需填写栏目 ID 与标题')
    return
  }
  chunkBusy.value = true
  try {
    const total = chunkTotalSize.value
    const csize = chunkSize.value
    const base = {
      storageId: storageId.value,
      objectKey: objectKey.value,
      totalSize: total,
      chunkSize: csize
    }
    const vo = chunkUseMam.value
      ? await createChunkSession({
          ...base,
          catalogId: chunkCatalogId.value.trim(),
          title: chunkTitle.value.trim(),
          type: chunkResourceType.value,
          parentId: chunkParentId.value?.trim() || undefined,
          chunkCrc32List: plannedChunkCrcList(total, csize)
        })
      : await createChunkSession({
          ...base,
          resourceId: chunkResourceId.value.trim()
        })
    chunkSession.value = vo
    chunkSessionId.value = vo.id
    message.success(`会话已创建，分片数 ${vo.chunkCount}`)
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '创建会话失败')
  } finally {
    chunkBusy.value = false
  }
}

async function submitGetChunkSession() {
  if (!chunkSessionId.value) {
    message.warning('请输入会话ID')
    return
  }
  chunkBusy.value = true
  try {
    chunkSession.value = await getChunkSession(chunkSessionId.value.trim())
    message.success('已刷新会话与分片列表（续传）')
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '查询失败')
  } finally {
    chunkBusy.value = false
  }
}

async function submitReportChunkPart() {
  if (!chunkSessionId.value) {
    message.warning('请输入会话ID')
    return
  }
  chunkBusy.value = true
  try {
    chunkSession.value = await reportChunkPart(chunkSessionId.value.trim(), {
      chunkIndex: chunkPartIndex.value,
      byteSize: chunkPartByteSize.value
    })
    message.success('分片已登记')
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '登记失败')
  } finally {
    chunkBusy.value = false
  }
}

async function submitCompleteChunkSession() {
  if (!chunkSessionId.value) return
  chunkBusy.value = true
  try {
    chunkSession.value = await completeChunkSession(chunkSessionId.value.trim())
    message.success('会话已完成')
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '完成失败')
  } finally {
    chunkBusy.value = false
  }
}

async function submitCancelChunkSession() {
  if (!chunkSessionId.value) return
  chunkBusy.value = true
  try {
    chunkSession.value = await cancelChunkSession(chunkSessionId.value.trim())
    message.success('会话已取消')
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '取消失败')
  } finally {
    chunkBusy.value = false
  }
}

/** 5.6：按当前 total/chunkSize 发送第一片演示（须已有会话且与规划一致） */
async function submitHeaderChunkUploadDemo() {
  if (!chunkSessionId.value) {
    message.warning('请先创建会话')
    return
  }
  const total = chunkTotalSize.value
  const csize = chunkSize.value
  if (total <= 0 || csize <= 0) {
    message.warning('演示需 total>0')
    return
  }
  const firstLen = Math.min(csize, total)
  const last = firstLen - 1
  const body = new Uint8Array(firstLen).fill(97)
  const range = `bytes 0-${last}/${total}`
  chunkBusy.value = true
  try {
    chunkSession.value = await uploadChunkWithHttpHeaders(chunkSessionId.value.trim(), body, range)
    message.success('首片已通过 Content-Range 校验并登记')
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '首片上传失败')
  } finally {
    chunkBusy.value = false
  }
}

onMounted(loadStorageIds)
</script>

<template>
  <div style="padding: 20px;">
    <a-space style="margin-bottom: 12px;" wrap>
      <a-select
        v-model:value="storageId"
        :options="storageIds.map((id) => ({ label: id, value: id }))"
        placeholder="选择存储ID"
        style="width: 260px;"
      />
      <a-input v-model:value="objectKey" style="width: 320px;" placeholder="对象键" />
      <a-button @click="runNormalize">对象键规范化</a-button>
      <a-button @click="runConnectivityCheck">连通性检查</a-button>
      <a-button :loading="loading" type="primary" @click="preview">路由预览</a-button>
    </a-space>

    <a-alert
      v-if="normalizeResult"
      :type="normalizeResult.valid ? 'success' : 'error'"
      :message="normalizeResult.valid ? '对象键合法' : '对象键不合法'"
      :description="`driver=${normalizeResult.driverName}, normalized=${normalizeResult.normalizedKey}`"
      style="margin-bottom: 12px;"
      show-icon
    />

    <a-alert
      v-if="connectivityResult"
      :type="connectivityResult.reachable ? 'success' : 'error'"
      :message="connectivityResult.reachable ? '存储可达' : '存储不可达'"
      :description="connectivityResult.detail"
      style="margin-bottom: 12px;"
      show-icon
    />

    <a-descriptions v-if="result" bordered :column="1" size="small">
      <a-descriptions-item label="storageId">{{ result.storageId }}</a-descriptions-item>
      <a-descriptions-item label="engineType">{{ result.engineType }}</a-descriptions-item>
      <a-descriptions-item label="driverName">{{ result.driverName }}</a-descriptions-item>
      <a-descriptions-item label="objectKey">{{ result.objectKey }}</a-descriptions-item>
      <a-descriptions-item label="routeTarget">{{ result.routeTarget }}</a-descriptions-item>
    </a-descriptions>

    <a-divider>分片上传会话（5.5）</a-divider>
    <a-space wrap style="margin-bottom: 12px;">
      <a-switch v-model:checked="chunkUseMam" checked-children="MAM 建档" un-checked-children="已有资源" />
      <a-input
        v-if="!chunkUseMam"
        v-model:value="chunkResourceId"
        style="width: 220px;"
        placeholder="资源ID（须已保存指纹）"
      />
      <template v-else>
        <a-input v-model:value="chunkCatalogId" style="width: 200px;" placeholder="栏目 catalogId" />
        <a-input v-model:value="chunkTitle" style="width: 180px;" placeholder="标题" />
        <a-input-number v-model:value="chunkResourceType" :min="1" :max="6" placeholder="类型" />
        <a-input v-model:value="chunkParentId" style="width: 160px;" placeholder="父文件夹ID 可空" />
      </template>
      <a-input-number v-model:value="chunkTotalSize" :min="0" placeholder="总大小" />
      <a-input-number v-model:value="chunkSize" :min="1" placeholder="分片大小" />
      <a-input v-model:value="chunkSessionId" style="width: 260px;" placeholder="会话ID" />
      <a-button :loading="chunkBusy" type="primary" ghost @click="submitCreateChunkSession">创建会话</a-button>
      <a-button :loading="chunkBusy" @click="submitGetChunkSession">续传查询</a-button>
    </a-space>
    <a-space wrap style="margin-bottom: 12px;">
      <a-input-number v-model:value="chunkPartIndex" :min="0" placeholder="分片序号" />
      <a-input-number v-model:value="chunkPartByteSize" :min="0" placeholder="本分片字节" />
      <a-button :loading="chunkBusy" @click="submitReportChunkPart">登记分片</a-button>
      <a-button :loading="chunkBusy" @click="submitCompleteChunkSession">完成会话</a-button>
      <a-button :loading="chunkBusy" danger @click="submitCancelChunkSession">取消会话</a-button>
      <a-button :loading="chunkBusy" type="dashed" @click="submitHeaderChunkUploadDemo">5.6 首片二进制上传</a-button>
    </a-space>
    <a-alert
      type="info"
      show-icon
      message="5.8 说明"
      description="完成会话：仅 file_engine=磁盘 时合并暂存分片到对象键并绑定 meta_file；MAM 建档在创建会话时已写入栏目/指纹；旧流程须资源上事先保存与分片边界一致的指纹。各分片须通过「二进制分片上传」落暂存。"
      style="margin: 12px 0;"
    />
    <a-descriptions v-if="chunkSession" title="当前会话" bordered size="small" :column="2">
      <a-descriptions-item label="id">{{ chunkSession.id }}</a-descriptions-item>
      <a-descriptions-item v-if="chunkSession.catalogId" label="catalogId">{{ chunkSession.catalogId }}</a-descriptions-item>
      <a-descriptions-item label="resourceId">{{ chunkSession.resourceId }}</a-descriptions-item>
      <a-descriptions-item label="status">{{ chunkSession.status }}</a-descriptions-item>
      <a-descriptions-item label="chunkCount">{{ chunkSession.chunkCount }}</a-descriptions-item>
      <a-descriptions-item label="totalSize">{{ chunkSession.totalSize }}</a-descriptions-item>
      <a-descriptions-item label="objectKey" :span="2">{{ chunkSession.objectKey }}</a-descriptions-item>
      <a-descriptions-item label="已登记分片" :span="2">
        {{ chunkSession.parts.map((p) => `${p.chunkIndex}:${p.byteSize}`).join(', ') || '（无）' }}
      </a-descriptions-item>
    </a-descriptions>
  </div>
</template>
