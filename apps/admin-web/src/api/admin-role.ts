import request from './request'

/** AdminRoleVO */
export interface AdminRoleVO {
  id: number
  /** 角色编码: USER/ADMIN */
  code: string
  name: string
  description?: string
  /** 状态: ACTIVE/INACTIVE */
  status?: string
  /** 该角色下的用户数 */
  userCount: number
}

/** AdminRoleUserVO */
export interface AdminRoleUserVO {
  userId: number
  nickname?: string
  username?: string
}

/** 角色分配/撤销请求 */
export interface RoleAssignParams {
  userId: number
  roleCode: string
}

/** 角色列表(含用户数)：GET /admin/role */
export function getRoleList(): Promise<AdminRoleVO[]> {
  return request.get<unknown, AdminRoleVO[]>('/admin/role')
}

/** 某角色下的用户列表：GET /admin/role/users?roleCode= */
export function getRoleUsers(
  roleCode: string
): Promise<AdminRoleUserVO[]> {
  return request.get<unknown, AdminRoleUserVO[]>('/admin/role/users', {
    params: { roleCode },
  })
}

/** 给用户分配角色：POST /admin/role/assign */
export function assignRole(data: RoleAssignParams): Promise<void> {
  return request.post<unknown, void>('/admin/role/assign', data)
}

/** 撤销用户角色：POST /admin/role/revoke */
export function revokeRole(data: RoleAssignParams): Promise<void> {
  return request.post<unknown, void>('/admin/role/revoke', data)
}
