// API - 认证模块
import request from './request'

export interface LoginResult {
  token: string
  tokenType?: string
  userId: number
  username: string
  role: string
}

/** 账号密码登录 → 返回 token 等 */
export async function login(username: string, password: string): Promise<LoginResult> {
  return request.post<any, LoginResult>('/auth/login', { username, password })
}

/** 退出登录（无状态：后端丢弃即可） */
export async function logout(): Promise<void> {
  try {
    await request.post('/auth/logout')
  } catch {
    // 忽略：本地清 token 即可
  }
}

/** 注册（个人/企业账号创建） */
export async function register(payload: {
  registerType?: 'username' | 'email' | 'phone'
  username?: string
  password: string
  userType?: string
  phone?: string
  email?: string
  code?: string
  [key: string]: any
}): Promise<any> {
  return request.post('/auth/register', payload)
}

/** 发送验证码（邮箱/手机） → dev 桩返回 { code } */
export async function sendCode(
  target: string,
  type: 'email' | 'phone',
): Promise<{ code: string }> {
  return request.post<any, { code: string }>('/auth/send-code', null, {
    params: { target, type },
  })
}

/** 文件上传（multipart） → 返回 { url } */
export async function uploadFile(file: File, bizType = 'common'): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)
  const data = await request.post<any, any>('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  // 兼容直接返回 url 或包一层
  return typeof data === 'string' ? data : (data?.url || data?.data?.url || '')
}

