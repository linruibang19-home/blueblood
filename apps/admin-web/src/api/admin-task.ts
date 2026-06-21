import request from './request'

/** 分页结果（与 admin-user / admin-course 保持一致） */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

// ==================== 任务 ====================

export type TaskStatus =
  | 'DRAFT'
  | 'PENDING_REVIEW'
  | 'APPROVED'
  | 'RECRUITING'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CLOSED'

export interface TaskListParams {
  page: number
  pageSize: number
  keyword?: string
  category?: number
  status?: TaskStatus
}

export interface AdminTaskVO {
  id: number
  title: string
  categoryId?: number
  category?: string
  employerId?: number
  employerName?: string
  description?: string
  reward?: number | string
  levelRequired?: number
  totalSlots?: number
  slotsLeft?: number
  deadline?: string
  status: TaskStatus
  viewCount?: number
  createdAt?: string
}

export interface TaskRequest {
  title: string
  categoryId?: number
  category?: string
  employerName?: string
  description?: string
  reward?: number | string
  levelRequired?: number
  totalSlots?: number
  deadline?: string
  status?: TaskStatus
}

export interface TaskCategoryVO {
  id: number
  name: string
  icon?: string
  taskCount?: number
  categoryOrder?: number
}

/** 任务列表：GET /admin/task */
export function getTaskList(
  params: TaskListParams
): Promise<PageResult<AdminTaskVO>> {
  return request.get<unknown, PageResult<AdminTaskVO>>('/admin/task', { params })
}

/** 任务详情：GET /admin/task/{id} */
export function getTaskDetail(id: number): Promise<AdminTaskVO> {
  return request.get<unknown, AdminTaskVO>(`/admin/task/${id}`)
}

/** 发布任务：POST /admin/task */
export function createTask(data: TaskRequest): Promise<number> {
  return request.post<unknown, number>('/admin/task', data)
}

/** 编辑任务：PUT /admin/task/{id} */
export function updateTask(id: number, data: TaskRequest): Promise<void> {
  return request.put<unknown, void>(`/admin/task/${id}`, data)
}

/** 改任务状态：PUT /admin/task/{id}/status?status= */
export function updateTaskStatus(id: number, status: TaskStatus): Promise<void> {
  return request.put<unknown, void>(`/admin/task/${id}/status`, null, {
    params: { status },
  })
}

/** 删除任务：DELETE /admin/task/{id} */
export function deleteTask(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/task/${id}`)
}

/** 任务分类列表：GET /admin/task/categories */
export function listTaskCategories(): Promise<TaskCategoryVO[]> {
  return request.get<unknown, TaskCategoryVO[]>('/admin/task/categories')
}

// ==================== 接单记录 ====================

export type OrderStatus =
  | 'applied'
  | 'accepted'
  | 'in_progress'
  | 'wait_acceptance'
  | 'passed'
  | 'rejected'
  | 'settling'
  | 'settled'

export interface OrderListParams {
  page: number
  pageSize: number
  taskId?: number
  status?: OrderStatus
}

export interface AdminTaskOrderVO {
  id: number
  taskId: number
  taskTitle?: string
  userId: number
  username?: string
  nickname?: string
  progress?: number
  status: OrderStatus
  remark?: string
  createdAt?: string
}

/** 接单记录列表：GET /admin/order */
export function getOrderList(
  params: OrderListParams
): Promise<PageResult<AdminTaskOrderVO>> {
  return request.get<unknown, PageResult<AdminTaskOrderVO>>('/admin/order', {
    params,
  })
}

/** 强制关闭订单：POST /admin/order/{id}/close */
export function closeOrder(id: number): Promise<void> {
  return request.post<unknown, void>(`/admin/order/${id}/close`)
}

/** 订单详情(含里程碑,复用用户端 /task/orders/{id}):GET /task/orders/{id} */
export function getOrderDetail(id: number): Promise<any> {
  return request.get<unknown, any>(`/task/orders/${id}`)
}

// ==================== 里程碑提交 + 审核 ====================

export type MilestoneStatus =
  | 'NOT_STARTED'
  | 'IN_PROGRESS'
  | 'SUBMITTED'
  | 'APPROVED'
  | 'REJECTED'
  | 'OVERDUE'

export interface MilestoneSubmissionListParams {
  page: number
  pageSize: number
  /** 默认 SUBMITTED */
  status?: MilestoneStatus
}

export interface AdminMilestoneSubmissionVO {
  id: number
  milestoneId: number
  orderId: number
  taskId?: number
  taskTitle?: string
  milestoneTitle?: string
  userId: number
  userNickname?: string
  githubUrl?: string
  description?: string
  /** 附件列表 JSON 字符串 */
  attachments?: string
  submittedAt?: string
  status: MilestoneStatus
}

export interface ReviewMilestoneParams {
  /** APPROVED / REJECTED */
  result: 'APPROVED' | 'REJECTED'
  feedback?: string
}

/** 里程碑提交列表：GET /admin/milestone/submissions */
export function getMilestoneSubmissionList(
  params: MilestoneSubmissionListParams
): Promise<PageResult<AdminMilestoneSubmissionVO>> {
  return request.get<
    unknown,
    PageResult<AdminMilestoneSubmissionVO>
  >('/admin/milestone/submissions', { params })
}

/** 审核里程碑：PUT /admin/milestone/{milestoneId}/review */
export function reviewMilestone(
  milestoneId: number,
  data: ReviewMilestoneParams
): Promise<number> {
  return request.put<unknown, number>(
    `/admin/milestone/${milestoneId}/review`,
    data
  )
}
