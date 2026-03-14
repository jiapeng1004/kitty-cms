<template>
  <div class="page-tenant-list list-page">
    <div class="page-header">
      <h1 class="page-title">租户管理</h1>
      <p class="page-desc">管理多租户与租户信息</p>
    </div>
    <a-card class="content-card" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="showModal({}, 'create')">
          <template #icon><PlusOutlined /></template>
          新增租户
        </a-button>
      </template>
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        :loading="loading"
        size="middle"
        @change="handleTableChange"
      >
        <template #emptyText>
          <a-empty :image="false" description="暂无租户数据" />
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a @click="showModal(record, 'edit')">编辑</a>
              <a-popconfirm
                title="确定要删除该租户吗?"
                @confirm="handleDelete(record.id)"
              >
                <a>删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleOk"
      @cancel="handleCancel"
      :confirm-loading="confirmLoading"
    >
      <a-form
        :model="formState"
        name="tenantForm"
        autocomplete="off"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item
          label="租户名称"
          name="name"
          :rules="[{ required: true, message: '请输入租户名称!' }]"
        >
          <a-input v-model:value="formState.name" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import {
  getTenantPage,
  createTenant,
  updateTenant,
  deleteTenant
} from '../../api/tenant_api'
import { getResponseMessage } from '../../utils/api'

const dataSource = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const confirmLoading = ref(false)
const modalType = ref('create')

const formState = reactive({
  id: undefined,
  name: ''
})

const modalTitle = computed(() => (modalType.value === 'create' ? '新增租户' : '编辑租户'))

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '租户名称', dataIndex: 'name', key: 'name' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action' }
]

onMounted(() => {
  fetchData()
})

async function fetchData(params = {}) {
  loading.value = true
  try {
    const data = await getTenantPage({
      page: params.page ?? pagination.current,
      size: params.size ?? pagination.pageSize,
      searchKey: params.searchKey
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

function handleTableChange(pager) {
  pagination.current = pager.current
  pagination.pageSize = pager.pageSize
  fetchData({ page: pager.current, size: pager.pageSize })
}

function showModal(record, type) {
  modalType.value = type
  if (type === 'edit') {
    formState.id = record.id
    formState.name = record.name ?? ''
  } else {
    formState.id = undefined
    formState.name = ''
  }
  modalVisible.value = true
}

async function handleOk() {
  confirmLoading.value = true
  try {
    if (modalType.value === 'create') {
      await createTenant({ name: formState.name })
      message.success('创建成功')
    } else {
      await updateTenant(formState.id, { name: formState.name })
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

function handleCancel() {
  modalVisible.value = false
}

async function handleDelete(id) {
  try {
    await deleteTenant(id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error(getResponseMessage(error))
  }
}
</script>

<style scoped>
.page-tenant-list {
  padding: 0;
}
</style>
