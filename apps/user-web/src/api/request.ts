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
// 开发联调：API 模式下若无 token，自动以演示账号 lin/123456 登录拿 token
// 仅用于前端联调，避免阶段一无登录页时各页面无法读取真实接口。
// 生产环境请勿依赖，正式登录页接入后可移除。
// -----------------------------------------------------------------------------
let loginPromise: Promise<string> | null = null

export async function ensureDevToken(): Promise<string> {
  const existing = localStorage.getItem('token')
  if (existing) return existing

  if (!loginPromise) {
    loginPromise = (async () => {
      // 直接用原始 axios 走 baseURL，绕过拦截器解包（登录接口需读 token 字段）
      const resp = await axios.post(
        `${request.defaults.baseURL}/auth/login`,
        { username: 'lin', password: '123456' },
        { headers: { 'Content-Type': 'application/json' } }
      )
      const body = resp.data
      const token: string | undefined =
        body?.data?.token || body?.data?.accessToken || body?.token
      if (!token) {
        throw new Error(
          `[dev-auto-login] 登录响应未包含 token 字段，实际返回：${JSON.stringify(body).slice(0, 300)}`
        )
      }
      localStorage.setItem('token', token)
      return token
    })().finally(() => {
      loginPromise = null
    })
  }
  return loginPromise
}

/**
 * 在 API 模式下发起请求前调用：若无 token 则先静默登录，再放行。
 * Mock 模式下直接 resolve，不影响原有 Mock 流程。
 */
export async function ensureReady(): Promise<void> {
  if (!USE_API) return
  if (localStorage.getItem('token')) return
  try {
    await ensureDevToken()
  } catch (e) {
    // 自动登录失败不应阻塞页面渲染，仅打印告警；后续真实接口会返回 401 由业务处理
    console.warn('[dev-auto-login] 演示账号登录失败：', e)
  }
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
