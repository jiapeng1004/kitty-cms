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
          <a-input v-model:value="form.inputPath" :placeholder="form.inputType === 'HTTP' ? 'https://...' : '/path/to/video.mp4'" />
        </a-form-item>
        <a-form-item label="策略" name="strategyId" :rules="[{ required: true }]">
          <a-select v-model:value="form.strategyId" placeholder="选择策略" allow-clear show-search :options="strategyOptions" :field-names="{ label: 'name', value: 'id' }" />
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
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listStrategies } from '../api/strategy_api'
import { createTask } from '../api/transcoder_api'

const router = useRouter()
const loading = ref(false)
const strategyOptions = ref([])
const form = reactive({ inputType: 'DISK', inputPath: '', strategyId: undefined, priority: 5 })

onMounted(async () => {
  try {
    const list = await listStrategies()
    strategyOptions.value = Array.isArray(list) ? list : []
  } catch {
    strategyOptions.value = []
  }
})

async function onSubmit() {
  try {
    loading.value = true
    const id = await createTask({ ...form, inputPath: form.inputPath || form.inputFile })
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
