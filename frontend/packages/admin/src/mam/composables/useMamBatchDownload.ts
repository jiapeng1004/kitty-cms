import { h } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  getResourceDownloadUrl,
  reportResourceDownloadBatch,
  type MaterialDownloadUrlVO,
  type MaterialResourceVO
} from '@/mam/api/mam_resource_api'

const FOLDER_TYPE = 7

function isCoverSprite(dt: string): boolean {
  const u = dt.trim().toUpperCase()
  return u === 'COVER' || u === 'SPRITE'
}

function isSourceRequest(dt: string): boolean {
  return dt.trim().toUpperCase() === 'SOURCE'
}

export type PreflightSuccess = {
  resource: MaterialResourceVO
  result: MaterialDownloadUrlVO
  /** 选了转码级但服务端降级为源码 */
  isTierFallback: boolean
}

/**
 * 并行解析批量下载；失败项、源码降级项分类，供确认后统一打开与上报。
 */
export async function preflightMamBatchDownload(
  items: MaterialResourceVO[],
  destinationType: string
): Promise<{ toDownload: PreflightSuccess[]; hardFail: { resource: MaterialResourceVO; error: string }[] }> {
  const dt = destinationType.trim()
  if (!dt) {
    return { toDownload: [], hardFail: [] }
  }
  const rows = items.filter((r) => r.type !== FOLDER_TYPE)
  if (!rows.length) {
    return { toDownload: [], hardFail: [] }
  }
  const results = await Promise.all(
    rows.map(async (r) => {
      try {
        const res = await getResourceDownloadUrl({ resourceId: r.id, destinationType: dt })
        const normActual = (res.actualDestinationType || '').trim().toUpperCase()
        const normReq = (res.destinationType || dt).trim().toUpperCase()
        const isTierFallback =
          !isSourceRequest(dt) &&
          !isCoverSprite(dt) &&
          normActual === 'SOURCE' &&
          normReq !== 'SOURCE'
        return {
          kind: 'ok' as const,
          resource: r,
          result: res,
          isTierFallback
        }
      } catch (e: unknown) {
        const err = e as { response?: { data?: { message?: string } }; message?: string }
        return {
          kind: 'fail' as const,
          resource: r,
          error: err?.response?.data?.message || err?.message || '获取下载地址失败'
        }
      }
    })
  )
  const toDownload: PreflightSuccess[] = []
  const hardFail: { resource: MaterialResourceVO; error: string }[] = []
  for (const x of results) {
    if (x.kind === 'fail') {
      hardFail.push({ resource: x.resource, error: x.error })
    } else {
      toDownload.push({
        resource: x.resource,
        result: x.result,
        isTierFallback: x.isTierFallback
      })
    }
  }
  return { toDownload, hardFail }
}

/**
 * 打开新窗口（间隔降低弹窗拦截），再批量上报。
 */
export async function executeMamBatchDownload(toDownload: PreflightSuccess[]): Promise<void> {
  if (!toDownload.length) {
    return
  }
  const items: {
    resourceId: string
    destinationType: string
    resourceTitle?: string
    actualDestinationType?: string
  }[] = []
  for (const p of toDownload) {
    if (!p.result?.url) {
      continue
    }
    window.open(p.result.url, '_blank', 'noopener,noreferrer')
    // eslint-disable-next-line no-await-in-loop
    await new Promise((r) => setTimeout(r, 160))
    items.push({
      resourceId: p.resource.id,
      destinationType: p.result.destinationType,
      resourceTitle: p.resource.title,
      actualDestinationType: p.result.actualDestinationType
    })
  }
  if (items.length) {
    await reportResourceDownloadBatch({ items })
  }
}

/**
 * 第二次确认：说明跳过项与码率降级项，确认后打开窗口并上报。返回是否已成功执行打开与上报。
 */
export function showMamBatchDownloadConfirm(
  toDownload: PreflightSuccess[],
  hardFail: { resource: MaterialResourceVO; error: string }[]
): Promise<boolean> {
  return new Promise((resolve) => {
    if (!toDownload.length) {
      resolve(false)
      return
    }
    const fallback = toDownload.filter((p) => p.isTierFallback)
    const hardTitles = hardFail
      .slice(0, 8)
      .map((h) => h.resource.title)
      .join('、')
    const moreHard = hardFail.length > 8 ? ` 等共 ${hardFail.length} 项` : ''
    const fbTitles = fallback
      .slice(0, 8)
      .map((f) => f.resource.title)
      .join('、')
    const moreFb = fallback.length > 8 ? ` 等共 ${fallback.length} 项` : ''
    const pStyle = { marginBottom: '8px', lineHeight: '1.55' as const }
    const lines: ReturnType<typeof h>[] = []
    if (hardFail.length) {
      lines.push(
        h('p', { style: pStyle }, [
          h('strong', null, '无法以下载分级取址（将跳过）：'),
          ` ${hardFail.length} 项${hardTitles ? ` — ${hardTitles}` : ''}${moreHard}`
        ])
      )
    }
    if (fallback.length) {
      lines.push(
        h('p', { style: pStyle }, [
          h('strong', null, '无该码率产物，将改下载源码：'),
          ` ${fallback.length} 项${fbTitles ? ` — ${fbTitles}` : ''}${moreFb}`
        ])
      )
    }
    lines.push(
      h(
        'p',
        { style: pStyle },
        `将打开 ${toDownload.length} 个新标签。若浏览器拦截弹窗，请允许本站弹出窗口。`
      )
    )
    Modal.confirm({
      title: '确认批量下载',
      width: 560,
      content: h('div', { style: { maxHeight: '360px', overflowY: 'auto' } }, lines),
      okText: '继续',
      cancelText: '取消',
      onOk: () =>
        executeMamBatchDownload(toDownload)
          .then(() => {
            message.success('已按顺序打开链接并完成批量上报')
            resolve(true)
          })
          .catch(() => {
            message.error('已尝试打开部分链接，若上报失败可稍后在网络面板检查')
            resolve(false)
          }),
      onCancel: () => resolve(false)
    })
  })
}

/**
 * 一站式：无 UI 时选用（预解析在调用方有 loading 时也可拆成 preflight + showMamBatchDownloadConfirm）。
 */
export async function runMamBatchDownloadWithConfirm(
  items: MaterialResourceVO[],
  destinationType: string
): Promise<boolean> {
  if (!items.length) {
    message.warning('请先选择资源')
    return false
  }
  const { toDownload, hardFail } = await preflightMamBatchDownload(items, destinationType)
  if (!toDownload.length) {
    if (hardFail.length) {
      message.warning('所选资源均无法以该分级下载，请改选分级或检查资源。')
    } else {
      message.warning('没有可下载的资源（已排除文件夹）')
    }
    return false
  }
  return showMamBatchDownloadConfirm(toDownload, hardFail)
}

export function resolveDestinationType(preset: string, custom: string): string {
  if (preset === 'CUSTOM') {
    return (custom || '').trim()
  }
  return (preset || '').trim()
}

export const MAM_BATCH_TIER_PRESETS: { value: string; label: string }[] = [
  { value: 'SOURCE', label: '源码' },
  { value: '720P', label: '720P' },
  { value: '1080P', label: '1080P' },
  { value: '480P', label: '480P' },
  { value: 'COVER', label: '封面' },
  { value: 'SPRITE', label: '雪碧图' },
  { value: 'CUSTOM', label: '自定义…' }
]
