<template>
  <div>
    <a-space class="mb">
      <a-button type="primary" @click="openCreate">新建策略</a-button>
    </a-space>
    <a-table :columns="columns" :data-source="list" :loading="loading" row-key="id">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a @click="openEdit(record)">编辑</a>
            <a-popconfirm title="确定删除该策略？删除后无法恢复。" ok-text="删除" cancel-text="取消" @confirm="onDelete(record.id)">
              <a class="danger">删除</a>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
    <a-modal v-model:open="showModal" :title="editingId ? '编辑策略' : '新建策略'" :ok-text="editingId ? '保存' : '创建'" width="720" @ok="submitForm" :confirm-loading="submitting" @cancel="closeModal">
      <a-form :model="stratForm" layout="vertical" ref="formRef">
        <a-form-item label="策略名称" name="name" required :rules="[{ required: true, message: '请填写名称' }]">
          <a-input v-model:value="stratForm.name" placeholder="如：多档位+水印" />
        </a-form-item>
        <a-form-item label="步骤（依赖留空=无依赖、用任务输入可并行；填 0 或 0,1 等=依赖该步骤输出）">
          <div v-for="(step, index) in stratForm.steps" :key="index" class="step-card">
            <a-card size="small" :title="`步骤 ${index + 1}：${stepTypeLabel(step.type)}`" class="step-item">
              <template #extra>
                <a-button type="text" danger size="small" :disabled="stratForm.steps.length <= 1" @click="removeStep(index)">删除</a-button>
              </template>
              <a-form-item label="类型">
                <a-radio-group v-model:value="step.type" size="small">
                  <a-radio value="transcode">转码</a-radio>
                  <a-radio value="extract_frames">抽帧</a-radio>
                  <a-radio value="sprite">雪碧图</a-radio>
                </a-radio-group>
              </a-form-item>
              <a-form-item :label="'依赖步骤（步骤 ' + (index + 1) + '）'">
                <a-input
                  v-model:value="step.depends"
                  placeholder="留空=无依赖，用任务输入且可并行；或填 0、0,1 等指定依赖"
                  size="small"
                  allow-clear
                />
              </a-form-item>
              <a-form-item label="输入模板（可选）">
                <a-input
                  v-model:value="step.inputTemplate"
                  placeholder="$TASK_INPUT 或 $STEP_OUTPUT_0、$TASK_ID、$DATE_TIME 等"
                  size="small"
                  allow-clear
                />
              </a-form-item>
              <a-form-item label="输出模板（可选）">
                <a-input
                  v-model:value="step.outputTemplate"
                  placeholder="$WORK_DIR/$TASK_ID/step_$STEP_INDEX、$DATE_TIME 等"
                  size="small"
                  allow-clear
                />
              </a-form-item>
              <template v-if="step.type === 'transcode'">
                <a-row :gutter="12">
                  <a-col :span="12">
                    <a-form-item label="目标格式"><a-input v-model:value="step.targetFormat" placeholder="mp4" size="small" /></a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="分辨率"><a-input v-model:value="step.resolution" placeholder="1920x1080" size="small" /></a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="码率(kbps)"><a-input-number v-model:value="step.bitrate" :min="0" style="width:100%" size="small" /></a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="帧率(fps)"><a-input-number v-model:value="step.frameRate" :min="0" style="width:100%" size="small" /></a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="编码器"><a-input v-model:value="step.encoder" placeholder="h264" size="small" /></a-form-item>
                  </a-col>
                </a-row>
                <a-form-item label="水印（可选，填写则转码后叠加水印）">
                  <a-row :gutter="12">
                    <a-col :span="14">
                      <a-input v-model:value="step.watermarkPath" placeholder="水印图路径，不填则不加" size="small" allow-clear />
                    </a-col>
                    <a-col :span="10">
                      <a-select v-model:value="step.watermarkPosition" placeholder="位置" size="small" style="width:100%">
                        <a-select-option value="bottom-right">右下</a-select-option>
                        <a-select-option value="bottom-left">左下</a-select-option>
                        <a-select-option value="top-right">右上</a-select-option>
                        <a-select-option value="top-left">左上</a-select-option>
                      </a-select>
                    </a-col>
                  </a-row>
                </a-form-item>
              </template>
              <template v-else-if="step.type === 'extract_frames'">
                <a-row :gutter="12">
                  <a-col :span="8">
                    <a-form-item label="抽帧间隔（每隔多少帧取一帧）"><a-input-number v-model:value="step.frameInterval" :min="1" style="width:100%" size="small" placeholder="30" /></a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <a-form-item label="抽取帧数"><a-input-number v-model:value="step.extractFrameCount" :min="1" style="width:100%" size="small" placeholder="不填默认1帧" /></a-form-item>
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
                    <a-form-item label="抽帧间隔"><a-input-number v-model:value="step.frameInterval" :min="1" style="width:100%" size="small" placeholder="30" /></a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <a-form-item label="列数"><a-input-number v-model:value="step.spriteColumns" :min="1" style="width:100%" size="small" placeholder="4" /></a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <a-form-item label="行数"><a-input-number v-model:value="step.spriteRows" :min="1" style="width:100%" size="small" placeholder="3" /></a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="输出格式"><a-select v-model:value="step.extractOutputFormat" size="small" style="width:100%">
                  <a-select-option value="jpg">jpg</a-select-option>
                  <a-select-option value="png">png</a-select-option>
                </a-select></a-form-item>
                  </a-col>
                </a-row>
              </template>
            </a-card>
          </div>
          <a-button type="dashed" block class="add-step-btn" @click="addStep">+ 添加步骤</a-button>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { listStrategies, getStrategy, createStrategy, updateStrategy, deleteStrategy } from '../api/strategy_api'

function stepTypeLabel(type) {
  const map = { transcode: '转码', extract_frames: '抽帧', sprite: '雪碧图' }
  return map[type] || type || '转码'
}

function defaultStep() {
  return {
    type: 'transcode',
    depends: '',
    inputTemplate: '',
    outputTemplate: '',
    targetFormat: 'mp4',
    resolution: '1920x1080',
    bitrate: 5000,
    frameRate: 30,
    encoder: 'h264',
    addWatermark: false,
    watermarkPosition: 'bottom-right',
    watermarkPath: '',
    frameInterval: 30,
    extractFrameCount: undefined,
    extractOutputFormat: 'jpg',
    spriteColumns: 4,
    spriteRows: 3
  }
}

const loading = ref(false)
const list = ref([])
const showModal = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const stratForm = reactive({
  name: '',
  steps: [defaultStep()]
})

const columns = [
  { title: '策略ID', dataIndex: 'id', key: 'id', ellipsis: true, width: 100 },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '步骤数', dataIndex: 'stepCount', key: 'stepCount', width: 90 },
  { title: '操作', key: 'action', width: 140 }
]

function addStep() {
  stratForm.steps.push(defaultStep())
}

function removeStep(index) {
  if (stratForm.steps.length <= 1) return
  stratForm.steps.splice(index, 1)
}

function resetForm() {
  stratForm.name = ''
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
      stratForm.steps = data.steps.map(s => ({
        type: s.type || 'transcode',
        depends: s.depends ?? '',
        inputTemplate: s.inputTemplate ?? '',
        outputTemplate: s.outputTemplate ?? '',
        targetFormat: s.targetFormat ?? 'mp4',
        resolution: s.resolution ?? '1920x1080',
        bitrate: s.bitrate ?? 5000,
        frameRate: s.frameRate ?? 30,
        encoder: s.encoder ?? 'h264',
        addWatermark: s.addWatermark ?? false,
        watermarkPosition: s.watermarkPosition ?? 'bottom-right',
        watermarkPath: s.watermarkPath ?? '',
        frameInterval: s.frameInterval ?? 30,
        extractFrameCount: s.extractFrameCount ?? undefined,
        extractOutputFormat: s.extractOutputFormat ?? 'jpg',
        spriteColumns: s.spriteColumns ?? 4,
        spriteRows: s.spriteRows ?? 3
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
    name: stratForm.name.trim(),
    steps: stratForm.steps.map(s => ({
      type: s.type || 'transcode',
      depends: s.depends || undefined,
      inputTemplate: s.inputTemplate || undefined,
      outputTemplate: s.outputTemplate || undefined,
      targetFormat: s.targetFormat || undefined,
      resolution: s.resolution || undefined,
      bitrate: s.bitrate ?? undefined,
      frameRate: s.frameRate ?? undefined,
      encoder: s.encoder || undefined,
      addWatermark: s.addWatermark ?? undefined,
      watermarkPosition: s.watermarkPosition || undefined,
      watermarkPath: s.watermarkPath || undefined,
      frameInterval: s.frameInterval ?? undefined,
      extractFrameCount: s.extractFrameCount ?? undefined,
      extractOutputFormat: s.extractOutputFormat || undefined,
      spriteColumns: s.spriteColumns ?? undefined,
      spriteRows: s.spriteRows ?? undefined
    }))
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
.step-card { margin-bottom: 12px; }
.step-item { margin-bottom: 8px; }
.add-step-btn { margin-top: 8px; }
.danger { color: var(--ant-color-error); }
</style>
