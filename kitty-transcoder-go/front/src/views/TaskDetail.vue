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
            <span v-if="formatDepends(s.depends)" class="step-depends" title="步骤依赖关系">↳ {{ formatDepends(s.depends) }}</span>
            <span class="step-status">{{ stepStatusText(s.status) }}</span>
            <a-progress v-if="s.status === 'processing'" type="circle" :size="24" :percent="s.progress ?? 0" />
            <a-tag v-else-if="s.status === 'completed'" color="success">完成</a-tag>
            <a-tag v-else-if="s.status === 'failed'" color="error">失败</a-tag>
            <a-tag v-else color="default">待执行</a-tag>
            <a-button
              v-if="task.status === 'COMPLETED' || s.status === 'failed'"
              type="link"
              size="small"
              :loading="retryingStep === s.stepId"
              @click="onRetryStep(s.stepId)"
            >重试该步骤</a-button>
          </div>
        </div>
      </a-descriptions-item>
      <a-descriptions-item label="输入类型">{{ task.inputType }}</a-descriptions-item>
      <a-descriptions-item label="输入">{{ task.inputPath || task.inputFile }}</a-descriptions-item>
      <a-descriptions-item label="策略ID">{{ task.strategyId }}</a-descriptions-item>
      <a-descriptions-item v-if="task.taskType === 'SCHEDULED_TRANSCODE'" label="回调通知">
        <template v-if="displayNotifications.length">
          <div v-for="(n, i) in displayNotifications" :key="i" class="notification-item">
            <a-tag>{{ n.method || 'HTTP' }}</a-tag>
            <span class="notification-target">{{ n.target }}</span>
          </div>
        </template>
        <span v-else style="color:#888">未配置</span>
      </a-descriptions-item>
      <a-descriptions-item label="输出路径">
        {{ task.outputPath || task.outputFile || (['PROCESSING', 'PENDING'].includes(task.status) ? '执行中，完成后显示' : '-') }}
      </a-descriptions-item>
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
      <a-button v-if="task.status === 'FAILED' || task.status === 'CANCELLED'" type="primary" :loading="retryingTask" @click="onRetryTask">任务重试</a-button>
      <a-button type="primary" @click="onFork">Fork（填充到新建任务）</a-button>
      <a-button @click="load">刷新</a-button>
    </a-space>
  </div>
  <a-spin v-else :spinning="loading" />
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useForkTaskStore } from '../stores/forkTask'
import { getTask, cancelTask, getProgress, subscribeProgressStream, getPreviewInfo, retryTask, retryStep } from '../api/transcoder_api'
import VideoPreview from '../components/VideoPreview.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const task = ref(null)
const progressData = ref({ progress: 0, stepProgressList: [] })
const previewFiles = ref([])
const retryingTask = ref(false)
const retryingStep = ref(null)
let unsubscribe = null

const displayNotifications = computed(() => {
  const t = task.value
  if (!t?.notifications || !Array.isArray(t.notifications)) return []
  return t.notifications
})

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

function formatDepends(depends) {
  const s = (depends || '').trim()
  if (!s) return ''
  const parts = s.split(',').map((p) => p.trim()).filter(Boolean)
  if (parts.length === 0) return ''
  return '依赖步骤' + parts.join('、')
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
      if (task.value?.outputPath || task.value?.status === 'COMPLETED') {
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

async function onRetryTask() {
  retryingTask.value = true
  try {
    const ok = await retryTask(route.params.id)
    if (ok) {
      message.success('已重新入队，请等待执行')
      load()
    } else {
      message.warning('仅失败或已取消的任务可重试')
    }
  } catch (e) {
    message.error(e?.response?.data?.message || e?.message || '任务重试失败')
  } finally {
    retryingTask.value = false
  }
}

async function onRetryStep(stepId) {
  retryingStep.value = stepId
  try {
    await retryStep(route.params.id, stepId)
    message.success('步骤重试完成')
    load()
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '步骤重试失败'
    message.error(msg)
  } finally {
    retryingStep.value = null
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
.step-depends { font-size: 12px; color: #666; margin-right: 8px; padding: 2px 6px; background: #f0f0f0; border-radius: 4px; }
.preview-list { display: flex; flex-direction: column; gap: 16px; }
.preview-item { display: flex; flex-direction: column; gap: 8px; padding: 8px; background: #fafafa; border-radius: 4px; }
.preview-path { font-family: monospace; font-size: 12px; color: #666; word-break: break-all; }
.preview-thumb { max-width: 200px; max-height: 150px; object-fit: contain; }
.preview-video-wrap { aspect-ratio: 16/9; max-width: 400px; min-height: 180px; background: #000; }
.preview-video { width: 100%; height: 100%; display: block; object-fit: contain; }
.preview-audio { max-width: 400px; }
.notification-item { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.notification-target { font-family: monospace; font-size: 12px; word-break: break-all; }
</style>
