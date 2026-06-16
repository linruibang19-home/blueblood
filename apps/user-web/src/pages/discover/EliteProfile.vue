<template>
  <SubPageLayout title="超级个体">
    <div class="elite-profile" v-if="user">
      <!-- 头部信息 -->
      <div class="profile-header">
        <div class="avatar-section">
          <UserAvatar :src="user.avatar" :size="'xl'" :verified="user.verified" />
          <h2 class="user-name">{{ user.name }}</h2>
          <div class="name-row">
            <LevelBadge :level="user.level" :level-name="user.levelName" />
            <span v-if="user.verified" class="verified-tag">
              <van-icon name="passed" /> 已认证
            </span>
          </div>
        </div>

        <div class="action-buttons">
          <van-button :type="isConnected ? 'default' : 'primary'" @click="handleConnect">
            {{ isConnected ? '已连接' : '建立连接' }}
          </van-button>
          <van-button plain @click="handleMessage">发消息</van-button>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="info-section">
        <div class="info-row">
          <van-icon name="location-o" />
          <span>{{ user.school }} · {{ user.major }}</span>
        </div>
        <div class="info-row" v-if="user.github">
          <van-icon name="link-o" />
          <a :href="user.github" target="_blank">{{ user.github }}</a>
        </div>
      </div>

      <!-- 个人简介 -->
      <div class="bio-section">
        <h3 class="section-title">个人简介</h3>
        <p class="bio-text">{{ user.bio }}</p>
      </div>

      <!-- 技能标签 -->
      <div class="skills-section">
        <h3 class="section-title">技能标签</h3>
        <div class="skills-list">
          <SkillTag v-for="skill in user.skills" :key="skill" :text="skill" />
        </div>
      </div>

      <!-- 成就徽章 -->
      <div class="badges-section" v-if="user.badges?.length">
        <h3 class="section-title">成就徽章</h3>
        <div class="badges-list">
          <div v-for="badge in user.badges" :key="badge.id" class="badge-item">
            <span class="badge-icon">{{ badge.icon }}</span>
            <div class="badge-info">
              <span class="badge-name">{{ badge.name }}</span>
              <span class="badge-desc">{{ badge.description }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 技能雷达 -->
      <div class="radar-section" v-if="user.radarData">
        <h3 class="section-title">能力雷达</h3>
        <div class="radar-chart">
          <div v-for="(label, i) in user.radarData.labels" :key="label" class="radar-item">
            <div class="radar-label">{{ label }}</div>
            <div class="radar-bar">
              <div class="radar-fill" :style="{ width: user.radarData.values[i] + '%' }" />
            </div>
            <span class="radar-value">{{ user.radarData.values[i] }}</span>
          </div>
        </div>
      </div>

      <!-- 数据统计 -->
      <div class="stats-section">
        <div class="stat-item">
          <span class="stat-num">{{ user.connections || 0 }}</span>
          <span class="stat-label">连接</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ user.followers || 0 }}</span>
          <span class="stat-label">粉丝</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ user.following || 0 }}</span>
          <span class="stat-label">关注</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ user.completedTasks }}</span>
          <span class="stat-label">已完成任务</span>
        </div>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import LevelBadge from '@/components/LevelBadge.vue'
import SkillTag from '@/components/SkillTag.vue'
import { getUserProfile, getConnectionStatus, toggleConnection } from '@/api/user'
import type { UserProfile } from '@/types/user'

const route = useRoute()
const router = useRouter()
const user = ref<UserProfile | null>(null)
const isConnected = ref(false)

onMounted(async () => {
  const id = route.params.id as string
  user.value = await getUserProfile(id)
  isConnected.value = await getConnectionStatus(id)
})

async function handleConnect() {
  if (!user.value) return
  isConnected.value = await toggleConnection(user.value.id)
  showToast(isConnected.value ? '连接成功' : '已取消连接')
}

function handleMessage() {
  if (!user.value) return
  router.push(`/chats/${user.value.id}`)
}
</script>

<style scoped>
.elite-profile {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 40px;
}

.profile-header {
  background: var(--bg-card);
  padding: var(--spacing-xl);
  text-align: center;
  border-bottom: 1px solid var(--divider);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.user-name {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
}

.name-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.verified-tag {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: var(--font-size-xs);
  color: var(--primary);
}

.action-buttons {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
}

.action-buttons .van-button {
  min-width: 100px;
}

.info-section {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.info-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-sm);
}

.info-row a {
  color: var(--primary);
}

.bio-section,
.skills-section,
.badges-section,
.radar-section {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.bio-text {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.6;
}

.skills-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.badges-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.badge-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.badge-icon {
  font-size: 28px;
}

.badge-info {
  display: flex;
  flex-direction: column;
}

.badge-name {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.badge-desc {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.radar-chart {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.radar-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.radar-label {
  width: 80px;
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.radar-bar {
  flex: 1;
  height: 8px;
  background: var(--bg-tertiary);
  border-radius: 4px;
  overflow: hidden;
}

.radar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), var(--accent));
  border-radius: 4px;
  transition: width 0.5s ease;
}

.radar-value {
  width: 30px;
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  text-align: right;
}

.stats-section {
  display: flex;
  justify-content: space-around;
  padding: var(--spacing-xl);
  background: var(--bg-card);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-num {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}
</style>