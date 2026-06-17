import request from './request'

/** 分页结果（与 admin-user 保持一致） */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

// ==================== 课程 ====================

export type CourseStatus = 'PUBLISHED' | 'DRAFT' | 'OFFLINE'

export interface CourseListParams {
  page: number
  pageSize: number
  keyword?: string
  status?: CourseStatus
}

export interface AdminCourseVO {
  id: number
  title: string
  subtitle?: string
  coverImage?: string
  instructor?: string
  instructorAvatar?: string
  totalChapters?: number
  rewardPoints?: number
  students?: number
  rating?: number | string
  status: CourseStatus
  createdAt?: string
}

export interface CourseRequest {
  title: string
  subtitle?: string
  coverImage?: string
  instructor?: string
  instructorAvatar?: string
  totalChapters?: number
  rewardPoints?: number
  rating?: number | string
  status?: CourseStatus
}

/** 课程列表：GET /admin/course */
export function getCourseList(
  params: CourseListParams
): Promise<PageResult<AdminCourseVO>> {
  return request.get<unknown, PageResult<AdminCourseVO>>('/admin/course', { params })
}

/** 课程详情：GET /admin/course/{id} */
export function getCourseDetail(id: number): Promise<AdminCourseVO> {
  return request.get<unknown, AdminCourseVO>(`/admin/course/${id}`)
}

/** 新增课程：POST /admin/course */
export function createCourse(data: CourseRequest): Promise<number> {
  return request.post<unknown, number>('/admin/course', data)
}

/** 编辑课程：PUT /admin/course/{id} */
export function updateCourse(id: number, data: CourseRequest): Promise<void> {
  return request.put<unknown, void>(`/admin/course/${id}`, data)
}

/** 上下架/改状态：PUT /admin/course/{id}/status?status= */
export function updateCourseStatus(
  id: number,
  status: CourseStatus
): Promise<void> {
  return request.put<unknown, void>(`/admin/course/${id}/status`, null, {
    params: { status },
  })
}

/** 删除课程：DELETE /admin/course/{id} */
export function deleteCourse(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/course/${id}`)
}

// ==================== 章节 ====================

export interface AdminChapterVO {
  id: number
  courseId: number
  courseTitle?: string
  title: string
  duration?: string
  videoUrl?: string
  chapterOrder?: number
  createdAt?: string
}

export interface ChapterRequest {
  courseId: number
  title: string
  duration?: string
  videoUrl?: string
  chapterOrder?: number
}

/** 章节列表：GET /admin/chapter?courseId= */
export function listChapters(courseId: number): Promise<AdminChapterVO[]> {
  return request.get<unknown, AdminChapterVO[]>('/admin/chapter', {
    params: { courseId },
  })
}

/** 新增章节：POST /admin/chapter */
export function createChapter(data: ChapterRequest): Promise<number> {
  return request.post<unknown, number>('/admin/chapter', data)
}

/** 编辑章节：PUT /admin/chapter/{id} */
export function updateChapter(
  id: number,
  data: ChapterRequest
): Promise<void> {
  return request.put<unknown, void>(`/admin/chapter/${id}`, data)
}

/** 删除章节：DELETE /admin/chapter/{id} */
export function deleteChapter(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/chapter/${id}`)
}

// ==================== 作业 ====================

export type AssignmentStatus = 'not_submitted' | 'submitted' | 'graded'

export interface AssignmentListParams {
  page: number
  pageSize: number
  courseId?: number
  keyword?: string
  status?: AssignmentStatus
}

export interface AdminAssignmentVO {
  id: number
  courseId: number
  courseTitle?: string
  chapterId?: number
  title: string
  description?: string
  deadline?: string
  /** 参考答案 */
  answer?: string
  status?: AssignmentStatus
  createdAt?: string
}

export interface AssignmentRequest {
  courseId: number
  chapterId?: number
  title: string
  description?: string
  deadline?: string
  answer?: string
  status?: AssignmentStatus
}

/** 作业列表：GET /admin/assignment */
export function getAssignmentList(
  params: AssignmentListParams
): Promise<PageResult<AdminAssignmentVO>> {
  return request.get<unknown, PageResult<AdminAssignmentVO>>('/admin/assignment', {
    params,
  })
}

/** 作业详情：GET /admin/assignment/{id} */
export function getAssignmentDetail(id: number): Promise<AdminAssignmentVO> {
  return request.get<unknown, AdminAssignmentVO>(`/admin/assignment/${id}`)
}

/** 新增作业：POST /admin/assignment */
export function createAssignment(data: AssignmentRequest): Promise<number> {
  return request.post<unknown, number>('/admin/assignment', data)
}

/** 编辑作业：PUT /admin/assignment/{id} */
export function updateAssignment(
  id: number,
  data: AssignmentRequest
): Promise<void> {
  return request.put<unknown, void>(`/admin/assignment/${id}`, data)
}

/** 删除作业：DELETE /admin/assignment/{id} */
export function deleteAssignment(id: number): Promise<void> {
  return request.delete<unknown, void>(`/admin/assignment/${id}`)
}

// ==================== 提交与批改 ====================

export type SubmissionStatus = 'submitted' | 'graded'

export interface SubmissionListParams {
  page: number
  pageSize: number
  assignmentId?: number
  status?: SubmissionStatus
}

export interface AdminSubmissionVO {
  id: number
  assignmentId: number
  assignmentTitle?: string
  userId: number
  studentNickname?: string
  content?: string
  attachments?: string
  submittedAt?: string
  status: SubmissionStatus
  /** 已评分才有 */
  score?: number | string
  feedback?: string
}

export interface GradeSubmissionParams {
  score: number | string
  feedback?: string
}

/** 提交列表：GET /admin/submission */
export function getSubmissionList(
  params: SubmissionListParams
): Promise<PageResult<AdminSubmissionVO>> {
  return request.get<unknown, PageResult<AdminSubmissionVO>>('/admin/submission', {
    params,
  })
}

/** 批改提交：PUT /admin/submission/{submissionId}/grade */
export function gradeSubmission(
  submissionId: number,
  data: GradeSubmissionParams
): Promise<number> {
  return request.put<unknown, number>(
    `/admin/submission/${submissionId}/grade`,
    data
  )
}
