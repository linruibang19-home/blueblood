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
  createdAt: string
  myOrderId?: string
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
  submission?: MilestoneSubmission
}

export type MilestoneStatus = 'not_started' | 'in_progress' | 'submitted' | 'approved' | 'rejected' | 'overdue'

export interface MilestoneSubmission {
  id: string
  milestoneId: string
  githubUrl?: string
  description: string
  attachments: string[]
  submittedAt: string
  reviewedAt?: string
  feedback?: string
}

export interface TaskCategory {
  id: string
  name: string
  icon: string
  count: number
}