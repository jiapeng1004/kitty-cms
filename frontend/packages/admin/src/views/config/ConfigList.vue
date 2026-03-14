<template>
  <div class="page-config-list list-page">
    <div class="page-header">
      <h1 class="page-title">配置管理</h1>
      <p class="page-desc">管理系统配置项与键值</p>
    </div>

    <a-card class="filter-card" :bordered="false">
      <a-space wrap>
        <a-select
          v-model:value="filterClassId"
          placeholder="分类"
          allow-clear
          style="width: 180px"
          :loading="classOptionsLoading"
          :options="classOptions"
          :field-names="{ label: 'className', value: 'id' }"
        />
        <a-input-search
          v-model:value="filterSearchKey"
          placeholder="关键词（配置名称/配置键）"
          allow-clear
          style="width: 260px"
          @search="onSearch"
        />
        <a-button type="primary" @click="onSearch">
          <template #icon><SearchOutlined /></template>
          查询
        </a-button>
        <a-button @click="resetSearch">重置</a-button>
      </a-space>
    </a-card>

    <a-card class="content-card" :bordered="false">
      <template #title>
        <span>配置列表</span>
      </template>
      <template #extra>
        <a-button type="primary" @click="showModal({}, 'create')">
          <template #icon>
            <PlusOutlined/>
          </template>
          新增配置
        </a-button>
      </template>
      <a-table
          :columns="columns"
          :data-source="dataSource"
          :pagination="pagination"
          :loading="loading"
          size="middle"
          @change="handleTableChange"
      >
        <template #emptyText>
          <a-empty :image="false" description="暂无配置数据"/>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'configKey' || column.key === 'configValue' || column.key === 'configDefault'">
            <span
              class="copyable-cell"
              :title="'点击复制: ' + (record[column.key] ?? '')"
              @click="copyToClipboard(record[column.key])"
            >
              {{ record[column.key] ?? '—' }}
            </span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="showModal(record, 'edit')">编辑</a>
              <a @click="showValueModal(record)">配置</a>
              <a-popconfirm
                  title="确定要删除这个配置吗?"
                  @confirm="handleDelete(record.id)"
              >
                <a>删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
        v-model:open="modalVisible"
        :title="modalTitle"
        @ok="handleOk"
        @cancel="handleCancel"
        :confirm-loading="confirmLoading"
    >
      <a-form
          :model="formState"
          name="configForm"
          autocomplete="off"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 18 }"
      >
        <a-form-item
            label="配置名称"
            name="configName"
            :rules="[{ required: true, message: '请输入配置名称!' }]"
        >
          <a-input v-model:value="formState.configName"/>
        </a-form-item>
        <a-form-item
            label="配置键"
            name="configKey"
            :rules="[{ required: true, message: '请输入配置键!' }]"
        >
          <a-input v-model:value="formState.configKey" :disabled="modalType === 'edit'"/>
        </a-form-item>
        <a-form-item label="配置方式" name="configWay">
          <a-select v-model:value="formState.configWay" placeholder="请选择">
            <a-select-option value="TEXT">文本框</a-select-option>
            <a-select-option value="TEXTAREA">长文本域</a-select-option>
            <a-select-option value="RADIO">单选框</a-select-option>
            <a-select-option value="CHECKBOX">多选框</a-select-option>
          </a-select>
        </a-form-item>
        <template v-if="formState.configWay === 'RADIO' || formState.configWay === 'CHECKBOX'">
          <a-form-item label="枚举选项" help="键为选项值，值为显示描述（RADIO/CHECKBOX 时生效）">
            <div v-for="(item, index) in formState.configEnumArray" :key="index" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center;">
              <a-input v-model:value="item.key" placeholder="值" style="width: 120px"/>
              <a-input v-model:value="item.label" placeholder="描述" style="flex: 1"/>
              <a-button type="text" danger size="small" @click="removeEnumItem(index)">删除</a-button>
            </div>
            <a-button type="dashed" block @click="addEnumItem">+ 添加选项</a-button>
          </a-form-item>
        </template>
        <a-form-item label="配置值" name="configValue">
          <a-input v-if="formState.configWay === 'TEXT'" v-model:value="formState.configValue"/>
          <a-textarea v-else-if="formState.configWay === 'TEXTAREA'" v-model:value="formState.configValue" placeholder="多行文本" :rows="3"/>
          <a-radio-group v-else-if="formState.configWay === 'RADIO'" :value="formState.configValue">
            <a-radio v-for="opt in enumOptions" :key="opt.key" :value="opt.key" @click.stop="onConfigValueRadioItemClick(opt.key)">{{ opt.label || opt.key }}</a-radio>
          </a-radio-group>
          <a-checkbox-group v-else-if="formState.configWay === 'CHECKBOX'" :value="configValueCheckboxList" @update:value="onConfigValueCheckboxChange">
            <a-checkbox v-for="opt in enumOptions" :key="opt.key" :value="opt.key">{{ opt.label || opt.key }}</a-checkbox>
          </a-checkbox-group>
          <a-input v-else v-model:value="formState.configValue"/>
        </a-form-item>
        <a-form-item label="默认值" name="configDefault">
          <a-input v-if="formState.configWay === 'TEXT' || formState.configWay === 'TEXTAREA'" v-model:value="formState.configDefault"/>
          <a-radio-group v-else-if="formState.configWay === 'RADIO'" :value="formState.configDefault">
            <a-radio v-for="opt in enumOptions" :key="'d-' + opt.key" :value="opt.key" @click.stop="onConfigDefaultRadioItemClick(opt.key)">{{ opt.label || opt.key }}</a-radio>
          </a-radio-group>
          <a-checkbox-group v-else-if="formState.configWay === 'CHECKBOX'" :value="configDefaultCheckboxList" @update:value="onConfigDefaultCheckboxChange">
            <a-checkbox v-for="opt in enumOptions" :key="'d-' + opt.key" :value="opt.key">{{ opt.label || opt.key }}</a-checkbox>
          </a-checkbox-group>
          <a-input v-else v-model:value="formState.configDefault"/>
        </a-form-item>
        <a-form-item label="描述" name="configDesc">
          <a-textarea v-model:value="formState.configDesc"/>
        </a-form-item>
        <a-form-item label="分类" name="classId" :rules="modalType === 'create' ? [{ required: true, message: '请选择分类!' }] : []">
          <a-select
            v-model:value="formState.classId"
            placeholder="请选择分类"
            allow-clear
            :loading="classOptionsLoading"
            :options="classOptions"
            :field-names="{ label: 'className', value: 'id' }"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 仅改配置值 -->
    <a-modal
      v-model:open="valueModalVisible"
      title="修改配置值"
      :confirm-loading="valueConfirmLoading"
      @ok="handleValueOk"
      @cancel="valueModalVisible = false"
    >
      <p v-if="valueEditRecord" class="value-modal-desc">配置项：{{ valueEditRecord.configName }}（{{ valueEditRecord.configKey }}）</p>
      <a-form layout="vertical">
        <a-form-item label="配置值">
          <a-input
            v-if="valueEditWay === 'TEXT'"
            v-model:value="valueEditValue"
            placeholder="请输入配置值"
          />
          <a-textarea
            v-else-if="valueEditWay === 'TEXTAREA'"
            v-model:value="valueEditValue"
            placeholder="请输入配置值"
            :rows="3"
          />
          <a-radio-group v-else-if="valueEditWay === 'RADIO'" :value="valueEditValue">
            <a-radio v-for="opt in valueEditEnumOptions" :key="opt.key" :value="opt.key" @click.stop="valueEditValue = valueEditValue === opt.key ? '' : opt.key">{{ opt.label || opt.key }}</a-radio>
          </a-radio-group>
          <a-checkbox-group
            v-else-if="valueEditWay === 'CHECKBOX'"
            :value="valueEditValue ? valueEditValue.split(',').map(s => s.trim()).filter(Boolean) : []"
            @update:value="(arr) => { valueEditValue = Array.isArray(arr) ? arr.join(',') : '' }"
          >
            <a-checkbox v-for="opt in valueEditEnumOptions" :key="opt.key" :value="opt.key">{{ opt.label || opt.key }}</a-checkbox>
          </a-checkbox-group>
          <a-input v-else v-model:value="valueEditValue" placeholder="请输入配置值"/>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, computed} from 'vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import {message} from 'ant-design-vue'
import {
  getConfigPage,
  getConfigById,
  createConfig,
  updateConfig,
  removeConfig,
  setConfigVal
} from '../../api/config_api'
import { getConfigClassPage } from '../../api/config_class_api'
import { getResponseMessage } from '../../utils/api'

const dataSource = ref([])
const loading = ref(false)
const modalVisible = ref(false)
const confirmLoading = ref(false)
const modalType = ref('create')
const classOptions = ref([])
const classOptionsLoading = ref(false)
const filterClassId = ref(undefined)
const filterSearchKey = ref('')
const valueModalVisible = ref(false)
const valueConfirmLoading = ref(false)
const valueEditRecord = ref(null)
const valueEditValue = ref('')
const valueEditWay = ref('TEXT')
const valueEditEnumOptions = ref([])

const formState = reactive({
  id: undefined,
  configName: '',
  configKey: '',
  configDesc: '',
  configWay: 'TEXT',
  configDefault: '',
  configValue: '',
  classId: '',
  configEnumArray: []
})

const modalTitle = computed(() => (modalType.value === 'create' ? '新增配置' : '编辑配置'))

const enumOptions = computed(() => (formState.configEnumArray || []).filter((e) => e && (e.key != null && e.key !== '')))

const configValueCheckboxList = computed(() => {
  if (formState.configWay !== 'CHECKBOX' || !formState.configValue) return []
  return formState.configValue.split(',').map((s) => s.trim()).filter(Boolean)
})

const configDefaultCheckboxList = computed(() => {
  if (formState.configWay !== 'CHECKBOX' || !formState.configDefault) return []
  return formState.configDefault.split(',').map((s) => s.trim()).filter(Boolean)
})

function addEnumItem() {
  if (!formState.configEnumArray) formState.configEnumArray = []
  formState.configEnumArray.push({ key: '', label: '' })
}

function removeEnumItem(index) {
  formState.configEnumArray.splice(index, 1)
}

function onConfigValueRadioItemClick(key) {
  formState.configValue = formState.configValue === key ? '' : key
}

function onConfigDefaultRadioItemClick(key) {
  formState.configDefault = formState.configDefault === key ? '' : key
}

function onConfigValueCheckboxChange(arr) {
  formState.configValue = Array.isArray(arr) ? arr.join(',') : ''
}

function onConfigDefaultCheckboxChange(arr) {
  formState.configDefault = Array.isArray(arr) ? arr.join(',') : ''
}

function configEnumToArray(obj) {
  if (!obj || typeof obj !== 'object') return []
  return Object.entries(obj).map(([key, label]) => ({ key, label }))
}

function configEnumToObject(arr) {
  if (!arr || !arr.length) return undefined
  const obj = {}
  arr.forEach((e) => {
    if (e && e.key != null && e.key !== '') obj[e.key] = (e.label != null ? e.label : '')
  })
  return Object.keys(obj).length ? obj : undefined
}

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

/** 配置方式存储值 -> 显示名称，前端维护，后期可改为数据字典 */
const CONFIG_WAY_NAMES = {
  TEXT: '文本框',
  TEXTAREA: '长文本域',
  RADIO: '单选框',
  CHECKBOX: '多选框'
}

const columns = [
  {title: '配置名称', dataIndex: 'configName', key: 'configName'},
  {title: '配置键', dataIndex: 'configKey', key: 'configKey'},
  {title: '当前值', dataIndex: 'configValue', key: 'configValue', ellipsis: true},
  {title: '默认值', dataIndex: 'configDefault', key: 'configDefault', ellipsis: true},
  {title: '配置方式', key: 'configWay', customRender: ({ record }) => (record.configWay && CONFIG_WAY_NAMES[record.configWay]) || ''},
  {title: '分类', dataIndex: 'className', key: 'className'},
  {title: '创建时间', dataIndex: 'createTime', key: 'createTime'},
  {title: '操作', key: 'action'}
]

onMounted(() => {
  loadClassOptions()
  fetchData()
})

function onSearch() {
  pagination.current = 1
  fetchData()
}

function resetSearch() {
  filterClassId.value = undefined
  filterSearchKey.value = ''
  pagination.current = 1
  fetchData()
}

async function copyToClipboard(text) {
  if (text == null || String(text).trim() === '') return
  try {
    await navigator.clipboard.writeText(String(text))
    message.success('已复制到剪贴板')
  } catch (_) {
    message.error('复制失败')
  }
}

async function fetchData(params = {}) {
  loading.value = true
  try {
    const data = await getConfigPage({
      page: params.page ?? pagination.current,
      size: params.size ?? pagination.pageSize,
      searchKey: params.searchKey ?? (filterSearchKey.value || undefined),
      classId: params.classId ?? filterClassId.value ?? undefined,
      configKey: params.configKey
    })
    dataSource.value = data?.records ?? []
    pagination.total = data?.total ?? 0
    if (data?.page != null) pagination.current = data.page
    if (data?.size != null) pagination.pageSize = data.size
  } catch (error) {
    message.error(getResponseMessage(error))
    dataSource.value = []
  } finally {
    loading.value = false
  }
}

function handleTableChange(pager) {
  pagination.current = pager.current
  pagination.pageSize = pager.pageSize
  fetchData({ page: pager.current, size: pager.pageSize })
}

async function loadClassOptions() {
  classOptionsLoading.value = true
  try {
    const res = await getConfigClassPage({ page: 1, size: 500 })
    classOptions.value = (res?.records ?? []).map((r) => ({ id: r.id, className: r.className }))
  } catch (_) {
    classOptions.value = []
  } finally {
    classOptionsLoading.value = false
  }
}

async function showModal(record, type) {
  modalType.value = type
  await loadClassOptions()
  if (type === 'edit') {
    try {
      const data = await getConfigById(record.id)
      Object.assign(formState, data, { id: record.id })
      formState.configEnumArray = configEnumToArray(data.configEnum)
    } catch (e) {
      message.error(getResponseMessage(e))
      return
    }
  } else {
    formState.id = undefined
    formState.configName = ''
    formState.configKey = ''
    formState.configDesc = ''
    formState.configWay = 'TEXT'
    formState.configDefault = ''
    formState.configValue = ''
    formState.classId = ''
    formState.configEnumArray = []
  }
  modalVisible.value = true
}

async function showValueModal(record) {
  valueEditRecord.value = record
  valueEditValue.value = record.configValue ?? ''
  valueEditWay.value = record.configWay || 'TEXT'
  valueEditEnumOptions.value = []
  if (record.configWay === 'RADIO' || record.configWay === 'CHECKBOX') {
    try {
      const data = await getConfigById(record.id)
      if (data && data.configEnum) valueEditEnumOptions.value = configEnumToArray(data.configEnum)
    } catch (_) {
      valueEditEnumOptions.value = []
    }
  }
  valueModalVisible.value = true
}

async function handleValueOk() {
  if (!valueEditRecord.value) return
  valueConfirmLoading.value = true
  try {
    await setConfigVal({
      id: valueEditRecord.value.id,
      configValue: valueEditValue.value ?? ''
    })
    message.success('配置值已更新')
    valueModalVisible.value = false
    fetchData()
  } catch (error) {
    message.error(getResponseMessage(error))
  } finally {
    valueConfirmLoading.value = false
  }
}

async function handleOk() {
  confirmLoading.value = true
  try {
    const payloadEnum = (formState.configWay === 'RADIO' || formState.configWay === 'CHECKBOX')
      ? configEnumToObject(formState.configEnumArray)
      : undefined
    if (modalType.value === 'create') {
      await createConfig({
        configName: formState.configName,
        configKey: formState.configKey,
        configDesc: formState.configDesc || undefined,
        configWay: formState.configWay,
        configDefault: formState.configDefault || undefined,
        configValue: formState.configValue || undefined,
        classId: formState.classId,
        configEnum: payloadEnum
      })
      message.success('创建成功')
    } else {
      await updateConfig(formState.id, {
        configName: formState.configName,
        configDesc: formState.configDesc || undefined,
        configWay: formState.configWay,
        configDefault: formState.configDefault || undefined,
        configValue: formState.configValue || undefined,
        classId: formState.classId ?? undefined,
        configEnum: payloadEnum
      })
      message.success('更新成功')
    }
    modalVisible.value = false
    fetchData()
  } catch (error) {
    message.error(getResponseMessage(error))
  } finally {
    confirmLoading.value = false
  }
}

function handleCancel() {
  modalVisible.value = false
}

async function handleDelete(id) {
  try {
    await removeConfig(id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error(getResponseMessage(error))
  }
}
</script>

<style scoped>
.page-config-list {
  padding: 0;
}
.value-modal-desc {
  margin-bottom: 12px;
  color: rgba(0, 0, 0, 0.65);
  font-size: 13px;
}
.copyable-cell {
  cursor: pointer;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}
.copyable-cell:hover {
  color: var(--ant-primary-color);
  text-decoration: underline;
}
</style>
