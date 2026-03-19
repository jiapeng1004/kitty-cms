<template>
  <div class="page-topic">
    <div class="page-header">
      <h1 class="page-title">选题管理</h1>
      <p class="page-desc">选题创建、审核与发布</p>
    </div>

    <a-card class="filter-card" :bordered="false">
      <a-space wrap>
        <a-input-search
          v-model:value="searchKey"
          placeholder="搜索标题或来源"
          allow-clear
          style="width: 280px"
          @search="onSearch"
        />
        <a-button type="primary" @click="onSearch">查询</a-button>
        <a-button @click="resetSearch">重置</a-button>
      </a-space>
    </a-card>

    <a-card class="content-card" :bordered="false" style="margin-top: 12px">
      <template #title>选题列表</template>
      <template #extra>
        <a-button type="primary" @click="showModal({}, 'create')">新增选题</a-button>
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
          <a-empty :image="false" description="暂无选题数据" />
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag
              :color="
                record.status === 0
                  ? 'default'
                  : record.status === 1
                    ? 'warning'
                    : record.status === 2
                      ? 'success'
                      : 'processing'
              "
            >
              {{ statusLabel(record.status) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'tags'">
            <span :title="record.tags">{{ record.tags || '—' }}</span>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="showModal(record, 'view')">查看详情</a>
              <a v-if="record.status === 0" @click="showModal(record, 'edit')">编辑</a>
              <a-popconfirm v-if="record.status === 0" title="确定删除该选题吗?" @confirm="handleDelete(record.id)">
                <a>删除</a>
              </a-popconfirm>
              <a v-if="record.status === 0" @click="handleWorkflow(record.id, 'submit')">提交审核</a>
              <a v-if="record.status === 1" @click="handleWorkflow(record.id, 'reject')">拒绝</a>
              <a v-if="record.status === 1" @click="handleWorkflow(record.id, 'approve')">通过审核</a>
              <a v-if="record.status === 2" @click="handleWorkflow(record.id, 'publish')">发布</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      :title="modalType === 'create' ? '新增选题' : modalType === 'view' ? '选题详情' : '编辑选题'"
      :confirm-loading="confirmLoading"
      @ok="handleOkProxy"
      @cancel="modalVisible = false"
    >
      <TopicDetail v-if="modalType === 'view'" :formState="formState" />
      <TopicForm v-else :formState="formState" :disabled="false" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import TopicForm from './TopicForm.vue'
import TopicDetail from './TopicDetail.vue'
import {
  approveTopic,
  createTopic,
  deleteTopic,
  getTopicById,
  getTopicPage,
  publishTopic,
  rejectTopic,
  submitTopic,
  updateTopic
} from '@/api/topic_api'

const dataSource = ref<any[]>([])
const loading = ref(false)
const modalVisible = ref(false)
const confirmLoading = ref(false)
const modalType = ref<'create' | 'edit' | 'view'>('create')

const searchKey = ref('')
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const formState = reactive({
  id: '',
  title: '',
  source: '',
  tags: '',
  content: '',
  status: 0
})

const columns = computed(() => {
  const base = [
    { title: '标题', dataIndex: 'title', key: 'title' },
    { title: '来源', dataIndex: 'source', key: 'source' },
    { title: '标签', dataIndex: 'tags', key: 'tags' },
    { title: '状态', dataIndex: 'status', key: 'status', width: 140 },
    { title: '操作', key: 'action', width: 260 }
  ] as Array<any>
  return base
})

onMounted(() => {
  fetchData()
})

function statusLabel(status: number) {
  switch (status) {
    case 0:
      return '草稿'
    case 1:
      return '待审核'
    case 2:
      return '已通过'
    case 3:
      return '已发布'
    default:
      return '未知'
  }
}

async function fetchData(params: { page?: number; size?: number; searchKey?: string } = {}) {
  loading.value = true
  try {
    const data = await getTopicPage({
      page: params.page ?? pagination.current,
      size: params.size ?? pagination.pageSize,
      searchKey: params.searchKey ?? (searchKey.value || undefined)
    })
    dataSource.value = data?.records ?? []
    pagination.total = data?.total ?? 0
  } catch (e) {
    message.error('加载失败')
    dataSource.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  pagination.current = 1
  fetchData({ page: 1 })
}

function resetSearch() {
  searchKey.value = ''
  pagination.current = 1
  fetchData({ page: 1 })
}

function handleTableChange(pager: any) {
  pagination.current = pager.current
  pagination.pageSize = pager.pageSize
  fetchData({ page: pager.current, size: pager.pageSize })
}

async function showModal(record: any, type: 'create' | 'edit' | 'view') {
  modalType.value = type
  if (type === 'edit' || type === 'view') {
    try {
      const data = await getTopicById(record.id)
      Object.assign(formState, {
        id: data.id,
        title: data.title,
        source: data.source,
        tags: data.tags ?? '',
        content: data.content ?? '',
        status: data.status ?? 0
      })
    } catch (e) {
      message.error('加载详情失败')
      return
    }
  } else {
    Object.assign(formState, {
      id: '',
      title: '',
      source: '',
      tags: '',
      content: '',
      status: 0
    })
  }
  modalVisible.value = true
}

async function handleOk() {
  confirmLoading.value = true
  try {
    if (modalType.value === 'view') {
      modalVisible.value = false
      return
    }

    const payload = {
      title: formState.title,
      source: formState.source,
      tags: formState.tags,
      content: formState.content
    }

    if (modalType.value === 'create') {
      await createTopic(payload)
      message.success('创建成功')
    } else {
      await updateTopic(formState.id, payload)
      message.success('更新成功')
    }
    modalVisible.value = false
    fetchData()
  } catch (e) {
    message.error('操作失败')
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
    await deleteTopic(id)
    message.success('删除成功')
    fetchData()
  } catch (e) {
    message.error('删除失败')
  }
}

async function handleWorkflow(id: string, action: 'submit' | 'reject' | 'approve' | 'publish') {
  try {
    if (action === 'submit') await submitTopic(id)
    if (action === 'reject') await rejectTopic(id)
    if (action === 'approve') await approveTopic(id)
    if (action === 'publish') await publishTopic(id)
    message.success('操作成功')
    fetchData()
  } catch (e) {
    message.error('操作失败')
  }
}
</script>

<style scoped>
.page-topic {
  padding: 0;
}
</style>

