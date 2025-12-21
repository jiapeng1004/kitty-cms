<template>
  <div class="config-list">
    <a-card title="配置管理">
      <template #extra>
        <a-button type="primary" @click="showModal({}, 'create')">
          <template #icon>
            <PlusOutlined />
          </template>
          新增配置
        </a-button>
      </template>
      
      <a-table 
        :columns="columns" 
        :data-source="dataSource" 
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a @click="showModal(record, 'edit')">编辑</a>
              <a-popconfirm
                title="确定要删除这个配置吗?"
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
        name="configForm"
        autocomplete="off"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item
          label="配置键"
          name="configKey"
          :rules="[{ required: true, message: '请输入配置键!' }]"
        >
          <a-input v-model:value="formState.configKey" />
        </a-form-item>
        
        <a-form-item
          label="配置值"
          name="configValue"
          :rules="[{ required: true, message: '请输入配置值!' }]"
        >
          <a-textarea v-model:value="formState.configValue" />
        </a-form-item>
        
        <a-form-item
          label="描述"
          name="description"
        >
          <a-textarea v-model:value="formState.description" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import api from '../../utils/api'

const dataSource = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const confirmLoading = ref(false)
const modalType = ref('create') // 'create' or 'edit'

const formState = reactive({
  id: undefined,
  configKey: '',
  configValue: '',
  description: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  {
    title: '配置键',
    dataIndex: 'configKey',
    key: 'configKey'
  },
  {
    title: '配置值',
    dataIndex: 'configValue',
    key: 'configValue'
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime'
  },
  {
    title: '操作',
    key: 'action'
  }
]

onMounted(() => {
  fetchData()
})

const fetchData = async (params = {}) => {
  loading.value = true
  try {
    // Mock data
    dataSource.value = [
      {
        id: '1',
        configKey: 'site.name',
        configValue: 'Kitty CMS',
        description: '网站名称',
        createTime: '2025-12-21'
      },
      {
        id: '2',
        configKey: 'site.url',
        configValue: 'https://kittycms.com',
        description: '网站地址',
        createTime: '2025-12-21'
      }
    ]
    pagination.total = 2
  } catch (error) {
    message.error(error.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pager) => {
  pagination.current = pager.current
  fetchData({
    page: pager.current,
    size: pager.pageSize
  })
}

const showModal = (record, type) => {
  modalType.value = type
  if (type === 'edit') {
    Object.assign(formState, record)
  } else {
    // Reset form
    Object.keys(formState).forEach(key => {
      formState[key] = ''
    })
  }
  modalVisible.value = true
}

const handleOk = async () => {
  confirmLoading.value = true
  try {
    if (modalType.value === 'create') {
      // await api.post('/api/config', formState)
      message.success('创建成功')
    } else {
      // await api.put(`/api/config/${formState.id}`, formState)
      message.success('更新成功')
    }
    modalVisible.value = false
    fetchData()
  } catch (error) {
    message.error(error.message || '操作失败')
  } finally {
    confirmLoading.value = false
  }
}

const handleCancel = () => {
  modalVisible.value = false
}

const handleDelete = async (id) => {
  try {
    // await api.delete(`/api/config/${id}`)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error(error.message || '删除失败')
  }
}
</script>

<style scoped>
.config-list {
  padding: 24px;
}
</style>