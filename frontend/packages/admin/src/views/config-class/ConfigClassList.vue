<template>
  <div class="page-config-class-list list-page">
    <div class="page-header">
      <h1 class="page-title">配置分类</h1>
      <p class="page-desc">管理配置分类，便于配置项归类</p>
    </div>
    <a-card class="content-card" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="showModal({}, 'create')">
          <template #icon>
            <PlusOutlined/>
          </template>
          新增分类
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
          <a-empty :image="false" description="暂无分类数据"/>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a @click="showModal(record, 'edit')">编辑</a>
              <a-popconfirm
                  title="确定要删除这个分类吗?"
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
          name="configClassForm"
          autocomplete="off"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 18 }"
      >
        <a-form-item
            label="分类名称"
            name="className"
            :rules="[{ required: true, message: '请输入分类名称!' }]"
        >
          <a-input v-model:value="formState.className"/>
        </a-form-item>
        <a-form-item label="分类描述" name="classDesc">
          <a-textarea v-model:value="formState.classDesc"/>
        </a-form-item>
        <a-form-item label="所有者" name="owner">
          <a-input v-model:value="formState.owner" placeholder="如 public"/>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, computed} from 'vue'
import {PlusOutlined} from '@ant-design/icons-vue'
import {message} from 'ant-design-vue'
import {
  getConfigClassPage,
  getConfigClassById,
  createConfigClass,
  updateConfigClass,
  removeConfigClass
} from '../../api/config_class_api'
import { getResponseMessage } from '../../utils/api'

const dataSource = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const confirmLoading = ref(false)
const modalType = ref('create')

const formState = reactive({
  id: undefined,
  className: '',
  classDesc: '',
  owner: ''
})

const modalTitle = computed(() => (modalType.value === 'create' ? '新增分类' : '编辑分类'))

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  {title: '分类名称', dataIndex: 'className', key: 'className'},
  {title: '分类描述', dataIndex: 'classDesc', key: 'classDesc'},
  {title: '所有者', dataIndex: 'owner', key: 'owner'},
  {title: '创建时间', dataIndex: 'createTime', key: 'createTime'},
  {title: '操作', key: 'action'}
]

onMounted(() => {
  fetchData()
})

async function fetchData(params = {}) {
  loading.value = true
  try {
    const data = await getConfigClassPage({
      page: params.page ?? pagination.current,
      size: params.size ?? pagination.pageSize,
      searchKey: params.searchKey,
      owner: params.owner
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
  fetchData({page: pager.current, size: pager.pageSize})
}

async function showModal(record, type) {
  modalType.value = type
  if (type === 'edit') {
    try {
      const data = await getConfigClassById(record.id)
      Object.assign(formState, data, {id: record.id})
    } catch (e) {
      message.error(getResponseMessage(e))
      return
    }
  } else {
    formState.id = undefined
    formState.className = ''
    formState.classDesc = ''
    formState.owner = ''
  }
  modalVisible.value = true
}

async function handleOk() {
  confirmLoading.value = true
  try {
    if (modalType.value === 'create') {
      await createConfigClass({
        className: formState.className,
        classDesc: formState.classDesc || undefined,
        owner: formState.owner || undefined
      })
      message.success('创建成功')
    } else {
      await updateConfigClass(formState.id, {
        className: formState.className,
        classDesc: formState.classDesc || undefined,
        owner: formState.owner || undefined
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

function handleCancel() {
  modalVisible.value = false
}

async function handleDelete(id) {
  try {
    await removeConfigClass(id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error(getResponseMessage(error))
  }
}
</script>

<style scoped>
.page-config-class-list {
  padding: 0;
}
</style>
