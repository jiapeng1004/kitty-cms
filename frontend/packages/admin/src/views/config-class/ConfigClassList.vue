<template>
  <div class="config-class-list">
    <a-card title="配置分类管理">
      <template #extra>
        <a-button type="primary" @click="showModal({}, 'create')">
          <template #icon>
            <PlusOutlined />
          </template>
          新增分类
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
          <a-input v-model:value="formState.className" />
        </a-form-item>
        
        <a-form-item
          label="分类编码"
          name="classCode"
          :rules="[{ required: true, message: '请输入分类编码!' }]"
        >
          <a-input v-model:value="formState.classCode" />
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
import { ref, reactive, onMounted, computed } from 'vue'
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
  className: '',
  classCode: '',
  description: ''
})

const modalTitle = computed(() => {
  return modalType.value === 'create' ? '新增分类' : '编辑分类'
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  {
    title: '分类名称',
    dataIndex: 'className',
    key: 'className'
  },
  {
    title: '分类编码',
    dataIndex: 'classCode',
    key: 'classCode'
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
        className: '系统配置',
        classCode: 'SYSTEM',
        description: '系统基础配置',
        createTime: '2025-12-21'
      },
      {
        id: '2',
        className: '用户配置',
        classCode: 'USER',
        description: '用户相关配置',
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
      // await api.post('/api/configClass', formState)
      message.success('创建成功')
    } else {
      // await api.put(`/api/configClass/${formState.id}`, formState)
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
    // await api.delete(`/api/configClass/${id}`)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error(error.message || '删除失败')
  }
}
</script>

<style scoped>
.config-class-list {
  padding: 24px;
}
</style>