import request from './request'
import type { PageResult } from './admin-user'

/** 黑客松状态 */
export type HackathonStatus = 'signup' | 'ongoing' | 'ended'

/** 黑客松列表查询参数 */
export interface HackathonListParams {
  page: number
  pageSize: number
  keyword?: string
  status?: HackathonStatus
}

/** AdminHackathonVO */
export interface AdminHackathonVO {
  id: number
  title: string
  description?: string
  coverImage?: string
  /** 奖金池（decimal，传输为字符串或数字） */
  prizePool?: string | number
  startTime?: string
  endTime?: string
  signupDeadline?: string
  location?: string
  maxTeams?: number
  currentTeams?: number
  status: HackathonStatus
  publishedBy?: number
  publisherNickname?: string
  createdAt?: string
}

/** 新增/编辑黑客松参数 */
export interface HackathonRequest {
  title: string
  description?: string
  coverImage?: string
  prizePool?: string | number
  startTime?: string
  endTime?: string
  signupDeadline?: string
  location?: string
  maxTeams?: number
  currentTeams?: number
  status?: HackathonStatus
  publishedBy?: number
}

/** 黑客松列表：GET /admin/hackathon */
export function getHackathonList(
  params: HackathonListParams
): Promise<PageResult<AdminHackathonVO>> {
  return request.get<unknown, PageResult<AdminHackathonVO>>('/admin/hackathon', {
    params,
  })
}

/** 黑客松详情：GET /admin/hackathon/{id} */
export function getHackathonDetail(id: number): Promise<AdminHackathonVO> {
  return request.get<unknown, AdminHackathonVO>(`/admin/hackathon/${id}`)
}

/** 新增黑客松：POST /admin/hackathon */
export function createHackathon(data: HackathonRequest): Promise<number> {
  return request.post<unknown, number>('/admin/hackathon', data)
}

/** 编辑黑客松：PUT /admin/hackathon/{id} */
export function updateHackathon(
  id: number,
  data: HackathonRequest
): Promise<void> {
  return request.put<unknown, void>(`/admin/hackathon/${id}`, data)
}

/** 删除黑客松：DELETE /admin/hackathon/{id} */
export function deleteHackathon(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/hackathon/${id}`)
}
