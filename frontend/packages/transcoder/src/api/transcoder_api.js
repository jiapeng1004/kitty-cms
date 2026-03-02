import api from '../utils/api'

const PREFIX = '/api/transcode'

/**
 * 获取单个任务详情
 * @param {string} taskId
 * @returns {Promise<object>}
 */
export function getTask(taskId) {
  return api.get(`${PREFIX}/task/${taskId}`)
}

/**
 * 分页获取任务列表（按创建时间倒序）
 * @param {{ page?: number, size?: number, taskId?: string }} params - taskId 支持模糊检索
 * @returns {Promise<Array<object>>}
 */
export function listTasks(params = {}) {
  const { page = 1, size = 50, taskId } = params
  const p = { page, size }
  if (taskId != null && String(taskId).trim()) p.taskId = String(taskId).trim()
  return api.get(`${PREFIX}/tasks`, { params: p })
}

/**
 * 创建转码任务
 * @param {object} body - inputType, inputPath, strategyId, priority 等
 * @returns {Promise<string>} 任务 ID
 */
export function createTask(body) {
  return api.post(`${PREFIX}/task`, body)
}

/**
 * 取消任务
 * @param {string} taskId
 * @returns {Promise<void>}
 */
export function cancelTask(taskId) {
  return api.delete(`${PREFIX}/task/${taskId}`)
}

/**
 * 删除任务记录（物理删除）
 * @param {string} taskId
 * @returns {Promise<boolean>}
 */
export function deleteTask(taskId) {
  return api.delete(`${PREFIX}/task/${taskId}/record`)
}

/**
 * 获取任务进度（含总进度与分步进度）
 * @param {string} taskId
 * @returns {Promise<{ taskId, progress, status, currentStep, totalSteps, stepProgressList }>}
 */
export function getProgress(taskId) {
  return api.get(`${PREFIX}/progress/${taskId}`)
}

/**
 * 获取任务可预览文件列表（路径 + 预览链接），不依赖 output 前缀配置
 * @param {string} taskId
 * @returns {Promise<{ taskId, files: Array<{ path, previewUrl }> }>}
 */
export function getPreviewInfo(taskId) {
  const base = typeof window !== 'undefined' ? window.location.origin : ''
  return api.get(`${PREFIX}/preview/info`, { params: { taskId, baseUrl: base } })
}

/**
 * 构建预览 URL（用于 img/video src 或新窗口打开）
 * @param {string} taskId
 * @param {string} [path] 相对路径，空则主输出
 * @returns {string}
 */
export function buildPreviewUrl(taskId, path) {
  const base = typeof window !== 'undefined' ? window.location.origin : ''
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('transcoder_token') : null
  let url = `${base}/api/transcode/preview?taskId=${encodeURIComponent(taskId)}`
  if (path) url += `&path=${encodeURIComponent(path)}`
  if (token) url += `&token=${encodeURIComponent(token)}`
  return url
}

/**
 * 订阅全任务进度 SSE 流，任意任务进度更新时回调
 * @param {function(ProgressVO): void} onProgress
 * @returns {function(): void} 取消订阅
 */
export function subscribeProgressStream(onProgress) {
  const token = localStorage.getItem('transcoder_token')
  const headers = { Accept: 'text/event-stream' }
  if (token) headers.Authorization = `Bearer ${token}`
  const url = `${PREFIX}/progress/stream`
  const controller = new AbortController()
  fetch(url, { headers, signal: controller.signal })
    .then((res) => {
      if (!res.ok) return
      const reader = res.body.getReader()
      const decoder = new TextDecoder()
      let buf = ''
      const read = () => {
        reader.read().then(({ done, value }) => {
          if (done) return
          buf += decoder.decode(value, { stream: true })
          const lines = buf.split('\n')
          buf = lines.pop() || ''
          let data = null
          for (const line of lines) {
            if (line.startsWith('data:')) {
              const payload = line.slice(5).trim()
              if (payload) {
                if (data) onProgress(data)
                try {
                  data = JSON.parse(payload)
                } catch (_) {
                  data = null
                }
              }
            } else if (line.startsWith('event:') || line === '' || line === '\r') {
              if (data) {
                onProgress(data)
                data = null
              }
            }
          }
          read()
        }).catch(() => {})
      }
      read()
    })
    .catch(() => {})
  return () => controller.abort()
}
