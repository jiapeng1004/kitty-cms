<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import {
  getResourceDetail,
  getResourceDownloadUrl,
  materialApiAbsoluteUrl,
  reportResourceDownload,
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

/** 打开直链并在后端记录下载行为（含降级码率时的 actualDestinationType） */
async function openTierDownload(destinationType: string) {
  const id = resourceId.value
  const res = r.value
  if (!id || !res || res.type === 7) return
  try {
    const d = await getResourceDownloadUrl({ resourceId: id, destinationType })
    window.open(d.url, '_blank', 'noopener,noreferrer')
    await reportResourceDownload({
      resourceId: id,
      destinationType: d.destinationType,
      resourceTitle: res.title,
      actualDestinationType: d.actualDestinationType
    })
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '获取下载地址失败')
  }
}
</script>

<template>
  <div class="mam-page mam-page--padded resource-detail-page">
    <div class="resource-detail-bar">
      <a-button type="link" class="resource-detail-back" @click="goBack">
        <template #icon>
          <arrow-left-outlined />
        </template>
        返回
      </a-button>
    </div>

    <a-spin :spinning="loading">
      <div v-if="r" class="resource-detail-body mam-panel">
        <header class="resource-detail-head">
          <h1 class="resource-detail-title mam-page-title">{{ r.title }}</h1>
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

        <div v-if="r.type !== 7" class="detail-block">
          <div class="detail-block-title">下载</div>
          <a-space wrap>
            <a-button type="primary" size="small" :loading="loading" @click="openTierDownload('SOURCE')">
              源码
            </a-button>
            <a-button
              v-if="r.type === 1"
              size="small"
              :loading="loading"
              @click="openTierDownload('COVER')"
            >
              封面
            </a-button>
            <a-button
              v-if="r.type === 1"
              size="small"
              :loading="loading"
              @click="openTierDownload('SPRITE')"
            >
              雪碧图
            </a-button>
          </a-space>
        </div>

        <div v-if="detail?.derivatives?.length" class="detail-block mam-table-wrap">
          <div class="detail-block-title">衍生产物</div>
          <a-table
            :data-source="detail!.derivatives!"
            :pagination="false"
            :row-key="(row) => row.destinationType"
            size="small"
          >
            <a-table-column title="分级" data-index="destinationType" width="100" />
            <a-table-column title="大小" key="fs" width="100">
              <template #default="{ record }">
                {{ record.fileSize == null ? '—' : formatSize(record.fileSize) }}
              </template>
            </a-table-column>
            <a-table-column title="可访问" key="url" ellipsis>
              <template #default="{ record }">
                <a
                  v-if="record.accessUrl"
                  :href="record.accessUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                >打开</a>
                <span v-else>—</span>
              </template>
            </a-table-column>
            <a-table-column title="操作" key="op" width="120">
              <template #default="{ record }">
                <a-button
                  type="link"
                  size="small"
                  :loading="loading"
                  @click="openTierDownload(record.destinationType)"
                >
                  拉取并上报
                </a-button>
              </template>
            </a-table-column>
          </a-table>
        </div>

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
  min-height: 100dvh;
  padding-bottom: 8px;
}

.resource-detail-bar {
  margin-bottom: 14px;
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;
}

.resource-detail-back {
  padding-left: 0;
  color: var(--mam-text-secondary, #64748b);
  font-weight: 500;
}

.resource-detail-back:hover {
  color: var(--mam-primary, #1677ff) !important;
}

.resource-detail-body {
  max-width: 1000px;
  margin: 0 auto;
  padding: 22px 26px 28px;
  border-radius: var(--mam-radius-lg, 12px);
}

.resource-detail-head {
  margin-bottom: 16px;
}

.resource-detail-title {
  margin: 0 0 10px;
  font-size: 22px;
  font-weight: 600;
  color: var(--mam-text, #0f172a);
  word-break: break-all;
  letter-spacing: 0.02em;
}

.resource-detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: var(--mam-text-secondary, #64748b);
}

.resource-detail-id {
  font-family: ui-monospace, monospace;
  font-size: 12px;
}

.resource-detail-visual {
  margin-bottom: 20px;
  border-radius: var(--mam-radius-lg, 12px);
  overflow: hidden;
  background: var(--mam-muted-bg, #f8fafc);
  border: 1px solid var(--mam-border, #e2e8f0);
  box-shadow: var(--mam-shadow-sm, 0 1px 3px rgba(15, 23, 42, 0.06));
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
  margin-bottom: 10px;
  color: var(--mam-text, #0f172a);
  font-size: 15px;
}

.detail-meta-card {
  margin-bottom: 12px;
  padding: 14px;
  background: var(--mam-muted-bg, #f8fafc);
  border-radius: 10px;
  border: 1px solid var(--mam-border, #e2e8f0);
}

.detail-meta-h {
  font-size: 13px;
  margin-bottom: 8px;
  color: var(--mam-text-secondary, #64748b);
  font-weight: 600;
}
</style>
