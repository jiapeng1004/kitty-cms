<template>
  <div class="page-role-list list-page">
    <div class="page-header">
      <h1 class="page-title">角色管理</h1>
      <p class="page-desc">管理系统角色，并为角色开通菜单及关联权限</p>
    </div>

    <a-card class="content-card" :bordered="false" title="角色列表">
      <a-table
          :columns="columns"
          :data-source="dataSource"
          :loading="loading"
          row-key="id"
          :pagination="false"
          size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" size="small" @click="openGrantMenu(record)">
                开通菜单
              </a-button>
            </a-space>
          </template>
        </template>
        <template #emptyText>
          <a-empty :image="false" description="暂无角色数据"/>
        </template>
      </a-table>
    </a-card>

    <!-- 为角色开通菜单及权限的弹窗 -->
    <RoleMenuGrantDialog
        v-model:open="grantDialogOpen"
        :role-id="currentRoleId"
        :menu="currentMenu"
        @success="reloadAfterGrant"
    />

    <!-- 简单的菜单选择对话框（从当前导航菜单树中选择一个菜单） -->
    <a-modal
        v-model:open="menuSelectOpen"
        title="选择要为该角色开通的菜单"
        :width="520"
        ok-text="下一步"
        cancel-text="取消"
        @ok="handleMenuSelectOk"
    >
      <p class="desc">当前从左侧导航菜单中读取菜单列表，请选择一个菜单为角色开通。</p>
      <a-select
          v-model:value="selectedMenuKey"
          placeholder="请选择菜单"
          style="width: 100%"
          :options="menuOptions"
      />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref, computed} from 'vue'
import {message} from 'ant-design-vue'
import RoleMenuGrantDialog from '../../components/RoleMenuGrantDialog.vue'
import {getResponseMessage} from '@/utils/api'
import {getAllMenuTree, type MenuTreeItem} from '@/api/menu_api'
import {getRolePage, type RoleItem} from '@/api/role_api'


const dataSource = ref<RoleItem[]>([])
const loading = ref(false)

const columns = [
  {title: '角色名称', dataIndex: 'roleName', key: 'roleName', ellipsis: true},
  {title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180},
  {title: '操作', key: 'action', width: 120}
]

const grantDialogOpen = ref(false)
const currentRoleId = ref<string>('')
const currentMenu = ref<MenuTreeItem | null>(null)

const menuSelectOpen = ref(false)
const selectedMenuKey = ref<string | undefined>(undefined)
const menuTree = ref<MenuTreeItem[]>([])

const menuOptions = computed(() => {
  const flat: { label: string; value: string }[] = []

  function walk(list: MenuTreeItem[]) {
    for (const item of list) {
      flat.push({label: item.menuName, value: item.menuKey})
      if (item.children && item.children.length > 0) {
        walk(item.children)
      }
    }
  }

  walk(menuTree.value)
  return flat
})

async function fetchRoles() {
  loading.value = true
  try {
    const res = await getRolePage()
    dataSource.value = res?.records ?? []
  } catch (error) {
    message.error(getResponseMessage(error))
    dataSource.value = []
  } finally {
    loading.value = false
  }
}

async function fetchMenuTree() {
  try {
    const tree = await getAllMenuTree()
    menuTree.value = Array.isArray(tree) ? tree : []
  } catch (error) {
    message.error(getResponseMessage(error))
  }
}

function openGrantMenu(role: RoleItem | Record<string, any>) {
  const r = role as RoleItem
  currentRoleId.value = r.id
  // 先弹出菜单选择弹窗
  selectedMenuKey.value = undefined
  menuSelectOpen.value = true
}

function findMenuByKey(key: string): MenuTreeItem | null {
  function walk(list: MenuTreeItem[]): MenuTreeItem | null {
    for (const item of list) {
      if (item.menuKey === key) return item
      if (item.children && item.children.length > 0) {
        const found = walk(item.children)
        if (found) return found
      }
    }
    return null
  }

  return walk(menuTree.value)
}

function handleMenuSelectOk() {
  if (!selectedMenuKey.value) {
    message.warning('请选择菜单')
    return
  }
  const menu = findMenuByKey(selectedMenuKey.value)
  if (!menu) {
    message.error('未找到对应菜单')
    return
  }
  currentMenu.value = menu
  menuSelectOpen.value = false
  grantDialogOpen.value = true
}

function reloadAfterGrant() {
  // 目前只需要提示和保留当前列表即可，如后续有「角色-菜单」视图，可以在此处刷新
  // 这里暂不重新加载角色列表（角色本身未发生变化）
}

onMounted(() => {
  fetchRoles()
  fetchMenuTree()
})
</script>

<style scoped>
.page-role-list {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.45);
  margin: 0;
}

.content-card {
  border-radius: var(--admin-radius);
  box-shadow: var(--admin-card-shadow);
}

.desc {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.65);
  margin-bottom: 12px;
}
</style>

