import request from './request'

/** 通用分页结果 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

// ==================== 系统配置 ====================

export interface SysConfig {
  id: number
  configKey: string
  configValue?: string
  configType?: string
  label?: string
  remark?: string
  createdAt?: string
}

export interface SysConfigParams {
  page: number
  pageSize: number
  keyword?: string
}

export interface SysConfigRequest {
  configKey: string
  configValue?: string
  configType?: string
  label?: string
  remark?: string
}

/** 系统配置列表：GET /admin/system/config */
export function getConfigList(
  params: SysConfigParams
): Promise<PageResult<SysConfig>> {
  return request.get<unknown, PageResult<SysConfig>>('/admin/system/config', {
    params,
  })
}

/** 新增配置：POST /admin/system/config */
export function createConfig(
  data: SysConfigRequest
): Promise<number> {
  return request.post<unknown, number>('/admin/system/config', data)
}

/** 编辑配置：PUT /admin/system/config/{id} */
export function updateConfig(
  id: number,
  data: SysConfigRequest
): Promise<void> {
  return request.put<unknown, void>(`/admin/system/config/${id}`, data)
}

/** 删除配置：DELETE /admin/system/config/{id} */
export function deleteConfig(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/system/config/${id}`)
}

// ==================== 字典 ====================

export interface SysDict {
  id: number
  dictType: string
  dictKey: string
  dictValue?: string
  label?: string
  sort?: number
  remark?: string
  status?: string
  createdAt?: string
}

export interface SysDictParams {
  page: number
  pageSize: number
  dictType?: string
  keyword?: string
}

export interface SysDictRequest {
  dictType: string
  dictKey: string
  dictValue?: string
  label?: string
  sort?: number
  remark?: string
  status?: string
}

/** 字典列表：GET /admin/system/dict */
export function getDictList(
  params: SysDictParams
): Promise<PageResult<SysDict>> {
  return request.get<unknown, PageResult<SysDict>>('/admin/system/dict', {
    params,
  })
}

/** 字典类型列表：GET /admin/system/dict/types */
export function getDictTypes(): Promise<string[]> {
  return request.get<unknown, string[]>('/admin/system/dict/types')
}

/** 新增字典项：POST /admin/system/dict */
export function createDict(data: SysDictRequest): Promise<number> {
  return request.post<unknown, number>('/admin/system/dict', data)
}

/** 编辑字典项：PUT /admin/system/dict/{id} */
export function updateDict(id: number, data: SysDictRequest): Promise<void> {
  return request.put<unknown, void>(`/admin/system/dict/${id}`, data)
}

/** 删除字典项：DELETE /admin/system/dict/{id} */
export function deleteDict(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/system/dict/${id}`)
}

// ==================== 任务分类 ====================

export interface TaskCategory {
  id: number
  name: string
  icon?: string
  taskCount?: number
  categoryOrder?: number
  status?: string
}

export interface TaskCategoryRequest {
  name: string
  icon?: string
  categoryOrder?: number
  status?: string
}

/** 任务分类列表：GET /admin/system/category */
export function getCategoryList(): Promise<TaskCategory[]> {
  return request.get<unknown, TaskCategory[]>('/admin/system/category')
}

/** 新增分类：POST /admin/system/category */
export function createCategory(
  data: TaskCategoryRequest
): Promise<number> {
  return request.post<unknown, number>('/admin/system/category', data)
}

/** 编辑分类：PUT /admin/system/category/{id} */
export function updateCategory(
  id: number,
  data: TaskCategoryRequest
): Promise<void> {
  return request.put<unknown, void>(`/admin/system/category/${id}`, data)
}

/** 删除分类：DELETE /admin/system/category/{id} */
export function deleteCategory(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/system/category/${id}`)
}

// ==================== 技能标签 ====================

export interface AdminSkillVO {
  id: number
  userId: number
  username?: string
  name: string
  category?: string
  proficiency?: number
  createdAt?: string
}

export interface SkillParams {
  page: number
  pageSize: number
  keyword?: string
}

/** 技能标签列表：GET /admin/system/skill */
export function getSkillList(
  params: SkillParams
): Promise<PageResult<AdminSkillVO>> {
  return request.get<unknown, PageResult<AdminSkillVO>>('/admin/system/skill', {
    params,
  })
}

/** 删除技能标签：DELETE /admin/system/skill/{id} */
export function deleteSkill(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/system/skill/${id}`)
}

// ==================== 操作日志 ====================

export interface SysOperationLog {
  id: number
  userId?: number
  username?: string
  module?: string
  action?: string
  method?: string
  url?: string
  params?: string
  ip?: string
  costMs?: number
  status?: string
  errorMsg?: string
  createdAt?: string
}

export interface LogParams {
  page: number
  pageSize: number
  keyword?: string
  module?: string
  status?: string
}

/** 操作日志列表：GET /admin/system/log */
export function getLogList(
  params: LogParams
): Promise<PageResult<SysOperationLog>> {
  return request.get<unknown, PageResult<SysOperationLog>>(
    '/admin/system/log',
    { params }
  )
}
