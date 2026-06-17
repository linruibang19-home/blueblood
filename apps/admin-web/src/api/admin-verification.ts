import request from './request'
import type { PageResult } from './admin-user'

/** 认证审核状态 */
export type VerificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

/** 认证列表查询参数 */
export interface VerificationListParams {
  page: number
  pageSize: number
  status?: VerificationStatus
}

/** AdminVerificationVO */
export interface AdminVerificationVO {
  id: number
  userId: number
  nickname: string
  realName: string
  idNumber: string
  /** JSON 字符串数组，材料 URL */
  materials: string
  status: VerificationStatus
  rejectReason?: string
  reviewerId?: number
  /** 审核人昵称/用户名（若后端返回） */
  reviewer?: string
  reviewedAt?: string
  createdAt?: string
}

/** 审核操作参数 */
export interface ReviewVerificationParams {
  status: 'APPROVED' | 'REJECTED'
  rejectReason?: string
}

/** 认证列表：GET /admin/verification */
export function getVerificationList(
  params: VerificationListParams
): Promise<PageResult<AdminVerificationVO>> {
  return request.get<unknown, PageResult<AdminVerificationVO>>(
    '/admin/verification',
    { params }
  )
}

/** 认证详情：GET /admin/verification/{id} */
export function getVerificationDetail(
  id: number
): Promise<AdminVerificationVO> {
  return request.get<unknown, AdminVerificationVO>(`/admin/verification/${id}`)
}

/** 审核认证：POST /admin/verification/{id}/review */
export function reviewVerification(
  id: number,
  data: ReviewVerificationParams
): Promise<void> {
  return request.post<unknown, void>(`/admin/verification/${id}/review`, data)
}
