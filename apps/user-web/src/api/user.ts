// API - 用户模块
import type { User, UserProfile } from '@/types/user'
import { mockCurrentUser, mockEliteUsers, mockConnectionStatus } from '@/mock/users'
import request, { USE_API, ensureReady } from './request'

// 模拟延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 后端 user VO -> 前端 User 类型映射（字段名差异兜底）
// 后端字段：id, username, nickname, avatar, phone, email, gender, level,
//           levelName, points, creditScore, completedTasks, verified, roles,
//           school, major, bio, github, connections, followers, following,
//           skills, lastLoginAt
function mapUser(raw: any): User {
  return {
    id: String(raw?.id ?? raw?.username ?? ''),
    // 后端用 nickname，前端用 name；缺失时回退到 username
    name: raw?.nickname || raw?.name || raw?.username || '',
    avatar: raw?.avatar || '',
    school: raw?.school || '',
    major: raw?.major || '',
    level: Number(raw?.level ?? 0),
    levelName: raw?.levelName || '',
    points: Number(raw?.points ?? 0),
    creditScore: Number(raw?.creditScore ?? 0),
    completedTasks: Number(raw?.completedTasks ?? 0),
    skills: Array.isArray(raw?.skills)
      ? raw.skills.map((s: any) => (s && typeof s === 'object' ? s.name : s)).filter((s: any) => s)
      : [],
    verified: !!raw?.verified,
    bio: raw?.bio || '',
    github: raw?.github || '',
    // 后端无 joinedAt，用 lastLoginAt 兜底，再缺失给空串
    joinedAt: raw?.joinedAt || raw?.lastLoginAt || '',
    // 用户类型：企业用户/个人用户
    userType: raw?.userType || 'personal',
  }
}

export async function getCurrentUser(): Promise<User> {
  if (USE_API) {
    await ensureReady()
    const data = await request.get<any, any>('/user/me')
    return mapUser(data)
  }
  await delay(300)
  return mockCurrentUser
}

export async function getUserProfile(userId: string): Promise<UserProfile | null> {
  if (USE_API) {
    await ensureReady()
    try {
      const data = await request.get<any, any>(`/user/${userId}`)
      const u = mapUser(data)
      return {
        ...u,
        connections: Number(data?.connections ?? 0),
        followers: Number(data?.followers ?? 0),
        following: Number(data?.following ?? 0),
        badges: [],
      }
    } catch {
      return null
    }
  }
  await delay(300)
  return mockEliteUsers.find(u => u.id === userId) || null
}

export async function getEliteUsers(): Promise<UserProfile[]> {
  if (USE_API) {
    await ensureReady()
    // 后端若有 GET /user 列表端点则取其 data（兼容数组或分页 .list）
    try {
      const data = await request.get<any, any>('/user')
      const arr: any[] = Array.isArray(data) ? data : Array.isArray(data?.list) ? data.list : []
      return arr.map(raw => ({
        ...mapUser(raw),
        connections: Number(raw?.connections ?? 0),
        followers: Number(raw?.followers ?? 0),
        following: Number(raw?.following ?? 0),
        badges: [],
      }))
    } catch {
      // 后端无列表端点则回退 Mock，避免页面空白
      return mockEliteUsers
    }
  }
  await delay(300)
  return mockEliteUsers
}

export async function getConnectionStatus(userId: string): Promise<boolean> {
  if (USE_API) {
    // 该关系类接口阶段二再做，暂回退 Mock
    return mockConnectionStatus[userId] || false
  }
  await delay(100)
  return mockConnectionStatus[userId] || false
}

export async function updateUserProfile(data: Partial<User>): Promise<User> {
  if (USE_API) {
    await ensureReady()
    const updated = await request.put<any, any>('/user/profile', data)
    return mapUser(updated)
  }
  await delay(500)
  return { ...mockCurrentUser, ...data }
}

export async function addSkillTag(tag: string): Promise<string[]> {
  if (USE_API) {
    // 通过更新 profile 间接实现
    return [tag]
  }
  await delay(300)
  if (!mockCurrentUser.skills.includes(tag)) {
    mockCurrentUser.skills.push(tag)
  }
  return mockCurrentUser.skills
}

export async function removeSkillTag(tag: string): Promise<string[]> {
  if (USE_API) {
    return []
  }
  await delay(300)
  mockCurrentUser.skills = mockCurrentUser.skills.filter(s => s !== tag)
  return mockCurrentUser.skills
}

export async function toggleConnection(userId: string): Promise<boolean> {
  if (USE_API) {
    return true
  }
  await delay(300)
  const current = mockConnectionStatus[userId] || false
  mockConnectionStatus[userId] = !current
  return !current
}
