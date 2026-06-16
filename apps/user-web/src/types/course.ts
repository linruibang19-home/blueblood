// 课程类型定义

export interface Course {
  id: string
  title: string
  subtitle: string
  coverImage: string
  instructor: string
  instructorAvatar: string
  chapters: Chapter[]
  progress: number
  totalChapters: number
  completedChapters: number
  rewardPoints: number
  students: number
  rating: number
  status: 'not_started' | 'in_progress' | 'completed'
  createdAt: string
}

export interface Chapter {
  id: string
  courseId: string
  title: string
  duration: string
  videoUrl?: string
  completed: boolean
  order: number
}

export interface Assignment {
  id: string
  courseId: string
  courseName: string
  chapterId: string
  chapterName: string
  title: string
  description: string
  deadline: string
  status: 'not_submitted' | 'submitted' | 'grading' | 'graded'
  score?: number
  feedback?: string
  answer?: string
  submittedAt?: string
}

export interface Hackathon {
  id: string
  title: string
  description: string
  coverImage: string
  prizePool: number
  startTime: string
  endTime: string
  signupDeadline: string
  location: string
  maxTeams: number
  currentTeams: number
  status: 'signup' | 'ongoing' | 'ended'
 myTeamId?: string
}

export interface HackathonTeam {
  id: string
  hackathonId: string
  name: string
  leaderId: string
  leaderName: string
  members: TeamMember[]
  projectName?: string
  projectDesc?: string
  submitted: boolean
  createdAt: string
}

export interface TeamMember {
  userId: string
  userName: string
  userAvatar: string
  role: string
}

export interface Job {
  id: string
  title: string
  company: string
  companyLogo: string
  location: string
  salary: string
  type: '实习' | '兼职' | '全职' | '外包'
  tags: string[]
  description: string
  requirements: string[]
  contact: string
  postedAt: string
}