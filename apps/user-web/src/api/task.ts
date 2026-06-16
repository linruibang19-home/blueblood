// API - 任务模块
import type { Task, TaskOrder, TaskCategory } from '@/types/task'
import { mockTasks, mockTaskCategories, mockMyTaskOrders } from '@/mock/tasks'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

export async function getTaskCategories(): Promise<TaskCategory[]> {
  await delay(200)
  return mockTaskCategories
}

export async function getTaskList(params: { category?: string; keyword?: string }): Promise<Task[]> {
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
  await delay(300)
  return mockTasks.find(t => t.id === taskId) || null
}

export async function getMyTaskOrders(): Promise<TaskOrder[]> {
  await delay(300)
  return mockMyTaskOrders
}

export async function getMyTaskOrderDetail(orderId: string): Promise<TaskOrder | null> {
  await delay(300)
  return mockMyTaskOrders.find(o => o.id === orderId) || null
}

export async function acceptTask(taskId: string): Promise<boolean> {
  await delay(500)
  const task = mockTasks.find(t => t.id === taskId)
  if (task && task.slotsLeft > 0) {
    task.slotsLeft -= 1
    return true
  }
  return false
}

export async function submitMilestone(orderId: string, milestoneId: string, data: any): Promise<boolean> {
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