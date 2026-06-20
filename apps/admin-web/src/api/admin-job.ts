import request from './request'
import type { PageResult } from './admin-user'

/** 岗位状态 */
export type JobStatus = 'ACTIVE' | 'CLOSED'

/** 岗位类型 */
export type JobType = '实习' | '兼职' | '全职' | '外包'

/** 岗位列表查询参数 */
export interface JobListParams {
  page: number
  pageSize: number
  keyword?: string
  company?: string
  status?: JobStatus
  type?: JobType
}

/** AdminJobVO */
export interface AdminJobVO {
  id: number
  title: string
  company?: string
  companyLogo?: string
  location?: string
  salary?: string
  type?: JobType
  /** JSON 字符串 */
  tags?: string
  description?: string
  /** JSON 字符串 */
  requirements?: string
  contact?: string
  status: JobStatus
  publishedBy?: number
  publisherNickname?: string
  createdAt?: string
}

/** 新增/编辑岗位参数 */
export interface JobRequest {
  title: string
  company?: string
  companyLogo?: string
  location?: string
  salary?: string
  type?: JobType
  /** JSON 字符串 */
  tags?: string
  description?: string
  /** JSON 字符串 */
  requirements?: string
  contact?: string
  status?: JobStatus
  publishedBy?: number
}

/** 岗位列表：GET /admin/job */
export function getJobList(
  params: JobListParams
): Promise<PageResult<AdminJobVO>> {
  return request.get<unknown, PageResult<AdminJobVO>>('/admin/job', { params })
}

/** 岗位详情：GET /admin/job/{id} */
export function getJobDetail(id: number): Promise<AdminJobVO> {
  return request.get<unknown, AdminJobVO>(`/admin/job/${id}`)
}

/** 新增岗位：POST /admin/job */
export function createJob(data: JobRequest): Promise<number> {
  return request.post<unknown, number>('/admin/job', data)
}

/** 编辑岗位：PUT /admin/job/{id} */
export function updateJob(id: number, data: JobRequest): Promise<void> {
  return request.put<unknown, void>(`/admin/job/${id}`, data)
}

/** 删除岗位：DELETE /admin/job/{id} */
export function deleteJob(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/job/${id}`)
}
