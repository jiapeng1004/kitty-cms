<script setup lang="ts">
import {computed, nextTick, ref, watch} from 'vue'
import {message} from 'ant-design-vue'
import type {MenuProps, UploadProps} from 'ant-design-vue'
import {
  AppstoreOutlined,
  DeleteOutlined,
  DownOutlined,
  ShareAltOutlined,
  SettingOutlined,
  ToolOutlined,
  UserOutlined,
  UnorderedListOutlined
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

const fileTabs = [
  {key: 'all', label: '全部'},
  {key: '1', label: '视频'},
  {key: '2', label: '音频'},
  {key: '3', label: '图片'},
  {key: '4', label: '文档'},
  {key: '6', label: '其他'}
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
      <header class="workspace-main-head">
        <div class="main-title-row">
          <h1 class="main-title">{{ currentCatalog?.name || '素材' }}</h1>
          <span class="main-count">共 {{ total }} 项</span>
        </div>

        <div class="main-search-row">
          <a-input-group compact class="main-search-compact">
            <a-select default-value="keyword" style="width: 110px" disabled>
              <a-select-option value="keyword">普通检索</a-select-option>
            </a-select>
            <a-input
                v-model:value="searchKeyword"
                placeholder="搜索素材标题或路径"
                style="width: 280px"
                allow-clear
                @keyup.enter="onSearch"
            />
            <a-button type="primary" danger @click="onSearch">搜索</a-button>
          </a-input-group>
        </div>

        <div class="main-type-tabs">
          <button
              v-for="tab in fileTabs"
              :key="tab.key"
              type="button"
              class="type-tab"
              :class="{ 'type-tab-active': fileTypeTab === tab.key }"
              @click="fileTypeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="main-actions">
          <a-select v-model:value="sortKey" style="width: 130px">
            <a-select-option value="title">名称</a-select-option>
            <a-select-option value="type">类型</a-select-option>
          </a-select>
          <a-space class="view-toggle">
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
            <a-button type="primary" danger :loading="uploading" :disabled="!selectedCatalogId">
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
                    class="resource-card resource-card--clickable"
                    @click="goResourceDetail(item.id)"
                >
                  <div class="card-thumbnail">
                    <img
                        v-if="thumbSrc(item)"
                        :src="thumbSrc(item)"
                        class="thumb-img"
                        alt=""
                    />
                    <span v-else class="thumb-placeholder">{{ getFileTypeLabel(item.type) }}</span>
                    <div class="type-tag" :style="{ background: getFileTypeColor(item.type) }">
                      {{ getFileTypeLabel(item.type) }}
                    </div>
                  </div>
                  <div class="card-info">
                    <div class="file-name" :title="item.title">{{ item.title }}</div>
                    <div class="file-meta">
                      <span class="file-size">{{ formatFileSize(item.fileSize) }}</span>
                      <a
                          v-if="item.previewUrl"
                          :href="materialApiAbsoluteUrl(item.previewUrl)"
                          class="file-path file-path-url"
                          target="_blank"
                          rel="noopener noreferrer"
                          title="预览（经接口鉴权）"
                          @click.stop
                      >预览</a>
                      <a
                          v-if="item.srcUrl"
                          :href="item.srcUrl"
                          class="file-path file-src-link"
                          target="_blank"
                          rel="noopener noreferrer"
                          title="原文件直链"
                          @click.stop
                      >原文件</a>
                      <span v-if="!item.previewUrl && !item.srcUrl" class="file-path">—</span>
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
  </div>
</template>

<style scoped lang="less">
.material-workspace {
  display: flex;
  flex: 1;
  min-height: 0;
  min-width: 0;
  align-self: stretch;
  box-sizing: border-box;
  /* 保底高度，避免父链断裂时主区塌成内容高；不限制 max-height，减少与浏览器 UI 叠加时的裁切 */
  min-height: 100vh;
  min-height: 100dvh;
  overflow: hidden;
  background: #eef1f6;
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
  padding: 16px 20px;
  background: #f5f7fb;
}

.workspace-rail {
  width: 56px;
  flex-shrink: 0;
  background: #1f2937;
  color: rgba(255, 255, 255, 0.75);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: stretch;
  padding: 12px 0;
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
  background: rgba(245, 108, 108, 0.42);
  color: #fff;
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
  background: #fff;
  border-right: 1px solid #e5e7eb;
  padding: 16px 12px;
  overflow: auto;
}

.workspace-main {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f7fb;
}

.workspace-main-head {
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 16px 20px 12px;
}

.main-title-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
}

.main-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.main-count {
  font-size: 13px;
  color: #6b7280;
}

.main-search-row {
  margin-bottom: 12px;
}

.main-search-compact {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.main-type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.type-tab {
  border: 1px solid #d1d5db;
  background: #fff;
  border-radius: 8px;
  padding: 6px 16px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.type-tab:hover {
  border-color: #ef5350;
  color: #ef5350;
}

.type-tab-active {
  border-color: #ef5350;
  background: #fff5f5;
  color: #ef5350;
  font-weight: 600;
}

.main-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.view-toggle :deep(.ant-btn) {
  padding: 4px 10px;
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
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.resource-card {
  height: 220px;
  display: flex;
  flex-direction: column;
}

.resource-card--clickable {
  cursor: pointer;
}

.title-detail-link {
  color: #155eef;
}

.title-detail-link:hover {
  text-decoration: underline;
}

.card-thumbnail {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border-radius: 4px 4px 0 0;
  position: relative;
}

.thumb-placeholder {
  font-size: 22px;
  color: #9ca3af;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.type-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 6px;
  border-radius: 2px;
  color: #fff;
  font-size: 12px;
}

.card-info {
  flex: 1;
  padding: 12px;
}

.file-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
}

.file-meta {
  font-size: 12px;
  color: #999;
  display: flex;
  flex-direction: column;
  gap: 4px;
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
  background: #fff;
  border-radius: 8px;
  padding: 8px;
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

</style>

<!-- 下拉挂载在 body，需非 scoped 样式 -->
<style lang="less">
.mam-admin-gear-overlay .ant-dropdown-menu {
  min-width: 168px;
  max-height: min(70vh, 420px);
  overflow-y: auto;
}
</style>
