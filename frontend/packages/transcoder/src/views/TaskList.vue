<template>
  <div>
    <a-space class="mb">
      <a-button type="primary" @click="$router.push('/tasks/create')">新建任务</a-button>
    </a-space>
    <a-table :columns="columns" :data-source="list" :loading="loading" row-key="id" :pagination="{ pageSize: 10 }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a @click="$router.push('/tasks/' + record.id)">详情</a>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listTasks } from '../api/transcoder_api'

const loading = ref(false)
const list = ref([])
const columns = [
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 220, ellipsis: true },
  { title: '输入类型', dataIndex: 'inputType', key: 'inputType', width: 90 },
  { title: '输入', dataIndex: 'inputPath', key: 'inputPath', ellipsis: true },
  { title: '策略ID', dataIndex: 'strategyId', key: 'strategyId', width: 160, ellipsis: true },
  { title: '状态', key: 'status', width: 100 },
  { title: '进度', dataIndex: 'progress', key: 'progress', width: 80, customRender: ({ text }) => text != null ? text + '%' : '-' },
  { title: '操作', key: 'action', width: 80 }
]

function statusColor(s) {
  const m = { PENDING: 'default', PROCESSING: 'processing', COMPLETED: 'success', FAILED: 'error', CANCELLED: 'default' }
  return m[s] || 'default'
}

async function load() {
  loading.value = true
  try {
    const data = await listTasks({ page: 1, size: 50 })
    list.value = Array.isArray(data) ? data : []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.mb { margin-bottom: 16px; }
</style>
