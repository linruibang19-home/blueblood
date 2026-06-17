// API - 通知模块
import type { Notification } from '@/types/notification'
import { mockNotifications } from '@/mock/notifications'
import request, { USE_API, ensureReady } from './request'
import type { PageResponse } from './request'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 后端通知 VO -> 前端 Notification
function mapNotification(raw: any): Notification {
  return {
    id: String(raw?.id ?? ''),
    type: raw?.type || 'system',
    title: raw?.title || '',
    content: raw?.content || '',
    read: !!raw?.read,
    link: raw?.link,
    createdAt: raw?.createdAt || '',
  }
}

export async function getNotifications(): Promise<Notification[]> {
  if (USE_API) {
    await ensureReady()
    const data = await request.get<any, any>('/notification')
    const list = Array.isArray(data) ? data : (data as PageResponse)?.list ?? []
    return list.map(mapNotification)
  }
  await delay(300)
  return mockNotifications
}

export async function getUnreadCount(): Promise<number> {
  if (USE_API) {
    await ensureReady()
    const data = await request.get<any, any>('/notification/unread-count')
    // 后端可能直接返回数字，也可能 { count: n }
    return Number(typeof data === 'number' ? data : (data?.count ?? 0))
  }
  await delay(100)
  return mockNotifications.filter(n => !n.read).length
}

export async function markAsRead(notificationId: string): Promise<boolean> {
  await delay(200)
  const notification = mockNotifications.find(n => n.id === notificationId)
  if (notification) notification.read = true
  return true
}

export async function markAllAsRead(): Promise<boolean> {
  await delay(300)
  mockNotifications.forEach(n => n.read = true)
  return true
}