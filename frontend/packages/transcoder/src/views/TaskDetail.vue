<template>
  <div v-if="task">
    <a-page-header :title="'任务 ' + task.id" @back="$router.push('/tasks')" />
    <a-descriptions bordered :column="1" class="mt">
      <a-descriptions-item label="状态"><a-tag :color="statusColor(task.status)">{{ task.status }}</a-tag></a-descriptions-item>
      <a-descriptions-item label="进度">{{ task.progress != null ? task.progress + '%' : '-' }}</a-descriptions-item>
      <a-descriptions-item label="输入类型">{{ task.inputType }}</a-descriptions-item>
      <a-descriptions-item label="输入">{{ task.inputPath || task.inputFile }}</a-descriptions-item>
      <a-descriptions-item label="策略ID">{{ task.strategyId }}</a-descriptions-item>
      <a-descriptions-item label="输出路径">{{ task.outputPath || task.outputFile || '-' }}</a-descriptions-item>
      <a-descriptions-item label="输出 HTTP">{{ task.outputHttpUrl || '-' }}</a-descriptions-item>
      <a-descriptions-item label="错误信息" v-if="task.errorMessage">{{ task.errorMessage }}</a-descriptions-item>
    </a-descriptions>
    <a-space class="mt">
      <a-button v-if="task.status === 'PENDING' || task.status === 'PROCESSING'" type="primary" danger @click="cancel">取消任务</a-button>
      <a-button @click="load">刷新</a-button>
    </a-space>
  </div>
  <a-spin v-else :spinning="loading" />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getTask, cancelTask } from '../api/transcoder_api'

const route = useRoute()
const loading = ref(true)
const task = ref(null)

function statusColor(s) {
  const m = { PENDING: 'default', PROCESSING: 'processing', COMPLETED: 'success', FAILED: 'error', CANCELLED: 'default' }
  return m[s] || 'default'
}

async function load() {
  loading.value = true
  try {
    task.value = await getTask(route.params.id)
  } catch {
    task.value = null
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function cancel() {
  try {
    await cancelTask(route.params.id)
    message.success('已取消')
    load()
  } catch (e) {
    message.error(e?.message || '取消失败')
  }
}

onMounted(load)
</script>

<style scoped>
.mt { margin-top: 16px; }
</style>
