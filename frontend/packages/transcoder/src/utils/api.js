import axios from 'axios'

const instance = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('transcoder_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
}, Promise.reject)

instance.interceptors.response.use(
  (res) => res.data,
  (err) => Promise.reject(err.response?.data?.message || err.message || '请求失败')
)

export default instance
