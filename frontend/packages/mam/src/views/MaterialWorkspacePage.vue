<script setup lang="ts">
import {computed, nextTick, ref, watch} from 'vue'
import {message} from 'ant-design-vue'
import type {MenuProps, UploadProps} from 'ant-design-vue'
import {
  AppstoreOutlined,
  AudioOutlined,
  DeleteOutlined,
  DownOutlined,
  FileTextOutlined,
  PictureOutlined,
  ReloadOutlined,
  ShareAltOutlined,
  SettingOutlined,
  ToolOutlined,
  UserOutlined,
  UnorderedListOutlined,
  VideoCameraOutlined
} from '@ant-design/icons-vue'
import MaterialCatalogTreePanel from '@/components/MaterialCatalogTreePanel.vue'
import {
  createFolder,
  materialApiAbsoluteUrl,
  pageResources,
  type MaterialResourceVO
} from '@/api/mam_resource_api'
import type {MaterialCatalogNode} from '@/api/mam_catalog_api'
import {useMaterialFileUpload} from '@/composables/useMaterialFileUpload'
import {
  MAM_BATCH_TIER_PRESETS,
  preflightMamBatchDownload,
  resolveDestinationType,
  showMamBatchDownloadConfirm
} from '@/composables/useMamBatchDownload'
import {useRoute, useRouter} from 'vue-router'

const route = useRoute()
const router = useRouter()

/**
 * 嵌入路由（/embed/...）下，管理页链接必须带 /embed 前缀，否则会跳到「带顶栏 Layout」的根路径，
 * 在 iframe / 微前端场景下父应用可能仍停留在素材壳子，表现为点了链接页面不变。
 */
function mamAdminPath(subPath: string): string {
  const clean = subPath.startsWith('/') ? subPath : `/${subPath}`
  return route.path.startsWith('/embed') ? `/embed${clean}` : clean
}

/** 进入管理子路由；嵌入模式下自动带 /embed 前缀 */
function navigateToAdmin(subPath: string) {
  nextTick(() => {
    router.push(mamAdminPath(subPath))
  })
}

function resourceDetailPath(id: string) {
  return route.path.startsWith('/embed') ? `/embed/material/resource/${id}` : `/material/resource/${id}`
}

function goResourceDetail(id: string) {
  router.push(resourceDetailPath(id))
}

function thumbSrc(item: MaterialResourceVO): string {
  if (item.coverUrl) return materialApiAbsoluteUrl(item.coverUrl)
  if (item.keyframeUrl) return materialApiAbsoluteUrl(item.keyframeUrl)
  if (item.previewUrl && item.type === 3) return materialApiAbsoluteUrl(item.previewUrl)
  return ''
}

const onAdminGearMenuClick: MenuProps['onClick'] = (info) => {
  const key = String(info.key)
  if (key.startsWith('/')) {
    navigateToAdmin(key)
  }
}
const folderModalVisible = ref(false)
const folderName = ref('')
const folderCreating = ref(false)

const catalogPanelRef = ref<InstanceType<typeof MaterialCatalogTreePanel>>()
const selectedCatalogId = ref<string>()
const currentCatalog = ref<MaterialCatalogNode | undefined>()

const loading = ref(false)
const resources = ref<MaterialResourceVO[]>([])
/** 服务端分页总条数（与 /api/material/resource/page 一致） */
const serverTotal = ref(0)
const searchKeyword = ref('')
const fileTypeTab = ref<string>('all')
const sortKey = ref<'title' | 'type'>('title')
const viewMode = ref<'grid' | 'list'>('grid')
const page = ref(1)
const pageSize = ref(48)

/** 多选 id（跨页保留）；对象快照用于跨页仍可取 resourceId/标题 参与批量 */
const selectedRowKeys = ref<string[]>([])
const knownResourceById = ref<Record<string, MaterialResourceVO>>({})

const batchModalVisible = ref(false)
const batchSubmitting = ref(false)
const batchTierPreset = ref('SOURCE')
const batchCustomTier = ref('')

function minimalResourceVo(id: string): MaterialResourceVO {
  return {
    id,
    title: id,
    catalogId: selectedCatalogId.value || '',
    parentId: '0',
    type: 6
  }
}

function onTableSelectChange(keys: string[]) {
  selectedRowKeys.value = keys
  const pageMap = new Map(pagedList.value.map((r) => [r.id, r]))
  const next: Record<string, MaterialResourceVO> = {}
  for (const k of keys) {
    const row = pageMap.get(k) || knownResourceById.value[k] || minimalResourceVo(k)
    next[k] = row
  }
  knownResourceById.value = next
}

function toggleGridSelect(item: MaterialResourceVO, checked: boolean) {
  if (item.type === 7) {
    return
  }
  const s = new Set(selectedRowKeys.value)
  if (checked) {
    s.add(item.id)
  } else {
    s.delete(item.id)
  }
  onTableSelectChange([...s])
}

const selectedResources = computed((): MaterialResourceVO[] => {
  return selectedRowKeys.value.map(
    (id) => knownResourceById.value[id] || minimalResourceVo(id)
  )
})

function openBatchDownloadModal() {
  if (!selectedRowKeys.value.length) {
    message.warning('请先勾选资源')
    return
  }
  batchModalVisible.value = true
}

/** 全选当前列表中的本页条目（跨页其它已选项保留） */
function selectAllCurrentPage() {
  const pageIds = new Set(pagedList.value.map((r) => r.id))
  const rest = selectedRowKeys.value.filter((id) => !pageIds.has(id))
  const selectable = pagedList.value.filter((r) => r.type !== 7).map((r) => r.id)
  onTableSelectChange([...rest, ...selectable])
}

function clearSelectionCurrentPage() {
  const pageIds = new Set(pagedList.value.map((r) => r.id))
  onTableSelectChange(selectedRowKeys.value.filter((id) => !pageIds.has(id)))
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
      knownResourceById.value = {}
    }
  } finally {
    batchSubmitting.value = false
  }
}

/** 输入关键词防抖后拉列表，避免「只打字不点搜索」时 Network 里始终没有带 keyword 的请求 */
const SEARCH_DEBOUNCE_MS = 400
let searchDebounceTimer: ReturnType<typeof setTimeout> | undefined

function onCatalogSelect(node: MaterialCatalogNode | undefined) {
  currentCatalog.value = node
}

async function loadResources(resetPage?: boolean) {
  const cid = selectedCatalogId.value
  if (!cid) {
    resources.value = []
    serverTotal.value = 0
    return
  }
  if (resetPage) {
    page.value = 1
  }
  loading.value = true
  try {
    const kw = searchKeyword.value.trim()
    const resp = await pageResources({
      catalogId: cid,
      parentId: '0',
      page: page.value,
      size: pageSize.value,
      ...(kw ? {keyword: kw} : {})
    })
    resources.value = resp.records ?? []
    serverTotal.value = Number(resp.total ?? 0)
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '加载素材失败')
  } finally {
    loading.value = false
  }
}

watch(selectedCatalogId, () => {
  selectedRowKeys.value = []
  knownResourceById.value = {}
  void loadResources(true)
})

watch(searchKeyword, () => {
  if (!selectedCatalogId.value) {
    return
  }
  if (searchDebounceTimer !== undefined) {
    clearTimeout(searchDebounceTimer)
  }
  searchDebounceTimer = setTimeout(() => {
    searchDebounceTimer = undefined
    void loadResources(true)
  }, SEARCH_DEBOUNCE_MS)
})

const {uploading, progressText, uploadFiles} = useMaterialFileUpload({
  getCatalogId: () => selectedCatalogId.value,
  getParentId: () => '0',
  onFinished: () => loadResources(true)
})

const uploadRequest: UploadProps['customRequest'] = (opt) => {
  const raw = opt.file as File | Blob
  const file = raw instanceof File ? raw : new File([raw], (opt.file as {
    name?: string
  }).name || 'upload.bin', {type: raw.type})
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
    await createFolder({title: name, catalogId: cid, parentId: '0'})
    message.success('文件夹已创建')
    folderModalVisible.value = false
    folderName.value = ''
    await loadResources(true)
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '创建失败')
  } finally {
    folderCreating.value = false
  }
}

const filteredByType = computed(() => {
  let list = resources.value
  if (fileTypeTab.value === 'all') {
    return list
  }
  if (fileTypeTab.value === '4') {
    return list.filter((item) => item.type === 4 || item.type === 5)
  }
  const t = parseInt(fileTypeTab.value, 10)
  return list.filter((item) => item.type === t)
})

const sortedList = computed(() => {
  const list = [...filteredByType.value]
  if (sortKey.value === 'title') {
    list.sort((a, b) => (a.title || '').localeCompare(b.title || '', 'zh-CN'))
  } else {
    list.sort((a, b) => (a.type ?? 0) - (b.type ?? 0))
  }
  return list
})

/** 与后端分页 total 一致（类型筛选仅作用于当前页数据） */
const total = computed(() => serverTotal.value)

/** 当前页记录在客户端排序/筛选后展示，不再做二次内存分页 */
const pagedList = computed(() => sortedList.value)

/** 服务端有总数，但类型筛选后当前页无匹配项（易误以为「布局坏了」） */
const filterEmptyButHasTotal = computed(
    () => !loading.value && serverTotal.value > 0 && sortedList.value.length === 0
)

function getFileTypeLabel(type: number) {
  switch (type) {
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
    case 7:
      return '文件夹'
    default:
      return '其他'
  }
}

function getFileTypeColor(type: number) {
  switch (type) {
    case 1:
      return '#1890ff'
    case 2:
      return '#52c41a'
    case 3:
      return '#faad14'
    case 4:
      return '#f5222d'
    case 5:
      return '#722ed1'
    case 7:
      return '#13c2c2'
    default:
      return '#8c8c8c'
  }
}

function formatFileSize(size?: number) {
  if (size == null) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(1)} MB`
  return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`
}

function onSearch() {
  if (searchDebounceTimer !== undefined) {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = undefined
  }
  void loadResources(true)
}

function onPaginationChange(p: number, ps?: number) {
  page.value = p
  if (ps != null) {
    pageSize.value = ps
  }
  void loadResources(false)
}

const fileTabs: { key: string; label: string; icon: typeof AppstoreOutlined }[] = [
  { key: 'all', label: '全部', icon: AppstoreOutlined },
  { key: '1', label: '视频', icon: VideoCameraOutlined },
  { key: '2', label: '音频', icon: AudioOutlined },
  { key: '3', label: '图片', icon: PictureOutlined },
  { key: '4', label: '文档', icon: FileTextOutlined },
  { key: '6', label: '其他', icon: AppstoreOutlined }
]
</script>

<template>
  <div class="material-workspace">
    <aside class="workspace-rail" aria-label="主导航">
      <div class="rail-top">
        <div class="rail-item rail-item-active" title="素材">
          <appstore-outlined/>
          <span>素材</span>
        </div>
        <div class="rail-item rail-item-disabled" title="即将推出">
          <user-outlined/>
          <span>我的</span>
        </div>
        <div class="rail-item rail-item-disabled" title="即将推出">
          <tool-outlined/>
          <span>工具集</span>
        </div>
        <div class="rail-item rail-item-disabled" title="即将推出">
          <share-alt-outlined/>
          <span>分享</span>
        </div>
        <div class="rail-item rail-item-disabled" title="即将推出">
          <delete-outlined/>
          <span>回收站</span>
        </div>
      </div>
      <div class="rail-bottom">
        <a-dropdown :trigger="['click']" placement="rightTop" overlay-class-name="mam-admin-gear-overlay">
          <button type="button" class="rail-item rail-item-gear" title="设置">
            <setting-outlined/>
            <span>设置</span>
          </button>
          <template #overlay>
            <a-menu class="admin-gear-menu" @click="onAdminGearMenuClick">
              <a-menu-item key="/resources">资源管理</a-menu-item>
              <a-menu-item key="/catalog-tree">栏目树</a-menu-item>
              <a-menu-item key="/permissions">栏目权限</a-menu-item>
              <a-menu-item key="/metadata">编目管理</a-menu-item>
              <a-menu-item key="/storage-route">存储路由</a-menu-item>
              <a-menu-item key="/storage-manage">存储管理</a-menu-item>
              <a-menu-item key="/tasks">任务中心</a-menu-item>
              <a-menu-item key="/transcode-policy">转码策略</a-menu-item>
              <a-menu-item key="/messages">站内信</a-menu-item>
              <a-menu-item key="/review">审核</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </aside>

    <aside class="workspace-catalog">
      <MaterialCatalogTreePanel
          ref="catalogPanelRef"
          v-model:selected-catalog-id="selectedCatalogId"
          @catalog-select="onCatalogSelect"
      />
    </aside>

    <main class="workspace-main">
      <header class="workspace-main-head workspace-main-head--mam-pro">
        <div class="main-title-row">
          <div class="main-title-block">
            <h1 class="main-title">{{ currentCatalog?.name || '媒资库' }}</h1>
            <span class="main-count"
            ><span class="main-count-num">{{ total }}</span> 条可检索结果</span
            >
          </div>
        </div>

        <div class="main-search-row">
          <div class="mam-search-shell">
            <a-input-group compact class="main-search-compact">
              <a-select default-value="keyword" class="mam-search-type" style="width: 112px" disabled>
                <a-select-option value="keyword">标题检索</a-select-option>
              </a-select>
              <a-input
                v-model:value="searchKeyword"
                class="mam-search-input"
                size="large"
                placeholder="输入关键词，搜索素材标题或路径"
                allow-clear
                @keyup.enter="onSearch"
              />
              <a-button type="primary" class="mam-search-btn" size="large" @click="onSearch">搜索</a-button>
            </a-input-group>
            <p class="mam-search-hint">支持在当前栏目内检索；与类型标签组合筛选。</p>
          </div>
        </div>

        <div class="main-type-tabs" role="tablist" aria-label="按类型筛选">
          <button
            v-for="tab in fileTabs"
            :key="tab.key"
            type="button"
            class="type-tab"
            :class="{ 'type-tab-active': fileTypeTab === tab.key }"
            role="tab"
            :aria-selected="fileTypeTab === tab.key"
            @click="fileTypeTab = tab.key"
          >
            <component :is="tab.icon" class="type-tab-icon" aria-hidden="true" />
            {{ tab.label }}
          </button>
        </div>

        <div class="mam-toolbar">
          <div class="mam-toolbar-left">
            <template v-if="viewMode === 'grid' && total > 0 && !filterEmptyButHasTotal">
              <a-button type="link" class="mam-toolbar-link" size="small" @click.stop="selectAllCurrentPage">
                全选本页
              </a-button>
              <a-button type="link" class="mam-toolbar-link" size="small" @click.stop="clearSelectionCurrentPage">
                清除本页
              </a-button>
              <a-divider type="vertical" class="mam-toolbar-divider" />
            </template>
            <a-button
              v-if="selectedRowKeys.length > 0"
              @click="openBatchDownloadModal"
            >
              批量下载
              ({{ selectedRowKeys.length }})
            </a-button>
            <a-button :disabled="!selectedCatalogId" @click="loadResources(false)">
              <template #icon>
                <reload-outlined />
              </template>
              刷新
            </a-button>
          </div>
          <div class="mam-toolbar-right">
            <a-select v-model:value="sortKey" class="mam-toolbar-sort" :disabled="!total">
            <a-select-option value="title">按名称</a-select-option>
            <a-select-option value="type">按类型</a-select-option>
          </a-select>
          <a-space class="view-toggle" :size="4">
            <a-button
                :type="viewMode === 'grid' ? 'primary' : 'default'"
                @click="viewMode = 'grid'"
            >
              <template #icon>
                <appstore-outlined/>
              </template>
            </a-button>
            <a-button
                :type="viewMode === 'list' ? 'primary' : 'default'"
                @click="viewMode = 'list'"
            >
              <template #icon>
                <unordered-list-outlined/>
              </template>
            </a-button>
          </a-space>
          <span v-if="progressText" class="upload-progress">{{ progressText }}</span>
          <a-dropdown :trigger="['click']">
            <a-button type="primary" :loading="uploading" :disabled="!selectedCatalogId">
              上传
              <down-outlined/>
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
        </div>
      </header>

      <!-- 列表可滚动区 flex:1 占满剩余高度，分页栏固定在主内容区底部 -->
      <div class="workspace-main-fill">
        <!-- 必须外包一层：a-spin 根节点是 .ant-spin-nested-loading，class 往往挂不上去，且 Ant 默认 display:block 会打断 flex 高度链 -->
        <div class="workspace-main-spin-wrap">
          <a-spin :spinning="loading">
            <div class="workspace-main-body">
              <div class="workspace-main-scroll">
                <a-empty
                    v-if="!loading && total === 0"
                    class="workspace-empty"
                    description="暂无素材"
                />
                <a-empty
                    v-else-if="filterEmptyButHasTotal"
                    class="workspace-empty"
                    description="当前类型筛选下没有匹配的素材，请点「全部」或切换类型标签"
                />
                <div v-else-if="viewMode === 'grid' && total > 0" class="resource-grid">
                <a-card
                    v-for="item in pagedList"
                    :key="item.id"
                    hoverable
                    :bordered="false"
                    class="mam-asset-card resource-card resource-card--clickable"
                    @click="goResourceDetail(item.id)"
                >
                  <div class="card-thumbnail">
                    <div class="card-thumb-gradient" aria-hidden="true" />
                    <div
                        v-if="item.type !== 7"
                        class="card-check-wrap"
                        @click.stop
                    >
                      <a-checkbox
                        :checked="selectedRowKeys.includes(item.id)"
                        @update:checked="(c: boolean) => toggleGridSelect(item, c)"
                      />
                    </div>
                    <img
                        v-if="thumbSrc(item)"
                        :src="thumbSrc(item)"
                        class="thumb-img"
                        alt=""
                    />
                    <span v-else class="thumb-placeholder">{{ getFileTypeLabel(item.type) }}</span>
                    <div
                      class="type-tag type-tag--on-thumb"
                      :style="{ background: getFileTypeColor(item.type) }"
                    >
                      {{ getFileTypeLabel(item.type) }}
                    </div>
                    <div v-if="item.fileSize != null" class="card-size-pill">
                      {{ formatFileSize(item.fileSize) }}
                    </div>
                  </div>
                  <div class="card-info">
                    <div class="file-name" :title="item.title">{{ item.title }}</div>
                    <div class="file-meta">
                      <span class="file-meta-line">{{
                        item.fileSize == null ? '—' : formatFileSize(item.fileSize) + ' · ' + getFileTypeLabel(item.type)
                      }}</span>
                      <div class="file-actions">
                        <a
                          v-if="item.previewUrl"
                          :href="materialApiAbsoluteUrl(item.previewUrl)"
                          class="file-link"
                          target="_blank"
                          rel="noopener noreferrer"
                          title="预览（经接口鉴权）"
                          @click.stop
                        >预览</a>
                        <a
                          v-if="item.srcUrl"
                          :href="item.srcUrl"
                          class="file-link file-link--muted"
                          target="_blank"
                          rel="noopener noreferrer"
                          title="原文件直链"
                          @click.stop
                        >原文件</a>
                        <span
                          v-if="!item.previewUrl && !item.srcUrl"
                          class="file-link-disabled"
                        >—</span>
                      </div>
                    </div>
                  </div>
                </a-card>
                </div>
                <div v-else-if="viewMode === 'list' && total > 0 && !filterEmptyButHasTotal" class="resource-list-wrap">
                <a-table
                    :data-source="pagedList"
                    :pagination="false"
                    row-key="id"
                    size="small"
                    :row-selection="{
                      selectedRowKeys,
                      onChange: onTableSelectChange,
                      getCheckboxProps: (record) => ({ disabled: record.type === 7 })
                    }"
                >
                  <a-table-column title="标题" data-index="title" ellipsis>
                    <template #default="{ record }">
                      <router-link :to="resourceDetailPath(record.id)" class="title-detail-link">{{
                          record.title
                        }}
                      </router-link>
                    </template>
                  </a-table-column>
                  <a-table-column title="类型" :width="88" key="type">
                    <template #default="{ record }">
                      <a-tag :color="getFileTypeColor(record.type)">{{ getFileTypeLabel(record.type) }}</a-tag>
                    </template>
                  </a-table-column>
                  <a-table-column title="大小" :width="100" key="fileSize">
                    <template #default="{ record }">
                      {{ formatFileSize(record.fileSize) }}
                    </template>
                  </a-table-column>
                  <a-table-column title="预览" key="previewUrl" :width="200" :ellipsis="true">
                    <template #default="{ record }">
                      <a
                          v-if="record.previewUrl"
                          :href="materialApiAbsoluteUrl(record.previewUrl)"
                          target="_blank"
                          rel="noopener noreferrer"
                          class="table-path-url"
                      >打开预览</a>
                      <span v-else>—</span>
                    </template>
                  </a-table-column>
                  <a-table-column title="封面" key="coverUrl" :width="100">
                    <template #default="{ record }">
                      <a
                          v-if="record.coverUrl"
                          :href="materialApiAbsoluteUrl(record.coverUrl)"
                          target="_blank"
                          rel="noopener noreferrer"
                          @click.stop
                      >打开</a>
                      <span v-else>—</span>
                    </template>
                  </a-table-column>
                  <a-table-column title="关键帧" key="keyframeUrl" :width="100">
                    <template #default="{ record }">
                      <a
                          v-if="record.keyframeUrl"
                          :href="materialApiAbsoluteUrl(record.keyframeUrl)"
                          target="_blank"
                          rel="noopener noreferrer"
                          @click.stop
                      >打开</a>
                      <span v-else>—</span>
                    </template>
                  </a-table-column>
                  <a-table-column title="原文件" key="srcUrl" :ellipsis="true">
                    <template #default="{ record }">
                      <a
                          v-if="record.srcUrl"
                          :href="record.srcUrl"
                          target="_blank"
                          rel="noopener noreferrer"
                          class="table-src-url"
                          @click.stop
                      >{{ record.srcUrl }}</a>
                      <span v-else>—</span>
                    </template>
                  </a-table-column>
                </a-table>
                </div>
              </div>

              <!-- 与列表同一列：列表在上方区域内滚动，分页始终贴主区下沿 -->
              <div v-if="total > 0" class="workspace-pagination">
                <div class="workspace-pagination-inner">
                  <div class="pagination-summary" aria-live="polite">
                    共 <span class="pagination-total-num">{{ total }}</span> 条数据
                  </div>
                  <a-pagination
                      class="pagination-controls"
                      v-model:current="page"
                      v-model:page-size="pageSize"
                      :total="total"
                      :show-size-changer="true"
                      :page-size-options="['24', '48', '100', '200']"
                      @change="onPaginationChange"
                  />
                </div>
              </div>
            </div>
          </a-spin>
        </div>
      </div>
    </main>

    <a-modal
        v-model:open="folderModalVisible"
        title="新建文件夹"
        :confirm-loading="folderCreating"
        ok-text="确定"
        @ok="submitCreateFolder"
        @cancel="folderModalVisible = false"
    >
      <a-input v-model:value="folderName" placeholder="文件夹名称" maxlength="200" @press-enter="submitCreateFolder"/>
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
        <a-form-item v-if="batchTierPreset === 'CUSTOM'" label="自定义分级，如 720P、HLS_MAIN">
          <a-input v-model:value="batchCustomTier" allow-clear placeholder="输入分级标识" />
        </a-form-item>
        <p class="batch-dl-hint">已选 {{ selectedRowKeys.length }} 项（已排除未勾选的当前页与文件夹行）。</p>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped lang="less">
/* 企事业媒资库：蓝白主色、卡片阴影与缩略图叠加，贴近 Ant Design Pro 系观感 */
.material-workspace {
  display: flex;
  flex: 1;
  min-height: 0;
  min-width: 0;
  align-self: stretch;
  box-sizing: border-box;
  min-height: 100vh;
  min-height: 100dvh;
  overflow: hidden;
  background: var(--mam-page-bg, #f0f2f5);
}

.workspace-main-fill {
  flex: 1;
  min-height: 0;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.workspace-main-spin-wrap {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Ant Design Spin 内部默认 display:block（css-dev-only 覆盖层），会压过普通 scoped 规则，需 !important 才能参与纵向 flex */
.workspace-main-spin-wrap :deep(.ant-spin-nested-loading) {
  flex: 1;
  min-height: 0;
  display: flex !important;
  flex-direction: column !important;
  align-items: stretch !important;
  justify-content: flex-start !important;
  overflow: hidden;
}

.workspace-main-spin-wrap :deep(.ant-spin-container) {
  flex: 1;
  min-height: 0;
  display: flex !important;
  flex-direction: column !important;
  align-items: stretch !important;
  justify-content: flex-start !important;
  overflow: hidden;
}

.workspace-main-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 占满主内容列剩余高度，内部滚动；分页在 body 底栏，避免视口底部大块留白 */
.workspace-main-scroll {
  flex: 1 1 0;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 24px;
  background: linear-gradient(180deg, #f5f7fa 0%, #f0f2f5 32%);
}

.workspace-rail {
  width: 60px;
  flex-shrink: 0;
  background: linear-gradient(195deg, #0c1629 0%, #1a2744 100%);
  color: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: stretch;
  padding: 14px 0;
  box-shadow: 2px 0 12px rgba(15, 23, 42, 0.08);
}

.rail-top {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 4px;
  font-size: 11px;
  cursor: pointer;
  border-radius: 8px;
  margin: 0 6px;
  transition: background 0.2s;
}

.rail-item:hover:not(.rail-item-disabled) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.rail-item-active {
  background: linear-gradient(180deg, rgba(22, 119, 255, 0.35), rgba(22, 119, 255, 0.12));
  color: #fff;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.12);
}

.rail-item-gear {
  border: none;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
  width: 100%;
}

.rail-item-gear:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  opacity: 1;
}

.rail-item-disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.rail-bottom {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 8px;
}

.workspace-catalog {
  width: 300px;
  flex-shrink: 0;
  background: var(--mam-surface);
  border-right: 1px solid var(--mam-border);
  padding: 16px 14px 20px;
  overflow: auto;
  box-shadow: 4px 0 24px rgba(15, 23, 42, 0.04);
}

.workspace-main {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--mam-page-bg);
}

.workspace-main-head {
  flex-shrink: 0;
}

.workspace-main-head--mam-pro {
  padding: 20px 24px 12px;
  background: var(--mam-surface);
  border-bottom: 1px solid var(--mam-border);
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.04);
}

.main-title-row {
  margin-bottom: 16px;
}

.main-title-block {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 10px 16px;
}

.main-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--mam-text);
}

.main-count {
  font-size: 13px;
  color: var(--mam-text-secondary);
}

.main-count-num {
  font-weight: 600;
  color: var(--mam-primary);
  margin-right: 2px;
}

.main-search-row {
  margin-bottom: 14px;
}

.mam-search-shell {
  max-width: 720px;
}

.mam-search-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--mam-text-secondary);
  line-height: 1.4;
}

.main-search-compact {
  display: flex;
  flex-wrap: wrap;
  align-items: stretch;
  width: 100%;
  min-height: 40px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
  border: 1px solid var(--mam-border);
  background: #fafbfc;
}

:deep(.mam-search-type.ant-select) .ant-select-selector {
  border: none !important;
  background: #f1f5f9 !important;
  height: 40px !important;
  line-height: 40px !important;
  border-radius: 0 !important;
}

:deep(.mam-search-input) {
  min-width: 0;
  flex: 1;
}

:deep(.mam-search-input .ant-input) {
  border: none !important;
  box-shadow: none !important;
  background: #fff !important;
  height: 40px;
}

:deep(.mam-search-btn) {
  min-width: 88px;
  font-weight: 500;
  border-radius: 0 9px 9px 0 !important;
}

.main-type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.type-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--mam-border);
  background: var(--mam-surface);
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
}

.type-tab:hover {
  border-color: var(--mam-primary);
  color: var(--mam-primary);
  background: var(--mam-primary-bg);
}

.type-tab-icon {
  font-size: 15px;
  opacity: 0.9;
}

.type-tab-active {
  border-color: var(--mam-primary);
  background: var(--mam-primary-bg);
  color: var(--mam-primary);
  font-weight: 600;
  box-shadow: 0 0 0 1px rgba(22, 119, 255, 0.15);
}

.mam-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px 16px;
  padding: 10px 0 2px;
  border-top: 1px solid #f1f5f9;
  margin-top: 4px;
}

.mam-toolbar-left,
.mam-toolbar-right {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 10px;
}

.mam-toolbar-right {
  margin-left: auto;
}

:deep(.mam-toolbar-sort) {
  min-width: 120px;
}

.mam-toolbar-link {
  padding: 0 4px;
  color: #475569 !important;
}

.mam-toolbar-link:hover {
  color: var(--mam-primary) !important;
}

.mam-toolbar-divider {
  margin: 0 2px;
  background: #e2e8f0;
}

.view-toggle :deep(.ant-btn) {
  padding: 4px 10px;
  border-radius: 8px;
}

.main-actions-links {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.admin-link {
  font-size: 13px;
  color: #155eef;
}

.upload-progress {
  font-size: 12px;
  color: #6b7280;
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
}

.mam-asset-card {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef0f3;
  transition: box-shadow 0.2s, border-color 0.2s, transform 0.2s;
  overflow: hidden;
}

.mam-asset-card:hover {
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.1);
  border-color: rgba(22, 119, 255, 0.25);
}

.mam-asset-card :deep(.ant-card-body) {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.resource-card {
  min-height: 256px;
  display: flex;
  flex-direction: column;
}

.resource-card--clickable {
  cursor: pointer;
}

.title-detail-link {
  color: #1677ff;
  font-weight: 500;
}

.title-detail-link:hover {
  text-decoration: underline;
  color: #4096ff;
}

.card-thumbnail {
  aspect-ratio: 16 / 9;
  min-height: 132px;
  max-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #e8eef5 0%, #f8fafc 100%);
  position: relative;
  overflow: hidden;
}

.card-thumb-gradient {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 48%;
  background: linear-gradient(180deg, transparent, rgba(15, 23, 42, 0.55));
  z-index: 1;
  pointer-events: none;
}

.card-check-wrap {
  position: absolute;
  top: 8px;
  right: 8px;
  left: auto;
  z-index: 3;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 6px;
  padding: 2px 5px;
  line-height: 1;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
}

.thumb-placeholder {
  font-size: 22px;
  color: #9ca3af;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  position: relative;
  z-index: 0;
}

.type-tag--on-thumb {
  position: absolute;
  left: 8px;
  bottom: 8px;
  right: auto;
  top: auto;
  z-index: 2;
  padding: 2px 8px;
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.02em;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.card-size-pill {
  position: absolute;
  right: 8px;
  bottom: 8px;
  z-index: 2;
  font-size: 11px;
  line-height: 1.2;
  padding: 2px 7px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.45);
  color: #f1f5f9;
  font-variant-numeric: tabular-nums;
}

.card-info {
  flex: 1;
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 0;
  background: var(--mam-surface);
}

.file-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--mam-text);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
  min-height: 2.9em;
}

.file-meta {
  font-size: 12px;
  color: var(--mam-text-secondary);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.file-meta-line {
  font-size: 12px;
  color: #94a3b8;
  font-variant-numeric: tabular-nums;
}

.file-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 10px;
  align-items: center;
}

.file-link {
  font-size: 12px;
  color: var(--mam-primary) !important;
  font-weight: 500;
}

.file-link--muted {
  color: #64748b !important;
  font-weight: 400;
}

.file-link-disabled {
  font-size: 12px;
  color: #cbd5e1;
}

.file-path {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-path-url {
  font-size: 12px;
  margin-top: 2px;
  color: #155eef;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.table-path-url {
  word-break: break-all;
  color: #155eef;
}

.file-src-link {
  font-size: 12px;
  margin-left: 8px;
  color: #64748b;
}

.table-src-url {
  word-break: break-all;
  color: #64748b;
  font-size: 12px;
}

.resource-list-wrap {
  background: var(--mam-surface);
  border-radius: 12px;
  padding: 8px 10px 12px;
  border: 1px solid var(--mam-border);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.05);
}

.resource-list-wrap :deep(.ant-table) {
  font-size: 13px;
}

/* 主区底栏：与参考站一致，列表在上方滚动，本栏始终贴主内容区下沿 */
.workspace-pagination {
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
  padding: 12px 20px;
  border-top: 1px solid #e8e8e8;
  background: #fff;
  box-shadow: 0 -4px 12px rgba(15, 23, 42, 0.04);
  z-index: 2;
}

.workspace-pagination-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  flex-wrap: nowrap;
  min-width: 0;
  overflow-x: auto;
}

.pagination-summary {
  font-size: 13px;
  color: #64748b;
  line-height: 32px;
  flex: 0 1 auto;
  min-width: 0;
}

.pagination-total-num {
  font-weight: 600;
  color: #111827;
}

.pagination-controls {
  flex: 0 1 auto;
  margin-left: auto;
}

.workspace-pagination :deep(.pagination-controls.ant-pagination) {
  flex-wrap: nowrap;
  justify-content: flex-end;
  margin: 0;
  flex-shrink: 0;
}

.workspace-empty {
  padding: 48px 0;
}

.batch-dl-hint {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

</style>

<!-- 下拉挂载在 body，需非 scoped 样式 -->
<style lang="less">
.mam-admin-gear-overlay .ant-dropdown-menu {
  min-width: 168px;
  max-height: min(70vh, 420px);
  overflow-y: auto;
}
</style>
