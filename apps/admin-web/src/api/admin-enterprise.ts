import request from './request'
import type { PageResult } from './admin-user'

/** 企业认证状态 */
export type EnterpriseStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

/** 企业认证列表查询参数 */
export interface EnterpriseListParams {
  page: number
  pageSize: number
  keyword?: string
  status?: EnterpriseStatus
}

/** AdminEnterpriseVO */
export interface AdminEnterpriseVO {
  id: number
  /** 申请用户 ID */
  userId?: number
  nickname?: string
  companyName?: string
  creditCode?: string
  licenseUrl?: string
  contact?: string
  status: EnterpriseStatus
  rejectReason?: string
  reviewer?: string
  reviewerId?: number
  reviewedAt?: string
  createdAt?: string
}

/** 企业认证审核参数 */
export interface EnterpriseReviewRequest {
  status: 'APPROVED' | 'REJECTED'
  rejectReason?: string
}

/** 企业认证列表：GET /admin/enterprise */
export function getEnterpriseList(
  params: EnterpriseListParams
): Promise<PageResult<AdminEnterpriseVO>> {
  return request.get<unknown, PageResult<AdminEnterpriseVO>>('/admin/enterprise', {
    params,
  })
}

/** 企业认证详情：GET /admin/enterprise/{id} */
export function getEnterpriseDetail(
  id: number
): Promise<AdminEnterpriseVO> {
  return request.get<unknown, AdminEnterpriseVO>(`/admin/enterprise/${id}`)
}

/** 企业认证审核：POST /admin/enterprise/{id}/review */
export function reviewEnterprise(
  id: number,
  data: EnterpriseReviewRequest
): Promise<void> {
  return request.post<unknown, void>(`/admin/enterprise/${id}/review`, data)
}
