<script setup lang="ts">
import {computed, ref, onMounted, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {message} from 'ant-design-vue'
import {
  catalogTreeWithRolePermission,
  type MaterialCatalogNode,
  upsertCatalogPermission
} from '@/mam/api/mam_catalog_api'
import { CATALOG_DIMENSION_PERMISSION_CODES } from '@/mam/constants/material_permission_code'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const treeData = ref<MaterialCatalogNode[]>([])
const selectedRole = ref('')
const selectedRoleName = ref('')

const checkedCatalogs = ref<Map<string, string[]>>(new Map())

const hasRoleId = computed(() => !!selectedRole.value?.trim())

const displayRoleName = computed(() => {
  return selectedRoleName.value || selectedRole.value
})

const flatRows = computed(() => {
  const rows: Array<MaterialCatalogNode & { level: number; checked: boolean }> = []
  const walk = (list: MaterialCatalogNode[], level: number) => {
    list.forEach((item) => {
      const checkedPerms = checkedCatalogs.value.get(item.id) || item.permissionCodes || []
      rows.push({
        ...item,
        level,
        checked: checkedPerms.length > 0
      })
      if (item.children?.length) walk(item.children, level + 1)
    })
  }
  walk(treeData.value, 0)
  return rows
})

async function load() {
  const roleId = selectedRole.value?.trim()
  if (!roleId) {
    return
  }
  loading.value = true
  try {
    const raw = await catalogTreeWithRolePermission(roleId)
    treeData.value = raw
    checkedCatalogs.value.clear()
    const walk = (list: MaterialCatalogNode[]) => {
      list.forEach(item => {
        checkedCatalogs.value.set(item.id, item.permissionCodes || [])
        if (item.children?.length) walk(item.children)
      })
    }
    walk(treeData.value)
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  const roleId = selectedRole.value?.trim()
  if (!roleId) {
    message.warning('请先选择角色')
    return
  }
  saving.value = true
  try {
    const permissionItems: Array<{ roleId: string; catalogId: string; permissionCodes: string[] }> = []
    checkedCatalogs.value.forEach((permCodes, catalogId) => {
      if (permCodes.length > 0) {
        permissionItems.push({
          roleId,
          catalogId,
          permissionCodes: permCodes
        })
      }
    })

    await upsertCatalogPermission({
      roleIds: [roleId],
      permissionItems
    })
    message.success('保存成功')
    await load()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function toggleCheck(catalogId: string, checked: boolean, currentPerms: string[]) {
  if (checked) {
    const allPerms = getAllPermissionCodes()
    checkedCatalogs.value.set(catalogId, allPerms)
  } else {
    checkedCatalogs.value.set(catalogId, [])
  }
}

function toggleAll(checked: boolean) {
  if (checked) {
    const allPerms = getAllPermissionCodes()
    flatRows.value.forEach(row => {
      checkedCatalogs.value.set(row.id, [...allPerms])
    })
  } else {
    flatRows.value.forEach(row => {
      checkedCatalogs.value.set(row.id, [])
    })
  }
}

/** 全选时授予的权限码集合，与后端 `CatalogPermission` / `MaterialPermissionCode` 一致 */
function getAllPermissionCodes(): string[] {
  return [...CATALOG_DIMENSION_PERMISSION_CODES]
}

const allChecked = computed(() => {
  return flatRows.value.length > 0 && flatRows.value.every(row => {
    const perms = checkedCatalogs.value.get(row.id)
    return perms && perms.length > 0
  })
})

const indeterminate = computed(() => {
  const checkedCount = flatRows.value.filter(row => {
    const perms = checkedCatalogs.value.get(row.id)
    return perms && perms.length > 0
  }).length
  return checkedCount > 0 && checkedCount < flatRows.value.length
})

function selectRole(roleId: string, roleName?: string) {
  selectedRole.value = roleId
  selectedRoleName.value = roleName || ''
  router.replace({
    query: {
      roleId,
      ...(roleName ? {roleName} : {})
    }
  })
}

function loadFromQuery() {
  const roleIdFromQuery = route.query.roleId as string
  const roleNameFromQuery = route.query.roleName as string
  if (roleIdFromQuery) {
    selectedRole.value = roleIdFromQuery
    selectedRoleName.value = roleNameFromQuery || ''
    load()
  }
}

onMounted(() => {
  loadFromQuery()
})

watch(() => route.query, () => {
  loadFromQuery()
}, {deep: true})
</script>

<template>
  <div class="catalog-permission-page">
    <a-card title="栏目权限授权">
      <div v-if="!hasRoleId" class="role-select-section">
        <a-alert message="请选择要授权的角色" type="info" style="margin-bottom: 16px;"/>
        <a-form layout="inline">
          <a-form-item label="角色ID">
            <a-input
                v-model:value="selectedRole"
                style="width: 200px;"
                placeholder="请输入角色ID"
                @pressEnter="selectRole(selectedRole)"
            />
          </a-form-item>
          <a-form-item label="角色名（可选）">
            <a-input
                v-model:value="selectedRoleName"
                style="width: 200px;"
                placeholder="请输入角色名"
                @pressEnter="selectRole(selectedRole, selectedRoleName)"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="selectRole(selectedRole, selectedRoleName)">
              确认选择
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <div v-else>
        <a-form layout="inline" style="margin-bottom: 16px;">
          <a-form-item label="当前角色">
            <a-tag color="blue">{{ displayRoleName }}</a-tag>
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" :loading="loading" @click="load">刷新</a-button>
              <a-button type="primary" :loading="saving" @click="save">保存</a-button>
            </a-space>
          </a-form-item>
        </a-form>

        <a-table
            :data-source="flatRows"
            :pagination="false"
            :loading="loading"
            row-key="id"
            size="small"
            bordered
        >
          <a-table-column width="60px" key="check">
            <template #header>
              <a-checkbox
                  v-model:checked="allChecked"
                  :indeterminate="indeterminate"
                  @change="e => toggleAll(!!e.target.checked)"
              />
            </template>
            <template #default="{ record }">
              <a-checkbox
                  :checked="record.checked"
                  @change="e => toggleCheck(record.id, !!e.target.checked, record.permissionCodes || [])"
              />
            </template>
          </a-table-column>
          <a-table-column title="栏目名称" key="name" width="300px">
            <template #default="{ record }">
              <span :style="{ paddingLeft: `${record.level * 24}px` }">
                {{ record.level > 0 ? '└─ ' : '' }}{{ record.name }}
              </span>
            </template>
          </a-table-column>
          <a-table-column title="栏目ID" data-index="id" key="id" width="200px"/>
          <a-table-column title="拥有权限" key="permissionCodes" width="500px">
            <template #default="{ record }">
              <a-space wrap>
                <a-tag v-for="perm in (checkedCatalogs.get(record.id) || [])" :key="perm" color="green">
                  {{ perm }}
                </a-tag>
                <span v-if="!checkedCatalogs.get(record.id)?.length" style="color: #999;">
                  无权限
                </span>
              </a-space>
            </template>
          </a-table-column>
        </a-table>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.catalog-permission-page {
  padding: 0;
}

.role-select-section {
  padding: 40px 0;
  text-align: center;
}
</style>
