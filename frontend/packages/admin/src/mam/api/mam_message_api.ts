import api from '@/utils/api'
import { getToken } from '@/utils/api'

export interface MaterialInternalMessageVO {
  id: string
  senderUserId: string
  receiverUserId: string
  content: string
  messageStatus: string
  createdAt?: string
}

export function listMessages(): Promise<MaterialInternalMessageVO[]> {
  return api.get('/api/material/message/list')
}

export function sendMessage(data: { receiverUserId: string; content: string }): Promise<MaterialInternalMessageVO> {
  return api.post('/api/material/message/send', data)
}

export function markMessageRead(messageId: string): Promise<void> {
  return api.post('/api/material/message/read', null, { params: { messageId } })
}

/**
 * 使用 fetch + Authorization 订阅 SSE（与 axios 同源 baseURL、同 token）。
 * 收到 `event:new-message` 时回调 data 为 messageId 字符串。
 * @returns 取消订阅函数
 */
export function subscribeMaterialMessageSse(
  onNewMessage: (messageId: string) => void,
  onConnected?: () => void,
  onError?: (e: unknown) => void
): () => void {
  const base = (import.meta as ImportMeta & { env: { VITE_API_BASEURL?: string } }).env.VITE_API_BASEURL || ''
  const url = `${base}/api/material/message/sse`
  const token = typeof localStorage !== 'undefined' ? getToken() : null

  let cancelled = false
  const controller = new AbortController()

  const headers: Record<string, string> = {
    Accept: 'text/event-stream'
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  fetch(url, { headers, signal: controller.signal })
    .then((res) => {
      if (!res.ok || !res.body) {
        onError?.(new Error(`SSE HTTP ${res.status}`))
        return
      }
      const reader = res.body.getReader()
      const decoder = new TextDecoder()
      let buf = ''
      let currentEvent = ''
      let dataLines: string[] = []

      const flush = () => {
        const data = dataLines.length ? dataLines.join('\n').trim() : ''
        dataLines = []
        const ev = currentEvent.trim() || 'message'
        currentEvent = ''
        if (ev === 'connected') {
          onConnected?.()
        }
        if (ev === 'new-message' && data) {
          onNewMessage(data)
        }
      }

      const read = (): void => {
        if (cancelled) return
        reader
          .read()
          .then(({ done, value }) => {
            if (cancelled) return
            if (done) return
            buf += decoder.decode(value, { stream: true })
            const parts = buf.split(/\r?\n/)
            buf = parts.pop() || ''
            for (let line of parts) {
              line = line.replace(/\r$/, '')
              if (line.startsWith('event:')) {
                currentEvent = line.slice(6).trim()
              } else if (line.startsWith('data:')) {
                dataLines.push(line.slice(5).trimStart())
              } else if (line === '') {
                flush()
              }
            }
            read()
          })
          .catch((e) => {
            if (!cancelled) onError?.(e)
          })
      }
      read()
    })
    .catch((e) => {
      if (!cancelled) onError?.(e)
    })

  return () => {
    cancelled = true
    controller.abort()
  }
}
