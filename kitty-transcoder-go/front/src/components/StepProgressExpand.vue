<template>
  <div class="step-expand">
    <a-spin v-if="loading" size="small" />
    <div v-else-if="!displayList?.length" class="step-placeholder">暂无分步数据</div>
    <div v-else class="step-list">
      <div v-for="s in displayList" :key="s.stepId" class="step-item">
        <a-tag :color="stepStatusColor(s.status)">{{ s.name || s.stepId }}</a-tag>
        <span v-if="formatDepends(s.depends)" class="step-depends" title="步骤依赖关系">↳ {{ formatDepends(s.depends) }}</span>
        <span class="step-status">{{ stepStatusText(s.status) }}</span>
        <template v-if="s.status === 'processing'">
          <a-progress type="circle" :size="24" :percent="stepPercent(s)" />
          <span class="step-pct">{{ stepPercent(s) }}%</span>
        </template>
        <template v-else-if="s.status === 'completed'">
          <a-tag color="success">完成</a-tag>
          <span class="step-pct">{{ stepPercent(s) }}%</span>
        </template>
        <template v-else-if="s.status === 'failed'">
          <a-tag color="error">失败</a-tag>
        </template>
        <a-tag v-else>待执行 0%</a-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { getProgress } from '../api/transcoder_api'

const props = defineProps({
  taskId: { type: String, required: true },
  stepProgressMap: { type: Object, default: () => ({}) }
})

const loading = ref(false)
const stepList = ref([])

const displayList = computed(() => {
  const fromMap = props.stepProgressMap?.[props.taskId]
  if (fromMap?.length) return fromMap
  return stepList.value
})

function stepPercent(s) {
  const v = s.progress
  if (v != null && typeof v === 'number') return v
  if (typeof v === 'string') return parseInt(v, 10) || 0
  return s.status === 'completed' ? 100 : 0
}

async function fetchProgress() {
  if (!props.taskId) return
  loading.value = true
  stepList.value = []
  try {
    const p = await getProgress(props.taskId)
    stepList.value = p.stepProgressList ?? []
  } catch {
    stepList.value = []
  } finally {
    loading.value = false
  }
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

watch(() => props.taskId, fetchProgress, { immediate: true })
</script>

<style scoped>
.step-expand { padding: 8px 0 8px 48px; min-height: 32px; }
.step-list { display: flex; flex-direction: column; gap: 6px; }
.step-item { display: flex; align-items: center; gap: 8px; }
.step-status { font-size: 12px; color: #666; margin-right: 4px; }
.step-depends { font-size: 12px; color: #666; margin-right: 8px; padding: 2px 6px; background: #f0f0f0; border-radius: 4px; }
.step-pct { font-size: 12px; color: #666; min-width: 36px; }
.step-placeholder { color: #999; font-size: 12px; }
</style>
