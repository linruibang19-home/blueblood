import request from './request'
import type { PageResult } from './admin-user'

/** 钱包流水类型 */
export type WalletRecordType = 'income' | 'expense' | 'withdraw'

/** 钱包流水状态 */
export type WalletRecordStatus =
  | 'pending'
  | 'available'
  | 'withdrawing'
  | 'withdrawn'
  | 'failed'

/** 提现状态 */
export type WithdrawStatus = 'pending' | 'processing' | 'completed' | 'failed'

/** 收益流水查询参数 */
export interface WalletRecordListParams {
  page: number
  pageSize: number
  userId?: number
  type?: WalletRecordType
  status?: WalletRecordStatus
}

/** 提现申请查询参数 */
export interface WithdrawListParams {
  page: number
  pageSize: number
  status?: WithdrawStatus
}

/** 钱包账户状态 */
export type WalletAccountStatus = 'ACTIVE' | 'FROZEN'

/** AdminWalletRecordVO */
export interface AdminWalletRecordVO {
  id: number
  userId: number
  username?: string
  nickname?: string
  type: WalletRecordType
  amount: number
  status: WalletRecordStatus
  title?: string
  description?: string
  bizType?: string
  bizId?: number
  createdAt?: string
}

/** AdminWalletAccountVO */
export interface AdminWalletAccountVO {
  id: number
  userId: number
  nickname?: string
  balance: number
  pendingAmount: number
  withdrawnAmount: number
  totalEarned: number
  status: WalletAccountStatus
  createdAt?: string
}

/** AdminWithdrawVO */
export interface AdminWithdrawVO {
  id: number
  userId: number
  username?: string
  nickname?: string
  amount: number
  status: WithdrawStatus
  bankName?: string
  bankAccount?: string
  failureReason?: string
  processedAt?: string
  createdAt?: string
}

/** 提现审核参数 */
export interface ReviewWithdrawParams {
  action: 'APPROVED' | 'REJECTED'
  failureReason?: string
}

/** 收益流水：GET /admin/wallet/record */
export function getWalletRecordList(
  params: WalletRecordListParams
): Promise<PageResult<AdminWalletRecordVO>> {
  return request.get<unknown, PageResult<AdminWalletRecordVO>>(
    '/admin/wallet/record',
    { params }
  )
}

/** 待结算流水：GET /admin/wallet/pending */
export function getPendingRecordList(
  params: Pick<WalletRecordListParams, 'page' | 'pageSize'>
): Promise<PageResult<AdminWalletRecordVO>> {
  return request.get<unknown, PageResult<AdminWalletRecordVO>>(
    '/admin/wallet/pending',
    { params }
  )
}

/** 用户钱包账户：GET /admin/wallet/account?userId= */
export function getWalletAccount(
  userId: number
): Promise<AdminWalletAccountVO> {
  return request.get<unknown, AdminWalletAccountVO>('/admin/wallet/account', {
    params: { userId },
  })
}

/** 提现申请列表：GET /admin/wallet/withdraw */
export function getWithdrawList(
  params: WithdrawListParams
): Promise<PageResult<AdminWithdrawVO>> {
  return request.get<unknown, PageResult<AdminWithdrawVO>>(
    '/admin/wallet/withdraw',
    { params }
  )
}

/** 提现审核：PUT /admin/wallet/withdraw/{id}/review */
export function reviewWithdraw(
  id: number,
  data: ReviewWithdrawParams
): Promise<void> {
  return request.put<unknown, void>(`/admin/wallet/withdraw/${id}/review`, data)
}
