<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import {
  getResourceDetail,
  materialApiAbsoluteUrl,
  type MaterialResourceDetailVO
} from '@/api/mam_resource_api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<MaterialResourceDetailVO | null>(null)

const resourceId = computed(() => {
  const p = route.params.resourceId
  return typeof p === 'string' ? p : Array.isArray(p) ? p[0] : ''
})

const backPath = computed(() =>
  route.path.startsWith('/embed') ? '/embed/material' : '/material'
)

function typeLabel(t: number) {
  switch (t) {
    case 1:
      return '视频'
    case 2:
      return '音频'
    case 3:
      return '图片'
    case 4:
      return '文本'
    case 5:
      return 'Office'
    case 7:
      return '文件夹'
    default:
      return '其他'
  }
}

function formatSize(size?: number) {
  if (size == null) return '—'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(1)} MB`
  return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`
}

const r = computed(() => detail.value?.resource)

/** 封面：优先 cover / 视频关键帧，否则图片预览 */
const posterSrc = computed(() => {
  const x = r.value
  if (!x) return ''
  if (x.coverUrl) return materialApiAbsoluteUrl(x.coverUrl)
  if (x.keyframeUrl) return materialApiAbsoluteUrl(x.keyframeUrl)
  if (x.previewUrl && x.type === 3) return materialApiAbsoluteUrl(x.previewUrl)
  return ''
})

const imagePreviewSrc = computed(() => {
  const x = r.value
  if (!x?.previewUrl || x.type !== 3) return ''
  return materialApiAbsoluteUrl(x.previewUrl)
})

async function load() {
  const id = resourceId.value
  if (!id) {
    message.warning('缺少资源 ID')
    return
  }
  loading.value = true
  detail.value = null
  try {
    detail.value = await getResourceDetail(id)
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void load()
})

watch(resourceId, () => {
  void load()
})

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push(backPath.value)
  }
}
</script>

<template>
  <div class="resource-detail-page">
    <div class="resource-detail-bar">
      <a-button type="link" class="resource-detail-back" @click="goBack">
        <template #icon>
          <arrow-left-outlined />
        </template>
        返回
      </a-button>
    </div>

    <a-spin :spinning="loading">
      <div v-if="r" class="resource-detail-body">
        <header class="resource-detail-head">
          <h1 class="resource-detail-title">{{ r.title }}</h1>
          <div class="resource-detail-meta">
            <span>{{ typeLabel(r.type) }}</span>
            <span>{{ formatSize(r.fileSize) }}</span>
            <span class="resource-detail-id" :title="r.id">ID {{ r.id }}</span>
          </div>
        </header>

        <section v-if="r.type === 3 && imagePreviewSrc" class="resource-detail-visual">
          <img :src="imagePreviewSrc" class="detail-img-full" alt="" />
        </section>

        <section v-else-if="r.previewUrl && r.type === 1" class="resource-detail-visual">
          <video
            class="detail-video"
            controls
            :src="materialApiAbsoluteUrl(r.previewUrl)"
            :poster="posterSrc || undefined"
          />
        </section>

        <section v-else-if="r.previewUrl && r.type === 2" class="resource-detail-visual">
          <audio class="detail-audio" controls :src="materialApiAbsoluteUrl(r.previewUrl)" />
        </section>

        <a-descriptions bordered size="small" :column="1" class="resource-detail-desc">
          <a-descriptions-item label="栏目 ID">{{ r.catalogId }}</a-descriptions-item>
          <a-descriptions-item label="父资源">{{ r.parentId }}</a-descriptions-item>
          <a-descriptions-item label="指纹">{{ r.fingerprint || '—' }}</a-descriptions-item>
          <a-descriptions-item label="封面（原图直链）">
            <a v-if="r.coverUrl" :href="r.coverUrl" target="_blank" rel="noopener noreferrer">{{ r.coverUrl }}</a>
            <span v-else>—</span>
          </a-descriptions-item>
          <a-descriptions-item label="关键帧 keyframeUrl">
            <a
              v-if="r.keyframeUrl"
              :href="materialApiAbsoluteUrl(r.keyframeUrl)"
              target="_blank"
              rel="noopener noreferrer"
            >{{ materialApiAbsoluteUrl(r.keyframeUrl) }}</a>
            <span v-else>—（视频抽帧接入后可用）</span>
          </a-descriptions-item>
          <a-descriptions-item label="预览">
            <a
              v-if="r.previewUrl"
              :href="materialApiAbsoluteUrl(r.previewUrl)"
              target="_blank"
              rel="noopener noreferrer"
            >打开预览接口</a>
            <span v-else>—</span>
          </a-descriptions-item>
          <a-descriptions-item label="原文件 srcUrl">
            <a v-if="r.srcUrl" :href="r.srcUrl" target="_blank" rel="noopener noreferrer">{{ r.srcUrl }}</a>
            <span v-else>—</span>
          </a-descriptions-item>
        </a-descriptions>

        <div v-if="detail?.transcodeTasks?.length" class="detail-block">
          <div class="detail-block-title">转码任务</div>
          <a-table
            :data-source="detail.transcodeTasks"
            :pagination="false"
            row-key="id"
            size="small"
          >
            <a-table-column title="类型" data-index="taskType" />
            <a-table-column title="状态" data-index="status" />
            <a-table-column title="进度" data-index="progress" />
          </a-table>
        </div>

        <div v-if="detail?.metadata?.length" class="detail-block">
          <div class="detail-block-title">编目快照</div>
          <div v-for="m in detail.metadata" :key="m.templateId" class="detail-meta-card">
            <div class="detail-meta-h">{{ m.templateName }} (v{{ m.version }})</div>
            <a-table
              v-if="m.entries?.length"
              :data-source="m.entries"
              :pagination="false"
              row-key="fieldId"
              size="small"
            >
              <a-table-column title="字段" data-index="fieldName" />
              <a-table-column title="值" data-index="fieldValue" ellipsis />
            </a-table>
          </div>
        </div>

        <div v-if="detail?.tasks?.length" class="detail-block">
          <div class="detail-block-title">全部任务</div>
          <a-table :data-source="detail.tasks" :pagination="false" row-key="id" size="small">
            <a-table-column title="类型" data-index="taskType" />
            <a-table-column title="状态" data-index="status" />
          </a-table>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<style scoped lang="less">
.resource-detail-page {
  min-height: 100vh;
  background: #eef1f6;
  padding: 16px 20px 32px;
}

.resource-detail-bar {
  margin-bottom: 12px;
}

.resource-detail-back {
  padding-left: 0;
  color: #374151;
}

.resource-detail-body {
  max-width: 960px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  border: 1px solid #e5e7eb;
}

.resource-detail-head {
  margin-bottom: 16px;
}

.resource-detail-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  color: #111827;
  word-break: break-all;
}

.resource-detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: #6b7280;
}

.resource-detail-id {
  font-family: ui-monospace, monospace;
  font-size: 12px;
}

.resource-detail-visual {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  background: #f3f4f6;
}

.detail-img-full {
  display: block;
  width: 100%;
  max-height: 520px;
  object-fit: contain;
}

.detail-video {
  width: 100%;
  max-height: 480px;
  border-radius: 8px;
  background: #000;
}

.detail-audio {
  width: 100%;
}

.resource-detail-desc {
  margin-bottom: 20px;
}

.detail-block {
  margin-top: 20px;
}

.detail-block-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #111827;
}

.detail-meta-card {
  margin-bottom: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.detail-meta-h {
  font-size: 13px;
  margin-bottom: 8px;
  color: #374151;
}
</style>
