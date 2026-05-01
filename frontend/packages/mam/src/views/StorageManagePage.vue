<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CloseOutlined, LeftOutlined } from '@ant-design/icons-vue'
import { Modal, message } from 'ant-design-vue'
import { FALLBACK_STORAGE_INSTANCE_OPTIONS } from '@/constants/mam_storage_instance_options'
import {
  createFileStorage,
  deleteFileStorage,
  listFileStorageConfigs,
  listStorageInstanceOptions,
  updateFileStorage,
  type MaterialFileStorageVO,
  type MaterialFileStorageUpsertDTO,
  type MaterialStorageInstanceOptionVO
} from '@/api/mam_storage_api'
import { useMamTableCellCopy } from '@/composables/useMamTableCellCopy'

const { copyCell } = useMamTableCellCopy()

const route = useRoute()
const router = useRouter()
const isEmbed = computed(() => route.path.startsWith('/embed/'))
/** 与素材库同壳：嵌入或 immersive 路由下不显示整站顶栏，需条内返回 */
const showBackToMaterial = computed(
  () => isEmbed.value || (route.meta as { immersive?: boolean }).immersive === true
)

function goBackToMaterial() {
  router.push(isEmbed.value ? '/embed/material' : '/material')
}

const loading = ref(false)
/** 列表加载失败原因（如未登录），用于顶部提示，避免误以为「保存了但列表不显示」 */
const loadError = ref<string | null>(null)
const rows = ref<MaterialFileStorageVO[]>([])
const instanceOptions = ref<MaterialStorageInstanceOptionVO[]>([...FALLBACK_STORAGE_INSTANCE_OPTIONS])
const formOpen = ref(false)
const saving = ref(false)
const editingId = ref<string | null>(null)

const form = reactive<MaterialFileStorageUpsertDTO>({
  id: '',
  storageType: 's3',
  bucket: '',
  internalEndpoint: '',
  externalEndpoint: '',
  accessKey: '',
  secretKey: '',
  primaryStorage: false
})

const engineOptions = [
  { value: 's3', label: '对象存储（S3 兼容）' },
  { value: 'disk', label: '本地磁盘' }
]

/** 快捷填入：预设建议主键（与后端枚举一致，仅省打字，非强制） */
function applyPresetId(opt: MaterialStorageInstanceOptionVO) {
  form.id = opt.code
  form.storageType = opt.storageType as 's3' | 'disk'
}

/** 全屏表单层打开时锁滚动，避免背后列表滚动穿透 */
watch(formOpen, (open) => {
  document.body.style.overflow = open ? 'hidden' : ''
})

onUnmounted(() => {
  document.body.style.overflow = ''
})

function friendlyListError(e: unknown): string {
  const err = e as {
    response?: { status?: number; data?: { message?: string; state?: number } }
    message?: string
  }
  const status = err.response?.status
  const msg = String(err.response?.data?.message ?? '')
  const state = err.response?.data?.state
  if (status === 401) {
    return '未登录或 token 无效：请先在 Kitty 管理端登录（或从管理端带 SSO 打开 MAM），确保 localStorage 中存在 kitty_admin_token 后再访问本页。'
  }
  if (status === 403) {
    return '无存储管理权限（material:storage:manage）'
  }
  if (msg === '参数错误' || state === 10012) {
    return '无法加载：请确认已登录，且账号已分配「material:storage:manage」权限。'
  }
  if (/token/i.test(msg) || msg.includes('登录')) {
    return `${msg} — 若直接打开 MAM 页面，请先完成管理端登录。`
  }
  return msg || err.message || '加载失败'
}

async function load() {
  loading.value = true
  loadError.value = null
  try {
    rows.value = await listFileStorageConfigs()
  } catch (e: unknown) {
    const text = friendlyListError(e)
    loadError.value = text
    message.error(text)
  } finally {
    loading.value = false
  }
}

async function loadInstanceOptions() {
  try {
    const list = await listStorageInstanceOptions()
    if (Array.isArray(list) && list.length > 0) {
      instanceOptions.value = list
    }
  } catch {
    /* 使用 FALLBACK_STORAGE_INSTANCE_OPTIONS */
  }
}

function openCreate() {
  editingId.value = null
  form.id = ''
  form.storageType = 's3'
  form.bucket = ''
  form.internalEndpoint = ''
  form.externalEndpoint = ''
  form.accessKey = ''
  form.secretKey = ''
  form.primaryStorage = false
  formOpen.value = true
}

function openEdit(row: MaterialFileStorageVO) {
  editingId.value = row.id
  form.id = row.id
  form.storageType = row.storageType
  form.bucket = row.bucket ?? ''
  form.internalEndpoint = row.internalEndpoint ?? ''
  form.externalEndpoint = row.externalEndpoint ?? ''
  form.accessKey = row.accessKey ?? ''
  form.secretKey = row.secretKey ?? ''
  form.primaryStorage = row.primaryStorage ?? false
  formOpen.value = true
}

function closeForm() {
  formOpen.value = false
}

async function submit() {
  const customId = form.id?.trim()
  if (!editingId.value && customId) {
    if (!/^[a-zA-Z0-9][a-zA-Z0-9._-]{0,62}$/.test(customId)) {
      message.warning('存储 ID 须1–64位：字母数字开头，仅含 . _ -')
      return Promise.reject()
    }
  }
  if (form.storageType === 's3' && !editingId.value && !form.secretKey?.trim()) {
    message.warning('S3 存储请填写 Secret Key')
    return Promise.reject()
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateFileStorage({ ...form })
      message.success('已保存')
    } else {
      const payload = { ...form } as MaterialFileStorageUpsertDTO
      const tid = payload.id?.trim()
      if (tid) {
        await createFileStorage({ ...payload, id: tid })
      } else {
        const { id: _unused, ...rest } = payload
        await createFileStorage(rest)
      }
      message.success('已创建')
    }
    closeForm()
    await load()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function confirmDelete(row: MaterialFileStorageVO) {
  Modal.confirm({
    title: '删除存储配置？',
    content: `将删除存储「${row.id}」，请确认无资源仍依赖该存储。`,
    okType: 'danger',
    onOk: () =>
      deleteFileStorage(row.id)
        .then(() => {
          message.success('已删除')
          return load()
        })
        .catch((e: unknown) => {
          const err = e as { response?: { data?: { message?: string } }; message?: string }
          message.error(err?.response?.data?.message || err?.message || '删除失败')
          return Promise.reject(e)
        })
  })
}

onMounted(async () => {
  await Promise.all([loadInstanceOptions(), load()])
})
</script>

<template>
  <div
    class="storage-manage-page"
    :class="{ 'storage-manage-page--immersive': showBackToMaterial }"
  >
    <div v-if="showBackToMaterial" class="storage-embed-bar">
      <a-button type="link" class="storage-back" @click="goBackToMaterial">
        <template #icon>
          <LeftOutlined />
        </template>
        返回素材库
      </a-button>
    </div>

    <a-card :bordered="false" class="storage-card">
      <a-alert
        v-if="loadError"
        type="error"
        show-icon
        class="storage-load-error"
        :message="loadError"
        closable
        @close="loadError = null"
      />
      <div class="storage-toolbar">
        <div class="storage-toolbar-text">
          <h1 class="storage-title">存储管理</h1>
          <p class="storage-sub">管理 S3 兼容对象存储与本地磁盘实例；新建时实例编码从系统枚举中选择。</p>
        </div>
        <a-button type="primary" @click="openCreate">新建存储</a-button>
      </div>

      <a-alert
        type="info"
        show-icon
        class="storage-hint"
        message="说明"
        description="存储主键 id 即对外 storageId（唯一）。新建可自填或留空由服务端随机生成。须指定一个「主存储」。列表较宽时可横向滚动；除「操作」外单击单元格可复制。"
      />

      <a-table
        :data-source="rows"
        :loading="loading"
        row-key="id"
        :pagination="false"
        size="small"
        class="storage-table"
        :scroll="{ x: 1120 }"
      >
        <a-table-column title="存储 ID" key="id" :ellipsis="true" :width="220">
          <template #default="{ record }">
            <span
              class="mam-table-cell-copy storage-cred-cell"
              :title="record.id"
              @click="copyCell(record.id)"
            >{{ record.id }}</span>
          </template>
        </a-table-column>
        <a-table-column title="引擎" key="storageType" :width="100">
          <template #default="{ record }">
            <span class="mam-table-cell-copy" @click="copyCell(record.storageType)">
              <a-tag :color="record.storageType === 's3' ? 'processing' : 'default'">{{ record.storageType }}</a-tag>
            </span>
          </template>
        </a-table-column>
        <a-table-column title="主存储" key="primary" :width="96">
          <template #default="{ record }">
            <span class="mam-table-cell-copy" @click="copyCell(record.primaryStorage ? '主' : '非主')">
              <a-tag v-if="record.primaryStorage" color="success">主</a-tag>
              <span v-else class="storage-primary-no">非主</span>
            </span>
          </template>
        </a-table-column>
        <a-table-column title="桶 / 路径" key="bucket" ellipsis>
          <template #default="{ record }">
            <span
              class="mam-table-cell-copy"
              :title="record.bucket ?? ''"
              @click="copyCell(record.bucket)"
            >{{ record.bucket || '—' }}</span>
          </template>
        </a-table-column>
        <a-table-column title="内网 Endpoint" key="internalEndpoint" ellipsis>
          <template #default="{ record }">
            <span
              class="mam-table-cell-copy storage-cred-cell"
              :title="record.internalEndpoint ?? ''"
              @click="copyCell(record.internalEndpoint)"
            >{{ record.internalEndpoint || '—' }}</span>
          </template>
        </a-table-column>
        <a-table-column title="AccessKey" key="ak" :width="200" ellipsis>
          <template #default="{ record }">
            <span
              class="mam-table-cell-copy storage-cred-cell"
              :title="record.accessKey ?? ''"
              @click="copyCell(record.accessKey)"
            >{{ record.accessKey || '—' }}</span>
          </template>
        </a-table-column>
        <a-table-column title="Secret" key="sk" :width="200" ellipsis>
          <template #default="{ record }">
            <span
              class="mam-table-cell-copy storage-cred-cell"
              :title="record.secretKey ?? ''"
              @click="copyCell(record.secretKey)"
            >{{ record.secretKey || '—' }}</span>
          </template>
        </a-table-column>
        <a-table-column title="操作" key="op" :width="140" fixed="right">
          <template #default="{ record }">
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger size="small" @click="confirmDelete(record)">删除</a-button>
          </template>
        </a-table-column>
      </a-table>
    </a-card>

    <Teleport to="body">
      <div
        v-if="formOpen"
        class="storage-form-overlay"
        role="dialog"
        aria-modal="true"
        aria-labelledby="storage-form-title"
      >
        <div class="storage-form-panel">
          <header class="storage-form-header">
            <h2 id="storage-form-title" class="storage-form-title">
              {{ editingId ? '编辑存储' : '新建存储' }}
            </h2>
            <button type="button" class="storage-form-close" aria-label="关闭" @click="closeForm">
              <close-outlined />
            </button>
          </header>
          <div class="storage-form-body">
            <a-form layout="vertical">
              <a-form-item v-if="!editingId" label="存储 ID（可选）">
                <a-input v-model:value="form.id" placeholder="留空则服务端随机生成 32 位十六进制；自填如 default-s3" allow-clear />
                <span class="form-item-hint">与库表主键、对外 storageId 为同一字段；1–64 位，字母数字开头，仅含 . _ -</span>
                <div v-if="instanceOptions.length" class="storage-id-presets">
                  <span class="storage-id-presets-label">快捷填入：</span>
                  <a-button
                    v-for="opt in instanceOptions"
                    :key="opt.code + opt.storageType"
                    type="link"
                    size="small"
                    class="storage-id-preset-btn"
                    @click="applyPresetId(opt)"
                  >
                    {{ opt.code }}
                  </a-button>
                </div>
              </a-form-item>
              <a-form-item v-else label="存储 ID（不可改）" required>
                <a-input :value="form.id" disabled />
              </a-form-item>
              <a-form-item label="引擎类型" required>
                <a-select v-model:value="form.storageType" :options="engineOptions" :disabled="!!editingId" />
              </a-form-item>
              <a-form-item :label="form.storageType === 's3' ? 'Bucket' : '本地根路径'">
                <a-input v-model:value="form.bucket" :placeholder="form.storageType === 's3' ? 'bucket 名称' : '如 D:/mam-data'" />
              </a-form-item>
              <a-form-item label="内网 Endpoint（S3 填服务端点 URL）">
                <a-input
                  v-model:value="form.internalEndpoint"
                  placeholder="如 https://s3.amazonaws.com 或 http://127.0.0.1:9000"
                />
              </a-form-item>
              <a-form-item label="外网 Endpoint（可选）">
                <a-input v-model:value="form.externalEndpoint" placeholder="浏览器直链场景可填" />
              </a-form-item>
              <a-form-item label="主存储">
                <a-switch v-model:checked="form.primaryStorage" checked-children="是" un-checked-children="否" />
                <span class="form-item-hint">全局仅能有一个主存储；开启后会自动取消其它条目的主存储。</span>
              </a-form-item>
              <a-form-item label="Access Key">
                <a-input v-model:value="form.accessKey" placeholder="新建必填；编辑可改；清空保存表示不修改已有 Key" />
              </a-form-item>
              <a-form-item :label="form.storageType === 's3' ? 'Secret Key' : 'Secret（磁盘一般留空）'">
                <a-input
                  v-model:value="form.secretKey"
                  :placeholder="editingId ? '编辑显示当前值；清空保存表示不修改' : (form.storageType === 's3' ? '必填' : '可选')"
                  type="text"
                  autocomplete="off"
                />
              </a-form-item>
            </a-form>
          </div>
          <footer class="storage-form-footer">
            <a-button @click="closeForm">取消</a-button>
            <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
          </footer>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped lang="less">
.storage-manage-page {
  min-height: 100%;
  padding: 16px 20px 32px;
  max-width: 1280px;
  margin: 0 auto;
  box-sizing: border-box;
  background: var(--mam-page-bg);
}

/* 与素材库同壳（无整站顶栏）：主区横向拉满，减少两侧灰边 */
.storage-manage-page--immersive {
  max-width: none;
  padding: 12px 16px 28px;
}

.storage-embed-bar {
  margin-bottom: 12px;
}

.storage-back {
  padding-left: 0;
  height: auto;
  font-weight: 500;
}

.storage-card {
  border-radius: var(--mam-radius-lg);
  box-shadow: var(--mam-shadow-sm);
}

.storage-load-error {
  margin-bottom: 12px;
}

.storage-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.storage-toolbar-text {
  flex: 1;
  min-width: 200px;
}

.storage-title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 600;
  color: var(--mam-text);
  line-height: 1.3;
}

.storage-sub {
  margin: 0;
  font-size: 13px;
  color: var(--mam-text-secondary);
  line-height: 1.5;
}

.storage-hint {
  margin-bottom: 16px;
}

.storage-hint :deep(.ant-alert-description) {
  font-size: 13px;
  color: var(--mam-text-secondary);
  line-height: 1.55;
}

.storage-table {
  margin-top: 0;
}

.storage-table :deep(.ant-table) {
  font-size: 13px;
}

.storage-primary-no {
  font-size: 13px;
  color: var(--mam-text-secondary);
}

.form-item-hint {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: var(--mam-text-secondary);
  line-height: 1.4;
}

.storage-id-presets {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 8px;
}

.storage-id-presets-label {
  font-size: 12px;
  color: var(--mam-text-secondary);
}

.storage-id-preset-btn {
  padding: 0 4px;
  height: auto;
}

.storage-cred-cell {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  word-break: break-all;
}

</style>

<!-- Teleport 到 body，需非 scoped：本页已 immersive，无整站顶栏，全屏从顶部算起 -->
<style lang="less">
.storage-form-overlay {
  position: fixed;
  z-index: 1000;
  left: 0;
  right: 0;
  bottom: 0;
  top: 0;
  background: var(--mam-page-bg);
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;
  padding: 20px 24px 32px;
}

.storage-form-panel {
  max-width: 720px;
  margin: 0 auto;
  background: var(--mam-surface);
  border-radius: var(--mam-radius-lg);
  box-shadow: var(--mam-shadow-md);
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 40px);
}

.storage-form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--mam-border);
}

.storage-form-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--mam-text);
}

.storage-form-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--mam-text-secondary);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.storage-form-close:hover {
  background: var(--mam-muted-bg);
  color: var(--mam-text);
}

.storage-form-body {
  padding: 20px 20px 8px;
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.storage-form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--mam-border);
  background: var(--mam-muted-bg);
  border-radius: 0 0 var(--mam-radius-lg) var(--mam-radius-lg);
}
</style>
