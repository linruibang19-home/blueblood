// API - 帖子模块
import type { Post, PostComment } from '@/types/post'
import { mockPosts, mockComments } from '@/mock/posts'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

export async function getPostsByGroup(groupId: string, tag?: string): Promise<Post[]> {
  await delay(300)
  let posts = mockPosts.filter(p => p.groupId === groupId)
  if (tag && tag !== '全部') {
    posts = posts.filter(p => p.tag === tag)
  }
  return posts
}

export async function getPostDetail(postId: string): Promise<Post | null> {
  await delay(300)
  return mockPosts.find(p => p.id === postId) || null
}

export async function getCommentsByPost(postId: string): Promise<PostComment[]> {
  await delay(300)
  return mockComments.filter(c => c.postId === postId)
}

export async function togglePostLike(postId: string): Promise<boolean> {
  await delay(200)
  const post = mockPosts.find(p => p.id === postId)
  if (post) {
    post.liked = !post.liked
    post.likes += post.liked ? 1 : -1
  }
  return post?.liked || false
}

export async function togglePostFavorite(postId: string): Promise<boolean> {
  await delay(200)
  const post = mockPosts.find(p => p.id === postId)
  if (post) {
    post.favorited = !post.favorited
  }
  return post?.favorited || false
}

export async function addComment(postId: string, content: string): Promise<PostComment> {
  await delay(300)
  const post = mockPosts.find(p => p.id === postId)
  if (post) post.comments += 1
  return {
    id: `c_${Date.now()}`,
    postId,
    authorId: 'u001',
    authorName: '林同学',
    authorAvatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    content,
    likes: 0,
    liked: false,
    createdAt: new Date().toLocaleString(),
  }
}

export async function getChatSessions(): Promise<any[]> {
  await delay(300)
  return []
}

export async function sendMessage(sessionId: string, content: string): Promise<any> {
  await delay(200)
  return {
    id: `msg_${Date.now()}`,
    sessionId,
    senderId: 'u001',
    senderName: '林同学',
    senderAvatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    content,
    type: 'text',
    createdAt: new Date().toLocaleString(),
  }
}