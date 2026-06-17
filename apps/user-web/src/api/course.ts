// API - 课程模块
import type { Course, Assignment } from '@/types/course'
import { mockCourses, mockAssignments } from '@/mock/courses'
import request, { USE_API, ensureReady } from './request'
import type { PageResponse } from './request'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 后端课程 VO -> 前端 Course（缺失字段兜底，避免页面崩溃）
function mapCourse(raw: any): Course {
  return {
    id: String(raw?.id ?? ''),
    title: raw?.title || '',
    subtitle: raw?.subtitle || '',
    coverImage: raw?.coverImage || '',
    instructor: raw?.instructor || '',
    instructorAvatar: raw?.instructorAvatar || '',
    chapters: Array.isArray(raw?.chapters) ? raw.chapters : [],
    progress: Number(raw?.progress ?? 0),
    totalChapters: Number(raw?.totalChapters ?? raw?.chapters?.length ?? 0),
    completedChapters: Number(raw?.completedChapters ?? 0),
    rewardPoints: Number(raw?.rewardPoints ?? 0),
    students: Number(raw?.students ?? 0),
    rating: Number(raw?.rating ?? 0),
    status: raw?.status || 'not_started',
    createdAt: raw?.createdAt || '',
  }
}

export async function getCourseList(): Promise<Course[]> {
  if (USE_API) {
    await ensureReady()
    const data = await request.get<any, any>('/course')
    const list = Array.isArray(data) ? data : (data as PageResponse)?.list ?? []
    return list.map(mapCourse)
  }
  await delay(300)
  return mockCourses
}

export async function getCourseDetail(courseId: string): Promise<Course | null> {
  if (USE_API) {
    await ensureReady()
    try {
      const data = await request.get<any, any>(`/course/${courseId}`)
      return mapCourse(data)
    } catch {
      return null
    }
  }
  await delay(300)
  return mockCourses.find(c => c.id === courseId) || null
}

export async function getMyAssignments(): Promise<Assignment[]> {
  await delay(300)
  return mockAssignments
}

export async function getAssignmentDetail(assignmentId: string): Promise<Assignment | null> {
  await delay(300)
  return mockAssignments.find(a => a.id === assignmentId) || null
}

export async function submitAssignment(assignmentId: string, _data: any): Promise<boolean> {
  await delay(500)
  const assignment = mockAssignments.find(a => a.id === assignmentId)
  if (assignment) {
    assignment.status = 'submitted'
    assignment.submittedAt = new Date().toLocaleString()
  }
  return true
}

export async function updateCourseProgress(courseId: string, chapterId: string): Promise<boolean> {
  await delay(200)
  const course = mockCourses.find(c => c.id === courseId)
  if (course) {
    const chapter = course.chapters.find(ch => ch.id === chapterId)
    if (chapter) chapter.completed = true
    course.completedChapters = course.chapters.filter(ch => ch.completed).length
    course.progress = Math.round((course.completedChapters / course.totalChapters) * 100)
  }
  return true
}