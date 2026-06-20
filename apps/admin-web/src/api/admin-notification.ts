import request from './request'
import type { PageResult } from './admin-user'

/** 通知类型 */
export type NotificationType =
  | 'milestone'
  | 'task_review'
  | 'income'
  | 'system'
  | 'group'
  | 'course'

/** 通知列表查询参数 */
export interface NotificationListParams {
  page: number
  pageSize: number
  /** 接收用户ID */
  userId?: number
  /** 通知类型 */
  type?: NotificationType
  /** 关键字(标题/内容) */
  keyword?: string
}

/** AdminNotificationVO */
export interface AdminNotificationVO {
  id: number
  userId: number
  /** 接收人昵称 */
  recipientNickname?: string
  type: NotificationType
  title: string
  content?: string
  link?: string
  createdAt?: string
}

/** 发送通知请求 */
export interface NotificationSendParams {
  userId: number
  type: NotificationType
  title: string
  content?: string
  link?: string
}

/** 通知列表：GET /admin/notification */
export function getNotificationList(
  params: NotificationListParams
): Promise<PageResult<AdminNotificationVO>> {
  return request.get<unknown, PageResult<AdminNotificationVO>>(
    '/admin/notification',
    { params }
  )
}

/** 发送通知：POST /admin/notification */
export function sendNotification(
  data: NotificationSendParams
): Promise<void> {
  return request.post<unknown, void>('/admin/notification', data)
}

/** 删除通知(软删)：DELETE /admin/notification/{id} */
export function deleteNotification(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/notification/${id}`)
}
