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
