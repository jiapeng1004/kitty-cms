<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import * as tc from '@/api/mam_transcode_api'

const loadingSt = ref(false)
const loadingBd = ref(false)
const strategies = ref<tc.MaterialTranscodeStrategyVO[]>([])
const binds = ref<tc.CatalogTranscodeBindVO[]>([])

const strategyForm = ref<tc.MaterialTranscodeStrategyUpsertDTO>({
  name: '',
  platformCode: 'kitty_transcoder_grpc',
  externalStrategyId: '',
  paramsJson: '',
  enabled: 1
})

const bindCatalogId = ref('')
const bindForm = ref<tc.CatalogTranscodeBindCreateDTO>({
  catalogId: '',
  strategyId: '',
  resourceType: undefined,
  sortNum: 0
})

async function refreshStrategies() {
  loadingSt.value = true
  try {
    strategies.value = await tc.listStrategies()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '加载策略失败')
  } finally {
    loadingSt.value = false
  }
}

async function saveStrategy() {
  try {
    if (strategyForm.value.id) {
      await tc.updateStrategy(strategyForm.value)
      message.success('已更新策略')
    } else {
      await tc.createStrategy(strategyForm.value)
      message.success('已创建策略')
    }
    strategyForm.value = {
      name: '',
      platformCode: 'kitty_transcoder_grpc',
      externalStrategyId: '',
      paramsJson: '',
      enabled: 1
    }
    await refreshStrategies()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '保存失败')
  }
}

function editStrategy(row: tc.MaterialTranscodeStrategyVO) {
  strategyForm.value = {
    id: row.id,
    name: row.name,
    platformCode: row.platformCode,
    externalStrategyId: row.externalStrategyId,
    paramsJson: row.paramsJson,
    enabled: row.enabled
  }
}

async function removeStrategy(id: string) {
  try {
    await tc.deleteStrategy(id)
    message.success('已删除')
    await refreshStrategies()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '删除失败')
  }
}

async function loadBinds() {
  if (!bindCatalogId.value.trim()) {
    message.warning('请输入栏目 ID')
    return
  }
  loadingBd.value = true
  try {
    binds.value = await tc.listBinds(bindCatalogId.value.trim())
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '加载绑定失败')
  } finally {
    loadingBd.value = false
  }
}

async function saveBind() {
  try {
    await tc.createBind({
      ...bindForm.value,
      catalogId: bindForm.value.catalogId || bindCatalogId.value.trim()
    })
    message.success('已创建绑定')
    bindForm.value = { catalogId: '', strategyId: '', resourceType: undefined, sortNum: 0 }
    await loadBinds()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '保存绑定失败')
  }
}

async function removeBind(id: string) {
  try {
    await tc.deleteBind(id)
    message.success('已删除绑定')
    await loadBinds()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '删除失败')
  }
}

function clearStrategyForm() {
  strategyForm.value = {
    name: '',
    platformCode: 'kitty_transcoder_grpc',
    externalStrategyId: '',
    paramsJson: '',
    enabled: 1
  }
}

onMounted(refreshStrategies)
</script>

<template>
  <div style="padding: 20px;">
    <a-typography-title :level="4">转码策略与栏目绑定</a-typography-title>

    <a-card title="策略" size="small" style="margin-bottom: 16px;">
      <a-space style="margin-bottom: 12px;">
        <a-button type="primary" :loading="loadingSt" @click="refreshStrategies">刷新策略列表</a-button>
      </a-space>
      <a-form layout="vertical" style="max-width: 560px; margin-bottom: 16px;">
        <a-form-item v-if="strategyForm.id" label="编辑 ID">
          <a-input v-model:value="strategyForm.id" disabled />
        </a-form-item>
        <a-form-item label="名称">
          <a-input v-model:value="strategyForm.name" />
        </a-form-item>
        <a-form-item label="platformCode">
          <a-input v-model:value="strategyForm.platformCode" />
        </a-form-item>
        <a-form-item label="externalStrategyId">
          <a-input v-model:value="strategyForm.externalStrategyId" />
        </a-form-item>
        <a-form-item label="paramsJson">
          <a-textarea v-model:value="strategyForm.paramsJson" :rows="3" />
        </a-form-item>
        <a-form-item label="enabled">
          <a-input-number v-model:value="strategyForm.enabled" :min="0" :max="1" />
        </a-form-item>
        <a-space>
          <a-button type="primary" @click="saveStrategy">{{ strategyForm.id ? '更新' : '创建' }}</a-button>
          <a-button v-if="strategyForm.id" @click="clearStrategyForm">清空表单</a-button>
        </a-space>
      </a-form>

      <a-table
        :data-source="strategies"
        :loading="loadingSt"
        row-key="id"
        size="small"
        :pagination="false"
      >
        <a-table-column title="名称" data-index="name" />
        <a-table-column title="平台" data-index="platformCode" />
        <a-table-column title="外部策略 ID" data-index="externalStrategyId" />
        <a-table-column title="启用" data-index="enabled" />
        <a-table-column title="操作" key="op">
          <template #default="{ record }">
            <a-button type="link" size="small" @click="editStrategy(record)">编辑</a-button>
            <a-button type="link" danger size="small" @click="removeStrategy(record.id)">删除</a-button>
          </template>
        </a-table-column>
      </a-table>
    </a-card>

    <a-card title="栏目绑定" size="small">
      <a-space wrap style="margin-bottom: 12px;">
        <a-input v-model:value="bindCatalogId" style="width: 280px" placeholder="catalogId" />
        <a-button type="primary" :loading="loadingBd" @click="loadBinds">查询绑定</a-button>
      </a-space>
      <a-form layout="inline" style="margin-bottom: 16px; gap: 8px;">
        <a-form-item label="catalogId">
          <a-input v-model:value="bindForm.catalogId" placeholder="默认同上" style="width: 200px" />
        </a-form-item>
        <a-form-item label="strategyId">
          <a-input v-model:value="bindForm.strategyId" style="width: 200px" />
        </a-form-item>
        <a-form-item label="resourceType">
          <a-input-number v-model:value="bindForm.resourceType" />
        </a-form-item>
        <a-form-item label="sortNum">
          <a-input-number v-model:value="bindForm.sortNum" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="saveBind">新增绑定</a-button>
        </a-form-item>
      </a-form>

      <a-table :data-source="binds" :loading="loadingBd" row-key="id" size="small" :pagination="false">
        <a-table-column title="栏目" data-index="catalogId" />
        <a-table-column title="策略" data-index="strategyId" />
        <a-table-column title="资源类型" data-index="resourceType" />
        <a-table-column title="排序" data-index="sortNum" />
        <a-table-column title="操作" key="op">
          <template #default="{ record }">
            <a-button type="link" danger size="small" @click="removeBind(record.id)">删除</a-button>
          </template>
        </a-table-column>
      </a-table>
    </a-card>
  </div>
</template>
