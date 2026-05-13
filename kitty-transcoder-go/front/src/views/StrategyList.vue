<template>
  <div>
    <a-space class="mb">
      <a-button type="primary" @click="openCreate">新建策略</a-button>
      <a-button @click="showImportModal = true">导入</a-button>
    </a-space>
    <a-table :columns="columns" :data-source="list" :loading="loading" row-key="id">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'id'">
          <span v-if="editingIdRow !== record.id" class="id-cell" title="双击修改" @dblclick="startEditId(record)">{{ record.id }}</span>
          <a-input
            v-else
            ref="idInputRef"
            :default-value="record.id"
            size="small"
            class="id-edit-input"
            @blur="(e) => finishEditId(record, e.target.value)"
            @keydown.enter="(e) => { e.preventDefault(); finishEditId(record, e.target.value) }"
          />
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openEdit(record)">编辑</a>
            <a @click="onCopy(record)">复制</a>
            <a @click="onExport(record)">导出</a>
            <a-popconfirm title="确定删除该策略？删除后无法恢复。" ok-text="删除" cancel-text="取消" @confirm="onDelete(record.id)">
              <a class="danger">删除</a>
            </a-popconfirm>
          </a-space>
        </template>
        <template v-else>
          {{ record[column.dataIndex] }}
        </template>
      </template>
    </a-table>
    <a-modal v-model:open="showModal" :title="editingId ? '编辑策略' : '新建策略'" :ok-text="editingId ? '保存' : '创建'" width="720" @ok="submitForm" :confirm-loading="submitting" @cancel="closeModal">
      <a-form :model="stratForm" layout="vertical" ref="formRef">
        <a-form-item label="策略名称" name="name" required :rules="[{ required: true, message: '请填写名称' }]">
          <a-input v-model:value="stratForm.name" placeholder="如：多档位+水印" @blur="stratForm.name = (stratForm.name || '').trim()" />
        </a-form-item>
        <a-form-item label="工作目录（可选）" name="workDir">
          <a-input
            v-model:value="stratForm.workDir"
            placeholder="如：/mnt/nas-a/transcoder 或 C:\transcoder-a，不填则使用全局配置"
            @blur="stratForm.workDir = (stratForm.workDir || '').trim()"
          />
        </a-form-item>
        <a-form-item label="步骤（依赖留空=无依赖、可并行；填 1 或 1,2 等=等待该步骤完成后再执行，输入来源由输入模板指定：$TASK_INPUT=任务整体输入，$STEP_OUTPUT_N=步骤 N 的输出）">
          <div v-for="(step, index) in stratForm.steps" :key="step._key ?? index" class="step-card">
            <a-button type="link" size="small" class="insert-step-above" @click="insertStepAt(index)">+ 在此后插入</a-button>
            <a-card size="small" :title="`步骤 ${step.stepId ?? index + 1}：${stepTypeLabel(step.type)}`" class="step-item">
              <template #extra>
                <a-button type="text" danger size="small" :disabled="stratForm.steps.length <= 1" @click="removeStep(index)">删除</a-button>
              </template>
              <a-form-item label="类型">
                <a-radio-group v-model:value="step.type" size="small">
                  <a-radio value="transcode">转码</a-radio>
                  <a-radio value="extract_frames">抽帧</a-radio>
                  <a-radio value="sprite">雪碧图</a-radio>
                  <a-radio value="image_convert">图片转换</a-radio>
                </a-radio-group>
              </a-form-item>
              <a-form-item :name="['steps', index, 'depends']" :label="'依赖步骤（步骤 ' + (step.stepId ?? index + 1) + '）'" :rules="dependsRules">
                <a-input
                  v-model:value="step.depends"
                  placeholder="留空=无依赖；填 1 或 1,2 等=等待该步骤完成"
                  size="small"
                  allow-clear
                  @blur="step.depends = (step.depends || '').trim()"
                />
              </a-form-item>
              <a-form-item label="输入模板（可选）">
                <TemplateVariableInput
                  v-model="step.inputTemplate"
                  placeholder="$TASK_INPUT 或 $STEP_OUTPUT_1、$STEP_OUTPUT_2…"
                  size="small"
                  :step-count="stratForm.steps.length"
                  @blur="step.inputTemplate = (step.inputTemplate || '').trim()"
                />
              </a-form-item>
              <a-form-item label="输出模板（可选）">
                <TemplateVariableInput
                  v-model="step.outputTemplate"
                  placeholder="$WORK_DIR/$DATE/$TASK_ID、$DATE_TIME=年/月/日/时分秒"
                  size="small"
                  :step-count="stratForm.steps.length"
                  @blur="step.outputTemplate = (step.outputTemplate || '').trim()"
                />
              </a-form-item>
              <template v-if="step.type === 'transcode'">
                <a-row :gutter="12">
                  <a-col :span="12">
                    <a-form-item label="目标格式">
                      <a-select v-model:value="step.targetFormat" size="small" style="width:100%">
                        <a-select-option value="mp4">mp4</a-select-option>
                        <a-select-option value="webm">webm</a-select-option>
                        <a-select-option value="mkv">mkv</a-select-option>
                        <a-select-option value="avi">avi</a-select-option>
                        <a-select-option value="mov">mov</a-select-option>
                      </a-select>
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item :name="['steps', index, 'resolution']" label="分辨率" :rules="resolutionRules(step)">
                      <a-space direction="vertical" style="width:100%" :size="4">
                        <a-select v-model:value="step.resolution" size="small" style="width:100%" placeholder="选择分辨率" show-search :filter-option="filterOption" @change="(v) => v !== 'custom' && (step.resolutionCustom = '')">
                          <a-select-option v-for="r in getResolutionOptions(step)" :key="r.value" :value="r.value">{{ r.label }}</a-select-option>
                          <a-select-option value="custom">自定义…</a-select-option>
                        </a-select>
                        <a-input v-if="step.resolution === 'custom'" v-model:value="step.resolutionCustom" size="small" placeholder="如 1920x1080" @blur="step.resolutionCustom = (step.resolutionCustom || '').trim()" />
                      </a-space>
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item :name="['steps', index, 'bitrate']" label="码率(kbps)" :rules="[{ required: true, message: '请填写码率' }, { type: 'number', min: 100, max: 50000, message: '100-50000' }]">
                      <a-input-number v-model:value="step.bitrate" :min="100" :max="50000" style="width:100%" size="small" placeholder="5000" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="帧率(fps)">
                      <a-select v-model:value="step.frameRate" size="small" style="width:100%">
                        <a-select-option :value="24">24</a-select-option>
                        <a-select-option :value="25">25</a-select-option>
                        <a-select-option :value="30">30</a-select-option>
                        <a-select-option :value="60">60</a-select-option>
                      </a-select>
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="编码器">
                      <a-select v-model:value="step.encoder" size="small" style="width:100%" placeholder="选择编码器">
                        <a-select-option value="h264">H.264</a-select-option>
                        <a-select-option value="libx265">H.265/HEVC（体积小，部分浏览器支持）</a-select-option>
                      </a-select>
                    </a-form-item>
                  </a-col>
                </a-row>
              </template>
              <template v-else-if="step.type === 'extract_frames'">
                <a-row :gutter="12">
                  <a-col :span="8">
                    <a-form-item :name="['steps', index, 'frameInterval']" label="抽帧间隔（每隔多少帧取一帧）" :rules="[{ type: 'number', min: 1, max: 300, message: '1-300' }]">
                      <a-input-number v-model:value="step.frameInterval" :min="1" :max="300" style="width:100%" size="small" placeholder="30" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <a-form-item :name="['steps', index, 'extractFrameCount']" label="抽取帧数" :rules="extractFrameCountRules">
                      <a-input-number v-model:value="step.extractFrameCount" :min="1" :max="100" style="width:100%" size="small" placeholder="不填默认1帧" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <a-form-item label="输出格式"><a-select v-model:value="step.extractOutputFormat" size="small" style="width:100%">
                  <a-select-option value="jpg">jpg</a-select-option>
                  <a-select-option value="png">png</a-select-option>
                </a-select></a-form-item>
                  </a-col>
                </a-row>
              </template>
              <template v-else-if="step.type === 'sprite'">
                <a-row :gutter="12">
                  <a-col :span="8">
                    <a-form-item :name="['steps', index, 'spriteColumns']" label="列数" :rules="[{ type: 'number', min: 1, max: 20, message: '1-20' }]">
                      <a-input-number v-model:value="step.spriteColumns" :min="1" :max="20" style="width:100%" size="small" placeholder="4" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <a-form-item :name="['steps', index, 'spriteRows']" label="行数" :rules="[{ type: 'number', min: 1, max: 20, message: '1-20' }]">
                      <a-input-number v-model:value="step.spriteRows" :min="1" :max="20" style="width:100%" size="small" placeholder="3" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <a-form-item :name="['steps', index, 'spriteScale']" label="缩放倍数">
                      <a-input-number v-model:value="step.spriteScale" :min="1" :max="32" style="width:100%" size="small" placeholder="默认为4" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <div class="sprite-count-hint">共 {{ (step.spriteColumns ?? 4) * (step.spriteRows ?? 3) }} 张，均匀分布</div>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="输出格式"><a-select v-model:value="step.extractOutputFormat" size="small" style="width:100%">
                  <a-select-option value="jpg">jpg</a-select-option>
                  <a-select-option value="png">png</a-select-option>
                </a-select></a-form-item>
                  </a-col>
                </a-row>
              </template>
              <template v-else-if="step.type === 'image_convert'">
                <a-row :gutter="12">
                  <a-col :span="8">
                    <a-form-item label="目标格式">
                      <a-select v-model:value="step.imageTargetFormat" size="small" style="width:100%">
                        <a-select-option value="webp">webp</a-select-option>
                        <a-select-option value="jpg">jpg</a-select-option>
                        <a-select-option value="png">png</a-select-option>
                        <a-select-option value="avif">avif</a-select-option>
                      </a-select>
                    </a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <a-form-item label="质量(1-100)">
                      <a-input-number v-model:value="step.imageQuality" :min="1" :max="100" style="width:100%" size="small" placeholder="85" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <a-form-item label="缩放(可选)">
                      <a-input v-model:value="step.imageResize" size="small" placeholder="800x600、800x、x600" @blur="step.imageResize = (step.imageResize || '').trim()" />
                    </a-form-item>
                  </a-col>
                </a-row>
              </template>
            </a-card>
          </div>
          <a-button type="dashed" block class="add-step-btn" @click="addStep">+ 添加步骤</a-button>
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="showImportModal" title="导入策略" :footer="null" @cancel="importContent = ''">
      <a-upload
        :before-upload="beforeImportUpload"
        :show-upload-list="false"
        accept=".yaml,.yml"
      >
        <a-button>选择文件 (.yaml)</a-button>
      </a-upload>
      <a-textarea
        v-model:value="importContent"
        placeholder="或粘贴 YAML 内容"
        :rows="8"
        class="import-textarea"
      />
      <a-button type="primary" block :loading="importing" :disabled="!importContent?.trim()" @click="doImport">导入</a-button>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { listStrategies, getStrategy, createStrategy, updateStrategy, deleteStrategy, exportStrategy, importStrategy, updateStrategyId } from '../api/strategy_api'
import TemplateVariableInput from '../components/TemplateVariableInput.vue'

function stepTypeLabel(type) {
  const map = { transcode: '转码', extract_frames: '抽帧', sprite: '雪碧图', image_convert: '图片转换' }
  return map[type] || type || '转码'
}

let _stepKey = 0
function defaultStep() {
  return {
    _key: `step_${++_stepKey}_${Date.now()}`,
    type: 'transcode',
    depends: '',
    inputTemplate: '',
    outputTemplate: '',
    targetFormat: 'mp4',
    resolution: '1920x1080',
    resolutionCustom: '',
    bitrate: 5000,
    frameRate: 30,
    encoder: 'h264',
    frameInterval: 30,
    extractFrameCount: undefined,
    extractOutputFormat: 'jpg',
    spriteColumns: 4,
    spriteRows: 3,
    spriteScale: 4,
    imageTargetFormat: 'webp',
    imageQuality: 85,
    imageResize: ''
  }
}

const loading = ref(false)
const list = ref([])
const showModal = ref(false)
const showImportModal = ref(false)
const importContent = ref('')
const importing = ref(false)
const submitting = ref(false)
const editingIdRow = ref(null)
const idInputRef = ref(null)
const editingId = ref(null)
const formRef = ref(null)
const stratForm = reactive({
  name: '',
  workDir: '',
  steps: [defaultStep()]
})

const resolutionOptions = [
  { label: '3840×2160 (4K)', value: '3840x2160' },
  { label: '1920×1080 (1080p)', value: '1920x1080' },
  { label: '1280×720 (720p)', value: '1280x720' },
  { label: '854×480 (480p)', value: '854x480' },
  { label: '640×360 (360p)', value: '640x360' }
]

function filterOption(input, option) {
  const v = option?.value ?? ''
  return v.toLowerCase().includes((input || '').toLowerCase())
}

function getResolutionOptions(step) {
  const val = step?.resolution
  if (!val || val === 'custom' || resolutionOptions.some((r) => r.value === val)) return resolutionOptions
  return [...resolutionOptions, { label: val, value: val }]
}

const dependsRules = [
  {
    validator: (_rule, value, callback) => {
      const s = String(value ?? '').trim()
      if (!s) return callback()
      if (/^(\d+)(,\d+)*$/.test(s)) return callback()
      callback(new Error('格式如 1 或 1,2'))
    }
  }
]

const extractFrameCountRules = [
  {
    validator: (_rule, value, callback) => {
      if (value == null || value === '') return callback()
      const n = Number(value)
      if (isNaN(n) || n < 1 || n > 100) return callback(new Error('1-100'))
      callback()
    }
  }
]

function resolutionRules(step) {
  return [
    {
      validator: (_rule, value, callback) => {
        const v = step?.resolution === 'custom' ? step?.resolutionCustom : value
        if (!v) return callback(new Error('请选择或输入分辨率'))
        if (!/^\d+[xX×]\d+$/.test(v)) return callback(new Error('格式如 1920x1080'))
        callback()
      }
    }
  ]
}

const columns = [
  { title: '策略ID', dataIndex: 'id', key: 'id', ellipsis: true, width: 100 },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '工作目录', dataIndex: 'workDir', key: 'workDir', ellipsis: true },
  { title: '步骤数', dataIndex: 'stepCount', key: 'stepCount', width: 90 },
  { title: '操作', key: 'action', width: 180 }
]

function addStep() {
  const newIdx = stratForm.steps.length
  const s = defaultStep()
  s.stepId = newIdx + 1
  if (newIdx >= 1) s.depends = String(newIdx) // 新步骤默认依赖前一步
  stratForm.steps.push(s)
}

/** 在 index 之后插入新步骤，其后步骤号及依赖自动递增 */
function insertStepAt(index) {
  const insertPos = index + 1
  const s = defaultStep()
  s.stepId = insertPos + 1
  s.depends = insertPos >= 1 ? String(insertPos) : ''
  stratForm.steps.splice(insertPos, 0, s)
  for (let i = 0; i < stratForm.steps.length; i++) {
    stratForm.steps[i].stepId = i + 1
  }
  shiftDependsAfterInsert(insertPos)
}

function shiftDependsAfterInsert(insertPos) {
  const before = insertPos + 1
  for (const step of stratForm.steps) {
    const d = (step.depends || '').trim()
    if (!d) continue
    const parts = d.split(',').map((x) => parseInt(x.trim(), 10)).filter((n) => !isNaN(n))
    const shifted = parts.map((n) => (n >= before ? n + 1 : n))
    step.depends = shifted.join(',')
  }
}

function removeStep(index) {
  if (stratForm.steps.length <= 1) return
  stratForm.steps.splice(index, 1)
  for (let i = 0; i < stratForm.steps.length; i++) {
    stratForm.steps[i].stepId = i + 1
  }
  shiftDependsAfterRemove(index)
}

function shiftDependsAfterRemove(removedIndex) {
  const before = removedIndex + 1
  for (const step of stratForm.steps) {
    const d = (step.depends || '').trim()
    if (!d) continue
    const parts = d.split(',').map((x) => parseInt(x.trim(), 10)).filter((n) => !isNaN(n))
    const shifted = parts
      .map((n) => {
        if (n > before) return n - 1
        if (n === before) return null
        return n
      })
      .filter((n) => n != null)
    step.depends = shifted.join(',')
  }
}

function resetForm() {
  stratForm.name = ''
  stratForm.workDir = ''
  stratForm.steps = [defaultStep()]
  editingId.value = null
}

async function openCreate() {
  resetForm()
  showModal.value = true
}

async function openEdit(record) {
  editingId.value = record.id
  showModal.value = true
  try {
    const data = await getStrategy(record.id)
    if (data && data.steps && data.steps.length) {
      stratForm.name = data.name ?? ''
      stratForm.workDir = data.workDir ?? ''
      stratForm.steps = data.steps.map((s, i) => ({
        _key: `step_${++_stepKey}_${Date.now()}_${i}`,
        stepId: s.stepId,
        type: s.type || 'transcode',
        depends: s.depends ?? '',
        inputTemplate: s.inputTemplate ?? '',
        outputTemplate: s.outputTemplate ?? '',
        targetFormat: s.targetFormat ?? 'mp4',
        resolution: resolutionOptions.some((r) => r.value === s.resolution) ? (s.resolution ?? '1920x1080') : (s.resolution ? 'custom' : '1920x1080'),
        resolutionCustom: resolutionOptions.some((r) => r.value === s.resolution) ? '' : (s.resolution || ''),
        bitrate: s.bitrate ?? 5000,
        frameRate: s.frameRate ?? 30,
        encoder: s.encoder ?? 'h264',
        frameInterval: s.frameInterval ?? 30,
        extractFrameCount: s.extractFrameCount ?? undefined,
        extractOutputFormat: s.extractOutputFormat ?? 'jpg',
        spriteColumns: s.spriteColumns ?? 4,
        spriteRows: s.spriteRows ?? 3,
        spriteScale: s.spriteScale ?? 4,
        imageTargetFormat: s.imageTargetFormat ?? 'webp',
        imageQuality: s.imageQuality ?? 85,
        imageResize: s.imageResize ?? ''
      }))
    } else {
      stratForm.name = data?.name ?? ''
      stratForm.steps = [defaultStep()]
    }
  } catch {
    message.error('加载策略失败')
    closeModal()
  }
}

function closeModal() {
  showModal.value = false
  resetForm()
}

async function submitForm() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  if (!stratForm.name?.trim()) {
    message.warning('请填写策略名称')
    return
  }
  const payload = {
    name: (stratForm.name || '').trim(),
    workDir: (stratForm.workDir || '').trim() || undefined,
    steps: stratForm.steps.map(s => {
      const { _key, ...rest } = s
      return {
        type: rest.type || 'transcode',
      depends: (rest.depends || '').trim() || undefined,
        inputTemplate: (rest.inputTemplate || '').trim() || undefined,
        outputTemplate: (rest.outputTemplate || '').trim() || undefined,
        targetFormat: rest.targetFormat || undefined,
        resolution: (rest.resolution === 'custom' ? (rest.resolutionCustom || '').trim() : rest.resolution) || undefined,
        bitrate: rest.bitrate ?? undefined,
        frameRate: rest.frameRate ?? undefined,
        encoder: rest.encoder || undefined,
        frameInterval: rest.frameInterval ?? undefined,
        extractFrameCount: rest.extractFrameCount ?? undefined,
        extractOutputFormat: rest.extractOutputFormat || undefined,
        spriteColumns: rest.spriteColumns ?? undefined,
        spriteRows: rest.spriteRows ?? undefined,
        spriteScale: rest.spriteScale ?? undefined,
        imageTargetFormat: rest.imageTargetFormat || undefined,
        imageQuality: rest.imageQuality ?? undefined,
        imageResize: (rest.imageResize || '').trim() || undefined
      }
    })
  }
  submitting.value = true
  try {
    if (editingId.value) {
      await updateStrategy(editingId.value, payload)
      message.success('更新成功')
    } else {
      await createStrategy(payload)
      message.success('创建成功')
    }
    showModal.value = false
    resetForm()
    load()
  } catch (e) {
    message.error(e?.message || (editingId.value ? '更新失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

async function onDelete(id) {
  try {
    await deleteStrategy(id)
    message.success('已删除')
    load()
  } catch (e) {
    message.error(e?.message || '删除失败')
  }
}

function startEditId(record) {
  editingIdRow.value = record.id
  nextTick(() => {
    const el = idInputRef.value?.$el?.querySelector?.('input') || idInputRef.value?.$el
    el?.focus?.()
  })
}

function finishEditId(record, newVal) {
  editingIdRow.value = null
  const v = (newVal || '').trim()
  if (!v || v === String(record.id)) return
  updateStrategyId(record.id, v).then(() => {
    message.success('策略ID已修改')
    load()
  }).catch((e) => {
    const msg = typeof e === 'string' ? e : (e?.message || e?.error || '修改失败')
    message.error(msg)
  })
}

function beforeImportUpload(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    importContent.value = e.target?.result || ''
  }
  reader.readAsText(file, 'UTF-8')
  return false
}

async function doImport() {
  if (!importContent.value?.trim()) return
  importing.value = true
  try {
    const id = await importStrategy(importContent.value.trim())
    message.success('导入成功，策略ID: ' + id)
    showImportModal.value = false
    importContent.value = ''
    load()
  } catch (e) {
    message.error(e?.message || '导入失败')
  } finally {
    importing.value = false
  }
}

async function onExport(record) {
  try {
    const content = await exportStrategy(record.id)
    const baseName = (record.name || 'strategy').replace(/[^\w\u4e00-\u9fa5-]/g, '_')
    const name = (record.id || '') + '_' + baseName + '.yaml'
    const blob = new Blob([content], { type: 'text/yaml' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = name
    a.click()
    URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (e) {
    message.error(e?.message || '导出失败')
  }
}

async function onCopy(record) {
  try {
    const data = await getStrategy(record.id)
    if (!data) {
      message.error('加载策略失败')
      return
    }
    const payload = {
      name: ((data.name || '').trim() || '未命名') + ' - 副本',
      workDir: (data.workDir || '').trim() || undefined,
      steps: (data.steps || []).map(s => ({
        type: s.type || 'transcode',
        depends: (s.depends || '').trim() || undefined,
        inputTemplate: (s.inputTemplate || '').trim() || undefined,
        outputTemplate: (s.outputTemplate || '').trim() || undefined,
        targetFormat: s.targetFormat || undefined,
        resolution: s.resolution || undefined,
        bitrate: s.bitrate ?? undefined,
        frameRate: s.frameRate ?? undefined,
        encoder: s.encoder || undefined,
        frameInterval: s.frameInterval ?? undefined,
        extractFrameCount: s.extractFrameCount ?? undefined,
        extractOutputFormat: s.extractOutputFormat || undefined,
        spriteColumns: s.spriteColumns ?? undefined,
        spriteRows: s.spriteRows ?? undefined,
        spriteScale: s.spriteScale ?? undefined,
        imageTargetFormat: s.imageTargetFormat || undefined,
        imageQuality: s.imageQuality ?? undefined,
        imageResize: (s.imageResize || '').trim() || undefined,
        condition: (s.condition || '').trim() || undefined,
        strategyIdWhenTrue: s.strategyIdWhenTrue || undefined,
        strategyIdWhenFalse: s.strategyIdWhenFalse || undefined
      }))
    }
    if (payload.steps.length === 0) {
      payload.steps = [{ type: 'transcode', targetFormat: 'mp4', resolution: '1920x1080', bitrate: 5000, frameRate: 30, encoder: 'h264' }]
    }
    await createStrategy(payload)
    message.success('复制成功')
    load()
  } catch (e) {
    message.error(e?.message || '复制失败')
  }
}

async function load() {
  loading.value = true
  try {
    const data = await listStrategies()
    list.value = Array.isArray(data) ? data : []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.mb { margin-bottom: 16px; }
.step-card { margin-bottom: 12px; position: relative; }
.insert-step-above {
  margin-bottom: 4px;
  padding: 0 4px;
  height: 24px;
  font-size: 12px;
}
.step-item { margin-bottom: 8px; }
.add-step-btn { margin-top: 8px; }
.danger { color: var(--ant-color-error); }
.sprite-count-hint { padding: 4px 0; font-size: 12px; color: var(--ant-color-text-secondary); }
.import-textarea { margin: 12px 0; }
.id-cell { cursor: pointer; }
.id-edit-input { width: 80px; }
</style>
