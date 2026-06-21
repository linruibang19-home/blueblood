// 任务类型定义

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
  status: TaskStatus
  employerName: string
  employerId?: string
  createdAt: string
  myOrderId?: string
  accepted?: boolean
}

export type TaskStatus = 'draft' | 'pending_review' | 'approved' | 'recruiting' | 'in_progress' | 'completed' | 'closed'

export interface TaskOrder {
  id: string
  taskId: string
  taskTitle: string
  userId: string
  userName: string
  userAvatar: string
  status: OrderStatus
  progress: number
  milestones: Milestone[]
  createdAt: string
}

export type OrderStatus = 'applied' | 'accepted' | 'in_progress' | 'wait_acceptance' | 'passed' | 'rejected' | 'settling' | 'settled'

export interface Milestone {
  id: string
  orderId: string
  title: string
  description: string
  dueDate: string
  status: MilestoneStatus
  order: number
  reward?: number
  milestoneOrder?: number
  submission?: MilestoneSubmission
  review?: MilestoneReviewInfo
}

export type MilestoneStatus = 'not_started' | 'in_progress' | 'submitted' | 'approved' | 'rejected' | 'overdue'

export interface MilestoneSubmission {
  id: string
  milestoneId: string
  githubUrl?: string
  description: string
  attachments: string[]
  submittedAt: string
}

export interface MilestoneReviewInfo {
  result: string
  feedback?: string
  reviewedAt?: string
}

export interface TaskCategory {
  id: string
  name: string
  icon: string
  count: number
}

// ===== 用户端发布 / 雇主工作台 =====

export interface MilestoneTemplate {
  title: string
  description?: string
  dueDate?: string
  milestoneOrder?: number
  reward: number
}

export interface PublishTaskPayload {
  title: string
  categoryId?: number | string
  description: string
  reward: number
  levelRequired?: number
  totalSlots?: number
  deadline?: string
  skills?: string[]
  milestones: MilestoneTemplate[]
}

export interface MilestoneReviewItem {
  milestoneId: string
  orderId: string
  taskId: string
  taskTitle: string
  milestoneTitle: string
  description: string
  milestoneOrder: number
  reward: number
  status: string
  workerId: string
  workerName: string
  githubUrl: string
  submissionDesc: string
  attachments: string
  submittedAt: string
}
