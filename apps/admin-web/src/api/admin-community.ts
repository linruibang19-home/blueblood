import request from './request'
import type { PageResult } from './admin-user'

// ==================== 小组 ====================

/** 小组状态 */
export type GroupStatus = 'ACTIVE' | 'INACTIVE'

/** 小组列表查询参数 */
export interface GroupListParams {
  page: number
  pageSize: number
  keyword?: string
  status?: GroupStatus
}

/** AdminGroupVO */
export interface AdminGroupVO {
  id: number
  name: string
  description?: string
  coverImage?: string
  leaderId?: number
  leaderNickname?: string
  category?: string
  /** JSON 字符串 */
  tags?: string
  memberCount: number
  postCount: number
  activityCount: number
  status: GroupStatus
  createdAt?: string
}

/** 新增/编辑小组参数 */
export interface GroupRequest {
  name: string
  description?: string
  coverImage?: string
  leaderId?: number
  category?: string
  /** JSON 字符串 */
  tags?: string
  status?: GroupStatus
}

/** 小组列表：GET /admin/group */
export function getGroupList(
  params: GroupListParams
): Promise<PageResult<AdminGroupVO>> {
  return request.get<unknown, PageResult<AdminGroupVO>>('/admin/group', { params })
}

/** 小组详情：GET /admin/group/{id} */
export function getGroupDetail(id: number): Promise<AdminGroupVO> {
  return request.get<unknown, AdminGroupVO>(`/admin/group/${id}`)
}

/** 新增小组：POST /admin/group */
export function createGroup(data: GroupRequest): Promise<number> {
  return request.post<unknown, number>('/admin/group', data)
}

/** 编辑小组：PUT /admin/group/{id} */
export function updateGroup(id: number, data: GroupRequest): Promise<void> {
  return request.put<unknown, void>(`/admin/group/${id}`, data)
}

/** 小组上架/下架：PUT /admin/group/{id}/status?status= */
export function updateGroupStatus(
  id: number,
  status: GroupStatus
): Promise<void> {
  return request.put<unknown, void>(`/admin/group/${id}/status`, null, {
    params: { status },
  })
}

/** 删除小组：DELETE /admin/group/{id} */
export function deleteGroup(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/group/${id}`)
}

// ==================== 帖子 ====================

/** 帖子状态 */
export type PostStatus = 'PUBLISHED' | 'DRAFT' | 'HIDDEN'

/** 帖子标签 */
export type PostTag = '话题' | '任务' | '经验分享' | '活动'

/** 帖子列表查询参数 */
export interface PostListParams {
  page: number
  pageSize: number
  keyword?: string
  groupId?: number
  tag?: PostTag
  status?: PostStatus
}

/** AdminPostVO */
export interface AdminPostVO {
  id: number
  groupId?: number
  groupName?: string
  authorId?: number
  authorNickname?: string
  title: string
  content?: string
  /** JSON 字符串 */
  images?: string
  tag?: PostTag
  likes: number
  comments: number
  views: number
  status: PostStatus
  createdAt?: string
}

/** 帖子列表：GET /admin/post */
export function getPostList(
  params: PostListParams
): Promise<PageResult<AdminPostVO>> {
  return request.get<unknown, PageResult<AdminPostVO>>('/admin/post', { params })
}

/** 显示/隐藏帖子：PUT /admin/post/{id}/status?status= */
export function updatePostStatus(
  id: number,
  status: 'HIDDEN' | 'PUBLISHED'
): Promise<void> {
  return request.put<unknown, void>(`/admin/post/${id}/status`, null, {
    params: { status },
  })
}

/** 删除帖子：DELETE /admin/post/{id} */
export function deletePost(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/post/${id}`)
}

// ==================== 评论 ====================

/** 评论状态 */
export type CommentStatus = 'NORMAL' | 'DELETED'

/** 评论列表查询参数 */
export interface CommentListParams {
  page: number
  pageSize: number
  postId?: number
}

/** AdminCommentVO */
export interface AdminCommentVO {
  id: number
  postId?: number
  authorId?: number
  authorNickname?: string
  parentId?: number
  content: string
  likes: number
  status: CommentStatus
  createdAt?: string
}

/** 评论列表：GET /admin/comment */
export function getCommentList(
  params: CommentListParams
): Promise<PageResult<AdminCommentVO>> {
  return request.get<unknown, PageResult<AdminCommentVO>>('/admin/comment', {
    params,
  })
}

/** 显示/隐藏评论：PUT /admin/comment/{id}/status?status= */
export function updateCommentStatus(
  id: number,
  status: CommentStatus
): Promise<void> {
  return request.put<unknown, void>(`/admin/comment/${id}/status`, null, {
    params: { status },
  })
}

/** 删除评论：DELETE /admin/comment/{id} */
export function deleteComment(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/comment/${id}`)
}

// ==================== 活动 ====================

/** 活动状态 */
export type ActivityStatus = 'upcoming' | 'ongoing' | 'ended'

/** 活动列表查询参数 */
export interface ActivityListParams {
  page: number
  pageSize: number
  groupId?: number
  keyword?: string
}

/** AdminActivityVO */
export interface AdminActivityVO {
  id: number
  groupId?: number
  groupName?: string
  title: string
  description?: string
  coverImage?: string
  startTime?: string
  endTime?: string
  location?: string
  signupCount: number
  maxCount: number
  status: ActivityStatus
  createdAt?: string
}

/** 新增/编辑活动参数 */
export interface ActivityRequest {
  groupId?: number
  title: string
  description?: string
  coverImage?: string
  startTime?: string
  endTime?: string
  location?: string
  maxCount?: number
  status?: ActivityStatus
}

/** 活动列表：GET /admin/activity */
export function getActivityList(
  params: ActivityListParams
): Promise<PageResult<AdminActivityVO>> {
  return request.get<unknown, PageResult<AdminActivityVO>>('/admin/activity', {
    params,
  })
}

/** 新增活动：POST /admin/activity */
export function createActivity(data: ActivityRequest): Promise<number> {
  return request.post<unknown, number>('/admin/activity', data)
}

/** 编辑活动：PUT /admin/activity/{id} */
export function updateActivity(
  id: number,
  data: ActivityRequest
): Promise<void> {
  return request.put<unknown, void>(`/admin/activity/${id}`, data)
}

/** 删除活动：DELETE /admin/activity/{id} */
export function deleteActivity(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/activity/${id}`)
}
