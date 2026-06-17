import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

const TOKEN_KEY = 'admin_token'

// axios 实例：baseURL=/api，后端 context-path 已为 /api，dev proxy 透传
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器：从 localStorage 读取 admin_token，注入 Authorization Bearer 头
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一解包 { code, message, data }
// code === 0 或 200 视为成功，直接返回 data；401 清 token 并跳登录
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, data, message } = response.data ?? {}
    if (code === 0 || code === 200) {
      return data
    }
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      // 避免在登录页本身触发循环跳转
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(new Error('未登录或登录已过期'))
    }
    const message =
      error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

export default request
export { TOKEN_KEY }
