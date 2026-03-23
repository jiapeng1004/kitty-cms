<template>
  <div class="page-oauth2-client list-page">
    <div class="page-header">
      <h1 class="page-title">OAuth2 客户端管理</h1>
      <p class="page-desc">管理系统内 OAuth2 客户端配置</p>
    </div>

    <a-card class="filter-card" :bordered="false">
      <a-space wrap>
        <a-input-search
          v-model:value="searchKey"
          placeholder="搜索客户端名称或 clientId"
          allow-clear
          style="width: 280px"
          @search="onSearch"
        />
        <a-button type="primary" @click="onSearch">
          查询
        </a-button>
        <a-button @click="resetSearch">重置</a-button>
      </a-space>
    </a-card>

    <a-card class="content-card" :bordered="false">
      <template #title>客户端列表</template>
      <template #extra>
        <a-button v-if="canCreate" type="primary" @click="showModal({}, 'create')">
          新增客户端
        </a-button>
      </template>
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        size="middle"
        @change="handleTableChange"
      >
        <template #emptyText>
          <a-empty :image="false" description="暂无客户端数据" />
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'default'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'clientId'">
            <span
              class="copyable-cell"
              :title="'点击复制: ' + (record.clientId ?? '')"
              @click="copyToClipboard(record.clientId)"
            >
              {{ record.clientId ?? '—' }}
            </span>
          </template>
          <template v-else-if="column.key === 'clientSecret'">
            <a-space :size="8" class="secret-cell-wrap">
              <span
                class="copyable-cell secret-cell"
                :title="'点击复制: ' + (record.clientSecret ?? '')"
                @click="copyToClipboard(record.clientSecret)"
              >
                {{ secretVisible[record.id] ? (record.clientSecret ?? '—') : maskClientSecret(record.clientSecret) }}
              </span>
              <a-button type="text" size="small" class="secret-eye-btn" @click.stop="toggleClientSecret(record.id)">
                <EyeOutlined v-if="!secretVisible[record.id]" />
                <EyeInvisibleOutlined v-else />
              </a-button>
            </a-space>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a v-if="canView" @click="showModal(record, 'view')">查看详情</a>
              <a v-if="canUpdate" @click="showModal(record, 'edit')">编辑</a>
              <a-popconfirm v-if="canDelete" title="确定删除该客户端吗?" @confirm="handleDelete(record.id)">
                <a>删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      :title="modalType === 'create' ? '新增客户端' : modalType === 'view' ? '客户端详情' : '编辑客户端'"
      :confirm-loading="confirmLoading"
      @ok="handleOkProxy"
      @cancel="modalVisible = false"
    >
      <a-form :model="formState" :label-col="{ span: 7 }" :wrapper-col="{ span: 17 }">
        <a-form-item label="客户端名称">
          <a-input v-model:value="formState.clientName" :disabled="modalType === 'view'" />
        </a-form-item>
        <a-form-item v-if="modalType === 'edit' || modalType === 'view'" label="客户端ID">
          <a-input v-model:value="formState.clientId" :disabled="true" />
        </a-form-item>
        <a-form-item v-if="modalType === 'edit'" label="客户端密钥">
          <a-input v-model:value="formState.clientSecret" />
        </a-form-item>
        <a-form-item v-else-if="modalType === 'view'" label="客户端密钥">
          <a-space :size="8" align="center">
            <a-input
              :value="secretVisible[formState.id] ? (formState.clientSecret ?? '—') : maskClientSecret(formState.clientSecret)"
              disabled
            />
            <a-button type="text" size="small" @click="toggleClientSecret(formState.id)">
              <EyeOutlined v-if="!secretVisible[formState.id]" />
              <EyeInvisibleOutlined v-else />
            </a-button>
          </a-space>
        </a-form-item>
        <a-form-item label="Scopes">
          <a-select
            v-model:value="selectedScopes"
            mode="multiple"
            allow-clear
            style="width: 100%"
            placeholder="请选择 scope"
            :options="scopeOptions"
            :disabled="modalType === 'view'"
          />
        </a-form-item>
        <a-form-item label="授权类型">
          <a-select
            v-model:value="selectedGrantTypes"
            mode="multiple"
            allow-clear
            style="width: 100%"
            placeholder="请选择授权类型"
            :options="grantTypeOptions"
            :disabled="modalType === 'view'"
          />
        </a-form-item>
        <a-form-item label="认证方式">
          <a-select
            v-model:value="selectedAuthMethods"
            mode="multiple"
            allow-clear
            style="width: 100%"
            placeholder="请选择令牌端点认证方式"
            :options="authMethodOptions"
            :disabled="modalType === 'view'"
          />
        </a-form-item>
        <a-form-item label="回调地址">
          <a-textarea
            v-model:value="formState.allowedRedirectUris"
            :rows="2"
            :disabled="modalType === 'view'"
          />
        </a-form-item>
        <a-form-item label="访问令牌秒数">
          <a-input-number
            v-model:value="formState.accessTokenTimeout"
            :min="0"
            style="width: 100%"
            :disabled="modalType === 'view'"
          />
        </a-form-item>
        <a-form-item label="刷新令牌秒数">
          <a-input-number
            v-model:value="formState.refreshTokenTimeout"
            :min="0"
            style="width: 100%"
            :disabled="modalType === 'view'"
          />
        </a-form-item>
        <a-form-item label="黑名单豁免">
          <a-input v-model:value="formState.blackListExemption" :disabled="modalType === 'view'" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formState.status" :disabled="modalType === 'view'">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="授权确认">
          <a-select v-model:value="formState.requireAuthorizationConsent" :disabled="modalType === 'view'">
            <a-select-option :value="1">需要</a-select-option>
            <a-select-option :value="0">不需要</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { EyeInvisibleOutlined, EyeOutlined } from '@ant-design/icons-vue'
import {
  createOauth2Client,
  deleteOauth2Client,
  getOauth2ClientById,
  getOauth2ClientPage,
  updateOauth2Client
} from '@/api/oauth2_client_api'
import {
  getOpenOauth2ClientAuthenticationMethodList,
  getOpenOauth2GrantTypeList
} from '@/api/oauth2_meta_api'
import { getOauth2ScopeList } from '@/api/oauth2_scope_api'
import { getCurrentUserPermissionList } from '@/api/permission_api'
import { getResponseMessage } from '@/utils/api'

const dataSource = ref<any[]>([])
const loading = ref(false)
const modalVisible = ref(false)
const confirmLoading = ref(false)
const modalType = ref<'create' | 'edit' | 'view'>('create')
const searchKey = ref('')
const selectedScopes = ref<string[]>([])
const selectedGrantTypes = ref<string[]>([])
const selectedAuthMethods = ref<string[]>([])
const scopeOptions = ref<{ label: string; value: string }[]>([])
const grantTypeOptions = ref<{ label: string; value: string }[]>([])
const authMethodOptions = ref<{ label: string; value: string }[]>([])
const permissionCodes = ref<string[]>([])
const secretVisible = reactive<Record<string, boolean>>({})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const formState = reactive({
  id: '',
  clientName: '',
  clientId: '',
  clientSecret: '',
  allowedScopes: '',
  allowedGrantTypes: '',
  allowAuthenticationMethods: '',
  allowedRedirectUris: '',
  accessTokenTimeout: 7200,
  refreshTokenTimeout: 2592000,
  blackListExemption: '',
  status: 1,
  requireAuthorizationConsent: 0
})

const canCreate = computed(() => permissionCodes.value.includes('oauth2:client:create'))
const canUpdate = computed(() => permissionCodes.value.includes('oauth2:client:update'))
const canDelete = computed(() => permissionCodes.value.includes('oauth2:client:delete'))
const canView = computed(() => permissionCodes.value.includes('oauth2:client:view'))
const showActionColumn = computed(() => canView.value || canUpdate.value || canDelete.value)

const columns = computed(() => {
  const base = [
  { title: '客户端名称', dataIndex: 'clientName', key: 'clientName' },
  { title: 'clientId', dataIndex: 'clientId', key: 'clientId' },
    { title: 'clientSecret', dataIndex: 'clientSecret', key: 'clientSecret', ellipsis: true },
  { title: '授权类型', dataIndex: 'allowedGrantTypes', key: 'allowedGrantTypes', ellipsis: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
  ] as Array<Record<string, unknown>>
  if (showActionColumn.value) {
    base.push({ title: '操作', key: 'action', width: 120 })
  }
  return base
})

onMounted(() => {
  fetchData()
  loadScopeOptions()
  loadGrantTypeOptions()
  loadAuthMethodOptions()
  loadPermissionCodes()
})

async function fetchData(params: Record<string, unknown> = {}) {
  loading.value = true
  try {
    const data = await getOauth2ClientPage({
      page: params.page ?? pagination.current,
      size: params.size ?? pagination.pageSize,
      searchKey: params.searchKey ?? (searchKey.value || undefined)
    })
    dataSource.value = data?.records ?? []
    pagination.total = data?.total ?? 0
    if (data?.page != null) pagination.current = data.page
    if (data?.size != null) pagination.pageSize = data.size
  } catch (error) {
    message.error(getResponseMessage(error))
    dataSource.value = []
  } finally {
    loading.value = false
  }
}

async function loadScopeOptions() {
  try {
    const list = await getOauth2ScopeList()
    scopeOptions.value = (list ?? []).map((item) => ({
      label: item.scopeName ? `${item.scopeName} (${item.scopeCode})` : item.scopeCode,
      value: item.scopeCode
    }))
  } catch (error) {
    message.error(getResponseMessage(error))
    scopeOptions.value = []
  }
}

async function loadGrantTypeOptions() {
  try {
    const list = await getOpenOauth2GrantTypeList()
    grantTypeOptions.value = (list ?? []).map((item) => ({
      label: item.desc ? `${item.desc} (${item.code})` : item.code,
      value: item.code
    }))
  } catch (error) {
    message.error(getResponseMessage(error))
    grantTypeOptions.value = []
  }
}

async function loadAuthMethodOptions() {
  try {
    const list = await getOpenOauth2ClientAuthenticationMethodList()
    authMethodOptions.value = (list ?? []).map((item) => ({
      label: item.desc ? `${item.desc} (${item.code})` : item.code,
      value: item.code
    }))
  } catch (error) {
    message.error(getResponseMessage(error))
    authMethodOptions.value = []
  }
}

async function loadPermissionCodes() {
  try {
    const list = await getCurrentUserPermissionList()
    permissionCodes.value = Array.isArray(list) ? list : []
  } catch (error) {
    message.error(getResponseMessage(error))
    permissionCodes.value = []
  }
}

function onSearch() {
  pagination.current = 1
  fetchData()
}

function resetSearch() {
  searchKey.value = ''
  pagination.current = 1
  fetchData()
}

function handleTableChange(pager: any) {
  pagination.current = pager.current
  pagination.pageSize = pager.pageSize
  fetchData({ page: pager.current, size: pager.pageSize })
}

async function copyToClipboard(text: unknown) {
  if (text == null || String(text).trim() === '') return
  try {
    await navigator.clipboard.writeText(String(text))
    message.success('已复制到剪贴板')
  } catch (_) {
    message.error('复制失败')
  }
}

function maskClientSecret(secret: unknown) {
  const s = secret == null ? '' : String(secret)
  if (s.trim() === '') return '—'
  return '*'.repeat(Math.min(s.length, 12))
}

function toggleClientSecret(id: string) {
  if (!id) return
  secretVisible[id] = !secretVisible[id]
}

async function showModal(record: any, type: 'create' | 'edit' | 'view') {
  modalType.value = type
  if (type === 'edit' || type === 'view') {
    try {
      const data = await getOauth2ClientById(record.id)
      Object.assign(formState, data)
      selectedScopes.value = data?.allowedScopes
        ? String(data.allowedScopes).split(',').map((item) => item.trim()).filter(Boolean)
        : []
      selectedGrantTypes.value = data?.allowedGrantTypes
        ? String(data.allowedGrantTypes).split(',').map((item) => item.trim()).filter(Boolean)
        : []
      selectedAuthMethods.value = data?.allowAuthenticationMethods
        ? String(data.allowAuthenticationMethods).split(',').map((item) => item.trim()).filter(Boolean)
        : []
    } catch (error) {
      message.error(getResponseMessage(error))
      return
    }
    // view 模式默认隐藏密钥内容
    if (type === 'view') {
      secretVisible[formState.id] = false
    }
  } else {
    Object.assign(formState, {
      id: '',
      clientName: '',
      clientId: '',
      clientSecret: '',
      allowedScopes: '',
      allowedGrantTypes: '',
      allowAuthenticationMethods: '',
      allowedRedirectUris: '',
      accessTokenTimeout: 7200,
      refreshTokenTimeout: 2592000,
      blackListExemption: '',
      status: 1,
      requireAuthorizationConsent: 0
    })
    selectedScopes.value = []
    selectedGrantTypes.value = []
    selectedAuthMethods.value = []
  }
  modalVisible.value = true
}

async function handleOk() {
  confirmLoading.value = true
  try {
    const payload = {
      clientName: formState.clientName,
      allowedScopes: selectedScopes.value.length ? selectedScopes.value.join(',') : undefined,
      allowedGrantTypes: selectedGrantTypes.value.length ? selectedGrantTypes.value.join(',') : undefined,
      allowAuthenticationMethods: selectedAuthMethods.value.length
        ? selectedAuthMethods.value.join(',')
        : undefined,
      allowedRedirectUris: formState.allowedRedirectUris || undefined,
      accessTokenTimeout: formState.accessTokenTimeout,
      refreshTokenTimeout: formState.refreshTokenTimeout,
      blackListExemption: formState.blackListExemption || undefined,
      status: formState.status,
      requireAuthorizationConsent: formState.requireAuthorizationConsent
    }
    if (modalType.value === 'view') {
      modalVisible.value = false
      return
    }
    if (modalType.value === 'create') {
      const created = await createOauth2Client(payload)
      message.success('创建成功')
      Modal.info({
        title: '客户端已生成',
        content: `clientId: ${created.clientId}\nclientSecret: ${created.clientSecret}`,
        okText: '知道了'
      })
    } else {
      await updateOauth2Client(formState.id, {
        ...payload,
        clientSecret: formState.clientSecret
      })
      message.success('更新成功')
    }
    modalVisible.value = false
    fetchData()
  } catch (error) {
    message.error(getResponseMessage(error))
  } finally {
    confirmLoading.value = false
  }
}

function handleOkProxy() {
  if (modalType.value === 'view') {
    modalVisible.value = false
    return
  }
  return handleOk()
}

async function handleDelete(id: string) {
  try {
    await deleteOauth2Client(id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error(getResponseMessage(error))
  }
}
</script>

<style scoped>
.page-oauth2-client {
  padding: 0;
}

.copyable-cell {
  cursor: pointer;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}
.copyable-cell:hover {
  color: var(--ant-primary-color);
  text-decoration: underline;
}

.secret-cell-wrap {
  width: 100%;
  justify-content: space-between;
}

.secret-cell {
  min-width: 0;
  flex: 1;
}

.secret-eye-btn {
  padding: 0 4px;
  height: 28px;
}
</style>

