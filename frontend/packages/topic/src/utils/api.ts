import axios from 'axios'
import { getProvider, getToken } from './auth'

const baseURL = import.meta.env.VITE_API_BASEURL || ''

const instance = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

instance.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  const provider = getProvider()
  if (provider) {
    config.headers['X-OAuth-Provider'] = provider
  }
  return config
})

instance.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response?.status as number | undefined
    const msg: string | undefined = error.response?.data?.message
    if (status === 401 || msg === 'token.expired' || msg === 'token.invalid') {
      localStorage.removeItem('kitty_topic_token')
      localStorage.removeItem('kitty_topic_oauth_provider')
    }
    return Promise.reject(error)
  }
)

export function getResponseMessage(error: unknown): string {
  const err = error as { response?: { data?: { message?: string } }; message?: string }
  const bodyMessage = err.response?.data?.message
  if (bodyMessage != null && String(bodyMessage).trim() !== '') {
    return String(bodyMessage).trim()
  }
  return err.message != null ? String(err.message) : '请求失败'
}

export default instance

