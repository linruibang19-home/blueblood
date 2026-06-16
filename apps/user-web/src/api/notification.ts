// API - 通知模块
import type { Notification } from '@/types/notification'
import { mockNotifications } from '@/mock/notifications'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

export async function getNotifications(): Promise<Notification[]> {
  await delay(300)
  return mockNotifications
}

export async function getUnreadCount(): Promise<number> {
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