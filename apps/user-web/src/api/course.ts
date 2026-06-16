// API - 课程模块
import type { Course, Assignment } from '@/types/course'
import { mockCourses, mockAssignments } from '@/mock/courses'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

export async function getCourseList(): Promise<Course[]> {
  await delay(300)
  return mockCourses
}

export async function getCourseDetail(courseId: string): Promise<Course | null> {
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