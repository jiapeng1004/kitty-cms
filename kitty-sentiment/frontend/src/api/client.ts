import axios, { AxiosError } from 'axios'

const client = axios.create({
  baseURL: '',
  timeout: 120000,
  headers: { 'Content-Type': 'application/json' },
})

export function errMessage(error: unknown, fallback = '请求失败'): string {
  if (axios.isAxiosError(error)) {
    const ax = error as AxiosError<{ error?: string; message?: string }>
    const d = ax.response?.data
    if (typeof d === 'string' && d) return d
    if (d && typeof d === 'object') {
      if (typeof d.error === 'string') return d.error
      if (typeof d.message === 'string') return d.message
    }
  }
  if (error instanceof Error) return error.message
  return fallback
}

export default client
