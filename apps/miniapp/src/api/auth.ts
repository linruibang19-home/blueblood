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
