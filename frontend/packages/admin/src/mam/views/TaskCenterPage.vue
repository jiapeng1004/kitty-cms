<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import * as taskApi from '@/mam/api/mam_task_api'

const resourceId = ref('')
const loading = ref(false)
const rows = ref<taskApi.MaterialResourceTaskVO[]>([])

const enqueueForm = ref({
  resourceId: '',
  inputType: 'HTTP',
  inputPath: '',
  strategyId: '',
  priority: 5
})

async function loadTasks() {
  if (!resourceId.value.trim()) {
    message.warning('请输入资源 ID')
    return
  }
  loading.value = true
  try {
    rows.value = await taskApi.listTasksByResource(resourceId.value.trim())
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '加载任务失败')
  } finally {
    loading.value = false
  }
}

async function onEnqueue() {
  if (!enqueueForm.value.resourceId.trim()) {
    message.warning('请填写资源 ID')
    return
  }
  try {
    await taskApi.enqueueTranscode({
      resourceId: enqueueForm.value.resourceId.trim(),
      inputType: enqueueForm.value.inputType || undefined,
      inputPath: enqueueForm.value.inputPath || undefined,
      strategyId: enqueueForm.value.strategyId || undefined,
      priority: enqueueForm.value.priority
    })
    message.success('已入队转码')
    resourceId.value = enqueueForm.value.resourceId.trim()
    await loadTasks()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '入队失败')
  }
}

async function onRetry(taskId: string) {
  try {
    await taskApi.retryTranscode(taskId)
    message.success('已发起重试')
    await loadTasks()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '重试失败')
  }
}
</script>

<template>
  <div style="padding: 20px;">
    <a-typography-title :level="4">资源任务中心</a-typography-title>

    <a-card title="按资源查询任务" size="small" style="margin-bottom: 16px;">
      <a-space wrap>
        <a-input v-model:value="resourceId" style="width: 320px" placeholder="resourceId" />
        <a-button type="primary" :loading="loading" @click="loadTasks">查询</a-button>
      </a-space>
    </a-card>

    <a-card title="转码入队" size="small" style="margin-bottom: 16px;">
      <a-form layout="vertical" style="max-width: 520px;">
        <a-form-item label="资源 ID">
          <a-input v-model:value="enqueueForm.resourceId" />
        </a-form-item>
        <a-form-item label="inputType（空默认 HTTP）">
          <a-input v-model:value="enqueueForm.inputType" placeholder="HTTP / DISK" />
        </a-form-item>
        <a-form-item label="inputPath">
          <a-input v-model:value="enqueueForm.inputPath" placeholder="路径或 URL" />
        </a-form-item>
        <a-form-item label="strategyId（可选）">
          <a-input v-model:value="enqueueForm.strategyId" />
        </a-form-item>
        <a-form-item label="priority（1–10）">
          <a-input-number v-model:value="enqueueForm.priority" :min="1" :max="10" style="width: 100%" />
        </a-form-item>
        <a-button type="primary" @click="onEnqueue">入队转码</a-button>
      </a-form>
    </a-card>

    <a-table
      :data-source="rows"
      :loading="loading"
      row-key="id"
      size="small"
      :pagination="false"
    >
      <a-table-column title="任务 ID" data-index="id" />
      <a-table-column title="类型" data-index="taskType" />
      <a-table-column title="状态" data-index="status" />
      <a-table-column title="进度" data-index="progress" />
      <a-table-column title="策略" data-index="strategyName" />
      <a-table-column title="操作" key="op">
        <template #default="{ record }">
          <a-button type="link" size="small" @click="onRetry(record.id)">重试转码</a-button>
        </template>
      </a-table-column>
    </a-table>
  </div>
</template>
