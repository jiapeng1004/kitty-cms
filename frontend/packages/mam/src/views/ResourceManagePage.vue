<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  bindMetaFile,
  createFolder,
  createResource,
  getMetaFile,
  getResourceDetail,
  listResources,
  planFolderUpload,
  precheckFingerprint,
  rebuildPaths,
  saveFingerprint,
  type FingerprintPrecheckResult,
  type MaterialMetaFileVO,
  type MaterialResourceDetailVO,
  type MaterialResourceUpsertDTO,
  type MaterialResourceVO,
  updateResource
} from '@/api/mam_resource_api'

const loading = ref(false)
const tableData = ref<MaterialResourceVO[]>([])
const folderName = ref('')
const relativePathsText = ref('')
const fingerprintFile = ref<File | null>(null)
const fingerprintChunkSize = ref(1024 * 1024)
const precheckResult = ref<FingerprintPrecheckResult | null>(null)
const bindStorageId = ref('')
const bindObjectKey = ref('')
const bindDisplayName = ref('')
const metaFileRow = ref<MaterialMetaFileVO | null>(null)
const resourceDetail = ref<MaterialResourceDetailVO | null>(null)
const form = reactive<MaterialResourceUpsertDTO>({
  id: '',
  title: '',
  catalogId: '',
  parentId: '0',
  type: 7
})

async function queryList() {
  loading.value = true
  try {
    tableData.value = await listResources({
      catalogId: form.catalogId || undefined,
      parentId: form.parentId || undefined
    })
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  try {
    await createResource({
      title: form.title,
      catalogId: form.catalogId,
      parentId: form.parentId,
      type: form.type
    })
    message.success('创建成功')
    await queryList()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '创建失败')
  }
}

async function submitUpdate() {
  if (!form.id) {
    message.warning('更新时请先输入资源ID')
    return
  }
  try {
    await updateResource({
      id: form.id,
      title: form.title,
      catalogId: form.catalogId,
      parentId: form.parentId,
      type: form.type
    })
    message.success('更新成功')
    await queryList()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '更新失败')
  }
}

async function submitCreateFolder() {
  if (!folderName.value) {
    message.warning('请输入文件夹名称')
    return
  }
  try {
    await createFolder({
      title: folderName.value,
      catalogId: form.catalogId,
      parentId: form.parentId
    })
    folderName.value = ''
    message.success('文件夹创建成功')
    await queryList()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '创建文件夹失败')
  }
}

async function submitRebuildPath() {
  if (!form.catalogId) {
    message.warning('请先输入栏目ID')
    return
  }
  try {
    await rebuildPaths({ catalogId: form.catalogId })
    message.success('路径重建完成')
    await queryList()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '路径重建失败')
  }
}

async function submitPlanFolderUpload() {
  if (!form.catalogId) {
    message.warning('请先输入栏目ID')
    return
  }
  const lines = relativePathsText.value
    .split('\n')
    .map((x) => x.trim())
    .filter((x) => x.length > 0)
  if (lines.length === 0) {
    message.warning('请输入相对路径清单')
    return
  }
  try {
    const created = await planFolderUpload({
      catalogId: form.catalogId,
      parentId: form.parentId,
      relativePaths: lines
    })
    message.success(`上传编排完成，新增 ${created.length} 条资源`)
    await queryList()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '上传编排失败')
  }
}

function onFileSelected(e: Event) {
  const input = e.target as HTMLInputElement
  fingerprintFile.value = input.files && input.files[0] ? input.files[0] : null
}

async function submitFingerprint() {
  if (!form.id) {
    message.warning('请先输入资源ID')
    return
  }
  if (!fingerprintFile.value) {
    message.warning('请选择文件用于指纹计算')
    return
  }
  try {
    const crcList = await calcChunkCrc32(fingerprintFile.value, fingerprintChunkSize.value)
    await saveFingerprint({
      resourceId: form.id,
      fileSize: fingerprintFile.value.size,
      chunkCrc32List: crcList
    })
    message.success('指纹保存成功')
    await queryList()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '指纹保存失败')
  }
}

async function loadResourceDetail() {
  if (!form.id) {
    message.warning('请先输入资源ID')
    return
  }
  try {
    resourceDetail.value = await getResourceDetail(form.id)
    message.success('已加载资源详情（含编目快照）')
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '加载详情失败')
  }
}

async function loadMetaFile() {
  if (!form.id) {
    message.warning('请先输入资源ID')
    return
  }
  try {
    metaFileRow.value = await getMetaFile(form.id)
    if (metaFileRow.value) {
      message.success('已加载物理文件记录')
    } else {
      message.info('该资源尚无 meta_file 记录')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '查询失败')
  }
}

async function submitBindMetaFile() {
  if (!form.id || !bindStorageId.value || !bindObjectKey.value) {
    message.warning('请填写资源ID、存储ID、对象键')
    return
  }
  try {
    metaFileRow.value = await bindMetaFile({
      resourceId: form.id,
      storageId: bindStorageId.value.trim(),
      objectKey: bindObjectKey.value.trim(),
      name: bindDisplayName.value.trim() || undefined
    })
    message.success('已写入 meta_file 关联')
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '绑定失败')
  }
}

async function submitPrecheckFingerprint() {
  if (!fingerprintFile.value) {
    message.warning('请先选择文件用于预检')
    return
  }
  try {
    const crcList = await calcChunkCrc32(fingerprintFile.value, fingerprintChunkSize.value)
    precheckResult.value = await precheckFingerprint({
      fileSize: fingerprintFile.value.size,
      chunkCrc32List: crcList
    })
    if (precheckResult.value.hit) {
      message.success(`命中秒传，可复用 ${precheckResult.value.matchedResources.length} 条已有资源`)
    } else {
      message.info('未命中秒传，需正常上传')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '秒传预检失败')
  }
}

async function calcChunkCrc32(file: File, chunkSize: number): Promise<number[]> {
  const result: number[] = []
  for (let offset = 0; offset < file.size; offset += chunkSize) {
    const chunk = file.slice(offset, Math.min(offset + chunkSize, file.size))
    const buf = await chunk.arrayBuffer()
    result.push(crc32(new Uint8Array(buf)) >>> 0)
  }
  return result
}

const CRC32_TABLE = (() => {
  const table = new Uint32Array(256)
  for (let i = 0; i < 256; i++) {
    let c = i
    for (let j = 0; j < 8; j++) {
      c = (c & 1) !== 0 ? 0xedb88320 ^ (c >>> 1) : c >>> 1
    }
    table[i] = c >>> 0
  }
  return table
})()

function crc32(data: Uint8Array): number {
  let crc = 0xffffffff
  for (let i = 0; i < data.length; i++) {
    crc = CRC32_TABLE[(crc ^ data[i]) & 0xff] ^ (crc >>> 8)
  }
  return (crc ^ 0xffffffff) >>> 0
}
</script>

<template>
  <div style="padding: 20px;">
    <a-space style="margin-bottom: 12px;" wrap>
      <a-input v-model:value="form.id" style="width: 220px;" placeholder="资源ID（更新时填）" />
      <a-input v-model:value="form.title" style="width: 200px;" placeholder="标题" />
      <a-input v-model:value="form.catalogId" style="width: 200px;" placeholder="栏目ID（必填）" />
      <a-input v-model:value="form.parentId" style="width: 180px;" placeholder="父资源ID（默认0）" />
      <a-input-number v-model:value="form.type" :min="1" :max="7" />
      <a-input v-model:value="folderName" style="width: 200px;" placeholder="新建文件夹名称" />
      <a-button :loading="loading" @click="queryList">查询</a-button>
      <a-button type="primary" @click="submitCreate">新增</a-button>
      <a-button @click="submitUpdate">更新</a-button>
      <a-button @click="submitCreateFolder">创建文件夹</a-button>
      <a-button @click="submitRebuildPath">重建路径</a-button>
      <a-button type="dashed" @click="submitPlanFolderUpload">目录上传编排</a-button>
      <input type="file" @change="onFileSelected" />
      <a-input-number v-model:value="fingerprintChunkSize" :min="1024" :step="1024" />
      <a-button @click="submitFingerprint">计算并保存指纹</a-button>
      <a-button type="dashed" @click="submitPrecheckFingerprint">秒传预检</a-button>
      <a-divider type="vertical" />
      <a-input v-model:value="bindStorageId" style="width: 180px;" placeholder="存储ID(file_storage)" />
      <a-input v-model:value="bindObjectKey" style="width: 220px;" placeholder="对象键 object_key" />
      <a-input v-model:value="bindDisplayName" style="width: 160px;" placeholder="展示文件名(可选)" />
      <a-button @click="loadResourceDetail">资源详情+编目</a-button>
      <a-button @click="loadMetaFile">查询 meta_file</a-button>
      <a-button type="primary" ghost @click="submitBindMetaFile">绑定存储写入 meta_file</a-button>
    </a-space>

    <a-textarea
      v-model:value="relativePathsText"
      :rows="6"
      placeholder="每行一个相对路径，例如：&#10;project/src/main.ts&#10;project/assets/logo.png&#10;project/docs/"
      style="margin-bottom: 12px;"
    />

    <a-table :data-source="tableData" row-key="id" :pagination="false" :loading="loading" size="small">
      <a-table-column title="ID" data-index="id" key="id" />
      <a-table-column title="标题" data-index="title" key="title" />
      <a-table-column title="栏目ID" data-index="catalogId" key="catalogId" />
      <a-table-column title="父资源ID" data-index="parentId" key="parentId" />
      <a-table-column title="路径" data-index="path" key="path" />
      <a-table-column title="文件大小" data-index="fileSize" key="fileSize" />
      <a-table-column title="指纹" data-index="fingerprint" key="fingerprint" />
      <a-table-column title="类型" data-index="type" key="type" />
    </a-table>

    <a-alert
      v-if="precheckResult"
      :type="precheckResult.hit ? 'success' : 'warning'"
      :message="precheckResult.hit ? '秒传命中' : '未命中秒传'"
      style="margin-top: 12px;"
      show-icon
    />

    <a-collapse v-if="resourceDetail" style="margin-top: 16px;">
      <a-collapse-panel key="res" header="资源详情 API：/resource/detail">
        <a-descriptions bordered size="small" :column="1">
          <a-descriptions-item label="ID">{{ resourceDetail.resource.id }}</a-descriptions-item>
          <a-descriptions-item label="标题">{{ resourceDetail.resource.title }}</a-descriptions-item>
          <a-descriptions-item label="栏目">{{ resourceDetail.resource.catalogId }}</a-descriptions-item>
          <a-descriptions-item label="类型">{{ resourceDetail.resource.type }}</a-descriptions-item>
          <a-descriptions-item label="路径">{{ resourceDetail.resource.path }}</a-descriptions-item>
          <a-descriptions-item label="指纹">{{ resourceDetail.resource.fingerprint }}</a-descriptions-item>
        </a-descriptions>
        <a-divider orientation="left">编目快照（每模板最新）</a-divider>
        <div v-if="!resourceDetail.metadata?.length" style="color: #999;">暂无编目数据</div>
        <a-collapse v-else ghost>
          <a-collapse-panel
            v-for="m in resourceDetail.metadata"
            :key="m.templateId"
            :header="`${m.templateName} · v${m.version ?? '—'}`"
          >
            <a-table :data-source="m.entries" row-key="fieldId" size="small" :pagination="false">
              <a-table-column title="字段" data-index="fieldCode" key="fieldCode" />
              <a-table-column title="名称" data-index="fieldName" key="fieldName" />
              <a-table-column title="值" data-index="fieldValue" key="fieldValue" />
            </a-table>
          </a-collapse-panel>
        </a-collapse>
        <a-divider orientation="left">资源任务</a-divider>
        <div v-if="!resourceDetail.tasks?.length" style="color: #999;">暂无任务记录</div>
        <a-table
          v-else
          :data-source="resourceDetail.tasks"
          row-key="id"
          size="small"
          :pagination="false"
        >
          <a-table-column title="类型" data-index="taskType" key="taskType" />
          <a-table-column title="状态" data-index="status" key="status" />
          <a-table-column title="进度" data-index="progress" key="progress" />
          <a-table-column title="外部任务" data-index="thirdTaskId" key="thirdTaskId" />
          <a-table-column title="策略" data-index="strategyName" key="strategyName" />
        </a-table>
        <a-divider orientation="left">转码任务（聚合）</a-divider>
        <div v-if="!resourceDetail.transcodeTasks?.length" style="color: #999;">暂无转码任务</div>
        <a-table
          v-else
          :data-source="resourceDetail.transcodeTasks"
          row-key="id"
          size="small"
          :pagination="false"
        >
          <a-table-column title="类型" data-index="taskType" key="taskType" />
          <a-table-column title="状态" data-index="status" key="status" />
          <a-table-column title="进度" data-index="progress" key="progress" />
        </a-table>
        <a-divider orientation="left">标签/向量类任务（聚合）</a-divider>
        <div v-if="!resourceDetail.taggingTasks?.length" style="color: #999;">暂无匹配任务</div>
        <a-table
          v-else
          :data-source="resourceDetail.taggingTasks"
          row-key="id"
          size="small"
          :pagination="false"
        >
          <a-table-column title="类型" data-index="taskType" key="taskType" />
          <a-table-column title="状态" data-index="status" key="status" />
        </a-table>
        <a-divider orientation="left">审核任务（review_task）</a-divider>
        <div v-if="!resourceDetail.reviewTasks?.length" style="color: #999;">暂无审核记录</div>
        <a-table
          v-else
          :data-source="resourceDetail.reviewTasks"
          row-key="id"
          size="small"
          :pagination="false"
        >
          <a-table-column title="任务 ID" data-index="id" key="id" :ellipsis="true" />
          <a-table-column title="状态" data-index="status" key="status" />
          <a-table-column title="提交人" data-index="submitUserId" key="submitUserId" />
          <a-table-column title="审核人" data-index="reviewUserId" key="reviewUserId" />
          <a-table-column title="意见" data-index="reviewComment" key="reviewComment" />
        </a-table>
      </a-collapse-panel>
    </a-collapse>

    <a-descriptions
      v-if="metaFileRow"
      title="meta_file（物理文件）"
      bordered
      size="small"
      style="margin-top: 16px;"
    >
      <a-descriptions-item label="记录ID">{{ metaFileRow.id }}</a-descriptions-item>
      <a-descriptions-item label="资源ID">{{ metaFileRow.resourceId }}</a-descriptions-item>
      <a-descriptions-item label="存储ID">{{ metaFileRow.storageId }}</a-descriptions-item>
      <a-descriptions-item label="对象键" :span="2">{{ metaFileRow.objectKey }}</a-descriptions-item>
      <a-descriptions-item label="文件名">{{ metaFileRow.name }}</a-descriptions-item>
      <a-descriptions-item label="大小">{{ metaFileRow.size }}</a-descriptions-item>
      <a-descriptions-item label="相对路径" :span="2">{{ metaFileRow.relaPath }}</a-descriptions-item>
    </a-descriptions>
  </div>
</template>
