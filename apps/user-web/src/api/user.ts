// API - 用户模块
import type { User, UserProfile } from '@/types/user'
import { mockCurrentUser, mockEliteUsers, mockConnectionStatus } from '@/mock/users'

// 模拟延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

export async function getCurrentUser(): Promise<User> {
  await delay(300)
  return mockCurrentUser
}

export async function getUserProfile(userId: string): Promise<UserProfile | null> {
  await delay(300)
  return mockEliteUsers.find(u => u.id === userId) || null
}

export async function getEliteUsers(): Promise<UserProfile[]> {
  await delay(300)
  return mockEliteUsers
}

export async function getConnectionStatus(userId: string): Promise<boolean> {
  await delay(100)
  return mockConnectionStatus[userId] || false
}

export async function updateUserProfile(data: Partial<User>): Promise<User> {
  await delay(500)
  return { ...mockCurrentUser, ...data }
}

export async function addSkillTag(tag: string): Promise<string[]> {
  await delay(300)
  if (!mockCurrentUser.skills.includes(tag)) {
    mockCurrentUser.skills.push(tag)
  }
  return mockCurrentUser.skills
}

export async function removeSkillTag(tag: string): Promise<string[]> {
  await delay(300)
  mockCurrentUser.skills = mockCurrentUser.skills.filter(s => s !== tag)
  return mockCurrentUser.skills
}

export async function toggleConnection(userId: string): Promise<boolean> {
  await delay(300)
  const current = mockConnectionStatus[userId] || false
  mockConnectionStatus[userId] = !current
  return !current
}