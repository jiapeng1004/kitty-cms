<template>
  <div>
    <div class="toolbar mb">
      <a-button type="primary" @click="$router.push('/tasks/create')">新建任务</a-button>
      <a-input-search v-model:value="filters.taskId" placeholder="按任务ID检索" allow-clear class="search-input" @search="() => { pagination.current = 1; load() }" />
      <a-input-search v-model:value="filters.filename" placeholder="按文件名检索" allow-clear class="search-input" @search="() => { pagination.current = 1; load() }" />
      <a-range-picker
        v-model:value="timeRange"
        show-time
        value-format="x"
        :placeholder="['开始时间', '结束时间']"
        class="range-picker"
        @change="onTimeRangeChange"
      />
      <a-select v-model:value="filters.strategyId" placeholder="策略ID" allow-clear class="filter-select" @change="() => { pagination.current = 1; load() }">
        <a-select-option value="-1">空不定策略</a-select-option>
        <a-select-option v-for="s in strategies" :key="s.id" :value="s.id">{{ s.name || s.id }}</a-select-option>
      </a-select>
      <a-select v-model:value="filters.status" placeholder="任务状态" allow-clear class="filter-select" @change="() => { pagination.current = 1; load() }">
        <a-select-option value="PENDING">PENDING</a-select-option>
        <a-select-option value="PROCESSING">PROCESSING</a-select-option>
        <a-select-option value="COMPLETED">COMPLETED</a-select-option>
        <a-select-option value="FAILED">FAILED</a-select-option>
        <a-select-option value="CANCELLED">CANCELLED</a-select-option>
      </a-select>
      <a-select v-model:value="filters.taskType" placeholder="任务类型" allow-clear class="filter-select" @change="() => { pagination.current = 1; load() }">
        <a-select-option value="SCHEDULED_TRANSCODE">预定策略转码</a-select-option>
        <a-select-option value="MAGIC_EXTRACT_FRAMES">同步抽帧</a-select-option>
        <a-select-option value="MAGIC_IMAGE_CONVERT">同步图转</a-select-option>
        <a-select-option value="MAGIC_SYNC_TRANSCODE">同步单目标转码</a-select-option>
      </a-select>
      <a-select v-model:value="sortBy" placeholder="排序" class="filter-select" @change="() => { pagination.current = 1; load() }">
        <a-select-option value="createdAt">添加时间</a-select-option>
        <a-select-option value="completedAt">结束时间</a-select-option>
      </a-select>
      <a-select v-model:value="sortOrder" placeholder="方向" class="filter-select sort-order" @change="() => { pagination.current = 1; load() }">
        <a-select-option value="desc">倒序</a-select-option>
        <a-select-option value="asc">正序</a-select-option>
      </a-select>
      <a-button @click="() => { pagination.current = 1; load() }">查询</a-button>
    </div>
    <a-table
      :columns="columns"
      :data-source="list"
      :loading="loading"
      row-key="id"
      :pagination="{
        current: pagination.current,
        pageSize: pagination.pageSize,
        total: total,
        showSizeChanger: true,
        showTotal: (t) => `共 ${t} 条`,
        onChange: (page, pageSize) => { pagination.current = page; pagination.pageSize = pageSize; load() }
      }"
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
        <template v-else-if="column.key === 'taskType'">
          {{ TASK_TYPE_LABELS[record.taskType] || record.taskType || '-' }}
        </template>
        <template v-else-if="column.key === 'strategyId'">
          {{ displayStrategyId(record) }}
        </template>
        <template v-else-if="column.key === 'completedAt'">
          {{ formatTime(record.completedAt) }}
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
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { listTasks, subscribeProgressStream, deleteTask } from '../api/transcoder_api'
import { listStrategies } from '../api/strategy_api'
import StepProgressExpand from '../components/StepProgressExpand.vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pagination = reactive({ current: 1, pageSize: 50 })
const filters = reactive({ taskId: '', filename: '', strategyId: undefined, status: undefined, taskType: undefined })
const timeRange = ref(null)
const sortBy = ref('createdAt')
const sortOrder = ref('desc')
const strategies = ref([])
const expandedRowKeys = ref([])
const stepProgressByTaskId = ref({})

function displayStrategyId(record) {
  const isMagic = record.taskType && MAGIC_TYPES.includes(record.taskType)
  const empty = record.strategyId == null || record.strategyId === '' || (typeof record.strategyId === 'string' && record.strategyId.trim() === '')
  if (isMagic && empty) return '空不定策略'
  return record.strategyId != null && record.strategyId !== '' ? record.strategyId : '-'
}

function onTimeRangeChange() {
  pagination.current = 1
  load()
}

function formatTime(ts) {
  if (ts == null) return '-'
  const d = new Date(Number(ts))
  return isNaN(d.getTime()) ? '-' : d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

const TASK_TYPE_LABELS = {
  SCHEDULED_TRANSCODE: '预定策略转码',
  MAGIC_EXTRACT_FRAMES: '同步抽帧',
  MAGIC_IMAGE_CONVERT: '同步图转',
  MAGIC_SYNC_TRANSCODE: '同步单目标转码'
}
const MAGIC_TYPES = ['MAGIC_EXTRACT_FRAMES', 'MAGIC_IMAGE_CONVERT', 'MAGIC_SYNC_TRANSCODE']

const columns = [
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 220, ellipsis: true },
  { title: '添加时间', key: 'createdAt', width: 170 },
  { title: '任务类型', key: 'taskType', width: 120 },
  { title: '输入类型', dataIndex: 'inputType', key: 'inputType', width: 90 },
  { title: '输入', dataIndex: 'inputPath', key: 'inputPath', ellipsis: true },
  { title: '策略ID', key: 'strategyId', width: 160, ellipsis: true },
  { title: '状态', key: 'status', width: 100 },
  { title: '进度', key: 'progress', width: 100 },
  { title: '结束时间', key: 'completedAt', width: 170 },
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
    list.value = [{ id: p.taskId, progress: p.progress, status: p.status, inputType: '-', inputPath: '-', strategyId: '-', taskType: '-' }, ...list.value]
  }
}

async function load() {
  loading.value = true
  try {
    const [timeFrom, timeTo] = timeRange.value || []
    const data = await listTasks({
      page: pagination.current || 1,
      size: pagination.pageSize || 50,
      taskId: filters.taskId || undefined,
      filename: filters.filename || undefined,
      timeFrom: timeFrom || undefined,
      timeTo: timeTo || undefined,
      strategyId: filters.strategyId,
      status: filters.status,
      taskType: filters.taskType,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value
    })
    // 后端返回 { list, total, page, pageSize }
    list.value = data?.list ?? []
    total.value = data?.total ?? 0
    pagination.current = data?.page ?? 1
    pagination.pageSize = data?.pageSize ?? 50
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

let unsubscribe = null
onMounted(async () => {
  try {
    const s = await listStrategies()
    strategies.value = Array.isArray(s) ? s : []
  } catch {
    strategies.value = []
  }
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
.toolbar .search-input { width: 180px; min-width: 140px; }
.toolbar .filter-select { width: 140px; min-width: 100px; }
.toolbar .sort-order { width: 90px; }
.toolbar .range-picker { width: 360px; }
.danger { color: var(--ant-color-error); }
.copy-btn { padding: 0 4px; min-width: 24px; }
.copy-icon { font-size: 12px; opacity: 0.6; }
.copy-btn:hover .copy-icon { opacity: 1; }
</style>
