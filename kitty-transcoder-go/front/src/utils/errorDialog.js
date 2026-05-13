import { Modal } from 'ant-design-vue'

/**
 * 从 API 异常中解析错误文案（优先后端返回的 error 字段）
 * @param {Error|{ response?: { data?: { error?: string, message?: string } }, message?: string }} e
 * @returns {string}
 */
export function getApiErrorMessage(e) {
  if (e?.response?.data && typeof e.response.data === 'object') {
    const d = e.response.data
    if (d.error != null && String(d.error).trim()) return String(d.error).trim()
    if (d.message != null && String(d.message).trim()) return String(d.message).trim()
  }
  if (e?.message && String(e.message).trim()) return String(e.message).trim()
  if (typeof e === 'string' && e.trim()) return e.trim()
  return '请求失败'
}

/**
 * 用弹窗展示 API 错误（解析 error 字段并显示）
 * @param {Error|object} e - 拦截器或 catch 得到的异常
 */
export function showApiErrorDialog(e) {
  const content = getApiErrorMessage(e)
  Modal.error({
    title: '错误',
    content: content,
    okText: '确定'
  })
}
