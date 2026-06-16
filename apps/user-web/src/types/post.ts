// 帖子类型定义

export interface Post {
  id: string
  groupId: string
  groupName: string
  authorId: string
  authorName: string
  authorAvatar: string
  authorLevel: number
  title: string
  content: string
  images: string[]
  likes: number
  comments: number
  views: number
  tag: '话题' | '任务' | '经验分享' | '活动'
  liked: boolean
  favorited: boolean
  createdAt: string
}

export interface PostComment {
  id: string
  postId: string
  authorId: string
  authorName: string
  authorAvatar: string
  content: string
  likes: number
  liked: boolean
  parentId?: string
  replies?: PostComment[]
  createdAt: string
}

export interface ChatSession {
  id: string
  userId: string
  userName: string
  userAvatar: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
}

export interface ChatMessage {
  id: string
  sessionId: string
  senderId: string
  senderName: string
  senderAvatar: string
  content: string
  type: 'text' | 'image' | 'system'
  createdAt: string
}