import http from '@/utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresAt: string
  userId: number
  username: string
  role: string
}

export interface LoginUser {
  userId?: number
  id?: number
  username: string
  nickname?: string
  role?: string
  avatar?: string
}

/** POST /auth/login */
export function login(params: LoginParams): Promise<LoginResult> {
  return http.post<LoginResult>('/auth/login', params)
}

/** GET /auth/me */
export function me(): Promise<LoginUser> {
  return http.get<LoginUser>('/auth/me')
}

/** POST /auth/logout */
export function logout(): Promise<void> {
  return http.post<void>('/auth/logout')
}

export interface WxLoginResult {
  token: string
  userId?: number
  username?: string
  role?: string
  nickname?: string
  avatar?: string
  [k: string]: any
}

/**
 * POST /auth/wx-login
 * body: { code, nickname?, avatar? }
 * 未配置 AppID 时后端返回演示 token，可正常使用。
 */
export function wxLogin(code: string, nickname?: string, avatar?: string): Promise<WxLoginResult> {
  return http.post<WxLoginResult>('/auth/wx-login', { code, nickname, avatar })
}
