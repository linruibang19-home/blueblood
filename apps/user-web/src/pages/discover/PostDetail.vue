<template>
  <SubPageLayout title="帖子详情">
    <div class="post-detail" v-if="post">
      <!-- 作者信息 -->
      <div class="post-author">
        <UserAvatar :src="post.authorAvatar" :size="'md'" />
        <div class="author-info">
          <div class="author-name-row">
            <span class="author-name">{{ post.authorName }}</span>
            <LevelBadge :level="post.authorLevel" />
          </div>
          <span class="post-group">{{ post.groupName }} · {{ post.createdAt }}</span>
        </div>
      </div>

      <!-- 帖子正文 -->
      <div class="post-content">
        <h2 class="post-title">{{ post.title }}</h2>
        <div class="post-body">
          <p v-for="(line, i) in (post.content || '').split('\n')" :key="i">{{ line }}</p>
        </div>
      </div>

      <!-- 帖子统计 -->
      <div class="post-stats">
        <span class="stat-item"><van-icon name="eye-o" /> {{ post.views }}</span>
        <span class="stat-item"><van-icon name="comment-o" /> {{ post.comments }}</span>
        <span class="stat-item" :class="{ 'liked': post.liked }" @click="handleLike">
          <van-icon :name="post.liked ? 'good-job' : 'good-job-o'" /> {{ post.likes }}
        </span>
      </div>

      <van-divider />

      <!-- 回复列表 -->
      <div class="comments-section">
        <h3 class="comments-title">全部回复 ({{ comments.length }})</h3>
        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <UserAvatar :src="comment.authorAvatar" :size="'sm'" />
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-author">{{ comment.authorName }}</span>
                <span class="comment-time">{{ comment.createdAt }}</span>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
              <div class="comment-actions">
                <span class="like-btn" :class="{ 'liked': comment.liked }">
                  <van-icon :name="comment.liked ? 'good-job' : 'good-job-o'" /> {{ comment.likes }}
                </span>
                <span class="reply-btn">回复</span>
              </div>
              <!-- 子回复 -->
              <div v-if="comment.replies?.length" class="reply-list">
                <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                  <UserAvatar :src="reply.authorAvatar" :size="'sm'" />
                  <div class="reply-body">
                    <div class="reply-header">
                      <span class="reply-author">{{ reply.authorName }}</span>
                      <span class="reply-time">{{ reply.createdAt }}</span>
                    </div>
                    <p class="reply-content">{{ reply.content }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 固定回复栏 -->
      <div class="reply-bar">
        <van-field
          v-model="replyText"
          placeholder="写下你的回复..."
          @keyup.enter="handleReply"
        >
          <template #button>
            <van-button size="small" type="primary" @click="handleReply" :disabled="!replyText.trim()">发送</van-button>
          </template>
        </van-field>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import LevelBadge from '@/components/LevelBadge.vue'
import { getPostDetail, getCommentsByPost, togglePostLike, addComment } from '@/api/post'
import type { Post } from '@/types/post'
import type { PostComment } from '@/types/post'

const route = useRoute()
const post = ref<Post | null>(null)
const comments = ref<PostComment[]>([])
const replyText = ref('')

onMounted(async () => {
  const id = route.params.id as string
  post.value = await getPostDetail(id)
  comments.value = await getCommentsByPost(id)
})

async function handleLike() {
  if (!post.value) return
  post.value.liked = !post.value.liked
  post.value.likes += post.value.liked ? 1 : -1
  await togglePostLike(post.value.id)
}

async function handleReply() {
  if (!replyText.value.trim()) return
  if (!post.value) return
  const newComment = await addComment(post.value.id, replyText.value)
  comments.value.push(newComment)
  post.value.comments += 1
  replyText.value = ''
  showToast('回复成功')
}
</script>

<style scoped>
.post-detail {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 60px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-card);
}

.author-info {
  flex: 1;
}

.author-name-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: 2px;
}

.author-name {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--text-primary);
}

.post-group {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.post-content {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-top: 1px solid var(--divider);
}

.post-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
}

.post-body p {
  font-size: var(--font-size-md);
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: var(--spacing-md);
}

.post-stats {
  display: flex;
  gap: var(--spacing-2xl);
  padding: var(--spacing-lg);
  background: var(--bg-card);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

.stat-item.liked {
  color: var(--danger);
}

.comments-section {
  padding: var(--spacing-lg);
}

.comments-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.comment-item {
  display: flex;
  gap: var(--spacing-sm);
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.comment-author {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.comment-time {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.comment-content {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: var(--spacing-sm);
}

.comment-actions {
  display: flex;
  gap: var(--spacing-lg);
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.like-btn, .reply-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.like-btn.liked {
  color: var(--danger);
}

.reply-list {
  margin-top: var(--spacing-md);
  padding-left: var(--spacing-lg);
  border-left: 2px solid var(--border);
}

.reply-item {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.reply-body {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: 2px;
}

.reply-author {
  font-size: var(--font-size-xs);
  font-weight: 500;
  color: var(--text-primary);
}

.reply-time {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.reply-content {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.4;
}

.reply-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--bg-card);
  border-top: 1px solid var(--border);
  padding: var(--spacing-sm) var(--spacing-lg);
  max-width: var(--max-width);
  margin: 0 auto;
}
</style>