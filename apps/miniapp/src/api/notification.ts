import http from '@/utils/request'

export interface AppNotification {
  id: string
  type: string
  title: string
  content: string
  read: boolean
  link?: string
  createdAt: string
}

interface PageResp {
  list?: any[]
}

function mapNotification(raw: any): AppNotification {
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

/** GET /notification */
export async function getNotifications(): Promise<AppNotification[]> {
  const data = await http.get<any>('/notification').catch(() => ({} as any))
  const list = Array.isArray(data) ? data : (data as PageResp)?.list ?? []
  return list.map(mapNotification)
}

/** GET /notification/unread-count */
export async function getUnreadCount(): Promise<number> {
  try {
    const data = await http.get<any>('/notification/unread-count')
    return Number(typeof data === 'number' ? data : data?.count ?? 0)
  } catch {
    return 0
  }
}
