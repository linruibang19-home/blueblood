// 通知类型定义

export interface Notification {
  id: string
  type: 'milestone' | 'task_review' | 'income' | 'system' | 'group' | 'course'
  title: string
  content: string
  read: boolean
  link?: string
  createdAt: string
}

export interface NotificationSettings {
  taskNotification: boolean
  bbsNotification: boolean
  incomeNotification: boolean
  systemNotification: boolean
}