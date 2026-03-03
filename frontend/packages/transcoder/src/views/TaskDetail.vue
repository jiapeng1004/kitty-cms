<template>
  <div v-if="task">
    <a-page-header :title="'任务 ' + task.id" @back="$router.push('/tasks')" />
    <a-descriptions bordered :column="1" class="mt">
      <a-descriptions-item label="状态"><a-tag :color="statusColor(task.status)">{{ task.status }}</a-tag></a-descriptions-item>
      <a-descriptions-item label="总进度">
        <a-progress
          v-if="task.status === 'PROCESSING' || task.status === 'PENDING'"
          :percent="progressData.progress ?? 0"
          :status="task.status === 'FAILED' ? 'exception' : undefined"
        />
        <span v-else>{{ task.progress != null ? task.progress + '%' : '-' }}</span>
      </a-descriptions-item>
      <a-descriptions-item v-if="progressData.stepProgressList?.length" label="分步进度">
        <div class="step-progress-list">
          <div v-for="s in progressData.stepProgressList" :key="s.stepId" class="step-item">
            <a-tag :color="stepStatusColor(s.status)">{{ s.name }}</a-tag>
            <span class="step-status">{{ stepStatusText(s.status) }}</span>
            <a-progress v-if="s.status === 'processing'" type="circle" :size="24" :percent="s.progress ?? 0" />
            <a-tag v-else-if="s.status === 'completed'" color="success">完成</a-tag>
            <a-tag v-else-if="s.status === 'failed'" color="error">失败</a-tag>
            <a-tag v-else color="default">待执行</a-tag>
          </div>
        </div>
      </a-descriptions-item>
      <a-descriptions-item label="输入类型">{{ task.inputType }}</a-descriptions-item>
      <a-descriptions-item label="输入">{{ task.inputPath || task.inputFile }}</a-descriptions-item>
      <a-descriptions-item label="策略ID">{{ task.strategyId }}</a-descriptions-item>
      <a-descriptions-item label="输出路径">{{ task.outputPath || task.outputFile || '-' }}</a-descriptions-item>
      <a-descriptions-item v-if="previewFiles.length" label="预览">
        <div class="preview-list">
          <div v-for="f in previewFiles" :key="f.path" class="preview-item">
            <div class="preview-path">{{ f.path }}</div>
            <a :href="f.previewUrl" target="_blank" rel="noopener">打开链接</a>
            <template v-if="isImage(f.path)">
              <img :src="f.previewUrl" class="preview-thumb" alt="预览" />
            </template>
            <template v-else-if="isVideo(f.path)">
              <div class="preview-video-wrap">
                <VideoPreview :url="f.previewUrl" />
              </div>
            </template>
            <template v-else-if="isAudio(f.path)">
              <audio :src="f.previewUrl" controls class="preview-audio" />
            </template>
          </div>
        </div>
      </a-descriptions-item>
      <a-descriptions-item label="错误信息" v-if="task.errorMessage">{{ task.errorMessage }}</a-descriptions-item>
    </a-descriptions>
    <a-space class="mt">
      <a-button v-if="task.status === 'PENDING' || task.status === 'PROCESSING'" type="primary" danger @click="cancel">取消任务</a-button>
      <a-button type="primary" @click="onFork">Fork（填充到新建任务）</a-button>
      <a-button @click="load">刷新</a-button>
    </a-space>
  </div>
  <a-spin v-else :spinning="loading" />
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useForkTaskStore } from '../stores/forkTask'
import { getTask, cancelTask, getProgress, subscribeProgressStream, getPreviewInfo } from '../api/transcoder_api'
import VideoPreview from '../components/VideoPreview.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const task = ref(null)
const progressData = ref({ progress: 0, stepProgressList: [] })
const previewFiles = ref([])
let unsubscribe = null

function isImage(path) {
  return /\.(jpg|jpeg|png|gif|webp)$/i.test(path || '')
}
function isVideo(path) {
  return /\.(mp4|webm|mkv|avi|mov)$/i.test(path || '')
}
function isAudio(path) {
  return /\.(mp3|wav|ogg|m4a|aac|flac)$/i.test(path || '')
}

function statusColor(s) {
  const m = { PENDING: 'default', PROCESSING: 'processing', COMPLETED: 'success', FAILED: 'error', CANCELLED: 'default' }
  return m[s] || 'default'
}

function stepStatusColor(s) {
  const m = { completed: 'success', processing: 'processing', pending: 'default', failed: 'error' }
  return m[s] || 'default'
}

function stepStatusText(s) {
  const m = { completed: '已完成', processing: '进行中', pending: '待执行', failed: '失败' }
  return m[s] || s
}

function onProgress(p) {
  if (!p?.taskId || p.taskId !== route.params.id) return
  progressData.value = {
    progress: p.progress ?? 0,
    status: p.status,
    stepProgressList: p.stepProgressList ?? []
  }
  task.value = { ...task.value, status: p.status, progress: p.progress }
  if (['COMPLETED', 'FAILED', 'CANCELLED'].includes(p.status)) {
    load()
  }
}

async function load() {
  loading.value = true
  try {
    task.value = await getTask(route.params.id)
    progressData.value = { progress: task.value?.progress ?? 0, stepProgressList: [] }
    if (task.value?.id) {
      const p = await getProgress(task.value.id)
      progressData.value = { progress: p.progress ?? 0, stepProgressList: p.stepProgressList ?? [] }
      if (['COMPLETED', 'FAILED'].includes(task.value?.status) && task.value?.outputPath) {
        try {
          const info = await getPreviewInfo(task.value.id)
          const token = localStorage.getItem('transcoder_token')
          const raw = info.files || []
          const seen = new Set()
          const deduped = raw.filter((f) => {
            const key = (f.path || '').toLowerCase().replace(/\\/g, '/')
            if (seen.has(key)) return false
            seen.add(key)
            return true
          })
          previewFiles.value = deduped.map((f) => ({
            path: f.path,
            previewUrl: f.previewUrl + (token ? (f.previewUrl.includes('?') ? '&' : '?') + 'token=' + encodeURIComponent(token) : '')
          }))
        } catch {
          previewFiles.value = []
        }
      } else {
        previewFiles.value = []
      }
    }
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

function onFork() {
  const t = task.value
  if (t) useForkTaskStore().set(t)
  router.push('/tasks/create')
}

onMounted(() => {
  load()
  unsubscribe = subscribeProgressStream(onProgress)
})
onBeforeUnmount(() => {
  unsubscribe?.()
})
</script>

<style scoped>
.mt { margin-top: 16px; }
.step-progress-list { display: flex; flex-direction: column; gap: 8px; }
.step-item { display: flex; align-items: center; gap: 8px; }
.step-status { font-size: 12px; color: #666; margin-right: 4px; }
.preview-list { display: flex; flex-direction: column; gap: 16px; }
.preview-item { display: flex; flex-direction: column; gap: 8px; padding: 8px; background: #fafafa; border-radius: 4px; }
.preview-path { font-family: monospace; font-size: 12px; color: #666; word-break: break-all; }
.preview-thumb { max-width: 200px; max-height: 150px; object-fit: contain; }
.preview-video-wrap { aspect-ratio: 16/9; max-width: 400px; min-height: 180px; background: #000; }
.preview-video { width: 100%; height: 100%; display: block; object-fit: contain; }
.preview-audio { max-width: 400px; }
</style>
