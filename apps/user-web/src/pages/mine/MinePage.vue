<template>
  <MobileTabLayout>
    <template #default>
      <div class="mine-page">
        <!-- 头部信息 -->
        <div class="profile-header">
          <div class="user-info">
            <div class="avatar-wrap" @click="goEditProfile">
              <UserAvatar :src="user?.avatar || ''" :size="'xl'" :verified="user?.verified" />
              <span class="avatar-edit-icon">
                <van-icon name="photograph" />
              </span>
            </div>
            <div class="user-details">
              <div class="name-row">
                <h2 class="user-name">{{ user?.name || '未登录' }}</h2>
                <LevelBadge :level="user?.level || 1" :level-name="user?.levelName || '新锐'" />
              </div>
              <p class="user-school">{{ user?.school || '' }} · {{ user?.major || '' }}</p>
              <div class="user-skills">
                <van-tag v-for="skill in (user?.skills || []).slice(0, 4)" :key="skill" type="primary">{{ skill }}</van-tag>
                <van-tag v-if="(user?.skills || []).length > 4">+{{ (user?.skills || []).length - 4 }}</van-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 数据统计 -->
        <div class="stats-section">
          <div class="stat-item" @click="goEarnings">
            <span class="stat-value">{{ user?.completedTasks || 0 }}</span>
            <span class="stat-label">已完成任务</span>
          </div>
          <div class="stat-divider" />
          <div class="stat-item">
            <span class="stat-value">{{ (user?.points || 0).toLocaleString() }}</span>
            <span class="stat-label">当前积分</span>
          </div>
          <div class="stat-divider" />
          <div class="stat-item">
            <span class="stat-value">{{ user?.creditScore || 0 }}</span>
            <span class="stat-label">信誉评分</span>
          </div>
        </div>

        <!-- 账户信息 -->
        <div class="wallet-section">
          <div class="wallet-card" @click="goEarnings">
            <div class="wallet-balance">
              <span class="balance-label">账户余额</span>
              <span class="balance-value">¥{{ (wallet?.balance || 0).toFixed(2) }}</span>
            </div>
            <div class="wallet-pending">
              <span class="pending-label">待结算</span>
              <span class="pending-value">¥{{ (wallet?.pendingAmount || 0).toFixed(2) }}</span>
            </div>
            <van-icon name="arrow" class="wallet-arrow" />
          </div>
        </div>

        <!-- 成长等级 -->
        <div class="level-section">
          <div class="level-header">
            <span class="level-title">成长等级</span>
            <span class="level-current">LV{{ user?.level || 1 }} {{ user?.levelName || '新锐' }}</span>
          </div>
          <div class="level-progress">
            <ProgressBar :percentage="levelProgress" :show-text="false" />
            <span class="level-next">再获 {{ nextLevelPoints }} 积分升级</span>
          </div>
        </div>

        <!-- 功能菜单 -->
        <div class="menu-section">
          <van-cell-group>
            <van-cell title="收益明细" is-link @click="goEarnings">
              <template #icon><van-icon name="cash-o" class="menu-icon" /></template>
            </van-cell>
            <van-cell title="消息通知" is-link @click="goNotifications">
              <template #icon><van-icon name="bell-o" class="menu-icon" /></template>
              <template #value>
                <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
              </template>
            </van-cell>
            <van-cell title="编辑个人资料" is-link @click="goEditProfile">
              <template #icon><van-icon name="user-o" class="menu-icon" /></template>
            </van-cell>
            <van-cell title="设置" is-link @click="goSettings">
              <template #icon><van-icon name="setting-o" class="menu-icon" /></template>
            </van-cell>
          </van-cell-group>
        </div>
      </div>
    </template>
  </MobileTabLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MobileTabLayout from '@/layouts/MobileTabLayout.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import LevelBadge from '@/components/LevelBadge.vue'
import ProgressBar from '@/components/ProgressBar.vue'
import { getCurrentUser } from '@/api/user'
import { getWalletSummary } from '@/api/wallet'
import { getUnreadCount } from '@/api/notification'
import type { User } from '@/types/user'
import type { WalletSummary } from '@/types/wallet'

const router = useRouter()
const user = ref<User | null>(null)
const wallet = ref<WalletSummary | null>(null)
const unreadCount = ref(0)

const nextLevelPoints = computed(() => {
  return ((user.value?.level || 1) + 1) * 1000 - (user.value?.points || 0) % 1000
})

const levelProgress = computed(() => {
  return (user.value?.points || 0) % 1000 / 10
})

onMounted(async () => {
  const [userData, walletData, unreadData] = await Promise.all([
    getCurrentUser(),
    getWalletSummary(),
    getUnreadCount()
  ])
  user.value = userData
  wallet.value = walletData
  unreadCount.value = unreadData
})

function goEarnings() {
  router.push('/mine/wallet')
}

function goNotifications() {
  router.push('/mine/notifications')
}

function goEditProfile() {
  router.push('/mine/profile/edit')
}

function goSettings() {
  router.push('/mine/settings')
}
</script>

<style scoped>
.mine-page {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 60px;
}

.profile-header {
  position: relative;
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.user-info {
  display: flex;
  gap: var(--spacing-lg);
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.avatar-edit-icon {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  background: var(--bg-secondary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--text-secondary);
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--spacing-xs);
}

.name-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.user-name {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
}

.user-school {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
  margin-bottom: var(--spacing-sm);
}

.user-skills {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.stats-section {
  display: flex;
  background: var(--bg-card);
  padding: var(--spacing-lg) 0;
  border-top: 1px solid var(--divider);
  border-bottom: 1px solid var(--divider);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-value {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.stat-divider {
  width: 1px;
  background: var(--divider);
}

.wallet-section {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.wallet-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  color: #fff;
}

.wallet-balance {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.balance-label {
  font-size: var(--font-size-xs);
  opacity: 0.8;
}

.balance-value {
  font-size: var(--font-size-2xl);
  font-weight: 700;
}

.wallet-pending {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 0 var(--spacing-xl);
  border-left: 1px solid rgba(255,255,255,0.3);
}

.pending-label {
  font-size: var(--font-size-xs);
  opacity: 0.8;
}

.pending-value {
  font-size: var(--font-size-md);
  font-weight: 600;
}

.wallet-arrow {
  font-size: var(--font-size-lg);
  opacity: 0.8;
}

.level-section {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.level-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.level-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
}

.level-current {
  font-size: var(--font-size-sm);
  color: var(--primary);
  font-weight: 500;
}

.level-progress {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.level-next {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  white-space: nowrap;
}

.menu-section {
  margin-top: var(--spacing-lg);
}

.menu-icon {
  margin-right: var(--spacing-sm);
  font-size: 18px;
  color: var(--text-tertiary);
}

.unread-badge {
  background: var(--danger);
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 500;
}
</style>