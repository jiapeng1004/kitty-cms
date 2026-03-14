<template>
  <div class="page-user-list list-page">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <p class="page-desc">管理系统用户账号、状态与信息</p>
    </div>

    <a-card class="filter-card" :bordered="false">
      <a-space wrap>
        <a-input-search
          v-model:value="searchKey"
          placeholder="搜索昵称、手机号或邮箱"
          allow-clear
          style="width: 260px"
          @search="onSearch"
        />
        <a-button type="primary" @click="onSearch">
          <template #icon><SearchOutlined /></template>
          查询
        </a-button>
        <a-button @click="resetSearch">重置</a-button>
      </a-space>
    </a-card>

    <a-card class="content-card" :bordered="false">
      <template #title>
        <span>用户列表</span>
      </template>
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        :loading="loading"
        size="middle"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" style="padding: 0" @click="showEditModal(record)">
              编辑
            </a-button>
          </template>
        </template>
        <template #emptyText>
          <a-empty :image="false" description="暂无用户数据" />
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      title="编辑用户"
      :width="520"
      @ok="handleOk"
      @cancel="handleCancel"
      :confirm-loading="confirmLoading"
      ok-text="保存"
      cancel-text="取消"
    >
      <a-form
        :model="formState"
        name="userForm"
        autocomplete="off"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="真实姓名" name="realName">
          <a-input v-model:value="formState.realName" placeholder="请输入" />
        </a-form-item>
        <a-form-item label="昵称" name="nickName">
          <a-input v-model:value="formState.nickName" placeholder="请输入" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="formState.phone" placeholder="请输入" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formState.email" placeholder="请输入" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="formState.status" style="width: 100%" placeholder="请选择">
            <a-select-option :value="0">禁用</a-select-option>
            <a-select-option :value="1">正常</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { getUserPage, getUserById, updateUser } from '../../api/user_api'
import { getResponseMessage } from '../../utils/api'

const dataSource = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const confirmLoading = ref(false)
const searchKey = ref('')

const formState = reactive({
  id: undefined,
  realName: '',
  nickName: '',
  phone: '',
  email: '',
  status: 1
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`
})

const columns = [
  { title: '昵称', dataIndex: 'nickName', key: 'nickName', ellipsis: true },
  { title: '真实姓名', dataIndex: 'realName', key: 'realName', ellipsis: true },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '邮箱', dataIndex: 'email', key: 'email', ellipsis: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 80 }
]

onMounted(() => {
  fetchData()
})

function onSearch() {
  pagination.current = 1
  fetchData({ searchKey: searchKey.value })
}

function resetSearch() {
  searchKey.value = ''
  pagination.current = 1
  fetchData()
}

async function fetchData(params = {}) {
  loading.value = true
  try {
    const data = await getUserPage({
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

function handleTableChange(pager) {
  pagination.current = pager.current
  pagination.pageSize = pager.pageSize
  fetchData({ page: pager.current, size: pager.pageSize })
}

async function showEditModal(record) {
  try {
    const data = await getUserById(record.id)
    formState.id = data.id
    formState.realName = data.realName ?? ''
    formState.nickName = data.nickName ?? ''
    formState.phone = data.phone ?? ''
    formState.email = data.email ?? ''
    formState.status = data.status ?? 1
  } catch (e) {
    message.error(getResponseMessage(e))
    return
  }
  modalVisible.value = true
}

async function handleOk() {
  confirmLoading.value = true
  try {
    await updateUser(formState.id, {
      realName: formState.realName,
      nickName: formState.nickName,
      phone: formState.phone,
      email: formState.email,
      status: formState.status
    })
    message.success('更新成功')
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
</script>

<style scoped>
.page-user-list {
  padding: 0;
}
</style>
