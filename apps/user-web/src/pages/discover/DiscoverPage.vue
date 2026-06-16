<template>
  <MobileTabLayout>
    <template #default>
      <div class="discover-page">
        <!-- 顶部标题 -->
        <div class="discover-header">
          <h1 class="discover-title">蓝血菁英</h1>
          <p class="discover-subtitle">发现 · 超级个体</p>
        </div>

        <!-- 搜索框 -->
        <div class="discover-search">
          <SearchBar placeholder="搜索小组、用户、帖子..." @click="handleSearchClick" />
        </div>

        <!-- 公告 Banner -->
        <div class="discover-banner">
          <BannerCard :banners="banners" />
        </div>

        <!-- 我的兴趣小组 -->
        <div class="section" v-if="myGroups.length">
          <div class="section-header">
            <span class="section-title">我的兴趣小组</span>
            <span class="section-more" @click="handleMoreGroups">更多 <van-icon name="arrow" /></span>
          </div>
          <div class="group-scroll">
            <div
              v-for="group in myGroups"
              :key="group.id"
              class="group-card"
              @click="goGroupDetail(group.id)"
            >
              <img :src="group.coverImage" class="group-cover" />
              <div class="group-info">
                <span class="group-name">{{ group.name }}</span>
                <span class="group-members">{{ group.memberCount }}人</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 热门任务 -->
        <div class="section" v-if="hotTasks.length">
          <div class="section-header">
            <span class="section-title">热门任务</span>
            <span class="section-more" @click="goTasks">查看更多 <van-icon name="arrow" /></span>
          </div>
          <div class="task-list">
            <div
              v-for="task in hotTasks"
              :key="task.id"
              class="task-card"
              @click="goTaskDetail(task.id)"
            >
              <div class="task-top">
                <span class="task-category">{{ task.category }}</span>
                <span class="task-reward">¥{{ task.reward }}</span>
              </div>
              <h4 class="task-title">{{ task.title }}</h4>
              <div class="task-tags">
                <van-tag v-for="skill in task.skills.slice(0, 2)" :key="skill">{{ skill }}</van-tag>
              </div>
              <div class="task-footer">
                <span class="task-level">LV{{ task.levelRequired }}</span>
                <span class="task-slots">剩余{{ task.slotsLeft }}名额</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 超级个体推荐 -->
        <div class="section" v-if="eliteUsers.length">
          <div class="section-header">
            <span class="section-title">超级个体</span>
            <span class="section-more">查看更多 <van-icon name="arrow" /></span>
          </div>
          <div class="elite-list">
            <div
              v-for="user in eliteUsers"
              :key="user.id"
              class="elite-card"
              @click="goEliteProfile(user.id)"
            >
              <UserAvatar :src="user.avatar" :size="'lg'" :verified="user.verified" />
              <div class="elite-info">
                <div class="elite-name-row">
                  <span class="elite-name">{{ user.name }}</span>
                  <LevelBadge :level="user.level" :level-name="user.levelName" />
                </div>
                <span class="elite-school">{{ user.school }} · {{ user.major }}</span>
                <div class="elite-skills">
                  <van-tag v-for="skill in user.skills.slice(0, 3)" :key="skill" type="primary">{{ skill }}</van-tag>
                </div>
              </div>
              <van-icon name="arrow" class="elite-arrow" />
            </div>
          </div>
        </div>
      </div>
    </template>
  </MobileTabLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MobileTabLayout from '@/layouts/MobileTabLayout.vue'
import SearchBar from '@/components/SearchBar.vue'
import BannerCard from '@/components/BannerCard.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import LevelBadge from '@/components/LevelBadge.vue'
import { getMyGroups } from '@/api/group'
import { getEliteUsers } from '@/api/user'
import type { Group } from '@/types/group'
import type { UserProfile } from '@/types/user'
import type { Task } from '@/types/task'
import { mockHotTasks } from '@/mock/tasks'

const router = useRouter()
const myGroups = ref<Group[]>([])
const eliteUsers = ref<UserProfile[]>([])
const hotTasks = ref<Task[]>(mockHotTasks)

const banners = [
  { title: 'AI Hackathon 2024', tag: '🔥 火热报名中', desc: '奖金池10万+，点击报名', bg: 'linear-gradient(135deg, #4A90E2, #5BC2C1)' },
  { title: '新版课程上线', tag: '📚 新课免费学', desc: 'Vue3 企业级实战课', bg: 'linear-gradient(135deg, #5BC2C1, #52C9A4)' },
]

onMounted(async () => {
  myGroups.value = await getMyGroups()
  eliteUsers.value = await getEliteUsers()
})

function handleSearchClick() {
  // 搜索功能后续实现
}

function goGroupDetail(id: string) {
  router.push(`/groups/${id}`)
}

function goTaskDetail(id: string) {
  router.push(`/tasks/detail/${id}`)
}

function goEliteProfile(id: string) {
  router.push(`/profiles/${id}`)
}

function goTasks() {
  router.push('/tasks')
}

function handleMoreGroups() {
  // 更多小组后续实现
}
</script>

<style scoped>
.discover-page {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 80px;
}

.discover-header {
  padding: var(--spacing-xl) var(--spacing-xl) var(--spacing-md);
  background: var(--bg-secondary);
}

.discover-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
}

.discover-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
  margin-top: 2px;
}

.discover-search {
  padding: 0 var(--spacing-lg) var(--spacing-md);
  background: var(--bg-secondary);
}

.discover-banner {
  padding: 0 var(--spacing-lg) var(--spacing-lg);
  background: var(--bg-secondary);
}

.section {
  padding: var(--spacing-xl) var(--spacing-lg);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.section-more {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
}

/* 小组横向滚动 */
.group-scroll {
  display: flex;
  gap: var(--spacing-md);
  overflow-x: auto;
  padding-bottom: var(--spacing-sm);
  -webkit-overflow-scrolling: touch;
}

.group-scroll::-webkit-scrollbar { display: none; }

.group-card {
  flex-shrink: 0;
  width: 120px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.group-cover {
  width: 100%;
  height: 80px;
  object-fit: cover;
}

.group-info {
  padding: var(--spacing-sm);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.group-name {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-members {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

/* 任务卡片 */
.task-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.task-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.task-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.task-category {
  font-size: var(--font-size-xs);
  color: var(--primary);
  background: var(--primary-alpha);
  padding: 2px 8px;
  border-radius: 4px;
}

.task-reward {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--warning);
}

.task-title {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.task-tags {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
  margin-bottom: var(--spacing-sm);
}

.task-footer {
  display: flex;
  justify-content: space-between;
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.task-level {
  color: var(--accent);
}

/* 超级个体 */
.elite-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.elite-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.elite-info {
  flex: 1;
  min-width: 0;
}

.elite-name-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: 2px;
}

.elite-name {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--text-primary);
}

.elite-school {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  display: block;
  margin-bottom: var(--spacing-sm);
}

.elite-skills {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.elite-arrow {
  color: var(--text-tertiary);
  font-size: var(--font-size-md);
}
</style>