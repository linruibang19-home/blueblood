import request from './request'

/** 用户状态 */
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BANNED'

/** 用户类型：personal=个人 enterprise=企业 */
export type UserType = 'personal' | 'enterprise'

/** 用户列表查询参数 */
export interface UserListParams {
  page: number
  pageSize: number
  keyword?: string
  status?: UserStatus
  /** 0=未认证 1=已认证，不传=全部 */
  verified?: number
  /** 用户类型筛选，不传=全部 */
  userType?: UserType
}

/** AdminUserVO */
export interface AdminUserVO {
  id: number
  username: string
  nickname: string
  avatar: string
  phone: string
  email: string
  gender?: string
  level: number
  levelName: string
  points: number
  creditScore: number
  completedTasks: number
  /** 0/1 */
  verified: number
  /** 用户类型：personal=个人 enterprise=企业 */
  userType?: UserType
  status: UserStatus
  lastLoginAt?: string
  createdAt?: string
}

/** 分页结果（后端 {list,total,page,pageSize,totalPages}） */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

/** 调整等级/积分参数 */
export interface AdjustUserParams {
  level?: number
  levelName?: string
  /** 正数增加，负数扣减 */
  pointsDelta?: number
  reason?: string
}

/** 用户列表：GET /admin/user */
export function getUserList(
  params: UserListParams
): Promise<PageResult<AdminUserVO>> {
  return request.get<unknown, PageResult<AdminUserVO>>('/admin/user', { params })
}

/** 用户详情：GET /admin/user/{id} */
export function getUserDetail(id: number): Promise<AdminUserVO> {
  return request.get<unknown, AdminUserVO>(`/admin/user/${id}`)
}

/** 修改用户状态：PUT /admin/user/{id}/status?status= */
export function updateUserStatus(
  id: number,
  status: UserStatus
): Promise<void> {
  return request.put<unknown, void>(`/admin/user/${id}/status`, null, {
    params: { status },
  })
}

/** 调整等级/积分：PUT /admin/user/{id}/adjust */
export function adjustUser(
  id: number,
  data: AdjustUserParams
): Promise<void> {
  return request.put<unknown, void>(`/admin/user/${id}/adjust`, data)
}
