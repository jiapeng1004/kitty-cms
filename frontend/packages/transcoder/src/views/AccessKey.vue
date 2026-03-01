<template>
  <div>
    <a-space class="mb">
      <a-button type="primary" @click="showCreate = true">创建 Access Key</a-button>
    </a-space>
    <a-table
        :columns="columns"
        :data-source="list" :loading="loading" row-key="accessKeyId">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-popconfirm title="确定删除该 Access Key？删除后无法恢复。" ok-text="删除" cancel-text="取消" @confirm="onDelete(record.accessKeyId)">
            <a class="danger">删除</a>
          </a-popconfirm>
        </template>
      </template>
    </a-table>
    <a-modal v-model:open="showCreate" title="创建 Access Key" @ok="doCreate" :confirm-loading="submitting"
             ok-text="创建并复制 Secret">
      <a-form layout="vertical">
        <a-form-item label="名称">
          <a-input v-model:value="createName" placeholder="如：前端控制台、API 调用方"/>
        </a-form-item>
        <a-alert v-if="createdSecret" type="warning" :message="'Secret Key（仅显示一次）：' + createdSecret" show-icon/>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { listAccessKeys, createAccessKey, deleteAccessKey } from '../api/auth_api'

const loading = ref(false)
const columns = [
  { title: 'Access Key ID', dataIndex: 'accessKeyId', key: 'accessKeyId' },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '操作', key: 'action', width: 80 }
]
const list = ref([])
const showCreate = ref(false)
const submitting = ref(false)
const createName = ref('')
const createdSecret = ref('')

async function load() {
  loading.value = true
  try {
    const data = await listAccessKeys()
    list.value = Array.isArray(data) ? data : []
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function doCreate() {
  submitting.value = true
  createdSecret.value = ''
  try {
    const res = await createAccessKey({ name: createName.value })
    createdSecret.value = res.secretKey || ''
    message.success('已创建，请复制 Secret Key 妥善保存')
    load()
  } catch (e) {
    message.error(e?.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

async function onDelete(accessKeyId) {
  try {
    await deleteAccessKey(accessKeyId)
    message.success('已删除')
    load()
  } catch (e) {
    message.error(e?.message || '删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.mb {
  margin-bottom: 16px;
}
.danger {
  color: var(--ant-color-error);
}
</style>
