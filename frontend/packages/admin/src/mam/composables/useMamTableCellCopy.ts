import { message } from 'ant-design-vue'

export interface MamTableCellCopyOptions {
  /** 无可复制内容时的提示 */
  emptyHint?: string
  /** 成功提示时长（秒） */
  successDuration?: number
}

/**
 * MAM 管理列表「单击单元格复制」：无独立复制按钮，复制完整字段值（不受省略号影响）。
 * 配套样式见 `styles/mam-admin-table-cell.css` 中的 `.mam-table-cell-copy`。
 *
 * 参考实现：`views/StorageManagePage.vue`
 */
export function useMamTableCellCopy(options?: MamTableCellCopyOptions) {
  const emptyHint = options?.emptyHint ?? '当前无内容可复制'
  const successDuration = options?.successDuration ?? 1.2

  async function copyCell(raw: string | null | undefined) {
    const t = String(raw ?? '').trim()
    if (!t || t === '—') {
      message.info(emptyHint)
      return
    }
    try {
      await navigator.clipboard.writeText(t)
      message.success('已复制到剪贴板', successDuration)
    } catch {
      message.error('复制失败，请检查浏览器剪贴板权限')
    }
  }

  return { copyCell }
}
