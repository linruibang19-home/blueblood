// 小组类型定义

export interface Group {
  id: string
  name: string
  description: string
  coverImage: string
  memberCount: number
  leaderId: string
  leaderName: string
  leaderAvatar: string
  category: 'AI' | 'Dev' | 'Design' | 'Product' | 'Study'
  tags: string[]
  joined: boolean
  postCount: number
  activityCount: number
  createdAt: string
}

export interface GroupMember {
  id: string
  groupId: string
  userId: string
  userName: string
  userAvatar: string
  role: 'leader' | 'admin' | 'member'
  joinedAt: string
}

export interface Activity {
  id: string
  groupId: string
  title: string
  description: string
  coverImage: string
  startTime: string
  endTime: string
  location: string
  signupCount: number
  maxCount: number
  status: 'upcoming' | 'ongoing' | 'ended'
}