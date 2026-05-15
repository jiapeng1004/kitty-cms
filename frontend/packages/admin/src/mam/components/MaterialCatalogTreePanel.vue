<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { TreeProps, TreeSelectProps } from 'ant-design-vue'
import {
  AppstoreOutlined,
  CaretDownFilled,
  CaretRightFilled,
  FolderFilled,
  PlusOutlined
} from '@ant-design/icons-vue'
import {
  createCatalog,
  deleteCatalog,
  queryCatalogTree,
  updateCatalog,
  type CatalogMovePosition,
  type MaterialCatalogNode
} from '@/mam/api/mam_catalog_api'
import {
  MaterialPermissionCode,
  catalogNodeHasPermission,
  type MaterialPermissionCodeValue
} from '@/mam/constants/material_permission_code'

type CatalogTreeNode = NonNullable<TreeProps['treeData']>[number] & {
  /** 与 {@link MaterialCatalogNode#id} 一致，供拖拽/接口使用（勿仅用 key） */
  id: string
  parentId: string
  name: string
  level: number
  virtualRoot: boolean
  children?: CatalogTreeNode[]
}

function rowFromDataRef(dataRef: unknown): CatalogTreeNode {
  return dataRef as CatalogTreeNode
}

const selectedCatalogId = defineModel<string | undefined>('selectedCatalogId', { required: false })

const emit = defineEmits<{
  (e: 'catalog-select', node: MaterialCatalogNode | undefined): void
}>()

const loading = ref(false)
const treeData = ref<CatalogTreeNode[]>([])
const rawTree = ref<MaterialCatalogNode[]>([])
const createModalVisible = ref(false)
const creating = ref(false)
const createName = ref('')
const searchKeyword = ref('')
const detailModalVisible = ref(false)
const detailNode = ref<MaterialCatalogNode>()
const actionMenuNodeKey = ref<string>()
const renameModalVisible = ref(false)
const renameLoading = ref(false)
const renameCatalogId = ref<string>()

const renameName = ref('')
const deleteModalVisible = ref(false)
const deleting = ref(false)
const deleteCatalogId = ref<string>()
const moveModalVisible = ref(false)
const moveLoading = ref(false)
const moveCatalogId = ref<string>()
const moveTargetParentId = ref<string>()

// 完全自主实现拖拽，不用 a-tree 内置 draggable（其 drop 样式类不会出现在自定义 title 上）
const draggingNode = ref<CatalogTreeNode | null>(null)
const dropTargetKey = ref<string | null>(null)
const dropPosition = ref<'before' | 'after' | 'inside' | null>(null)

const PERM_ADD = MaterialPermissionCode.MATERIAL_CATALOG_CREATE
const PERM_EDIT = MaterialPermissionCode.MATERIAL_CATALOG_UPDATE
const PERM_DELETE = MaterialPermissionCode.MATERIAL_CATALOG_DELETE

function buildTree(nodes: MaterialCatalogNode[], level = 0): CatalogTreeNode[] {
  return nodes.map((n) => ({
    key: n.id,
    id: n.id,
    parentId: n.parentId,
    title: n.name,
    name: n.name,
    level,
    virtualRoot: !!n.virtualRoot,
    sortNum: n.sortNum,
    children: n.children?.length ? buildTree(n.children, level + 1) : undefined
  }))
}

function filterTreeByKeyword(nodes: CatalogTreeNode[], keyword: string): CatalogTreeNode[] {
  const normalized = keyword.trim().toLowerCase()
  if (!normalized) {
    return nodes
  }
  const walk = (arr: CatalogTreeNode[]): CatalogTreeNode[] =>
    arr.reduce<CatalogTreeNode[]>((result, node) => {
      const filteredChildren = node.children?.length ? walk(node.children) : undefined
      const matched = node.name.toLowerCase().includes(normalized)
      if (matched || (filteredChildren && filteredChildren.length > 0)) {
        result.push({
          ...node,
          children: filteredChildren
        })
      }
      return result
    }, [])
  return walk(nodes)
}

async function load() {
  loading.value = true
  try {
    const nodes = await queryCatalogTree()
    rawTree.value = nodes
    treeData.value = buildTree(nodes)
    if (!selectedCatalogId.value && nodes.length > 0) {
      selectedCatalogId.value = nodes[0].id
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '加载栏目树失败')
  } finally {
    loading.value = false
  }
}

function flatten(nodes: MaterialCatalogNode[]): MaterialCatalogNode[] {
  const result: MaterialCatalogNode[] = []
  const walk = (arr: MaterialCatalogNode[]) => {
    arr.forEach((n) => {
      result.push(n)
      if (n.children?.length) {
        walk(n.children)
      }
    })
  }
  walk(nodes)
  return result
}

const selectedNode = computed(() => {
  if (!selectedCatalogId.value) {
    return undefined
  }
  return flatten(rawTree.value).find((n) => n.id === selectedCatalogId.value)
})

watch(
  [selectedCatalogId, rawTree],
  () => {
    const id = selectedCatalogId.value
    const node = id ? flatten(rawTree.value).find((n) => n.id === id) : undefined
    emit('catalog-select', node)
  },
  { immediate: true }
)

const displayTreeData = computed(() => filterTreeByKeyword(treeData.value, searchKeyword.value))
const hasSearchResult = computed(() => displayTreeData.value.length > 0)

function hasNodePermission(code: string): boolean {
  return !!selectedNode.value?.permissionCodes?.includes(code)
}

const canCreate = computed(() => hasNodePermission(PERM_ADD))

function openCreateModal() {
  if (!selectedNode.value) {
    message.warning('请先选择父栏目')
    return
  }
  if (!canCreate.value) {
    message.warning('当前栏目无新增权限')
    return
  }
  createName.value = ''
  createModalVisible.value = true
}

function hasPermissionOnNode(nodeId: string, code: MaterialPermissionCodeValue): boolean {
  const node = flatten(rawTree.value).find((n) => n.id === nodeId)
  return catalogNodeHasPermission(node?.permissionCodes, code)
}

function openNodeDetail(nodeId: string) {
  detailNode.value = flatten(rawTree.value).find((n) => n.id === nodeId)
  detailModalVisible.value = true
}

function openRenameModal(nodeId: string) {
  const node = flatten(rawTree.value).find((n) => n.id === nodeId)
  if (!node) {
    message.warning('栏目不存在')
    return
  }
  renameCatalogId.value = nodeId
  renameName.value = node.name
  renameModalVisible.value = true
}

function openDeleteModal(nodeId: string) {
  const node = flatten(rawTree.value).find((n) => n.id === nodeId)
  if (!node) {
    message.warning('栏目不存在')
    return
  }
  deleteCatalogId.value = nodeId
  deleteModalVisible.value = true
}

function getSubtreeIdSet(rootId: string, nodes: MaterialCatalogNode[]): Set<string> {
  const ids = new Set<string>()
  // 非空判断
  if (!rootId || !nodes || !Array.isArray(nodes)) {
    return ids
  }
  const root = findNodeDeep(nodes, rootId)
  if (!root) {
    return ids
  }
  const walk = (n: MaterialCatalogNode) => {
    if (n && n.id) {
      ids.add(n.id)
    }
    n.children?.forEach(walk)
  }
  walk(root)
  return ids
}

function findNodeDeep(nodes: MaterialCatalogNode[], id: string): MaterialCatalogNode | undefined {
  for (const n of nodes) {
    if (n.id === id) {
      return n
    }
    const found = n.children?.length ? findNodeDeep(n.children, id) : undefined
    if (found) {
      return found
    }
  }
  return undefined
}

function mapToMoveParentTreeData(
  nodes: MaterialCatalogNode[],
  exclude: Set<string>
): NonNullable<TreeSelectProps['treeData']> {
  const out: NonNullable<TreeSelectProps['treeData']> = []
  for (const n of nodes) {
    if (exclude.has(n.id)) {
      continue
    }
    out.push({
      value: n.id,
      title: n.virtualRoot ? `${n.name}（虚拟根）` : n.name,
      children: n.children?.length ? mapToMoveParentTreeData(n.children, exclude) : undefined
    })
  }
  return out
}

const moveParentTreeData = computed(() => {
  if (!moveCatalogId.value) {
    return []
  }
  const exclude = getSubtreeIdSet(moveCatalogId.value, rawTree.value)
  const children = mapToMoveParentTreeData(rawTree.value, exclude)
  return [
    {
      value: '0',
      title: '根目录（顶层）',
      children: children.length ? children : undefined
    }
  ] as NonNullable<TreeSelectProps['treeData']>
})

function onNodeDragStart(e: DragEvent, node: CatalogTreeNode) {
  if (node.virtualRoot) {
    e.preventDefault()
    return
  }
  draggingNode.value = node
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
    e.dataTransfer.setData('text/plain', node.id)
  }
}

function clearDropHint() {
  dropTargetKey.value = null
  dropPosition.value = null
}

function onNodeDragEnd() {
  draggingNode.value = null
  clearDropHint()
}

function onNodeDragOver(e: DragEvent, node: CatalogTreeNode) {
  if (!draggingNode.value || node.virtualRoot) {
    return
  }
  e.preventDefault()
  e.stopPropagation()
  if (e.dataTransfer) {
    e.dataTransfer.dropEffect = 'move'
  }
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
  const y = e.clientY - rect.top
  const h = rect.height || 1
  let pos: 'before' | 'after' | 'inside'
  if (y < h * 0.25) {
    pos = 'before'
  } else if (y > h * 0.75) {
    pos = 'after'
  } else {
    pos = 'inside'
  }
  dropTargetKey.value = String(node.key)
  dropPosition.value = pos
}

function onNodeDragLeave(e: DragEvent, node: CatalogTreeNode) {
  const el = e.currentTarget as HTMLElement
  const to = e.relatedTarget as Node | null
  if (to && el.contains(to)) {
    return
  }
  if (dropTargetKey.value === String(node.key)) {
    clearDropHint()
  }
}

async function onNodeDrop(e: DragEvent, targetNode: CatalogTreeNode) {
  e.preventDefault()
  e.stopPropagation()
  const hintPos = dropPosition.value
  clearDropHint()

  if (targetNode.virtualRoot) {
    message.warning('不能移动到虚拟根')
    draggingNode.value = null
    return
  }
  if (!draggingNode.value) {
    return
  }
  if (draggingNode.value.id === targetNode.id) {
    return
  }

  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
  const y = e.clientY - rect.top
  const height = rect.height || 1
  const position: 'before' | 'after' | 'inside' =
    hintPos ??
    (y < height * 0.25 ? 'before' : y > height * 0.75 ? 'after' : 'inside')

  const dragId = draggingNode.value.id
  const targetId = targetNode.id

  const subtreeIds = getSubtreeIdSet(dragId, rawTree.value)
  if (subtreeIds.has(targetId) && position === 'inside') {
    message.warning('不能移动到自己的子栏目下')
    draggingNode.value = null
    return
  }

  try {
    const pos: CatalogMovePosition =
      position === 'before' ? 'BEFORE' : position === 'after' ? 'AFTER' : 'INSIDE'
    await updateCatalog({
      id: dragId,
      targetId,
      position: pos
    })
    message.success('栏目移动成功')
    await load()
    selectedCatalogId.value = dragId
  } catch (err: unknown) {
    const errEx = err as { response?: { data?: { message?: string } }; message?: string }
    message.error(errEx?.response?.data?.message || errEx?.message || '移动失败')
  } finally {
    draggingNode.value = null
  }
}

function openMoveModal(nodeId: string) {
  const node = flatten(rawTree.value).find((n) => n.id === nodeId)
  if (!node) {
    message.warning('栏目不存在')
    return
  }
  moveCatalogId.value = nodeId
  moveTargetParentId.value = node.parentId && node.parentId !== '' ? node.parentId : '0'
  moveModalVisible.value = true
}



function handleNodeMenuClick(action: string, nodeId: string) {
  actionMenuNodeKey.value = undefined
  selectedCatalogId.value = nodeId
  if (action === 'createChild') {
    openCreateModal()
    return
  }
  if (action === 'detail') {
    openNodeDetail(nodeId)
    return
  }
  if (action === 'rename') {
    openRenameModal(nodeId)
    return
  }
  if (action === 'move') {
    openMoveModal(nodeId)
    return
  }
  if (action === 'sort') {
    openSortModal(nodeId)
    return
  }
  if (action === 'setting') {
    message.info('栏目设置功能开发中')
    return
  }
  if (action === 'delete') {
    openDeleteModal(nodeId)
  }
}

function toggleNodeActionMenu(nodeId: string) {
  actionMenuNodeKey.value = actionMenuNodeKey.value === nodeId ? undefined : nodeId
}

function closeActionMenuOnOutsideClick() {
  actionMenuNodeKey.value = undefined
}

/** 点击侧栏「素材」标题区：取消当前栏目选中，便于回到未选栏目的列表态 */
function clearCatalogSelection() {
  selectedCatalogId.value = undefined
}

async function submitCreate() {
  const parentId = selectedCatalogId.value
  const name = createName.value.trim()
  if (!parentId || !name) {
    message.warning('请输入栏目名称')
    return
  }
  creating.value = true
  try {
    await createCatalog({ name, parentId })
    message.success('创建成功')
    createModalVisible.value = false
    await load()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '创建栏目失败')
  } finally {
    creating.value = false
  }
}

async function submitRename() {
  const catalogId = renameCatalogId.value
  const name = renameName.value.trim()
  if (!catalogId || !name) {
    message.warning('请输入栏目名称')
    return
  }
  renameLoading.value = true
  try {
    await updateCatalog({ id: catalogId, name })
    message.success('重命名成功')
    renameModalVisible.value = false
    await load()
    selectedCatalogId.value = catalogId
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '重命名失败')
  } finally {
    renameLoading.value = false
  }
}

async function submitMove() {
  const id = moveCatalogId.value
  const parentId = moveTargetParentId.value
  if (!id || parentId === undefined || parentId === null || parentId === '') {
    message.warning('请选择目标父栏目')
    return
  }
  moveLoading.value = true
  try {
    await updateCatalog({ id, targetId: parentId, position: 'INSIDE' })
    message.success('移动成功')
    moveModalVisible.value = false
    await load()
    selectedCatalogId.value = id
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '移动失败')
  } finally {
    moveLoading.value = false
  }
}



async function submitDelete() {
  const catalogId = deleteCatalogId.value
  if (!catalogId) {
    return
  }
  deleting.value = true
  try {
    await deleteCatalog(catalogId)
    message.success('删除成功')
    deleteModalVisible.value = false
    if (selectedCatalogId.value === catalogId) {
      selectedCatalogId.value = undefined
    }
    await load()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '删除失败')
  } finally {
    deleting.value = false
  }
}

onMounted(() => {
  load()
  document.addEventListener('click', closeActionMenuOnOutsideClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeActionMenuOnOutsideClick)
})

defineExpose({
  load,
  loading
})
</script>

<template>
  <div class="material-catalog-tree-panel" :class="{ 'is-catalog-dragging': !!draggingNode }">
    <div class="catalog-tree-search">
      <a-input
        v-model:value="searchKeyword"
        allow-clear
        placeholder="搜索栏目"
        class="catalog-tree-search-input"
      />
      <a-button type="primary" class="catalog-tree-search-btn">筛选</a-button>
    </div>

    <div class="catalog-tree-panel">
      <div class="catalog-tree-panel-header" @click="clearCatalogSelection">
        <div class="catalog-tree-panel-title">
          <appstore-outlined />
          <span>素材</span>
        </div>
        <a-button
          class="catalog-tree-panel-add"
          type="text"
          size="small"
          :disabled="!canCreate"
          @click.stop="openCreateModal"
        >
          <template #icon>
            <plus-outlined />
          </template>
        </a-button>
      </div>

      <a-spin class="catalog-tree-spin" :spinning="loading">
        <a-tree
      v-if="treeData?.length && hasSearchResult"
      class="catalog-tree-widget"
      :tree-data="displayTreeData"
      default-expand-all
      block-node
      :selected-keys="selectedCatalogId ? [selectedCatalogId] : []"
      @select="(keys) => { selectedCatalogId = (keys?.[0] as string) || undefined }"
    >
          <template #switcherIcon="{ expanded }">
            <caret-down-filled v-if="expanded" class="catalog-tree-switcher" />
            <caret-right-filled v-else class="catalog-tree-switcher" />
          </template>
          <template #title="{ dataRef }">
            <div
              class="catalog-tree-node"
              :class="{
                'catalog-tree-drop-before':
                  dropTargetKey === String(dataRef?.key) && dropPosition === 'before',
                'catalog-tree-drop-after':
                  dropTargetKey === String(dataRef?.key) && dropPosition === 'after',
                'catalog-tree-drop-inside':
                  dropTargetKey === String(dataRef?.key) && dropPosition === 'inside'
              }"
              draggable="true"
              @dragstart="(e) => onNodeDragStart(e, rowFromDataRef(dataRef))"
              @dragend="onNodeDragEnd"
              @dragover="(e) => onNodeDragOver(e, rowFromDataRef(dataRef))"
              @dragleave="(e) => onNodeDragLeave(e, rowFromDataRef(dataRef))"
              @drop="(e) => onNodeDrop(e, rowFromDataRef(dataRef))"
            >
              <folder-filled class="catalog-tree-icon" />
              <span class="catalog-tree-name">{{ dataRef?.name || dataRef?.title || "-" }}</span>
              <span v-if="dataRef?.virtualRoot" class="catalog-tree-tag">虚拟根</span>
              <a-button
                class="catalog-tree-node-more"
                type="text"
                size="small"
                @click.stop="toggleNodeActionMenu((dataRef?.key as string) || '')"
              >
                ...
              </a-button>
              <div
                v-if="actionMenuNodeKey === ((dataRef?.key as string) || '')"
                class="catalog-tree-action-menu"
                @click.stop
              >
                <button class="catalog-tree-action-item" @click="handleNodeMenuClick('detail', ((dataRef?.key as string) || ''))">栏目详情</button>
                <button
                  class="catalog-tree-action-item"
                  :disabled="!hasPermissionOnNode(((dataRef?.key as string) || ''), PERM_EDIT)"
                  @click="handleNodeMenuClick('rename', ((dataRef?.key as string) || ''))"
                >
                  重命名
                </button>

                
                <button
                  class="catalog-tree-action-item"
                  :disabled="!hasPermissionOnNode(((dataRef?.key as string) || ''), PERM_ADD)"
                  @click="handleNodeMenuClick('createChild', ((dataRef?.key as string) || ''))"
                >
                  添加子栏目
                </button>
                <button class="catalog-tree-action-item" @click="handleNodeMenuClick('setting', ((dataRef?.key as string) || ''))">设置</button>
                <button
                  class="catalog-tree-action-item danger"
                  :disabled="!hasPermissionOnNode(((dataRef?.key as string) || ''), PERM_DELETE)"
                  @click="handleNodeMenuClick('delete', ((dataRef?.key as string) || ''))"
                >
                  删除
                </button>
              </div>
            </div>
          </template>
        </a-tree>
        <a-empty v-else-if="treeData?.length" description="未搜索到匹配栏目" />
        <a-empty v-else description="无数据或加载中" />
      </a-spin>
    </div>
  </div>

  <a-modal
    v-model:open="createModalVisible"
    title="新建子栏目"
    :confirm-loading="creating"
    @ok="submitCreate"
  >
    <a-form layout="vertical">
      <a-form-item label="父栏目">
        <a-input :value="selectedNode?.name || selectedCatalogId || ''" disabled />
      </a-form-item>
      <a-form-item label="栏目名称" required>
        <a-input v-model:value="createName" placeholder="请输入栏目名称" maxlength="50" />
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal
    v-model:open="detailModalVisible"
    title="栏目详情"
    :footer="null"
  >
    <a-descriptions v-if="detailNode" :column="1" bordered size="small">
      <a-descriptions-item label="栏目ID">{{ detailNode.id }}</a-descriptions-item>
      <a-descriptions-item label="栏目名称">{{ detailNode.name }}</a-descriptions-item>
      <a-descriptions-item label="父栏目ID">{{ detailNode.parentId || '-' }}</a-descriptions-item>
      <a-descriptions-item label="节点类型">{{ detailNode.virtualRoot ? '虚拟根' : '普通栏目' }}</a-descriptions-item>
      <a-descriptions-item label="权限码">
        <a-space wrap>
          <a-tag v-if="!detailNode.permissionCodes?.length">无</a-tag>
          <a-tag v-for="code in detailNode.permissionCodes || []" :key="code" color="processing">{{ code }}</a-tag>
        </a-space>
      </a-descriptions-item>
    </a-descriptions>
    <a-empty v-else description="未选择栏目" />
  </a-modal>

  <a-modal
    v-model:open="renameModalVisible"
    title="重命名栏目"
    :confirm-loading="renameLoading"
    @ok="submitRename"
  >
    <a-form layout="vertical">
      <a-form-item label="新名称" required>
        <a-input v-model:value="renameName" placeholder="请输入栏目新名称" maxlength="50" />
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal
    v-model:open="moveModalVisible"
    title="移动栏目"
    :confirm-loading="moveLoading"
    ok-text="确定"
    @ok="submitMove"
  >
    <a-form layout="vertical">
      <a-form-item label="目标父栏目" required>
        <a-tree-select
          v-model:value="moveTargetParentId"
          class="catalog-move-tree-select"
          show-search
          :dropdown-style="{ maxHeight: '360px', overflow: 'auto' }"
          placeholder="请选择要移动到的父栏目"
          allow-clear
          tree-default-expand-all
          :tree-data="moveParentTreeData"
          tree-node-filter-prop="title"
        />
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal
    v-model:open="deleteModalVisible"
    title="删除栏目"
    :confirm-loading="deleting"
    ok-type="danger"
    ok-text="确认删除"
    cancel-text="取消"
    @ok="submitDelete"
  >
    <p>删除后不可恢复，确定要删除该栏目吗？</p>
  </a-modal>

  
</template>

<style scoped lang="less">
.material-catalog-tree-panel {
  width: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

:deep(.ant-tree-node-content-wrapper) {
  position: relative;
}

/* 自定义 HTML5 拖拽落点（上 / 下 / 内） */
.is-catalog-dragging .catalog-tree-node-more {
  pointer-events: none;
}

.catalog-tree-node.catalog-tree-drop-before::before {
  content: '';
  position: absolute;
  top: -1px;
  left: 0;
  right: 0;
  height: 3px;
  background: #1677ff;
  border-radius: 2px;
  z-index: 5;
  pointer-events: none;
}

.catalog-tree-node.catalog-tree-drop-after::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 3px;
  background: #1677ff;
  border-radius: 2px;
  z-index: 5;
  pointer-events: none;
}

.catalog-tree-node.catalog-tree-drop-inside {
  background: rgba(22, 119, 255, 0.14) !important;
  outline: 2px solid #1677ff;
  outline-offset: -1px;
  border-radius: 8px;
}

/* 与素材主区同系：白底、蓝强调、无大红/大粉，避免与右侧「工作台」像两个产品 */
.catalog-tree-search {
  display: flex;
  flex-wrap: nowrap;
  align-items: stretch;
  gap: 0;
  margin-bottom: 12px;
  min-height: 40px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--mam-border, #e2e8f0);
  background: var(--mam-surface, #fff);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.05);
}

.catalog-tree-search-input {
  flex: 1;
  min-width: 0;
}

.catalog-tree-search-input :deep(.ant-input) {
  height: 40px;
  border: none !important;
  box-shadow: none !important;
  border-radius: 0 !important;
  background: var(--mam-surface, #fff) !important;
}

.catalog-tree-search-btn {
  min-width: 72px;
  height: 40px;
  font-weight: 500;
  border-radius: 0 9px 9px 0 !important;
  flex-shrink: 0;
}

.catalog-tree-panel {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-radius: var(--mam-radius-lg, 12px);
  background: var(--mam-surface, #fff);
  border: 1px solid var(--mam-border, #e2e8f0);
  box-shadow: var(--mam-shadow-sm, 0 1px 3px rgba(15, 23, 42, 0.06));
  padding: 8px;
  overflow: hidden;
}

.catalog-tree-spin {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.catalog-tree-spin :deep(.ant-spin-nested-loading),
.catalog-tree-spin :deep(.ant-spin-container) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.catalog-tree-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 10px;
  border-radius: 10px;
  background: var(--mam-primary-bg, #e6f4ff);
  border: 1px solid rgba(22, 119, 255, 0.12);
  margin-bottom: 8px;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.2s, border-color 0.2s;
}

.catalog-tree-panel-header:hover {
  background: #dbeafe;
  border-color: rgba(22, 119, 255, 0.2);
}

.catalog-tree-panel-title {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  color: var(--mam-text, #0f172a);
  font-weight: 600;
  font-size: 13px;
}

.catalog-tree-panel-title :deep(.anticon) {
  color: var(--mam-primary, #1677ff);
  font-size: 15px;
}

.catalog-tree-panel-add {
  border-radius: 8px;
  color: var(--mam-primary, #1677ff) !important;
  background: rgba(22, 119, 255, 0.1) !important;
}

.catalog-tree-panel-add:hover:not(:disabled) {
  color: var(--mam-primary-hover, #4096ff) !important;
  background: rgba(22, 119, 255, 0.16) !important;
}

.catalog-tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 26px;
  width: 100%;
  line-height: 22px;
  white-space: nowrap;
  position: relative;
  overflow: visible;
  padding-right: 28px;
}
/* 让图标和文字不阻挡拖拽事件 */
.catalog-tree-icon,
.catalog-tree-name {
  pointer-events: none;
}

.catalog-tree-node-more {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  margin-left: 0;
  flex-shrink: 0;
  z-index: 2;
  display: none;
  font-weight: 700;
  color: #6b7280;
}

.catalog-tree-node:hover .catalog-tree-node-more,
.catalog-tree-node-more:hover,
.catalog-tree-node-more:focus-visible {
  display: inline-flex;
}

.catalog-tree-widget :deep(.ant-tree-node-selected .catalog-tree-node-more) {
  display: inline-flex;
}

.catalog-tree-action-menu {
  position: absolute;
  top: 24px;
  right: 6px;
  z-index: 30;
  min-width: 132px;
  padding: 6px 0;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.16);
}

.catalog-tree-action-item {
  display: block;
  width: 100%;
  padding: 8px 12px;
  border: none;
  background: transparent;
  text-align: left;
  color: #4b5563;
  font-size: 13px;
  cursor: pointer;
}

.catalog-tree-action-item:hover {
  background: #f9fafb;
}

.catalog-tree-action-item:disabled {
  color: #c4c4c4;
  cursor: not-allowed;
  background: transparent;
}

.catalog-tree-action-item.danger {
  color: #ef4444;
}

.catalog-move-tree-select {
  width: 100%;
}

.catalog-tree-name {
  display: inline-flex;
  align-items: center;
  color: var(--mam-text, #0f172a);
  line-height: 22px;
  font-size: 14px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

.catalog-tree-tag {
  padding: 0 6px;
  border-radius: 10px;
  background: #f6ffed;
  color: #389e0d;
  font-size: 12px;
  line-height: 20px;
}

.catalog-tree-switcher {
  color: #667085;
  font-size: 10px;
}

.catalog-tree-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  color: var(--mam-primary, #1677ff);
  font-size: 14px;
  line-height: 1;
  opacity: 0.9;
}

.catalog-tree-widget {
  flex: 1;
  min-height: 0;
  overflow: auto;
  background: transparent;
  padding: 2px 0;
}

.catalog-tree-widget :deep(.ant-tree-list-holder-inner) {
  width: 100%;
}

.catalog-tree-widget :deep(.ant-tree-node-content-wrapper) {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  width: 100% !important;
  box-sizing: border-box;
  border-radius: 10px;
  padding: 3px 10px 3px 8px;
  margin: 2px 0;
  transition: background 0.2s;
}

.catalog-tree-widget :deep(.ant-tree-title) {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  width: 100%;
}

.catalog-tree-widget :deep(.ant-tree-node-content-wrapper:hover) {
  background: var(--mam-primary-bg, #e6f4ff);
}

.catalog-tree-widget :deep(.ant-tree-node-selected),
.catalog-tree-widget :deep(.ant-tree-node-selected:hover) {
  background: var(--mam-primary-bg, #e6f4ff);
  box-shadow: inset 0 0 0 1px rgba(22, 119, 255, 0.12);
}

.catalog-tree-widget :deep(.ant-tree-indent-unit) {
  width: 20px;
}

.catalog-tree-widget :deep(.ant-tree-treenode) {
  display: flex;
  align-items: center;
  width: 100% !important;
  min-width: 0;
  width: 100%;
}

.catalog-tree-widget :deep(.ant-tree-list-holder-inner > .ant-tree-treenode) {
  width: 100% !important;
}

.catalog-tree-widget :deep(.ant-tree-switcher),
.catalog-tree-widget :deep(.ant-tree-iconEle) {
  display: flex;
  align-items: center;
  justify-content: center;
  align-self: center;
  line-height: 1;
}
</style>
