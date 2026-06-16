// 通用类型定义

export interface PageParams {
  page?: number
  pageSize?: number
}

export interface PageResult<T> {
  list: T[]
  page: number
  pageSize: number
  total: number
}

export type Status = 'pending' | 'active' | 'completed' | 'closed' | 'rejected'

export type BaseEntity = {
  id: string
  createdAt: string
  updatedAt?: string
}