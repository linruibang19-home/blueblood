import http from '@/utils/request'

/** 当前登录用户 VO（后端 camelCase） */
export interface User {
  id: string
  name: string
  avatar: string
  school: string
  major: string
  level: number
  levelName: string
  points: number
  creditScore: number
  completedTasks: number
  skills: string[]
  verified: boolean
  bio: string
  github: string
  joinedAt: string
}

function mapUser(raw: any): User {
  return {
    id: String(raw?.id ?? raw?.username ?? ''),
    name: raw?.nickname || raw?.name || raw?.username || '',
    avatar: raw?.avatar || '',
    school: raw?.school || '',
    major: raw?.major || '',
    level: Number(raw?.level ?? 0),
    levelName: raw?.levelName || '',
    points: Number(raw?.points ?? 0),
    creditScore: Number(raw?.creditScore ?? 0),
    completedTasks: Number(raw?.completedTasks ?? 0),
    skills: Array.isArray(raw?.skills) ? raw.skills : [],
    verified: !!raw?.verified,
    bio: raw?.bio || '',
    github: raw?.github || '',
    joinedAt: raw?.joinedAt || raw?.lastLoginAt || '',
  }
}

/** GET /user/me */
export function getCurrentUser(): Promise<User> {
  return http.get<any>('/user/me').then(mapUser)
}

/** GET /user/{id} */
export function getUserProfile(userId: string): Promise<User | null> {
  return http
    .get<any>(`/user/${userId}`)
    .then(mapUser)
    .catch(() => null)
}

/** GET /user （精英列表） */
export async function getEliteUsers(): Promise<User[]> {
  try {
    const data = await http.get<any>('/user')
    const arr: any[] = Array.isArray(data) ? data : Array.isArray(data?.list) ? data.list : []
    return arr.map(mapUser)
  } catch {
    return []
  }
}

export interface UpdateProfilePayload {
  avatar?: string
  nickname?: string
  name?: string
  bio?: string
  github?: string
  [k: string]: any
}

/** PUT /user/profile 更新当前用户资料（部分字段） */
export function updateProfile(payload: UpdateProfilePayload): Promise<any> {
  return http.put<any>('/user/profile', payload)
}
