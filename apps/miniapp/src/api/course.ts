import http from '@/utils/request'

export interface Course {
  id: string
  title: string
  subtitle: string
  coverImage: string
  instructor: string
  instructorAvatar: string
  totalChapters: number
  completedChapters: number
  progress: number
  rewardPoints: number
  students: number
  rating: number
  status: string
}

interface PageResp {
  list?: any[]
}

function mapCourse(raw: any): Course {
  return {
    id: String(raw?.id ?? ''),
    title: raw?.title || '',
    subtitle: raw?.subtitle || '',
    coverImage: raw?.coverImage || '',
    instructor: raw?.instructor || '',
    instructorAvatar: raw?.instructorAvatar || '',
    totalChapters: Number(raw?.totalChapters ?? raw?.chapters?.length ?? 0),
    completedChapters: Number(raw?.completedChapters ?? 0),
    progress: Number(raw?.progress ?? 0),
    rewardPoints: Number(raw?.rewardPoints ?? 0),
    students: Number(raw?.students ?? 0),
    rating: Number(raw?.rating ?? 0),
    status: raw?.status || 'not_started',
  }
}

/** GET /course */
export async function getCourseList(): Promise<Course[]> {
  const data = await http.get<any>('/course').catch(() => ({} as any))
  const list = Array.isArray(data) ? data : (data as PageResp)?.list ?? []
  return list.map(mapCourse)
}
