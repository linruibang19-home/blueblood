// API - 小组模块
import type { Group } from '@/types/group'
import { mockGroups, mockMyGroups } from '@/mock/groups'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

export async function getGroups(): Promise<Group[]> {
  await delay(300)
  return mockGroups
}

export async function getMyGroups(): Promise<Group[]> {
  await delay(300)
  return mockMyGroups
}

export async function getGroupDetail(groupId: string): Promise<Group | null> {
  await delay(300)
  return mockGroups.find(g => g.id === groupId) || null
}

export async function joinGroup(groupId: string): Promise<boolean> {
  await delay(300)
  const group = mockGroups.find(g => g.id === groupId)
  if (group) {
    group.joined = true
    group.memberCount += 1
  }
  return true
}

export async function leaveGroup(groupId: string): Promise<boolean> {
  await delay(300)
  const group = mockGroups.find(g => g.id === groupId)
  if (group) {
    group.joined = false
    group.memberCount -= 1
  }
  return true
}