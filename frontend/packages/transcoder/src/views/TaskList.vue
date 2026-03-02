<template>
  <div>
    <div class="toolbar mb">
      <a-button type="primary" @click="$router.push('/tasks/create')">新建任务</a-button>
      <a-input-search
        v-model:value="searchTaskId"
        placeholder="按任务ID检索"
        allow-clear
        class="search-input"
        @blur="searchTaskId = (searchTaskId || '').trim()"
        @search="load"
      />
    </div>
    <a-table
      :columns="columns"
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{ pageSize: 10 }"
      :expandable="{ expandedRowKeys, onExpand: onRowExpand }"
    >
      <template #expandedRowRender="{ record }">
        <StepProgressExpand :task-id="record.id" :step-progress-map="stepProgressByTaskId" />
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'id'">
          <a-space :size="4">
            <a-button type="text" size="small" class="copy-btn" title="复制任务ID" @click.stop="copyTaskId(record.id)">
              <span class="copy-icon">⎘</span>
            </a-button>
            <span>{{ record.id }}</span>
          </a-space>
        </template>
        <template v-else-if="column.key === 'createdAt'">
          {{ formatTime(record.createdAt) }}
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
        </template>
        <template v-else-if="column.key === 'progress'">
          <a-progress v-if="record.status === 'PROCESSING'" :percent="record.progress ?? 0" size="small" style="width:60px" />
          <span v-else>{{ record.progress != null ? record.progress + '%' : '-' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="$router.push('/tasks/' + record.id)">详情</a>
            <a-popconfirm title="确定删除该任务记录？" ok-text="删除" cancel-text="取消" @confirm="onDelete(record.id)">
              <a class="danger">删除</a>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { listTasks, subscribeProgressStream, deleteTask } from '../api/transcoder_api'
import StepProgressExpand from '../components/StepProgressExpand.vue'

const loading = ref(false)
const list = ref([])
const searchTaskId = ref('')
const expandedRowKeys = ref([])
const stepProgressByTaskId = ref({})

function formatTime(ts) {
  if (ts == null) return '-'
  const d = new Date(Number(ts))
  return isNaN(d.getTime()) ? '-' : d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

const columns = [
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 220, ellipsis: true },
  { title: '添加时间', key: 'createdAt', width: 170 },
  { title: '输入类型', dataIndex: 'inputType', key: 'inputType', width: 90 },
  { title: '输入', dataIndex: 'inputPath', key: 'inputPath', ellipsis: true },
  { title: '策略ID', dataIndex: 'strategyId', key: 'strategyId', width: 160, ellipsis: true },
  { title: '状态', key: 'status', width: 100 },
  { title: '进度', key: 'progress', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

async function copyTaskId(id) {
  try {
    await navigator.clipboard.writeText(id)
    message.success('已复制到剪贴板')
  } catch {
    message.error('复制失败')
  }
}

function onRowExpand(expanded, record) {
  if (expanded) {
    const keys = expandedRowKeys.value
    if (!keys.includes(record.id)) expandedRowKeys.value = [...keys, record.id]
  } else {
    expandedRowKeys.value = expandedRowKeys.value.filter((k) => k !== record.id)
  }
}

async function onDelete(taskId) {
  try {
    await deleteTask(taskId)
    message.success('已删除')
    list.value = list.value.filter((t) => t.id !== taskId)
  } catch (e) {
    message.error(e?.message || '删除失败')
  }
}

function statusColor(s) {
  const m = { PENDING: 'default', PROCESSING: 'processing', COMPLETED: 'success', FAILED: 'error', CANCELLED: 'default' }
  return m[s] || 'default'
}

function applyProgress(p) {
  if (!p?.taskId) return
  if (Array.isArray(p.stepProgressList)) {
    stepProgressByTaskId.value = { ...stepProgressByTaskId.value, [p.taskId]: p.stepProgressList }
  }
  const idx = list.value.findIndex((t) => t.id === p.taskId)
  if (idx >= 0) {
    list.value = list.value.map((t) =>
      t.id === p.taskId ? { ...t, progress: p.progress, status: p.status } : t
    )
  } else {
    list.value = [{ id: p.taskId, progress: p.progress, status: p.status, inputType: '-', inputPath: '-', strategyId: '-' }, ...list.value]
  }
}

async function load() {
  loading.value = true
  try {
    const data = await listTasks({ page: 1, size: 50, taskId: searchTaskId.value || undefined })
    list.value = Array.isArray(data) ? data : []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

let unsubscribe = null
onMounted(() => {
  load()
  unsubscribe = subscribeProgressStream(applyProgress)
})
onBeforeUnmount(() => {
  unsubscribe?.()
})
</script>

<style scoped>
.mb { margin-bottom: 16px; }
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.toolbar .search-input { width: 240px; min-width: 200px; }
.danger { color: var(--ant-color-error); }
.copy-btn { padding: 0 4px; min-width: 24px; }
.copy-icon { font-size: 12px; opacity: 0.6; }
.copy-btn:hover .copy-icon { opacity: 1; }
</style>
