import request from './request'

/** 登录请求体 */
export interface LoginParams {
  username: string
  password: string
}

/** 登录响应（后端 LoginResponse） */
export interface LoginResult {
  token: string
  tokenType?: string
  expiresAt?: string
  userId: number
  username: string
  role: string
}

/** 当前用户（后端 LoginUser） */
export interface CurrentUser {
  userId: number
  username: string
  role: string
}

/** 登录：POST /api/auth/login */
export function login(params: LoginParams): Promise<LoginResult> {
  return request.post<unknown, LoginResult>('/auth/login', params)
}

/** 当前登录用户：GET /api/auth/me */
export function me(): Promise<CurrentUser> {
  return request.get<unknown, CurrentUser>('/auth/me')
}

/** 登出：POST /api/auth/logout */
export function logout(): Promise<void> {
  return request.post<unknown, void>('/auth/logout')
}
