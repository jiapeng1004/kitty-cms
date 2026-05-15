<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import * as meta from '@/mam/api/mam_metadata_api'

type BindingRow = {
  fieldId: string
  sortNum: number
  fieldCode?: string
  fieldName?: string
}

const loadingTpl = ref(false)
const loadingFld = ref(false)
const templates = ref<meta.MaterialMetadataTemplateVO[]>([])
const allFields = ref<meta.MaterialMetadataFieldVO[]>([])
const tplQuery = reactive<{ catalogId: string; resourceType?: number; enabledOnly: boolean }>({
  catalogId: '',
  resourceType: undefined,
  enabledOnly: false
})
const tplForm = reactive<meta.MaterialMetadataTemplateUpsertDTO>({
  id: '',
  name: '',
  catalogId: '',
  resourceType: undefined,
  enabled: 1
})

const fieldForm = reactive<meta.MaterialMetadataFieldUpsertDTO>({
  id: '',
  fieldCode: '',
  fieldName: '',
  inputType: 'TEXT',
  required: 0,
  optionsJson: ''
})

const bindTemplateId = ref('')
const bindingRows = ref<BindingRow[]>([])
const addFieldId = ref<string | undefined>(undefined)

const instResourceId = ref('')
const instTemplateId = ref('')
const instanceFormRows = ref<meta.MaterialMetadataFormFieldVO[]>([])
const instanceValues = reactive<Record<string, string>>({})
const lastSnapshot = ref<meta.MaterialMetadataSnapshotVO | null>(null)
const historyMax = ref(5)
const historyRows = ref<meta.MaterialMetadataInstanceEntryVO[]>([])

const availableFieldsForBind = computed(() =>
  allFields.value.filter((f) => !bindingRows.value.some((b) => b.fieldId === f.id))
)

function parseSelectOptions(json: string | undefined): { label: string; value: string }[] {
  if (!json) return []
  try {
    const arr = JSON.parse(json) as unknown
    if (!Array.isArray(arr)) return []
    return arr
      .filter((x): x is string => typeof x === 'string')
      .map((x) => ({ label: x, value: x }))
  } catch {
    return []
  }
}

async function refreshTemplates() {
  loadingTpl.value = true
  try {
    templates.value = await meta.listTemplates({
      catalogId: tplQuery.catalogId || undefined,
      resourceType: tplQuery.resourceType,
      enabledOnly: tplQuery.enabledOnly || undefined
    })
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '加载模板失败')
  } finally {
    loadingTpl.value = false
  }
}

async function refreshFields() {
  loadingFld.value = true
  try {
    allFields.value = await meta.listFields()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '加载字段失败')
  } finally {
    loadingFld.value = false
  }
}

onMounted(() => {
  refreshTemplates()
  refreshFields()
})

function fillTplForm(row: meta.MaterialMetadataTemplateVO) {
  tplForm.id = row.id
  tplForm.name = row.name
  tplForm.catalogId = row.catalogId
  tplForm.resourceType = row.resourceType ?? undefined
  tplForm.enabled = row.enabled
}

async function submitTemplate() {
  try {
    if (tplForm.id) {
      await meta.updateTemplate({
        id: tplForm.id,
        name: tplForm.name,
        catalogId: tplForm.catalogId,
        resourceType: tplForm.resourceType,
        enabled: tplForm.enabled
      })
      message.success('模板已更新')
    } else {
      await meta.createTemplate({
        name: tplForm.name,
        catalogId: tplForm.catalogId,
        resourceType: tplForm.resourceType,
        enabled: tplForm.enabled ?? 1
      })
      message.success('模板已创建')
    }
    await refreshTemplates()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '保存模板失败')
  }
}

async function removeTemplate(id: string) {
  try {
    await meta.deleteTemplate(id)
    message.success('已删除')
    if (tplForm.id === id) {
      tplForm.id = ''
    }
    await refreshTemplates()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '删除失败')
  }
}

async function submitField() {
  try {
    if (fieldForm.id) {
      await meta.updateField({ ...fieldForm })
      message.success('字段已更新')
    } else {
      await meta.createField({
        fieldCode: fieldForm.fieldCode,
        fieldName: fieldForm.fieldName,
        inputType: fieldForm.inputType,
        required: fieldForm.required,
        optionsJson: fieldForm.optionsJson || undefined
      })
      message.success('字段已创建')
    }
    fieldForm.id = ''
    await refreshFields()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '保存字段失败')
  }
}

function fillFieldForm(row: meta.MaterialMetadataFieldVO) {
  fieldForm.id = row.id
  fieldForm.fieldCode = row.fieldCode
  fieldForm.fieldName = row.fieldName
  fieldForm.inputType = row.inputType
  fieldForm.required = row.required ?? 0
  fieldForm.optionsJson = row.optionsJson || ''
}

async function removeField(id: string) {
  try {
    await meta.deleteField(id)
    message.success('已删除')
    if (fieldForm.id === id) fieldForm.id = ''
    await refreshFields()
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '删除失败')
  }
}

async function loadBindings() {
  if (!bindTemplateId.value) {
    message.warning('请选择模板')
    return
  }
  try {
    const rows = await meta.listTemplateBindings(bindTemplateId.value)
    bindingRows.value = rows.map((r) => ({
      fieldId: r.fieldId,
      sortNum: r.sortNum ?? 0,
      fieldCode: r.fieldCode,
      fieldName: r.fieldName
    }))
    message.success('已加载绑定')
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '加载绑定失败')
  }
}

function addBindingRow() {
  if (!addFieldId.value) {
    message.warning('请选择要添加的字段')
    return
  }
  const f = allFields.value.find((x) => x.id === addFieldId.value)
  if (!f) return
  const next =
    bindingRows.value.length === 0
      ? 0
      : Math.max(...bindingRows.value.map((b) => b.sortNum)) + 1
  bindingRows.value.push({
    fieldId: f.id,
    sortNum: next,
    fieldCode: f.fieldCode,
    fieldName: f.fieldName
  })
  addFieldId.value = undefined
}

function removeBindingRow(fieldId: string) {
  bindingRows.value = bindingRows.value.filter((b) => b.fieldId !== fieldId)
}

async function saveBindings() {
  if (!bindTemplateId.value) {
    message.warning('请选择模板')
    return
  }
  try {
    await meta.bindTemplateFields({
      templateId: bindTemplateId.value,
      bindings: bindingRows.value.map((b) => ({ fieldId: b.fieldId, sortNum: b.sortNum }))
    })
    message.success('绑定已保存')
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '保存绑定失败')
  }
}

async function loadInstanceForm() {
  if (!instResourceId.value || !instTemplateId.value) {
    message.warning('请填写资源ID与模板ID')
    return
  }
  try {
    const rows = await meta.formFieldsForResource(instResourceId.value.trim(), instTemplateId.value.trim())
    instanceFormRows.value = rows
    Object.keys(instanceValues).forEach((k) => delete instanceValues[k])
    for (const r of rows) {
      instanceValues[r.fieldCode] = ''
    }
    message.success(`已加载 ${rows.length} 个字段`)
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '加载表单字段失败')
  }
}

async function saveInstanceMeta() {
  if (!instResourceId.value || !instTemplateId.value) {
    message.warning('请填写资源ID与模板ID')
    return
  }
  try {
    const fieldValues: Record<string, string> = {}
    for (const r of instanceFormRows.value) {
      fieldValues[r.fieldCode] = instanceValues[r.fieldCode] ?? ''
    }
    const snap = await meta.saveInstance({
      resourceId: instResourceId.value.trim(),
      templateId: instTemplateId.value.trim(),
      fieldValues
    })
    lastSnapshot.value = snap
    message.success(`已保存，版本 ${snap.version}`)
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '保存编目失败')
  }
}

async function loadLastInstance() {
  if (!instResourceId.value || !instTemplateId.value) {
    message.warning('请填写资源ID与模板ID')
    return
  }
  try {
    const snap = await meta.getLastMetadata(instResourceId.value.trim(), instTemplateId.value.trim())
    lastSnapshot.value = snap
    for (const e of snap.entries) {
      instanceValues[e.fieldCode] = e.fieldValue ?? ''
    }
    message.success(snap.version == null ? '尚无编目数据' : `最新版本 ${snap.version}`)
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '查询最新失败')
  }
}

async function loadHistory() {
  if (!instResourceId.value || !instTemplateId.value) {
    message.warning('请填写资源ID与模板ID')
    return
  }
  try {
    historyRows.value = await meta.metadataHistory(
      instResourceId.value.trim(),
      instTemplateId.value.trim(),
      historyMax.value
    )
    message.success(`共 ${historyRows.value.length} 条明细`)
  } catch (e: any) {
    message.error(e?.response?.data?.message || e?.message || '加载历史失败')
  }
}
</script>

<template>
  <div style="padding: 20px;">
    <a-typography-title :level="4">编目模板</a-typography-title>
    <a-space wrap style="margin-bottom: 12px;">
      <a-input v-model:value="tplQuery.catalogId" style="width: 200px;" placeholder="栏目ID（筛选用）" />
      <a-input-number v-model:value="tplQuery.resourceType" :min="1" :max="7" placeholder="资源类型" />
      <a-switch v-model:checked="tplQuery.enabledOnly" checked-children="仅启用" un-checked-children="全部" />
      <a-button :loading="loadingTpl" @click="refreshTemplates">刷新模板</a-button>
    </a-space>
    <a-row :gutter="16">
      <a-col :span="14">
        <a-table
          :data-source="templates"
          row-key="id"
          size="small"
          :pagination="false"
          :loading="loadingTpl"
        >
          <a-table-column title="名称" data-index="name" key="name" />
          <a-table-column title="模板ID" data-index="id" key="id" />
          <a-table-column title="栏目" data-index="catalogId" key="catalogId" />
          <a-table-column title="资源类型" key="resourceType">
            <template #default="{ record }">
              {{ record.resourceType ?? '通用' }}
            </template>
          </a-table-column>
          <a-table-column title="启用" key="enabled">
            <template #default="{ record }">
              <a-tag :color="record.enabled === 1 ? 'green' : 'default'">{{ record.enabled === 1 ? '是' : '否' }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="操作" key="act">
            <template #default="{ record }">
              <a-space>
                <a-button type="link" size="small" @click="fillTplForm(record)">填入表单</a-button>
                <a-button type="link" size="small" danger @click="removeTemplate(record.id)">删</a-button>
              </a-space>
            </template>
          </a-table-column>
        </a-table>
      </a-col>
      <a-col :span="10">
        <a-form layout="vertical">
          <a-form-item label="模板ID（更新时必填）">
            <a-input v-model:value="tplForm.id" placeholder="新建留空" />
          </a-form-item>
          <a-form-item label="名称" required>
            <a-input v-model:value="tplForm.name" />
          </a-form-item>
          <a-form-item label="栏目ID" required>
            <a-input v-model:value="tplForm.catalogId" />
          </a-form-item>
          <a-form-item label="资源类型（空=栏目通用）">
            <a-input-number v-model:value="tplForm.resourceType" :min="1" :max="7" style="width: 100%;" />
          </a-form-item>
          <a-form-item label="启用 1/0">
            <a-input-number v-model:value="tplForm.enabled" :min="0" :max="1" style="width: 100%;" />
          </a-form-item>
          <a-button type="primary" @click="submitTemplate">保存模板</a-button>
        </a-form>
      </a-col>
    </a-row>

    <a-divider />
    <a-typography-title :level="4">字段定义</a-typography-title>
    <a-row :gutter="16">
      <a-col :span="14">
        <a-table
          :data-source="allFields"
          row-key="id"
          size="small"
          :pagination="false"
          :loading="loadingFld"
        >
          <a-table-column title="编码" data-index="fieldCode" key="fieldCode" />
          <a-table-column title="名称" data-index="fieldName" key="fieldName" />
          <a-table-column title="类型" data-index="inputType" key="inputType" />
          <a-table-column title="必填" key="required">
            <template #default="{ record }">
              {{ record.required === 1 ? '是' : '否' }}
            </template>
          </a-table-column>
          <a-table-column title="操作" key="act2">
            <template #default="{ record }">
              <a-space>
                <a-button type="link" size="small" @click="fillFieldForm(record)">编辑</a-button>
                <a-button type="link" size="small" danger @click="removeField(record.id)">删</a-button>
              </a-space>
            </template>
          </a-table-column>
        </a-table>
      </a-col>
      <a-col :span="10">
        <a-form layout="vertical">
          <a-form-item label="字段ID（更新）">
            <a-input v-model:value="fieldForm.id" placeholder="新建留空" />
          </a-form-item>
          <a-form-item label="field_code" required>
            <a-input v-model:value="fieldForm.fieldCode" />
          </a-form-item>
          <a-form-item label="展示名" required>
            <a-input v-model:value="fieldForm.fieldName" />
          </a-form-item>
          <a-form-item label="input_type" required>
            <a-select v-model:value="fieldForm.inputType" style="width: 100%;">
              <a-select-option value="TEXT">TEXT</a-select-option>
              <a-select-option value="NUMBER">NUMBER</a-select-option>
              <a-select-option value="SELECT">SELECT</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="必填 1/0">
            <a-input-number v-model:value="fieldForm.required" :min="0" :max="1" style="width: 100%;" />
          </a-form-item>
          <a-form-item label="options_json（SELECT）">
            <a-textarea v-model:value="fieldForm.optionsJson" :rows="3" placeholder='例如 ["A","B"]' />
          </a-form-item>
          <a-space>
            <a-button type="primary" @click="submitField">保存字段</a-button>
            <a-button
              @click="
                fieldForm.id = '';
                fieldForm.fieldCode = '';
                fieldForm.fieldName = '';
                fieldForm.inputType = 'TEXT';
                fieldForm.required = 0;
                fieldForm.optionsJson = '';
              "
            >
              清空表单
            </a-button>
          </a-space>
        </a-form>
      </a-col>
    </a-row>

    <a-divider />
    <a-typography-title :level="4">模板字段绑定</a-typography-title>
    <a-space wrap style="margin-bottom: 12px;">
      <a-select
        v-model:value="bindTemplateId"
        show-search
        style="min-width: 280px;"
        placeholder="选择模板"
        :options="templates.map((t) => ({ label: `${t.name} (${t.id})`, value: t.id }))"
        option-filter-prop="label"
      />
      <a-button @click="loadBindings">加载当前绑定</a-button>
      <a-select
        v-model:value="addFieldId"
        style="min-width: 220px;"
        placeholder="添加字段到绑定"
        :options="availableFieldsForBind.map((f) => ({ label: `${f.fieldName} (${f.fieldCode})`, value: f.id }))"
        allow-clear
      />
      <a-button type="dashed" @click="addBindingRow">添加</a-button>
      <a-button type="primary" @click="saveBindings">保存绑定</a-button>
    </a-space>
    <a-table :data-source="bindingRows" row-key="fieldId" size="small" :pagination="false">
      <a-table-column title="field_code" key="fc" data-index="fieldCode" />
      <a-table-column title="名称" key="fn" data-index="fieldName" />
      <a-table-column title="排序" key="sn">
        <template #default="{ record }">
          <a-input-number v-model:value="record.sortNum" :min="0" size="small" />
        </template>
      </a-table-column>
      <a-table-column title="操作" key="rb">
        <template #default="{ record }">
          <a-button type="link" danger size="small" @click="removeBindingRow(record.fieldId)">移除</a-button>
        </template>
      </a-table-column>
    </a-table>

    <a-divider />
    <a-typography-title :level="4">编目实例（资源维度）</a-typography-title>
    <a-space wrap style="margin-bottom: 12px;">
      <a-input v-model:value="instResourceId" style="width: 220px;" placeholder="资源ID" />
      <a-input v-model:value="instTemplateId" style="width: 280px;" placeholder="模板ID" />
      <a-button @click="loadInstanceForm">加载可填字段</a-button>
      <a-button type="primary" @click="saveInstanceMeta">保存新版本</a-button>
      <a-button @click="loadLastInstance">加载最新到表单</a-button>
      <a-input-number v-model:value="historyMax" :min="1" />
      <a-button @click="loadHistory">历史 version≤上界</a-button>
    </a-space>

    <a-row :gutter="16">
      <a-col :span="12">
        <a-form layout="vertical" v-if="instanceFormRows.length">
          <a-form-item v-for="row in instanceFormRows" :key="row.fieldId" :label="`${row.fieldName} (${row.fieldCode})`">
            <a-select
              v-if="row.inputType?.toUpperCase() === 'SELECT'"
              v-model:value="instanceValues[row.fieldCode]"
              allow-clear
              style="width: 100%;"
              :options="parseSelectOptions(row.optionsJson)"
            />
            <a-input v-else v-model:value="instanceValues[row.fieldCode]" />
          </a-form-item>
        </a-form>
        <a-alert v-else message="先点击「加载可填字段」" type="info" show-icon />
      </a-col>
      <a-col :span="12">
        <div v-if="lastSnapshot">
          <div style="margin-bottom: 8px;">
            <b>最近一次结果：</b>
            {{ lastSnapshot.templateName }} / version {{ lastSnapshot.version ?? '—' }}
          </div>
          <a-table
            :data-source="lastSnapshot.entries"
            row-key="fieldId"
            size="small"
            :pagination="false"
          >
            <a-table-column title="字段" data-index="fieldCode" key="fieldCode" />
            <a-table-column title="值" data-index="fieldValue" key="fieldValue" />
          </a-table>
        </div>
        <a-divider v-if="historyRows.length">历史明细</a-divider>
        <a-table
          v-if="historyRows.length"
          :data-source="historyRows"
          :row-key="(r) => `${r.fieldId}-${r.version ?? 0}`"
          size="small"
          :pagination="false"
        >
          <a-table-column title="ver" data-index="version" key="version" />
          <a-table-column title="字段" data-index="fieldCode" key="fieldCode" />
          <a-table-column title="值" data-index="fieldValue" key="fieldValue" />
        </a-table>
      </a-col>
    </a-row>
  </div>
</template>
