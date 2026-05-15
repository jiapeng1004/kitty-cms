<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { MenuProps } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  CloudDownloadOutlined,
  FieldTimeOutlined,
  FileSearchOutlined,
  InfoCircleOutlined,
  RocketOutlined,
  SwapOutlined,
  TagsOutlined
} from '@ant-design/icons-vue'
import {
  getResourceDetail,
  getResourceDownloadUrl,
  materialApiAbsoluteUrl,
  reportResourceDownload,
  type MaterialResourceDetailVO
} from '@/mam/api/mam_resource_api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<MaterialResourceDetailVO | null>(null)

type RailKey = 'info' | 'ai' | 'asset' | 'produce' | 'logs'

const activeRail = ref<RailKey>('info')

const railItems: { key: RailKey; label: string; sub: string; icon: typeof InfoCircleOutlined }[] = [
  { key: 'info', label: '信息', sub: '基础', icon: InfoCircleOutlined },
  { key: 'ai', label: 'AI', sub: '识别', icon: TagsOutlined },
  { key: 'asset', label: '资源', sub: '链路', icon: FileSearchOutlined },
  { key: 'produce', label: '生产', sub: '产物', icon: RocketOutlined },
  { key: 'logs', label: '记录', sub: '流程', icon: FieldTimeOutlined }
]

const resourceId = computed(() => {
  const p = route.params.resourceId
  return typeof p === 'string' ? p : Array.isArray(p) ? p[0] : ''
})

const backPath = computed(() =>
  route.path.startsWith('/embed') ? '/embed/material' : '/material'
)

const reviewPath = computed(() =>
  route.path.startsWith('/embed') ? '/embed/review' : '/review'
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

const showSpriteStrip = computed(() => r.value?.type === 1 && !!r.value?.keyframeUrl)

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

function goReview() {
  void router.push(reviewPath.value)
}


const onDownloadMenuClick: MenuProps['onClick'] = async ({ key }) => {
  const k = String(key)
  if (k === 'menu-src' || k === 'menu-cover' || k === 'menu-sprite') {
    const map: Record<string, string> = {
      'menu-src': 'SOURCE',
      'menu-cover': 'COVER',
      'menu-sprite': 'SPRITE'
    }
    await openTierDownload(map[k]!)
    return
  }
  if (k.startsWith('der-')) {
    const dt = k.slice('der-'.length)
    await openTierDownload(dt)
  }
}

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
  <div class="detail-shell mam-page">
    <a-spin :spinning="loading" class="detail-spin">
      <template v-if="r">
        <header class="detail-topbar">
          <div class="detail-topbar-left">
            <a-button type="link" class="detail-back" @click="goBack">
              <template #icon>
                <arrow-left-outlined />
              </template>
              返回内容库
            </a-button>
            <span class="detail-topbar-divider" aria-hidden="true" />
            <h1 class="detail-topbar-title" :title="r.title">{{ r.title }}</h1>
          </div>
          <div class="detail-topbar-right">
            <a-dropdown v-if="r.type !== 7" :trigger="['click']">
              <template #overlay>
                <a-menu class="detail-dl-menu" @click="onDownloadMenuClick">
                  <a-menu-item key="menu-src">源码（SOURCE）</a-menu-item>
                  <a-menu-item v-if="r.type === 1" key="menu-cover">封面（COVER）</a-menu-item>
                  <a-menu-item v-if="r.type === 1" key="menu-sprite">雪碧图（SPRITE）</a-menu-item>
                  <a-menu-divider v-if="detail?.derivatives?.length" />
                  <a-menu-item
                    v-for="d in detail?.derivatives ?? []"
                    :key="'der-' + d.destinationType"
                    :disabled="!d.available && !d.accessUrl"
                  >
                    分级：{{ d.destinationType }}
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button class="detail-ghost-btn" size="small">
                <template #icon>
                  <cloud-download-outlined />
                </template>
                下载
              </a-button>
            </a-dropdown>
            <a-button type="primary" size="small" @click="goReview">提交审核</a-button>
          </div>
        </header>

        <div class="detail-body">
          <section class="detail-play" aria-label="预览">
            <div class="detail-play-head">
              <span class="detail-play-hint">代理预览 · 空格在播放器内生效</span>
              <span class="detail-play-meta">{{ typeLabel(r.type) }} · {{ formatSize(r.fileSize) }}</span>
            </div>

            <div class="detail-play-stage">
              <div v-if="r.type === 3 && imagePreviewSrc" class="detail-play-inner">
                <img :src="imagePreviewSrc" class="detail-play-img" alt="" />
              </div>
              <div v-else-if="r.previewUrl && r.type === 1" class="detail-play-inner detail-play-inner--video">
                <video
                  class="detail-video"
                  controls
                  :src="materialApiAbsoluteUrl(r.previewUrl)"
                  :poster="posterSrc || undefined"
                />
              </div>
              <div v-else-if="r.previewUrl && r.type === 2" class="detail-play-inner">
                <audio class="detail-audio" controls :src="materialApiAbsoluteUrl(r.previewUrl)" />
              </div>
              <div v-else class="detail-play-empty">
                <p>暂无可内嵌预览的代理流</p>
                <p class="detail-play-empty-sub">文件夹或尚未产出预览时，可在右侧「资源」查看直链。</p>
              </div>
            </div>

            <div v-if="showSpriteStrip" class="detail-sprite-row">
              <span class="detail-sprite-label">关键帧 / 雪碧条带（示意）</span>
              <div class="detail-sprite-strip">
                <img :src="materialApiAbsoluteUrl(r.keyframeUrl!)" alt="" class="detail-sprite-thumb" />
              </div>
            </div>
          </section>

          <nav class="detail-rail" aria-label="详情分区">
            <button
              v-for="item in railItems"
              :key="item.key"
              type="button"
              class="detail-rail-btn"
              :class="{ 'detail-rail-btn--active': activeRail === item.key }"
              @click="activeRail = item.key"
            >
              <component :is="item.icon" class="detail-rail-ico" />
              <span class="detail-rail-lab">{{ item.label }}</span>
              <span class="detail-rail-sub">{{ item.sub }}</span>
            </button>
          </nav>

          <aside class="detail-side" aria-live="polite">
            <div v-show="activeRail === 'info'" class="detail-side-scroll">
              <div class="detail-side-section">
                <h2 class="detail-side-h">基础信息</h2>
                <a-descriptions bordered size="small" :column="1" class="detail-desc">
                  <a-descriptions-item label="资源 ID">
                    <span class="detail-mono">{{ r.id }}</span>
                  </a-descriptions-item>
                  <a-descriptions-item label="类型">{{ typeLabel(r.type) }}</a-descriptions-item>
                  <a-descriptions-item label="大小">{{ formatSize(r.fileSize) }}</a-descriptions-item>
                  <a-descriptions-item label="栏目 ID">{{ r.catalogId }}</a-descriptions-item>
                  <a-descriptions-item label="父资源">{{ r.parentId }}</a-descriptions-item>
                  <a-descriptions-item label="指纹">{{ r.fingerprint || '—' }}</a-descriptions-item>
                </a-descriptions>
              </div>
              <div v-if="detail?.metadata?.length" class="detail-side-section">
                <h2 class="detail-side-h">编目快照</h2>
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
            </div>

            <div v-show="activeRail === 'ai'" class="detail-side-scroll">
              <div class="detail-side-section">
                <div class="detail-ai-head">
                  <h2 class="detail-side-h detail-ai-title">AI 解析标签</h2>
                  <span class="detail-ai-hint">模型与置信度展示接入后可在此纠错</span>
                </div>
                <a-alert
                  type="info"
                  show-icon
                  message="智能标签"
                  description="与编目模板、审核流程联动建设中；下方为识别类任务占位。"
                  class="detail-ai-alert"
                />
                <div v-if="detail?.taggingTasks?.length" class="detail-side-section detail-side-tight">
                  <h3 class="detail-side-h3">识别任务</h3>
                  <a-table
                    :data-source="detail.taggingTasks"
                    :pagination="false"
                    row-key="id"
                    size="small"
                  >
                    <a-table-column title="类型" data-index="taskType" />
                    <a-table-column title="状态" data-index="status" />
                    <a-table-column title="进度" data-index="progress" />
                  </a-table>
                </div>
              </div>
            </div>

            <div v-show="activeRail === 'asset'" class="detail-side-scroll mam-table-wrap">
              <div class="detail-side-section">
                <h2 class="detail-side-h">资源链路</h2>
                <a-descriptions bordered size="small" :column="1" class="detail-desc">
                  <a-descriptions-item label="封面">
                    <a v-if="r.coverUrl" :href="r.coverUrl" target="_blank" rel="noopener noreferrer">打开</a>
                    <span v-else>—</span>
                  </a-descriptions-item>
                  <a-descriptions-item label="关键帧">
                    <a
                      v-if="r.keyframeUrl"
                      :href="materialApiAbsoluteUrl(r.keyframeUrl)"
                      target="_blank"
                      rel="noopener noreferrer"
                    >打开</a>
                    <span v-else>—</span>
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
              </div>
            </div>

            <div v-show="activeRail === 'produce'" class="detail-side-scroll mam-table-wrap">
              <div class="detail-side-section">
                <h2 class="detail-side-h">下载与产物</h2>
                <div v-if="r.type !== 7" class="detail-dl-row">
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
                <div v-if="detail?.derivatives?.length" class="detail-side-section detail-side-tight">
                  <h3 class="detail-side-h3">衍生产物</h3>
                  <a-table
                    :data-source="detail!.derivatives!"
                    :pagination="false"
                    :row-key="(row) => row.destinationType"
                    size="small"
                  >
                    <a-table-column title="分级" data-index="destinationType" :width="100" />
                    <a-table-column title="大小" key="fs" :width="100">
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
                    <a-table-column title="操作" key="op" :width="120">
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
                <div v-if="detail?.transcodeTasks?.length" class="detail-side-section detail-side-tight">
                  <h3 class="detail-side-h3">转码任务</h3>
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
                <div v-if="detail?.tasks?.length" class="detail-side-section detail-side-tight">
                  <h3 class="detail-side-h3">全部任务</h3>
                  <a-table :data-source="detail.tasks" :pagination="false" row-key="id" size="small">
                    <a-table-column title="类型" data-index="taskType" />
                    <a-table-column title="状态" data-index="status" />
                  </a-table>
                </div>
              </div>
            </div>

            <div v-show="activeRail === 'logs'" class="detail-side-scroll">
              <div class="detail-side-section">
                <h2 class="detail-side-h">操作与审核</h2>
                <a-alert
                  type="warning"
                  show-icon
                  message="操作时间线"
                  description="全量操作审计接入后，将在此按时间线展示入库、替换、编目与审核节点。"
                  class="detail-log-alert"
                />
                <div v-if="detail?.reviewTasks?.length" class="detail-side-section detail-side-tight">
                  <h3 class="detail-side-h3">审核任务</h3>
                  <a-table :data-source="detail.reviewTasks" :pagination="false" row-key="id" size="small">
                    <a-table-column title="任务 ID" data-index="id" ellipsis :width="200" />
                    <a-table-column title="状态" data-index="status" :width="100" />
                    <a-table-column title="提交人" data-index="submitUserId" :width="120" />
                    <a-table-column title="创建时间" data-index="createdAt" ellipsis />
                  </a-table>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </template>
    </a-spin>
  </div>
</template>

<style scoped lang="less">
.detail-shell {
  box-sizing: border-box;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: var(--mam-shell-bg, #eef2f7);
  color: var(--mam-text);
}

.detail-spin {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.detail-spin :deep(.ant-spin-container) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.detail-topbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 52px;
  padding: 10px 24px;
  background: var(--mam-muted-bg, #f8fafc);
  border-bottom: 1px solid var(--mam-border, #e2e8f0);
}

.detail-topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.detail-back {
  padding: 0 4px 0 0;
  height: auto;
  color: var(--mam-primary, #1677ff);
  font-weight: 500;
  flex-shrink: 0;
}

.detail-topbar-divider {
  width: 1px;
  height: 22px;
  background: var(--mam-border, #e2e8f0);
  flex-shrink: 0;
}

.detail-topbar-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--mam-text, #0f172a);
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.detail-ghost-btn {
  border-radius: 6px !important;
  border: 1px solid var(--mam-border, #e2e8f0) !important;
  background: var(--mam-surface, #fff) !important;
  color: var(--mam-text-secondary, #64748b) !important;
}

.detail-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: row;
  background: var(--mam-detail-rail-bg, #0f1629);
}

.detail-play {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 14px 12px;
  background: var(--mam-detail-play-bg, #0b1220);
}

.detail-play-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 11px;
  color: var(--mam-text-secondary, #64748b);
}

.detail-play-meta {
  color: var(--mam-text-tertiary, #94a3b8);
  flex-shrink: 0;
}

.detail-play-hint {
  color: var(--mam-text-secondary, #64748b);
}

.detail-play-stage {
  flex: 1;
  min-height: 200px;
  border-radius: 6px;
  overflow: hidden;
  background: #020617;
  border: 1px solid #1e293b;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-play-inner {
  width: 100%;
  height: 100%;
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-play-inner--video {
  background: #000;
}

.detail-play-img {
  display: block;
  max-width: 100%;
  max-height: min(520px, 72vh);
  object-fit: contain;
}

.detail-video {
  width: 100%;
  max-height: min(480px, 68vh);
  background: #000;
}

.detail-audio {
  width: 100%;
  max-width: 560px;
}

.detail-play-empty {
  padding: 24px;
  text-align: center;
  color: var(--mam-text-secondary, #64748b);
  font-size: 13px;
}

.detail-play-empty-sub {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--mam-text-tertiary, #94a3b8);
}

.detail-sprite-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-sprite-label {
  font-size: 10px;
  color: var(--mam-text-tertiary, #94a3b8);
}

.detail-sprite-strip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px;
  border-radius: 6px;
  background: #020617;
  border: 1px solid #334155;
  min-height: 48px;
}

.detail-sprite-thumb {
  height: 34px;
  width: auto;
  max-width: 100%;
  border-radius: 3px;
  object-fit: cover;
  border: 1px solid #64748b;
}

.detail-rail {
  width: 64px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 6px 24px;
  background: var(--mam-detail-rail-bg, #0f1629);
  border-left: 1px solid rgba(148, 163, 184, 0.12);
}

.detail-rail-btn {
  width: 54px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 10px;
  padding: 10px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  color: #94a3b8;
  transition: background 0.15s ease, color 0.15s ease, box-shadow 0.15s ease;
}

.detail-rail-btn:hover {
  color: #e2e8f0;
  background: rgba(255, 255, 255, 0.04);
}

.detail-rail-btn--active {
  color: #e0f2fe;
  background: var(--mam-detail-rail-active, rgba(30, 58, 138, 0.35));
  box-shadow: inset 0 0 0 1px var(--mam-detail-rail-border, #3b82f6);
}

.detail-rail-ico {
  font-size: 14px;
}

.detail-rail-lab {
  font-size: 9px;
  font-weight: 600;
  line-height: 1.1;
}

.detail-rail-sub {
  font-size: 8px;
  opacity: 0.85;
  line-height: 1.1;
}

.detail-side {
  width: min(520px, 38vw);
  flex-shrink: 0;
  background: var(--mam-surface, #fff);
  border-left: 1px solid var(--mam-border, #e2e8f0);
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.detail-side-scroll {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 18px 22px 28px;
}

.detail-side-section {
  margin-bottom: 20px;
}

.detail-side-tight {
  margin-top: 8px;
}

.detail-side-h {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--mam-text, #0f172a);
}

.detail-side-h3 {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--mam-text-secondary, #64748b);
}

.detail-desc {
  margin: 0;
}

.detail-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  word-break: break-all;
}

.detail-meta-card {
  margin-bottom: 12px;
  padding: 14px;
  background: var(--mam-muted-bg, #f8fafc);
  border-radius: var(--mam-radius-sm, 8px);
  border: 1px solid var(--mam-border, #e2e8f0);
}

.detail-meta-h {
  font-size: 13px;
  margin-bottom: 8px;
  color: var(--mam-text-secondary, #64748b);
  font-weight: 600;
}

.detail-ai-head {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.detail-ai-title {
  margin-bottom: 0;
}

.detail-ai-hint {
  font-size: 10px;
  color: var(--mam-text-tertiary, #94a3b8);
}

.detail-ai-alert {
  margin-bottom: 12px;
}

.detail-dl-row {
  margin-bottom: 14px;
}

.detail-log-alert {
  margin-bottom: 12px;
}

.detail-dl-menu {
  min-width: 200px;
}
</style>
