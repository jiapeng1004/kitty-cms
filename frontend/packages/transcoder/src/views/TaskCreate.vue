<template>
  <div>
    <a-page-header title="新建转码任务" @back="$router.push('/tasks')" />
    <a-card class="form-card">
      <a-form :model="form" layout="vertical" @finish="onSubmit">
        <a-form-item label="任务类型" name="taskType" :rules="[{ required: true, message: '请选择任务类型' }]">
          <a-select v-model:value="form.taskType" placeholder="选择任务类型" @change="onTaskTypeChange">
            <a-select-option value="SCHEDULED_TRANSCODE">预定策略转码</a-select-option>
            <a-select-option value="MAGIC_EXTRACT_FRAMES">同步抽帧</a-select-option>
            <a-select-option value="MAGIC_IMAGE_CONVERT">同步图转</a-select-option>
            <a-select-option value="MAGIC_SYNC_TRANSCODE">同步单目标转码</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="输入类型" name="inputType">
          <a-radio-group v-model:value="form.inputType">
            <a-radio value="DISK">磁盘路径</a-radio>
            <a-radio value="HTTP">HTTP 地址</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :label="form.inputType === 'HTTP' ? '输入 URL' : '输入路径'" name="inputPath" :rules="[{ required: true, message: '请输入' }]">
          <a-input v-model:value="form.inputPath" :placeholder="inputPathPlaceholder" @blur="form.inputPath = (form.inputPath || '').trim()" />
        </a-form-item>

        <!-- 预定策略转码：策略、水印、优先级 -->
        <template v-if="form.taskType === 'SCHEDULED_TRANSCODE'">
          <a-form-item label="策略" name="strategyId" :rules="[{ required: true, message: '请选择策略' }]">
            <a-select v-model:value="form.strategyId" placeholder="选择策略" allow-clear show-search :options="strategyOptions" :field-names="{ label: 'name', value: 'id' }" />
          </a-form-item>
          <a-form-item label="水印（可选）">
            <a-row :gutter="12">
              <a-col :span="14">
                <a-input v-model:value="form.watermarkUrl" :placeholder="form.inputType === 'HTTP' ? 'https://...watermark.png' : '本地路径或 HTTP URL'" allow-clear @blur="form.watermarkUrl = (form.watermarkUrl || '').trim()" />
              </a-col>
              <a-col :span="10">
                <a-select v-model:value="form.watermarkPosition" placeholder="位置" style="width:100%">
                  <a-select-option value="bottom-right">右下</a-select-option>
                  <a-select-option value="bottom-left">左下</a-select-option>
                  <a-select-option value="top-right">右上</a-select-option>
                  <a-select-option value="top-left">左上</a-select-option>
                </a-select>
              </a-col>
            </a-row>
          </a-form-item>
          <a-form-item label="优先级" name="priority">
            <a-input-number v-model:value="form.priority" :min="1" :max="10" />
          </a-form-item>
          <a-form-item label="回调通知（可选）">
            <a-collapse v-if="form.notifications?.length" ghost>
              <a-collapse-panel v-for="(n, i) in form.notifications" :key="i" :header="`${n.method || 'HTTP'} → ${n.target || '(未填)'}`">
                <a-row :gutter="12">
                  <a-col :span="6">
                    <a-select v-model:value="n.method" placeholder="方式" style="width:100%">
                      <a-select-option value="HTTP">HTTP</a-select-option>
                      <a-select-option value="GRPC">GRPC</a-select-option>
                    </a-select>
                  </a-col>
                  <a-col :span="16">
                    <a-input v-model:value="n.target" :placeholder="n.method === 'GRPC' ? 'host:port 如 192.168.1.100:9901' : 'http://localhost:9703/api/transcode/introspection/notification'" allow-clear />
                  </a-col>
                  <a-col :span="2">
                    <a-button type="text" danger size="small" @click="form.notifications.splice(i, 1)">删除</a-button>
                  </a-col>
                </a-row>
              </a-collapse-panel>
            </a-collapse>
            <a-button type="dashed" size="small" @click="addNotification">+ 添加回调</a-button>
            <div class="form-hint">与 gRPC/HTTP 调用一致，支持 HTTP（URL）或 GRPC（host:port），完成后推送 TranscodeProgressNotifyVO</div>
          </a-form-item>
        </template>

        <!-- 同步抽帧 -->
        <template v-else-if="form.taskType === 'MAGIC_EXTRACT_FRAMES'">
          <a-form-item label="抽帧间隔" name="frameInterval">
            <a-input-number v-model:value="form.frameInterval" :min="1" placeholder="每隔多少帧取一帧" style="width:100%" />
          </a-form-item>
          <a-form-item label="抽帧数量" name="frameCount">
            <a-input-number v-model:value="form.frameCount" :min="1" placeholder="抽取帧数" style="width:100%" />
          </a-form-item>
          <a-form-item label="输出格式" name="outputFormat">
            <a-select v-model:value="form.outputFormat" placeholder="选择格式" style="width:100%">
              <a-select-option value="jpg">JPG</a-select-option>
              <a-select-option value="png">PNG</a-select-option>
            </a-select>
          </a-form-item>
        </template>

        <!-- 同步图转 -->
        <template v-else-if="form.taskType === 'MAGIC_IMAGE_CONVERT'">
          <a-form-item label="目标格式" name="targetFormat">
            <a-select v-model:value="form.targetFormat" placeholder="选择格式" style="width:100%">
              <a-select-option value="webp">WebP</a-select-option>
              <a-select-option value="jpg">JPG</a-select-option>
              <a-select-option value="png">PNG</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="质量 (1-100)" name="quality">
            <a-input-number v-model:value="form.quality" :min="1" :max="100" style="width:100%" />
          </a-form-item>
          <a-form-item label="缩放（可选）" name="resize">
            <a-input v-model:value="form.resize" placeholder="如 800x600、800x、x600" allow-clear />
          </a-form-item>
        </template>

        <!-- 同步单目标转码 -->
        <template v-else-if="form.taskType === 'MAGIC_SYNC_TRANSCODE'">
          <a-form-item label="目标格式" name="targetFormat">
            <a-select v-model:value="form.targetFormat" placeholder="选择格式" style="width:100%">
              <a-select-option value="mp4">MP4</a-select-option>
              <a-select-option value="webm">WebM</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="分辨率" name="resolution">
            <a-input v-model:value="form.resolution" placeholder="如 1920x1080" />
          </a-form-item>
          <a-form-item label="码率 (kbps)" name="bitrate">
            <a-input-number v-model:value="form.bitrate" :min="100" style="width:100%" />
          </a-form-item>
          <a-form-item label="帧率" name="frameRate">
            <a-input-number v-model:value="form.frameRate" :min="1" style="width:100%" />
          </a-form-item>
        </template>

        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="loading">提交</a-button>
            <a-button @click="$router.push('/tasks')">取消</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useForkTaskStore } from '../stores/forkTask'
import { listStrategies } from '../api/strategy_api'
import { createTask, magicExtractFrames, magicImageConvert, magicTranscode } from '../api/transcoder_api'

const router = useRouter()
const loading = ref(false)
const strategyOptions = ref([])
const form = reactive({
  taskType: 'SCHEDULED_TRANSCODE',
  inputType: 'DISK',
  inputPath: '',
  strategyId: undefined,
  watermarkUrl: '',
  watermarkPosition: 'bottom-right',
  priority: 5,
  notifications: [],
  // 抽帧
  frameInterval: 30,
  frameCount: 1,
  outputFormat: 'jpg',
  // 图转
  targetFormat: 'webp',
  quality: 85,
  resize: undefined,
  // 单目标转码
  resolution: '1920x1080',
  bitrate: 5000,
  frameRate: 30
})

const inputPathPlaceholder = computed(() => {
  if (form.taskType === 'MAGIC_IMAGE_CONVERT') return form.inputType === 'HTTP' ? 'https://...' : '/path/to/image.jpg'
  return form.inputType === 'HTTP' ? 'https://...' : '/path/to/video.mp4'
})

function onTaskTypeChange() {
  form.strategyId = undefined
  if (form.taskType === 'MAGIC_EXTRACT_FRAMES') {
    form.frameInterval = 30
    form.frameCount = 1
    form.outputFormat = 'jpg'
  } else if (form.taskType === 'MAGIC_IMAGE_CONVERT') {
    form.targetFormat = 'webp'
    form.quality = 85
    form.resize = undefined
  } else if (form.taskType === 'MAGIC_SYNC_TRANSCODE') {
    form.targetFormat = 'mp4'
    form.resolution = '1920x1080'
    form.bitrate = 5000
    form.frameRate = 30
  }
}

function addNotification() {
  if (!form.notifications) form.notifications = []
  form.notifications.push({ method: 'HTTP', target: '' })
}

function applyForkTask(t) {
  if (!t) return
  form.taskType = t.taskType || 'SCHEDULED_TRANSCODE'
  form.inputType = t.inputType || 'DISK'
  form.inputPath = t.inputPath || t.inputFile || ''
  form.strategyId = t.strategyId
  form.watermarkUrl = t.watermarkUrl || ''
  form.watermarkPosition = t.watermarkPosition || 'bottom-right'
  form.priority = t.priority ?? 5
  const notifs = t.notifications && Array.isArray(t.notifications) ? t.notifications : []
  if (notifs.length) form.notifications = notifs.map((n) => ({ method: n.method || 'HTTP', target: n.target || '' }))
}

onMounted(async () => {
  const forkTask = useForkTaskStore().take()
  try {
    const list = await listStrategies()
    strategyOptions.value = Array.isArray(list) ? list : []
  } catch {
    strategyOptions.value = []
  }
  await nextTick()
  applyForkTask(forkTask)
})

onBeforeUnmount(() => {
  useForkTaskStore().clear()
})

async function onSubmit() {
  try {
    loading.value = true
    const inputPath = (form.inputPath || '').trim()
    if (!inputPath) {
      message.error('请输入输入路径')
      return
    }

    let taskId = null

    if (form.taskType === 'SCHEDULED_TRANSCODE') {
      const body = { inputType: form.inputType, inputPath, strategyId: form.strategyId, priority: form.priority }
      if (form.watermarkUrl?.trim()) {
        body.watermarkUrl = form.watermarkUrl.trim()
        body.watermarkPosition = form.watermarkPosition || undefined
      }
      const validNotifs = (form.notifications || []).filter((n) => n?.method && (n?.target || '').trim())
      if (validNotifs.length) {
        body.notifications = validNotifs.map((n) => ({ method: n.method, target: (n.target || '').trim() }))
      }
      taskId = await createTask(body)
    } else if (form.taskType === 'MAGIC_EXTRACT_FRAMES') {
      const res = await magicExtractFrames({
        inputType: form.inputType,
        inputPath,
        frameInterval: form.frameInterval,
        frameCount: form.frameCount,
        outputFormat: form.outputFormat
      })
      taskId = res?.id
    } else if (form.taskType === 'MAGIC_IMAGE_CONVERT') {
      const res = await magicImageConvert({
        inputType: form.inputType,
        inputPath,
        targetFormat: form.targetFormat,
        quality: form.quality,
        resize: form.resize || undefined
      })
      taskId = res?.id
    } else if (form.taskType === 'MAGIC_SYNC_TRANSCODE') {
      const res = await magicTranscode({
        inputType: form.inputType,
        inputPath,
        targetFormat: form.targetFormat,
        resolution: form.resolution,
        bitrate: form.bitrate,
        frameRate: form.frameRate
      })
      taskId = res?.id
    }

    if (taskId) {
      message.success('任务已创建：' + taskId)
      router.push('/tasks/' + taskId)
    } else {
      message.error('创建失败')
    }
  } catch (e) {
    message.error(e?.message || '创建失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form-card { max-width: 600px; margin-top: 16px; }
.form-hint { font-size: 12px; color: #888; margin-top: 6px; }
</style>
