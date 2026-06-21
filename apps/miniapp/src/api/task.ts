import http from '@/utils/request'

export interface TaskCategory {
  id: string
  name: string
  icon: string
  count: number
}

export interface Milestone {
  id: string
  title: string
  description?: string
  status?: string
  reward?: number
}

export interface Task {
  id: string
  title: string
  category: string
  description: string
  reward: number
  levelRequired: number
  slotsLeft: number
  totalSlots: number
  deadline: string
  skills: string[]
  status: string
  employerName: string
  createdAt: string
  myOrderId?: string
}

export interface TaskOrder {
  id: string
  taskId: string
  taskTitle: string
  userId: string
  userName: string
  userAvatar: string
  status: string
  progress: number
  milestones: Milestone[]
  createdAt: string
}

interface PageResp {
  list?: any[]
  total?: number
}

function fromPage(data: any): any[] {
  return Array.isArray(data) ? data : (data as PageResp)?.list ?? []
}

function mapTask(raw: any): Task {
  return {
    id: String(raw?.id ?? ''),
    title: raw?.title || '',
    category: raw?.category || '',
    description: raw?.description || '',
    reward: Number(raw?.reward ?? 0),
    levelRequired: Number(raw?.levelRequired ?? 0),
    slotsLeft: Number(raw?.slotsLeft ?? raw?.totalSlots ?? 0),
    totalSlots: Number(raw?.totalSlots ?? 0),
    deadline: raw?.deadline || '',
    skills: Array.isArray(raw?.skills) ? raw.skills : [],
    status: raw?.status || 'recruiting',
    employerName: raw?.employerName || '',
    createdAt: raw?.createdAt || '',
    myOrderId: raw?.myOrderId ?? raw?.accepted ? String(raw?.myOrderId ?? raw?.id ?? '') : undefined,
  }
}

function mapOrder(raw: any): TaskOrder {
  return {
    id: String(raw?.id ?? ''),
    taskId: String(raw?.taskId ?? ''),
    taskTitle: raw?.taskTitle || '',
    userId: String(raw?.userId ?? ''),
    userName: raw?.userName || '',
    userAvatar: raw?.userAvatar || '',
    status: raw?.status || 'in_progress',
    progress: Number(raw?.progress ?? 0),
    milestones: Array.isArray(raw?.milestones)
      ? raw.milestones.map((m: any) => ({
          ...m,
          id: String(m?.id ?? ''),
          status: String(m?.status ?? '').toLowerCase(),
          reward: Number(m?.reward ?? 0),
        }))
      : [],
    createdAt: raw?.createdAt || '',
  }
}

/** GET /task/categories */
export async function getTaskCategories(): Promise<TaskCategory[]> {
  try {
    const data = await http.get<any>('/task/categories')
    const arr = fromPage(data)
    if (arr.length === 0) return [{ id: 'all', name: '全部', icon: 'apps', count: 0 }]
    return arr.map((c) => ({
      id: String(c?.id ?? c?.name ?? ''),
      name: c?.name || '',
      icon: c?.icon || 'apps',
      count: Number(c?.taskCount ?? c?.count ?? 0),
    }))
  } catch {
    return [{ id: 'all', name: '全部', icon: 'apps', count: 0 }]
  }
}

/** GET /task */
export async function getTaskList(params: {
  category?: string
  keyword?: string
  page?: number
  pageSize?: number
}): Promise<Task[]> {
  const data = await http.get<any>('/task', { data: params })
  return fromPage(data).map(mapTask)
}

/** GET /task/{id} */
export async function getTaskDetail(taskId: string): Promise<Task | null> {
  try {
    return mapTask(await http.get<any>(`/task/${taskId}`))
  } catch {
    return null
  }
}

/** GET /task/orders/mine */
export async function getMyTaskOrders(): Promise<TaskOrder[]> {
  const data = await http.get<any>('/task/orders/mine')
  return fromPage(data).map(mapOrder)
}

/** GET /task/orders/{id} */
export async function getTaskOrderDetail(orderId: string): Promise<TaskOrder | null> {
  try {
    return mapOrder(await http.get<any>(`/task/orders/${orderId}`))
  } catch {
    return null
  }
}

/** POST /task/{id}/accept */
export async function acceptTask(taskId: string): Promise<boolean> {
  await http.post<any>(`/task/${taskId}/accept`)
  return true
}

export interface MilestoneSubmitPayload {
  githubUrl?: string
  description?: string
  attachments?: any[]
}

/** POST /task/milestones/{milestoneId}/submit */
export async function submitMilestone(
  _orderId: string,
  milestoneId: string,
  payload: MilestoneSubmitPayload
): Promise<boolean> {
  // 后端要 attachments: string[](URL 列表);小程序上传后是 {url,name}[] 对象,这里展平
  const attachments = Array.isArray(payload.attachments)
    ? payload.attachments.map((a: any) => (typeof a === 'string' ? a : a?.url)).filter(Boolean)
    : []
  await http.post<any>(`/task/milestones/${milestoneId}/submit`, {
    githubUrl: payload.githubUrl || '',
    description: payload.description || '',
    attachments,
  })
  return true
}
