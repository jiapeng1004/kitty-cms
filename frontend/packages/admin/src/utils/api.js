import axios from 'axios'

const instance = axios.create({
  baseURL: 'http://localhost:9701', // 后端服务地址
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    // 可以在这里添加token等信息
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    // 可以在这里统一处理响应数据
    return response.data
  },
  (error) => {
    // 统一处理错误
    if (error.response?.status === 401) {
      // 处理未授权错误
    }
    return Promise.reject(error)
  }
)

export default instance