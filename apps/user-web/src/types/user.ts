// 用户类型定义

export interface User {
  id: string
  name: string
  avatar: string
  school: string
  major: string
  level: number
  levelName: string
  points: number
  creditScore: number
  completedTasks: number
  skills: string[]
  verified: boolean
  bio: string
  github: string
  joinedAt: string
  /** 用户类型：enterprise 企业用户 / personal 个人用户 */
  userType?: string
}

export interface UserProfile extends User {
  connections: number
  followers: number
  following: number
  badges: Badge[]
  radarData?: RadarData
}

export interface Badge {
  id: string
  name: string
  icon: string
  description: string
}

export interface RadarData {
  labels: string[]
  values: number[]
}

export interface SkillTag {
  id: string
  name: string
  category: string
}

export interface UserLevelLog {
  id: string
  userId: string
  fromLevel: number
  toLevel: number
  reason: string
  createdAt: string
}