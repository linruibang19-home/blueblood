// API - 任务模块
import type { Task, TaskOrder, TaskCategory, Milestone, MilestoneStatus, PublishTaskPayload, MilestoneReviewItem } from '@/types/task'
import { mockTasks, mockTaskCategories, mockMyTaskOrders } from '@/mock/tasks'
import request, { USE_API, ensureReady } from './request'
import type { PageResponse } from './request'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 解析附件(后端返回 string[] 或 JSON 字符串)
function parseAttachments(raw: any): string[] {
  if (Array.isArray(raw)) return raw.filter(Boolean)
  if (typeof raw === 'string' && raw.trim().startsWith('[')) {
    try { return JSON.parse(raw) } catch { return [] }
  }
  return []
}

// 后端 Task VO -> 前端 Task
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
    status: String(raw?.status || 'recruiting').toLowerCase() as Task['status'],
    employerName: raw?.employerName || '',
    employerId: raw?.employerId != null ? String(raw.employerId) : undefined,
    createdAt: raw?.createdAt || '',
    accepted: !!raw?.accepted,
    myOrderId: raw?.myOrderId != null ? String(raw.myOrderId) : undefined,
  }
}

// 后端 TaskMilestone VO -> 前端 Milestone
function mapMilestone(raw: any): Milestone {
  const sub = raw?.submission
  const review = raw?.review
  return {
    id: String(raw?.id ?? ''),
    orderId: String(raw?.orderId ?? ''),
    title: raw?.title || '',
    description: raw?.description || '',
    dueDate: raw?.dueDate || '',
    status: String(raw?.status || 'not_started').toLowerCase() as MilestoneStatus,
    order: Number(raw?.milestoneOrder ?? raw?.order ?? 0),
    reward: Number(raw?.reward ?? 0),
    milestoneOrder: Number(raw?.milestoneOrder ?? 0),
    submission: sub ? {
      id: String(sub.id ?? ''),
      milestoneId: String(raw.id ?? ''),
      githubUrl: sub.githubUrl || '',
      description: sub.description || '',
      attachments: parseAttachments(sub.attachments),
      submittedAt: sub.submittedAt || '',
    } : undefined,
    review: review ? {
      result: String(review.result || '').toLowerCase(),
      feedback: review.feedback || '',
      reviewedAt: review.reviewedAt || '',
    } : undefined,
  }
}

// 后端 TaskOrder VO -> 前端 TaskOrder
function mapTaskOrder(raw: any): TaskOrder {
  return {
    id: String(raw?.id ?? ''),
    taskId: String(raw?.taskId ?? ''),
    taskTitle: raw?.taskTitle || '',
    userId: String(raw?.userId ?? ''),
    userName: raw?.userName || raw?.employerName || '',
    userAvatar: raw?.userAvatar || '',
    status: String(raw?.status || 'in_progress') as TaskOrder['status'],
    progress: Number(raw?.progress ?? 0),
    milestones: Array.isArray(raw?.milestones) ? raw.milestones.map(mapMilestone) : [],
    createdAt: raw?.createdAt || '',
  }
}

export async function getTaskCategories(): Promise<TaskCategory[]> {
  if (USE_API) {
    await ensureReady()
    try {
      const data = await request.get<any, any>('/task/categories')
      const arr: any[] = Array.isArray(data) ? data : Array.isArray(data?.list) ? data.list : []
      if (arr.length === 0) return mockTaskCategories
      return arr.map(c => ({
        id: String(c?.id ?? c?.name ?? ''),
        name: c?.name || '',
        icon: c?.icon || 'apps-o',
        count: Number(c?.taskCount ?? c?.count ?? 0),
      }))
    } catch {
      return mockTaskCategories
    }
  }
  await delay(200)
  return mockTaskCategories
}

export async function getTaskList(params: { category?: string; keyword?: string; status?: string; page?: number; pageSize?: number }): Promise<Task[]> {
  if (USE_API) {
    await ensureReady()
    const data = await request.get<any, any>('/task', { params })
    const list = Array.isArray(data) ? data : (data as PageResponse)?.list ?? []
    return list.map(mapTask)
  }
  await delay(300)
  let tasks = [...mockTasks]
  if (params.category && params.category !== '全部') tasks = tasks.filter(t => t.category === params.category)
  if (params.keyword) {
    const kw = params.keyword.toLowerCase()
    tasks = tasks.filter(t => t.title.toLowerCase().includes(kw) || t.description.toLowerCase().includes(kw))
  }
  return tasks
}

export async function getTaskDetail(taskId: string): Promise<Task | null> {
  if (USE_API) {
    await ensureReady()
    try {
      const data = await request.get<any, any>(`/task/${taskId}`)
      return mapTask(data)
    } catch {
      return null
    }
  }
  await delay(300)
  return mockTasks.find(t => t.id === taskId) || null
}

export async function getMyTaskOrders(): Promise<TaskOrder[]> {
  if (USE_API) {
    await ensureReady()
    const data = await request.get<any, any>('/task/orders/mine')
    const list = Array.isArray(data) ? data : (data as PageResponse)?.list ?? []
    return list.map(mapTaskOrder)
  }
  await delay(300)
  return mockMyTaskOrders
}

export async function getMyTaskOrderDetail(orderId: string): Promise<TaskOrder | null> {
  if (USE_API) {
    await ensureReady()
    try {
      const data = await request.get<any, any>(`/task/orders/${orderId}`)
      return mapTaskOrder(data)
    } catch {
      return null
    }
  }
  await delay(300)
  return mockMyTaskOrders.find(o => o.id === orderId) || null
}

/** 接单:返回新订单 id(用于接单后跳转执行页);失败返回 null */
export async function acceptTask(taskId: string): Promise<string | null> {
  if (USE_API) {
    await ensureReady()
    const data = await request.post<any, any>(`/task/${taskId}/accept`)
    return data?.orderId != null ? String(data.orderId) : null
  }
  await delay(500)
  const task = mockTasks.find(t => t.id === taskId)
  if (task && task.slotsLeft > 0) {
    task.slotsLeft -= 1
    return `order_${Date.now()}`
  }
  return null
}

export async function submitMilestone(orderId: string, milestoneId: string, data: { githubUrl?: string; description?: string; attachments?: string[] }): Promise<boolean> {
  if (USE_API) {
    await ensureReady()
    await request.post<any, any>(`/task/milestones/${milestoneId}/submit`, {
      githubUrl: data.githubUrl || '',
      description: data.description || '',
      attachments: Array.isArray(data.attachments) ? data.attachments : [],
    })
    return true
  }
  await delay(500)
  const order = mockMyTaskOrders.find(o => o.id === orderId)
  if (order) {
    const milestone = order.milestones.find(m => m.id === milestoneId)
    if (milestone) {
      milestone.status = 'submitted'
      milestone.submission = {
        id: `sub_${Date.now()}`,
        milestoneId,
        githubUrl: data.githubUrl,
        description: data.description || '',
        attachments: data.attachments || [],
        submittedAt: new Date().toLocaleString(),
      }
    }
  }
  return true
}

// ===== 用户端发布 / 雇主工作台 =====

export async function getIdempotentToken(biz = 'publish'): Promise<string> {
  if (!USE_API) { return 'mock-token' }
  await ensureReady()
  const data = await request.get<any, any>('/task/idempotent-token', { params: { biz } })
  return data?.token || ''
}

export async function publishTask(payload: PublishTaskPayload, token?: string): Promise<number> {
  if (!USE_API) { await delay(400); return Math.floor(Date.now() / 1000) }
  await ensureReady()
  const headers: Record<string, string> = token ? { 'X-Idempotent-Token': token } : {}
  const data = await request.post<any, any>('/task', payload, { headers })
  return Number(data)
}

export async function updateTask(taskId: string, payload: PublishTaskPayload): Promise<void> {
  if (!USE_API) { await delay(300); return }
  await ensureReady()
  await request.put(`/task/${taskId}`, payload)
}

export async function closeTask(taskId: string): Promise<void> {
  if (!USE_API) { await delay(200); return }
  await ensureReady()
  await request.put(`/task/${taskId}/close`)
}

export async function getPublishedTasks(params: { status?: string; page?: number; pageSize?: number } = {}): Promise<Task[]> {
  if (!USE_API) { await delay(200); return [] }
  await ensureReady()
  const data = await request.get<any, any>('/task/published', { params })
  const list = Array.isArray(data) ? data : (data as PageResponse)?.list ?? []
  return list.map(mapTask)
}

export async function getMilestonesForReview(params: { status?: string; page?: number; pageSize?: number } = {}): Promise<MilestoneReviewItem[]> {
  if (!USE_API) { await delay(200); return [] }
  await ensureReady()
  const data = await request.get<any, any>('/task/published/milestones', { params })
  const list = Array.isArray(data) ? data : (data as PageResponse)?.list ?? []
  return list.map((r: any) => ({
    milestoneId: String(r?.milestoneId ?? ''),
    orderId: String(r?.orderId ?? ''),
    taskId: String(r?.taskId ?? ''),
    taskTitle: r?.taskTitle || '',
    milestoneTitle: r?.milestoneTitle || '',
    description: r?.description || '',
    milestoneOrder: Number(r?.milestoneOrder ?? 0),
    reward: Number(r?.reward ?? 0),
    status: String(r?.status || '').toLowerCase(),
    workerId: String(r?.workerId ?? ''),
    workerName: r?.workerName || '',
    githubUrl: r?.githubUrl || '',
    submissionDesc: r?.submissionDesc || '',
    attachments: r?.attachments || '[]',
    submittedAt: r?.submittedAt || '',
  }))
}

export async function reviewMilestone(milestoneId: string, payload: { result: 'APPROVED' | 'REJECTED'; feedback?: string }): Promise<void> {
  if (!USE_API) { await delay(300); return }
  await ensureReady()
  await request.post(`/task/milestones/${milestoneId}/review`, payload)
}
