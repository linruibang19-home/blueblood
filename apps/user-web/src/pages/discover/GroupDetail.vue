<template>
  <SubPageLayout title="小组详情">
    <div class="group-detail" v-if="group">
      <!-- 小组头部 -->
      <div class="group-header">
        <img :src="group.coverImage" class="group-cover" />
        <div class="group-overlay">
          <div class="group-info">
            <h2 class="group-name">{{ group.name }}</h2>
            <div class="group-meta">
              <span><van-icon name="friends-o" /> {{ group.memberCount }}人</span>
              <span><van-icon name="star-o" /> {{ group.leaderName }}</span>
            </div>
          </div>
          <van-button
            :type="group.joined ? 'default' : 'primary'"
            size="small"
            :disabled="group.joined"
            @click="handleJoin"
          >
            {{ group.joined ? '已加入' : '加入小组' }}
          </van-button>
        </div>
      </div>

      <!-- 小组简介 -->
      <div class="group-desc">
        <p>{{ group.description }}</p>
        <div class="group-tags">
          <van-tag v-for="tag in group.tags" :key="tag" type="primary">{{ tag }}</van-tag>
        </div>
      </div>

      <!-- BBS Tab -->
      <van-tabs v-model:active="activeTab" sticky offset-top="46">
        <van-tab title="话题" name="话题">
          <div class="post-list">
            <div v-for="post in filteredPosts" :key="post.id" class="post-card" @click="goPost(post.id)">
              <div class="post-author">
                <UserAvatar :src="post.authorAvatar" :size="'sm'" />
                <div class="author-info">
                  <span class="author-name">{{ post.authorName }}</span>
                  <span class="post-time">{{ post.createdAt }}</span>
                </div>
                <LevelBadge :level="post.authorLevel" />
              </div>
              <h4 class="post-title">{{ post.title }}</h4>
              <p class="post-content text-ellipsis-2">{{ post.content }}</p>
              <div class="post-footer">
                <span><van-icon name="good-job-o" /> {{ post.likes }}</span>
                <span><van-icon name="comment-o" /> {{ post.comments }}</span>
                <span><van-icon name="eye-o" /> {{ post.views }}</span>
              </div>
            </div>
          </div>
        </van-tab>
        <van-tab title="任务" name="任务">
          <div class="post-list">
            <div v-for="post in filteredPosts" :key="post.id" class="post-card" @click="goPost(post.id)">
              <div class="post-author">
                <UserAvatar :src="post.authorAvatar" :size="'sm'" />
                <div class="author-info">
                  <span class="author-name">{{ post.authorName }}</span>
                  <span class="post-time">{{ post.createdAt }}</span>
                </div>
                <LevelBadge :level="post.authorLevel" />
              </div>
              <h4 class="post-title">{{ post.title }}</h4>
              <p class="post-content text-ellipsis-2">{{ post.content }}</p>
              <div class="post-footer">
                <span><van-icon name="good-job-o" /> {{ post.likes }}</span>
                <span><van-icon name="comment-o" /> {{ post.comments }}</span>
              </div>
            </div>
          </div>
        </van-tab>
        <van-tab title="经验分享" name="经验分享">
          <div class="post-list">
            <div v-for="post in filteredPosts" :key="post.id" class="post-card" @click="goPost(post.id)">
              <div class="post-author">
                <UserAvatar :src="post.authorAvatar" :size="'sm'" />
                <div class="author-info">
                  <span class="author-name">{{ post.authorName }}</span>
                  <span class="post-time">{{ post.createdAt }}</span>
                </div>
                <LevelBadge :level="post.authorLevel" />
              </div>
              <h4 class="post-title">{{ post.title }}</h4>
              <p class="post-content text-ellipsis-2">{{ post.content }}</p>
              <div class="post-footer">
                <span><van-icon name="good-job-o" /> {{ post.likes }}</span>
                <span><van-icon name="comment-o" /> {{ post.comments }}</span>
              </div>
            </div>
          </div>
        </van-tab>
        <van-tab title="活动" name="活动">
          <div class="empty-state">
            <van-empty description="暂无活动" />
          </div>
        </van-tab>
      </van-tabs>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import LevelBadge from '@/components/LevelBadge.vue'
import { getGroupDetail, joinGroup } from '@/api/group'
import { getPostsByGroup } from '@/api/post'
import type { Group } from '@/types/group'
import type { Post } from '@/types/post'

const route = useRoute()
const router = useRouter()
const group = ref<Group | null>(null)
const posts = ref<Post[]>([])
const activeTab = ref('话题')

onMounted(async () => {
  const id = route.params.id as string
  group.value = await getGroupDetail(id)
  posts.value = await getPostsByGroup(id)
})

const filteredPosts = computed(() => {
  if (activeTab.value === '话题') return posts.value.filter(p => p.tag === '话题' || p.tag === '活动')
  return posts.value.filter(p => p.tag === activeTab.value)
})

async function handleJoin() {
  if (!group.value) return
  await joinGroup(group.value.id)
  group.value.joined = true
  group.value.memberCount += 1
  showToast('加入成功')
}

function goPost(postId: string) {
  router.push(`/posts/${postId}`)
}
</script>

<style scoped>
.group-detail {
  min-height: 100vh;
  background: var(--bg-primary);
}

.group-header {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.group-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.group-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--spacing-lg);
  background: linear-gradient(transparent, rgba(0,0,0,0.6));
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  color: #fff;
}

.group-name {
  font-size: var(--font-size-xl);
  font-weight: 700;
  margin-bottom: var(--spacing-xs);
}

.group-meta {
  display: flex;
  gap: var(--spacing-lg);
  font-size: var(--font-size-sm);
  opacity: 0.9;
}

.group-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.group-desc {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.group-desc p {
  font-size: var(--font-size-md);
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: var(--spacing-md);
}

.group-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.post-list {
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.post-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.post-author {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.author-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.post-time {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.post-title {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.post-content {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: var(--spacing-sm);
}

.post-footer {
  display: flex;
  gap: var(--spacing-lg);
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.post-footer span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-state {
  padding: 60px 0;
}
</style>