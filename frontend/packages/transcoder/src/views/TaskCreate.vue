<template>
  <div>
    <a-page-header title="新建转码任务" @back="$router.push('/tasks')" />
    <a-card class="form-card">
      <a-form :model="form" layout="vertical" @finish="onSubmit">
        <a-form-item label="输入类型" name="inputType">
          <a-radio-group v-model:value="form.inputType">
            <a-radio value="DISK">磁盘路径</a-radio>
            <a-radio value="HTTP">HTTP 地址</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :label="form.inputType === 'HTTP' ? '输入 URL' : '输入路径'" name="inputPath" :rules="[{ required: true, message: '请输入' }]">
          <a-input v-model:value="form.inputPath" :placeholder="form.inputType === 'HTTP' ? 'https://...' : '/path/to/video.mp4'" @blur="form.inputPath = (form.inputPath || '').trim()" />
        </a-form-item>
        <a-form-item label="策略" name="strategyId" :rules="[{ required: true }]">
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
import { reactive, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useForkTaskStore } from '../stores/forkTask'
import { listStrategies } from '../api/strategy_api'
import { createTask } from '../api/transcoder_api'

const router = useRouter()
const loading = ref(false)
const strategyOptions = ref([])
const form = reactive({ inputType: 'DISK', inputPath: '', strategyId: undefined, watermarkUrl: '', watermarkPosition: 'bottom-right', priority: 5 })

function applyForkTask(t) {
  if (!t) return
  form.inputType = t.inputType || 'DISK'
  form.inputPath = t.inputPath || t.inputFile || ''
  form.strategyId = t.strategyId
  form.watermarkUrl = t.watermarkUrl || ''
  form.watermarkPosition = t.watermarkPosition || 'bottom-right'
  form.priority = t.priority ?? 5
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
  // options 渲染完成后再填充，避免 a-select 清空 value
  applyForkTask(forkTask)
})

onBeforeUnmount(() => {
  useForkTaskStore().clear()
})

async function onSubmit() {
  try {
    loading.value = true
    const body = { inputType: form.inputType, inputPath: (form.inputPath || form.inputFile || '').trim(), strategyId: form.strategyId, priority: form.priority }
    if (form.watermarkUrl?.trim()) {
      body.watermarkUrl = form.watermarkUrl.trim()
      body.watermarkPosition = form.watermarkPosition || undefined
    }
    const id = await createTask(body)
    message.success('任务已创建：' + id)
    router.push('/tasks/' + id)
  } catch (e) {
    message.error(e?.message || '创建失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form-card { max-width: 600px; margin-top: 16px; }
</style>
