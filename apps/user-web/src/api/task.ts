// API - 任务模块
import type { Task, TaskOrder, TaskCategory } from '@/types/task'
import { mockTasks, mockTaskCategories, mockMyTaskOrders } from '@/mock/tasks'
import request, { USE_API, ensureReady } from './request'
import type { PageResponse } from './request'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 后端 Task VO -> 前端 Task（缺失字段兜底）
// 后端：id,title,category,employerName,reward,levelRequired,totalSlots,
//      slotsLeft,deadline,status,viewCount,skills,description,accepted
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

// 后端 TaskOrder VO -> 前端 TaskOrder
function mapTaskOrder(raw: any): TaskOrder {
  return {
    id: String(raw?.id ?? ''),
    taskId: String(raw?.taskId ?? ''),
    taskTitle: raw?.taskTitle || '',
    userId: String(raw?.userId ?? ''),
    userName: raw?.userName || '',
    userAvatar: raw?.userAvatar || '',
    status: raw?.status || 'in_progress',
    progress: Number(raw?.progress ?? 0),
    milestones: Array.isArray(raw?.milestones) ? raw.milestones : [],
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
        count: Number(c?.count ?? 0),
      }))
    } catch {
      return mockTaskCategories
    }
  }
  await delay(200)
  return mockTaskCategories
}

export async function getTaskList(params: { category?: string; keyword?: string; page?: number; pageSize?: number }): Promise<Task[]> {
  if (USE_API) {
    await ensureReady()
    const data = await request.get<any, any>('/task', { params })
    // 后端分页返回 { list, total, page, pageSize, totalPages }
    const list = Array.isArray(data) ? data : (data as PageResponse)?.list ?? []
    return list.map(mapTask)
  }
  await delay(300)
  let tasks = [...mockTasks]
  if (params.category && params.category !== '全部') {
    tasks = tasks.filter(t => t.category === params.category)
  }
  if (params.keyword) {
    const kw = params.keyword.toLowerCase()
    tasks = tasks.filter(t =>
      t.title.toLowerCase().includes(kw) ||
      t.description.toLowerCase().includes(kw)
    )
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

export async function acceptTask(taskId: string): Promise<boolean> {
  if (USE_API) {
    await ensureReady()
    await request.post<any, any>(`/task/${taskId}/accept`)
    return true
  }
  await delay(500)
  const task = mockTasks.find(t => t.id === taskId)
  if (task && task.slotsLeft > 0) {
    task.slotsLeft -= 1
    return true
  }
  return false
}

export async function submitMilestone(orderId: string, milestoneId: string, data: any): Promise<boolean> {
  if (USE_API) {
    await ensureReady()
    // 后端按里程碑 id 提交：POST /task/milestones/{milestoneId}/submit
    await request.post<any, any>(`/task/milestones/${milestoneId}/submit`, data)
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
        description: data.description,
        attachments: data.attachments || [],
        submittedAt: new Date().toLocaleString(),
      }
    }
  }
  return true
}
