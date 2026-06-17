import { BASE_URL, TOKEN_KEY } from '@/config'

/** 后端统一响应体 { code, message, data } */
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}

export interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  /** 是否需要鉴权（默认 true） */
  auth?: boolean
}

/**
 * 封装 uni.request
 * - baseURL：dev = http://localhost:8090/api
 * - 自动注入 Authorization: Bearer <token>
 * - 解包 { code, message, data }：code 0/200 视为成功并返回 data
 * - 401：清 token + 跳登录
 */
function request<T = any>(options: RequestOptions): Promise<T> {
  const { url, method = 'GET', data, header = {}, auth = true } = options

  if (auth) {
    const token = uni.getStorageSync(TOKEN_KEY)
    if (token) {
      header.Authorization = `Bearer ${token}`
    }
  }

  return new Promise<T>((resolve, reject) => {
    uni.request({
      url: /^https?:\/\//.test(url) ? url : BASE_URL + url,
      method,
      data,
      header: { 'Content-Type': 'application/json', ...header },
      timeout: 15000,
      success: (res) => {
        const status = res.statusCode
        const body = res.data as ApiResult<T> | undefined

        // 401 鉴权失败：清 token 并跳登录
        if (status === 401) {
          uni.removeStorageSync(TOKEN_KEY)
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/mine/index' })
          }, 800)
          return reject(new Error('未授权'))
        }

        if (status < 200 || status >= 300) {
          return reject(new Error(`HTTP ${status}`))
        }

        // 业务体解包
        if (body && typeof body.code === 'number') {
          if (body.code === 0 || body.code === 200) {
            return resolve(body.data)
          }
          return reject(new Error(body.message || `业务错误 ${body.code}`))
        }

        // 兜底：非标准响应直接返回
        resolve(res.data as T)
      },
      fail: (err) => {
        reject(new Error(err.errMsg || '网络错误'))
      },
    })
  })
}

export const http = {
  get: <T = any>(url: string, options?: Partial<RequestOptions>) =>
    request<T>({ url, method: 'GET', ...options }),
  post: <T = any>(url: string, data?: any, options?: Partial<RequestOptions>) =>
    request<T>({ url, method: 'POST', data, ...options }),
  put: <T = any>(url: string, data?: any, options?: Partial<RequestOptions>) =>
    request<T>({ url, method: 'PUT', data, ...options }),
  delete: <T = any>(url: string, options?: Partial<RequestOptions>) =>
    request<T>({ url, method: 'DELETE', ...options }),
}

export default http
