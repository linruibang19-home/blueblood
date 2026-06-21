import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'

// 是否走真实后端 API（由 .env.development / .env.api 的 VITE_USE_API 控制）
export const USE_API = import.meta.env.VITE_USE_API === 'true'

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器：自动注入 Authorization 头
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一解包 { code, message, data }，code=0/200 视为成功并直接返回 data
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const body = response.data
    // 兼容非标准 envelope（裸数组/裸数据）：无 code 字段时原样返回
    if (body && typeof body === 'object' && 'code' in body) {
      const { code, data, message } = body
      if (code === 0 || code === 200) {
        return data
      }
      return Promise.reject(new Error(message || '请求失败'))
    }
    return body
  },
  (error) => {
    // 401：token 过期/无效，清除本地 token 触发重新登录，避免死循环
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
    }
    const message = error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

// -----------------------------------------------------------------------------
// 登录态守卫：API 模式下各页面调用 ensureReady() 作为请求前置钩子。
// 登录页已接入，不再做 dev 自动登录（否则退出后会被自动登录回演示账号 lin，
// 导致“退出登录”失效且无法体验新注册账号）。无 token 时直接返回，
// 后续接口返回 401 由响应拦截器清理 token、由业务页面引导登录。
// -----------------------------------------------------------------------------
export async function ensureReady(): Promise<void> {
  // 登录页接入后：无 token 即放行（由各页面对 401 做降级），不再自动登录
  return
}

export default request

// 统一响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  traceId?: string
}

// 分页响应（后端返回 { list, total, page, pageSize, totalPages }）
export interface PageResponse<T = any> {
  list: T[]
  page: number
  pageSize: number
  total: number
  totalPages?: number
}
