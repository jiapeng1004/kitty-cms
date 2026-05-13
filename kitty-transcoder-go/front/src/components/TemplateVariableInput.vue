<template>
  <div class="template-variable-input" ref="containerRef">
    <a-input
      ref="inputRef"
      :value="modelValue"
      :placeholder="placeholder"
      :size="size"
      allow-clear
      @update:value="onInput"
      @keydown="onKeydown"
      @blur="onBlur"
    >
      <template #addonAfter>
        <a-dropdown v-model:open="dropdownOpen" trigger="click" placement="bottomRight">
          <a-button type="link" size="small" class="insert-btn">插入变量</a-button>
          <template #overlay>
            <div class="variable-dropdown">
              <div class="variable-dropdown-title">点击插入，支持 Tab 补全（输入 $ 后按 Tab）</div>
              <div class="variable-list">
                <a-tooltip v-for="v in displayVariables" :key="v.name" placement="left">
                  <template #title>
                    <span class="variable-tooltip-title">{{ v.name }}</span>
                    <div class="variable-tooltip-desc">{{ v.desc }}</div>
                  </template>
                  <div class="variable-item" @click="insertVariable(v.name)">
                    <code>{{ v.name }}</code>
                    <span class="variable-desc">{{ v.shortDesc }}</span>
                  </div>
                </a-tooltip>
              </div>
            </div>
          </template>
        </a-dropdown>
      </template>
    </a-input>
    <!-- Tab 补全浮层：输入 $ 时显示 -->
    <div v-if="suggestOpen && filteredSuggestions.length" class="suggest-overlay" :style="suggestStyle">
      <div
        v-for="(v, i) in filteredSuggestions"
        :key="v.name"
        class="suggest-item"
        :class="{ active: suggestIndex === i }"
        @mousedown.prevent="insertVariable(v.name); suggestOpen = false"
      >
        <code>{{ v.name }}</code>
        <span class="suggest-desc">{{ v.shortDesc }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  size: { type: String, default: 'small' },
  /** 步骤总数，用于生成 $STEP_OUTPUT_1 ~ $STEP_OUTPUT_N */
  stepCount: { type: Number, default: 4 }
})

const emit = defineEmits(['update:modelValue', 'blur'])

const TEMPLATE_VARIABLES = [
  {
    name: '$TASK_INPUT',
    shortDesc: '任务整体输入',
    desc: '任务整体的输入路径，即用户提交的原始文件或 URL。依赖步骤 N 仅表示等待该步骤完成，输入来源需在输入模板中显式指定：用 $TASK_INPUT 表示用任务原始输入，用 $STEP_OUTPUT_N 表示用步骤 N 的输出'
  },
  { name: '$TASK_ID', shortDesc: '任务ID', desc: '当前任务的唯一标识符' },
  { name: '$WORK_DIR', shortDesc: '工作目录', desc: '策略配置的工作目录，用于输出文件的基础路径' },
  { name: '$DATE', shortDesc: '年/月/日', desc: '执行时的日期，格式为 2026/03/03，适合作为文件夹层级' },
  { name: '$TIME', shortDesc: '时分秒', desc: '执行时的时分秒，格式为 143052' },
  { name: '$DATE_TIME', shortDesc: '日期+时间', desc: '年/月/日/时分秒，如 2026/03/03/143052' },
  { name: '$STEP_INDEX', shortDesc: '当前步骤序号', desc: '当前步骤的序号（1、2、3…），由执行引擎自动填入' }
]

const inputRef = ref(null)
const containerRef = ref(null)
const dropdownOpen = ref(false)
const suggestOpen = ref(false)
const suggestIndex = ref(0)
const cursorPos = ref(0)

const displayVariables = computed(() => {
  const base = [...TEMPLATE_VARIABLES]
  for (let i = 1; i <= Math.max(props.stepCount, 4); i++) {
    base.push({
      name: `$STEP_OUTPUT_${i}`,
      shortDesc: `步骤${i}输出`,
      desc: `步骤 ${i} 的输出路径。HTTP 输入会本地化后的本地路径，或 HTTP 绝对路径。依赖步骤 ${i} 且要使用其输出时，在输入模板中填 $STEP_OUTPUT_${i}`
    })
  }
  return base
})

const partialToken = computed(() => {
  const val = props.modelValue || ''
  const pos = cursorPos.value
  if (pos <= 0 || val[pos - 1] !== '$') return ''
  let start = pos - 1
  while (start > 0 && /[A-Za-z0-9_$]/.test(val[start - 1])) start--
  return val.slice(start, pos).toUpperCase()
})

const filteredSuggestions = computed(() => {
  const token = partialToken.value
  if (!token) return displayVariables.value
  const upper = token.toUpperCase()
  return displayVariables.value.filter((v) => v.name.toUpperCase().startsWith(upper))
})

const suggestStyle = computed(() => {
  if (!containerRef.value) return {}
  const rect = containerRef.value.getBoundingClientRect()
  return {
    top: `${rect.bottom + 2}px`,
    left: `${rect.left}px`,
    width: `${rect.width}px`
  }
})

function onInput(val) {
  emit('update:modelValue', val || '')
  nextTick(() => {
    const input = getInputEl()
    cursorPos.value = input ? input.selectionStart : (val?.length ?? 0)
    checkSuggestTrigger()
  })
}

function getInputEl() {
  const comp = inputRef.value
  if (!comp) return null
  const el = comp.$el || comp
  return el?.querySelector?.('input') || (el?.tagName === 'INPUT' ? el : null)
}

function checkSuggestTrigger() {
  const val = props.modelValue || ''
  const pos = cursorPos.value
  const beforeCursor = val.slice(0, pos)
  const lastDollar = beforeCursor.lastIndexOf('$')
  if (lastDollar >= 0 && !/[A-Za-z0-9_]/.test(val[lastDollar + 1] || '')) {
    suggestOpen.value = true
    suggestIndex.value = 0
  } else if (lastDollar >= 0) {
    const token = beforeCursor.slice(lastDollar)
    if (/^\$[A-Za-z0-9_]*$/.test(token)) {
      suggestOpen.value = true
      suggestIndex.value = 0
    } else {
      suggestOpen.value = false
    }
  } else {
    suggestOpen.value = false
  }
}

function onKeydown(e) {
  if (!suggestOpen.value || !filteredSuggestions.value.length) return
  if (e.key === 'Tab' || e.key === 'Enter') {
    e.preventDefault()
    insertVariable(filteredSuggestions.value[suggestIndex.value].name)
    suggestOpen.value = false
    return
  }
  if (e.key === 'Escape') {
    suggestOpen.value = false
    return
  }
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    suggestIndex.value = (suggestIndex.value + 1) % filteredSuggestions.value.length
    return
  }
  if (e.key === 'ArrowUp') {
    e.preventDefault()
    suggestIndex.value = (suggestIndex.value - 1 + filteredSuggestions.value.length) % filteredSuggestions.value.length
    return
  }
}

function onBlur(e) {
  suggestOpen.value = false
  emit('blur', e)
}

function insertVariable(name) {
  const input = getInputEl()
  if (!input) return
  const start = input.selectionStart ?? 0
  const end = input.selectionEnd ?? start
  const val = props.modelValue || ''
  const before = val.slice(0, start)
  const after = val.slice(end)
  let newStart = start
  if (partialToken.value) {
    const dollarPos = before.lastIndexOf('$')
    if (dollarPos >= 0) {
      const newBefore = before.slice(0, dollarPos)
      newStart = newBefore.length + name.length
      const newVal = newBefore + name + after
      emit('update:modelValue', newVal)
      dropdownOpen.value = false
      suggestOpen.value = false
      setTimeout(() => {
        input.focus()
        input.setSelectionRange(newStart, newStart)
      }, 0)
      return
    }
  }
  const newVal = before + name + after
  newStart = before.length + name.length
  emit('update:modelValue', newVal)
  dropdownOpen.value = false
  suggestOpen.value = false
  setTimeout(() => {
    input.focus()
    input.setSelectionRange(newStart, newStart)
  }, 0)
}

watch(() => props.modelValue, () => {
  const input = getInputEl()
  if (input) cursorPos.value = input.selectionStart ?? props.modelValue?.length ?? 0
})
</script>

<style scoped>
.template-variable-input {
  position: relative;
}
.insert-btn {
  padding: 0 8px;
  height: 24px;
  font-size: 12px;
}
.variable-dropdown {
  min-width: 320px;
  max-height: 360px;
  overflow-y: auto;
  padding: 8px 0;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
@media (prefers-color-scheme: dark) {
  .variable-dropdown {
    background: rgba(30, 30, 30, 0.92);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  }
}
.variable-dropdown-title {
  padding: 4px 12px 8px;
  font-size: 12px;
  color: var(--ant-color-text-secondary);
}
.variable-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.variable-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  cursor: pointer;
  font-size: 13px;
}
.variable-item:hover {
  background: var(--ant-color-primary-bg);
}
.variable-item code {
  font-size: 12px;
  color: var(--ant-color-primary);
  min-width: 140px;
}
.variable-desc {
  font-size: 12px;
  color: var(--ant-color-text-secondary);
}
.variable-tooltip-title {
  font-weight: 600;
}
.variable-tooltip-desc {
  font-size: 12px;
  margin-top: 4px;
  max-width: 260px;
}
.suggest-overlay {
  position: fixed;
  z-index: 1050;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  max-height: 240px;
  overflow-y: auto;
  padding: 4px 0;
}
@media (prefers-color-scheme: dark) {
  .suggest-overlay {
    background: rgba(30, 30, 30, 0.92);
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  }
}
.suggest-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  cursor: pointer;
  font-size: 13px;
}
.suggest-item:hover,
.suggest-item.active {
  background: var(--ant-color-primary-bg);
}
.suggest-item code {
  font-size: 12px;
  color: var(--ant-color-primary);
  min-width: 140px;
}
.suggest-desc {
  font-size: 12px;
  color: var(--ant-color-text-secondary);
}
</style>
