<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import { DownOutlined, LeftOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import MaterialCatalogTreePanel from '@/mam/components/MaterialCatalogTreePanel.vue'
import {
  createFolder,
  materialApiAbsoluteUrl,
  pageResources,
  updateResource,
  type MaterialResourceVO
} from '@/mam/api/mam_resource_api'
import type { MaterialCatalogNode } from '@/mam/api/mam_catalog_api'
import { useMaterialFileUpload } from '@/mam/composables/useMaterialFileUpload'
import {
  MAM_BATCH_TIER_PRESETS,
  preflightMamBatchDownload,
  resolveDestinationType,
  showMamBatchDownloadConfirm
} from '@/mam/composables/useMamBatchDownload'
import { useMamTableCellCopy } from '@/mam/composables/useMamTableCellCopy'

const route = useRoute()
const router = useRouter()

const { copyCell } = useMamTableCellCopy()

const isEmbed = computed(() => route.path.startsWith('/embed/'))
const showBackToMaterial = computed(
  () => isEmbed.value || (route.meta as { immersive?: boolean }).immersive === true
)

function goBackToMaterial() {
  void router.push(isEmbed.value ? '/embed/material' : '/material')
}

function thumbSrc(row: MaterialResourceVO): string {
  if (row.coverUrl) return materialApiAbsoluteUrl(row.coverUrl)
  if (row.keyframeUrl) return materialApiAbsoluteUrl(row.keyframeUrl)
  if (row.previewUrl && row.type === 3) return materialApiAbsoluteUrl(row.previewUrl)
  return ''
}

const selectedCatalogId = ref<string>()
const currentCatalog = ref<MaterialCatalogNode | undefined>()
const catalogPanelRef = ref<InstanceType<typeof MaterialCatalogTreePanel>>()

const loading = ref(false)
const resources = ref<MaterialResourceVO[]>([])
const searchKeyword = ref('')

const folderModalVisible = ref(false)
const folderName = ref('')
const folderCreating = ref(false)

const renameModalVisible = ref(false)
const renameTitle = ref('')
const renameRow = ref<MaterialResourceVO>()
const renameLoading = ref(false)

const selectedRowKeys = ref<string[]>([])
const batchModalVisible = ref(false)
const batchSubmitting = ref(false)
const batchTierPreset = ref('SOURCE')
const batchCustomTier = ref('')

function onCatalogSelect(node: MaterialCatalogNode | undefined) {
  currentCatalog.value = node
}

async function loadList() {
  const cid = selectedCatalogId.value
  if (!cid) {
    resources.value = []
    return
  }
  loading.value = true
  resources.value = []
  try {
    const kw = searchKeyword.value.trim()
    const resp = await pageResources({
      catalogId: cid,
      parentId: '0',
      page: 1,
      size: 500,
      ...(kw ? { keyword: kw } : {})
    })
    resources.value = resp.records ?? []
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

watch(selectedCatalogId, () => {
  selectedRowKeys.value = []
  loadList()
})

const { uploading, progressText, uploadFiles } = useMaterialFileUpload({
  getCatalogId: () => selectedCatalogId.value,
  getParentId: () => '0',
  onFinished: () => loadList()
})

const uploadRequest: UploadProps['customRequest'] = (opt) => {
  const raw = opt.file as File | Blob
  const file = raw instanceof File ? raw : new File([raw], (opt.file as { name?: string }).name || 'upload.bin', { type: raw.type })
  uploadFiles([file]).then(
    () => opt.onSuccess?.({}, opt.file),
    () => opt.onError?.(new Error('upload failed'))
  )
}

async function submitCreateFolder() {
  const name = folderName.value.trim()
  const cid = selectedCatalogId.value
  if (!cid) {
    message.warning('请先选择栏目')
    return
  }
  if (!name) {
    message.warning('请输入文件夹名称')
    return
  }
  folderCreating.value = true
  try {
    await createFolder({ title: name, catalogId: cid, parentId: '0' })
    message.success('已创建')
    folderModalVisible.value = false
    folderName.value = ''
    await loadList()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '创建失败')
  } finally {
    folderCreating.value = false
  }
}

function openRename(row: MaterialResourceVO) {
  renameRow.value = row
  renameTitle.value = row.title
  renameModalVisible.value = true
}

async function submitRename() {
  const row = renameRow.value
  const title = renameTitle.value.trim()
  if (!row || !title) {
    message.warning('请输入标题')
    return
  }
  renameLoading.value = true
  try {
    await updateResource({
      id: row.id,
      title,
      catalogId: row.catalogId,
      type: row.type
    })
    message.success('已更新')
    renameModalVisible.value = false
    await loadList()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '更新失败')
  } finally {
    renameLoading.value = false
  }
}

function resourceDetailPath(id: string) {
  return route.path.startsWith('/embed') ? `/embed/material/resource/${id}` : `/material/resource/${id}`
}

function openDetail(row: MaterialResourceVO) {
  router.push(resourceDetailPath(row.id))
}

function formatSize(size?: number) {
  if (size == null) return '—'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(1)} MB`
  return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`
}

function typeLabel(t: number) {
  switch (t) {
    case 1:
      return '视频'
    case 2:
      return '音频'
    case 3:
      return '图片'
    case 4:
      return '文本'
    case 5:
      return 'Office'
    case 6:
      return '其他'
    case 7:
      return '文件夹'
    default:
      return String(t)
  }
}

const filteredRows = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) {
    return resources.value
  }
  return resources.value.filter(
    (r) =>
      (r.title && r.title.toLowerCase().includes(kw)) ||
      (r.previewUrl && r.previewUrl.toLowerCase().includes(kw)) ||
      (r.coverUrl && r.coverUrl.toLowerCase().includes(kw)) ||
      (r.keyframeUrl && r.keyframeUrl.toLowerCase().includes(kw)) ||
      (r.srcUrl && r.srcUrl.toLowerCase().includes(kw))
  )
})

const selectedResources = computed(() =>
  filteredRows.value.filter((r) => selectedRowKeys.value.includes(r.id))
)

function onBatchSelectChange(keys: string[]) {
  selectedRowKeys.value = keys
}

function openBatchDownloadModal() {
  if (!selectedRowKeys.value.length) {
    message.warning('请先勾选资源')
    return
  }
  batchModalVisible.value = true
}

async function submitBatchDownload() {
  const dt = resolveDestinationType(batchTierPreset.value, batchCustomTier.value)
  if (!dt) {
    message.warning('请选择或输入下载分级（destinationType）')
    return
  }
  batchSubmitting.value = true
  try {
    const { toDownload, hardFail } = await preflightMamBatchDownload(selectedResources.value, dt)
    if (!toDownload.length) {
      if (hardFail.length) {
        message.warning('所选资源均无法以该分级下载，请改选分级或检查资源。')
      } else {
        message.warning('没有可下载的资源（已排除文件夹）')
      }
      return
    }
    batchModalVisible.value = false
    const ok = await showMamBatchDownloadConfirm(toDownload, hardFail)
    if (ok) {
      selectedRowKeys.value = []
    }
  } finally {
    batchSubmitting.value = false
  }
}

async function refresh() {
  await catalogPanelRef.value?.load()
  await loadList()
}
</script>

<template>
  <div
    class="mam-page resource-manage-page"
    :class="{
      'mam-page--padded': !showBackToMaterial,
      'resource-manage-page--immersive': showBackToMaterial
    }"
  >
    <div v-if="showBackToMaterial" class="resource-embed-bar">
      <a-button type="link" class="resource-back" @click="goBackToMaterial">
        <template #icon>
          <left-outlined />
        </template>
        返回素材库
      </a-button>
    </div>

    <div class="mam-page-head resource-page-head">
      <h1 class="mam-page-title">资源管理</h1>
      <span class="mam-page-sub"
        >当前栏目：<span class="mam-em">{{ currentCatalog?.name || '未选择' }}</span
        > · 共 <span class="mam-em">{{ filteredRows.length }}</span> 条（当前筛选）</span
      >
    </div>

    <div class="resource-manage-body">
      <aside class="resource-manage-catalog">
        <MaterialCatalogTreePanel
          ref="catalogPanelRef"
          v-model:selected-catalog-id="selectedCatalogId"
          @catalog-select="onCatalogSelect"
        />
      </aside>

      <main class="resource-manage-main mam-panel">
        <div class="resource-toolbar">
          <a-input
            v-model:value="searchKeyword"
            allow-clear
            class="resource-toolbar-search"
            placeholder="筛选标题、预览或原链（本地）"
            @press-enter="loadList"
          />
          <a-button @click="loadList">检索后端</a-button>
          <a-button :loading="loading" @click="refresh">
            <template #icon>
              <reload-outlined />
            </template>
            刷新
          </a-button>
          <a-button v-if="selectedRowKeys.length > 0" @click="openBatchDownloadModal">
            批量下载 ({{ selectedRowKeys.length }})
          </a-button>
          <span v-if="progressText" class="upload-progress">{{ progressText }}</span>
          <a-dropdown :trigger="['click']">
            <a-button type="primary" :loading="uploading" :disabled="!selectedCatalogId">
              上传
              <down-outlined />
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item key="file" @click.stop>
                  <a-upload
                    :custom-request="uploadRequest"
                    :multiple="true"
                    :show-upload-list="false"
                    :disabled="uploading || !selectedCatalogId"
                    :before-upload="(file) => {
                      const ok = file.size / 1024 / 1024 / 1024 < 5
                      if (!ok) message.error('单文件不能超过 5GB')
                      return ok
                    }"
                  >
                    <span>上传文件</span>
                  </a-upload>
                </a-menu-item>
                <a-menu-item key="folder" :disabled="!selectedCatalogId" @click="folderModalVisible = true">
                  新建文件夹
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>

        <a-alert
          type="info"
          show-icon
          class="resource-hint"
          message="使用说明"
          description="在左侧选择栏目后加载列表；除「操作」列外单击单元格可复制字段全文。预览列为缩略图，点击标题或「详情」进入资源详情页。"
        />

        <!-- 列表容器占满剩余高度，内部滚动，让分页留在底部 -->
        <div class="resource-list-container mam-table-wrap">
          <a-table
            :data-source="filteredRows"
            :loading="loading"
            row-key="id"
            size="small"
            :pagination="{ pageSize: 50, showSizeChanger: true }"
            :scroll="{ x: 1200 }"
            :row-selection="{
              selectedRowKeys,
              onChange: onBatchSelectChange,
              getCheckboxProps: (record) => ({ disabled: record.type === 7 })
            }"
          >
          <a-table-column title="预览" key="thumb" :width="76" fixed="left">
            <template #default="{ record }">
              <button
                type="button"
                class="resource-thumb"
                :title="record.title"
                @click="openDetail(record)"
              >
                <img v-if="thumbSrc(record)" :src="thumbSrc(record)" alt="" />
                <span v-else class="resource-thumb-ph">{{ typeLabel(record.type).slice(0, 1) }}</span>
              </button>
            </template>
          </a-table-column>
          <a-table-column title="标题" data-index="title" key="title" :ellipsis="true">
            <template #default="{ record }">
              <span class="mam-table-cell-copy" @click="copyCell(record.title)">{{ record.title }}</span>
            </template>
          </a-table-column>
          <a-table-column title="类型" key="type" :width="100">
            <template #default="{ record }">
              <span class="mam-table-cell-copy" @click="copyCell(typeLabel(record.type))">{{
                typeLabel(record.type)
              }}</span>
            </template>
          </a-table-column>
          <a-table-column title="大小" key="size" :width="108">
            <template #default="{ record }">
              <span class="mam-table-cell-copy" @click="copyCell(formatSize(record.fileSize))">{{
                formatSize(record.fileSize)
              }}</span>
            </template>
          </a-table-column>
          <a-table-column title="预览链" key="previewUrl" :width="120">
            <template #default="{ record }">
              <span
                v-if="record.previewUrl"
                class="mam-table-cell-copy"
                @click="copyCell(materialApiAbsoluteUrl(record.previewUrl))"
              >
                <a
                  :href="materialApiAbsoluteUrl(record.previewUrl)"
                  target="_blank"
                  rel="noopener noreferrer"
                  @click.stop
                >打开</a>
              </span>
              <span v-else class="mam-table-cell-copy" @click="copyCell(undefined)">—</span>
            </template>
          </a-table-column>
          <a-table-column title="封面" key="coverUrl" :width="88">
            <template #default="{ record }">
              <span
                v-if="record.coverUrl"
                class="mam-table-cell-copy"
                @click="copyCell(record.coverUrl)"
              >
                <a :href="materialApiAbsoluteUrl(record.coverUrl)" target="_blank" rel="noopener noreferrer" @click.stop
                  >打开</a
                >
              </span>
              <span v-else class="mam-table-cell-copy" @click="copyCell(undefined)">—</span>
            </template>
          </a-table-column>
          <a-table-column title="关键帧" key="keyframeUrl" :width="88">
            <template #default="{ record }">
              <span
                v-if="record.keyframeUrl"
                class="mam-table-cell-copy"
                @click="copyCell(materialApiAbsoluteUrl(record.keyframeUrl))"
              >
                <a
                  :href="materialApiAbsoluteUrl(record.keyframeUrl)"
                  target="_blank"
                  rel="noopener noreferrer"
                  @click.stop
                >打开</a>
              </span>
              <span v-else class="mam-table-cell-copy" @click="copyCell(undefined)">—</span>
            </template>
          </a-table-column>
          <a-table-column title="原文件" key="srcUrl" :ellipsis="true">
            <template #default="{ record }">
              <span v-if="record.srcUrl" class="mam-table-cell-copy" @click="copyCell(record.srcUrl)">
                <a :href="record.srcUrl" target="_blank" rel="noopener noreferrer" @click.stop>{{ record.srcUrl }}</a>
              </span>
              <span v-else class="mam-table-cell-copy" @click="copyCell(undefined)">—</span>
            </template>
          </a-table-column>
          <a-table-column title="操作" key="act" :width="200" fixed="right">
            <template #default="{ record }">
              <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
              <a-button type="link" size="small" @click="openRename(record)">重命名</a-button>
            </template>
          </a-table-column>
        </a-table>
      </div> <!-- 结束列表滚动容器 -->
    </main>
    </div>

    <a-modal
      v-model:open="folderModalVisible"
      title="新建文件夹"
      :confirm-loading="folderCreating"
      @ok="submitCreateFolder"
      @cancel="folderModalVisible = false"
    >
      <a-input v-model:value="folderName" placeholder="文件夹名称" maxlength="200" @press-enter="submitCreateFolder" />
    </a-modal>

    <a-modal
      v-model:open="renameModalVisible"
      title="重命名"
      :confirm-loading="renameLoading"
      @ok="submitRename"
      @cancel="renameModalVisible = false"
    >
      <a-input v-model:value="renameTitle" placeholder="标题" maxlength="200" />
    </a-modal>

    <a-modal
      v-model:open="batchModalVisible"
      title="批量下载"
      ok-text="开始"
      :confirm-loading="batchSubmitting"
      @ok="submitBatchDownload"
      @cancel="batchModalVisible = false"
    >
      <a-form layout="vertical">
        <a-form-item label="下载分级（与后端 destinationType 一致；无该码率时将提示改为源码）">
          <a-select v-model:value="batchTierPreset" style="width: 100%" placeholder="选择分级">
            <a-select-option v-for="o in MAM_BATCH_TIER_PRESETS" :key="o.value" :value="o.value">
              {{ o.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="batchTierPreset === 'CUSTOM'" label="自定义分级">
          <a-input v-model:value="batchCustomTier" allow-clear placeholder="如 720P、HLS_MAIN" />
        </a-form-item>
        <p class="rm-batch-hint">已选 {{ selectedRowKeys.length }} 项。</p>
      </a-form>
    </a-modal>

  </div>
</template>

<style scoped lang="less">
.resource-manage-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.resource-manage-page--immersive {
  max-width: none;
  margin: 0;
  padding: 12px 16px 24px;
  background: var(--mam-page-bg, #f0f2f5);
}

.resource-embed-bar {
  flex-shrink: 0;
  margin-bottom: 10px;
}

.resource-back {
  padding-left: 0;
  height: auto;
  font-weight: 500;
}

.resource-hint {
  flex-shrink: 0;
  margin-bottom: 12px;
}

.resource-hint :deep(.ant-alert-description) {
  font-size: 13px;
  color: var(--mam-text-secondary, #64748b);
  line-height: 1.55;
}

.resource-thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 40px;
  padding: 0;
  border: 1px solid var(--mam-border, #e2e8f0);
  border-radius: var(--mam-radius-sm, 8px);
  background: var(--mam-muted-bg, #f8fafc);
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.resource-thumb:hover {
  border-color: var(--mam-primary, #1677ff);
  box-shadow: 0 0 0 1px rgba(22, 119, 255, 0.12);
}

.resource-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.resource-thumb-ph {
  font-size: 12px;
  font-weight: 600;
  color: var(--mam-text-secondary, #64748b);
}

.resource-page-head {
  flex-shrink: 0;
}

.mam-em {
  color: var(--mam-primary, #1677ff);
  font-weight: 600;
}

.resource-manage-body {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0; /* 关键：防止flex布局溢出，App.vue里特意加的规则 */
}

.resource-manage-catalog {
  width: 248px;
  flex-shrink: 0;
  background: var(--mam-surface, #fff);
  border-radius: var(--mam-radius-lg, 12px);
  padding: 14px 12px 16px;
  border: 1px solid var(--mam-border, #e2e8f0);
  overflow-y: auto;
  box-shadow: var(--mam-shadow-sm, 0 1px 3px rgba(15, 23, 42, 0.06));
}

.resource-manage-main {
  flex: 1;
  min-width: 0;
  padding: 16px 18px 12px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.resource-toolbar {
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--mam-border, #e2e8f0);
}

.resource-toolbar-search {
  width: 260px;
  max-width: 100%;
}

/* 表格容器占满剩余高度，内部滚动 */
:deep(.ant-table-wrapper) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

:deep(.ant-table) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.ant-table-container) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

:deep(.ant-table-body) {
  flex: 1;
  overflow-y: auto !important;
}

/* 分页栏自动吸附到底部，完全符合全局布局规则 */
::v-deep .ant-pagination {
  margin-top: auto !important;
  padding: 12px 0 !important;
  margin-bottom: 0 !important;
  border-top: 1px solid #f0f0f0 !important;
  flex-shrink: 0;
}

.upload-progress {
  font-size: 12px;
  color: var(--mam-text-secondary, #64748b);
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rm-batch-hint {
  font-size: 12px;
  color: var(--mam-text-secondary, #64748b);
  margin: 0;
}

</style>
