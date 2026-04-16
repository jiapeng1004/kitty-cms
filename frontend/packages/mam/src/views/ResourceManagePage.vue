<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import {
  DownOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import MaterialCatalogTreePanel from '@/components/MaterialCatalogTreePanel.vue'
import {
  createFolder,
  materialApiAbsoluteUrl,
  pageResources,
  updateResource,
  type MaterialResourceVO
} from '@/api/mam_resource_api'
import type { MaterialCatalogNode } from '@/api/mam_catalog_api'
import { useMaterialFileUpload } from '@/composables/useMaterialFileUpload'

// 删除之前的强制注入样式，用原生布局适配

const route = useRoute()
const router = useRouter()

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

async function refresh() {
  await catalogPanelRef.value?.load()
  await loadList()
}
</script>

<template>
  <div class="resource-manage-page">
    <div class="resource-page-head">
      <h1 class="resource-page-title">资源管理</h1>
      <span class="resource-page-sub">当前栏目：{{ currentCatalog?.name || '未选择' }}</span>
    </div>

    <div class="resource-manage-body">
      <aside class="resource-manage-catalog">
        <MaterialCatalogTreePanel
          ref="catalogPanelRef"
          v-model:selected-catalog-id="selectedCatalogId"
          @catalog-select="onCatalogSelect"
        />
      </aside>

      <main class="resource-manage-main">
        <div class="resource-toolbar">
          <a-input
            v-model:value="searchKeyword"
            allow-clear
            placeholder="筛选标题、预览或原链（本地）"
            style="width: 240px"
            @press-enter="loadList"
          />
          <a-button @click="loadList">检索后端</a-button>
          <a-button :loading="loading" @click="refresh">
            <template #icon>
              <reload-outlined />
            </template>
            刷新
          </a-button>
          <span v-if="progressText" class="upload-progress">{{ progressText }}</span>
          <a-dropdown :trigger="['click']">
            <a-button type="primary" danger :loading="uploading" :disabled="!selectedCatalogId">
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

        <!-- 列表容器占满剩余高度，内部滚动，让分页留在底部 -->
        <div class="resource-list-container">
          <a-table
            :data-source="filteredRows"
            :loading="loading"
            row-key="id"
            size="small"
            :pagination="{ pageSize: 50, showSizeChanger: true }"
            :scroll="{ x: 1200 }"
          >
          <a-table-column title="标题" data-index="title" key="title" :ellipsis="true" />
          <a-table-column title="类型" key="type" :width="100">
            <template #default="{ record }">
              {{ typeLabel(record.type) }}
            </template>
          </a-table-column>
          <a-table-column title="大小" key="size" :width="108">
            <template #default="{ record }">
              {{ formatSize(record.fileSize) }}
            </template>
          </a-table-column>
          <a-table-column title="预览" key="previewUrl" :width="120">
            <template #default="{ record }">
              <a
                v-if="record.previewUrl"
                :href="materialApiAbsoluteUrl(record.previewUrl)"
                target="_blank"
                rel="noopener noreferrer"
              >打开</a>
              <span v-else>—</span>
            </template>
          </a-table-column>
          <a-table-column title="封面" key="coverUrl" :width="88">
            <template #default="{ record }">
              <a
                v-if="record.coverUrl"
                :href="materialApiAbsoluteUrl(record.coverUrl)"
                target="_blank"
                rel="noopener noreferrer"
              >打开</a>
              <span v-else>—</span>
            </template>
          </a-table-column>
          <a-table-column title="关键帧" key="keyframeUrl" :width="88">
            <template #default="{ record }">
              <a
                v-if="record.keyframeUrl"
                :href="materialApiAbsoluteUrl(record.keyframeUrl)"
                target="_blank"
                rel="noopener noreferrer"
              >打开</a>
              <span v-else>—</span>
            </template>
          </a-table-column>
          <a-table-column title="原文件" key="srcUrl" :ellipsis="true">
            <template #default="{ record }">
              <a v-if="record.srcUrl" :href="record.srcUrl" target="_blank" rel="noopener noreferrer">{{ record.srcUrl }}</a>
              <span v-else>—</span>
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

  </div>
</template>

<style scoped lang="less">
/* 完全适配全局100%高度flex布局 */
.resource-manage-page {
  height: 100%; /* 继承全局的height:100%，不需要硬编码calc */
  display: flex;
  flex-direction: column;
  padding: 20px 20px 0 20px;
  box-sizing: border-box;
}

.resource-page-head {
  margin-bottom: 16px;
  flex-shrink: 0;
}

.resource-page-title {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.resource-page-sub {
  font-size: 13px;
  color: #6b7280;
}

.resource-manage-body {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0; /* 关键：防止flex布局溢出，App.vue里特意加的规则 */
}

.resource-manage-catalog {
  width: 240px; /* 适配你截图里的窄侧边栏宽度 */
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  overflow-y: auto;
}

.resource-manage-main {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  min-height: 0; /* 关键：和全局规则对齐 */
}

.resource-toolbar {
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
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
  color: #6b7280;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

</style>
