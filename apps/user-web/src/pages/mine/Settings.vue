<template>
  <SubPageLayout title="设置">
    <div class="settings-page">
      <!-- 菜单列表 -->
      <van-cell-group>
        <van-cell title="个人信息" is-link @click="goEditProfile">
          <template #icon><van-icon name="user-o" class="menu-icon" /></template>
        </van-cell>
        <van-cell title="账号安全" is-link value="未设置">
          <template #icon><van-icon name="shield-o" class="menu-icon" /></template>
        </van-cell>
      </van-cell-group>

      <!-- 通知设置 -->
      <van-cell-group class="notification-group">
        <van-cell title="任务通知">
          <template #icon><van-icon name="bell-o" class="menu-icon" /></template>
          <template #label><span class="setting-desc">接收任务进度、审核等通知</span></template>
          <van-switch v-model="settings.taskNotification" size="20" @change="handleTaskNotification" />
        </van-cell>
        <van-cell title="社区通知">
          <template #icon><van-icon name="chat-o" class="menu-icon" /></template>
          <template #label><span class="setting-desc">接收帖子、评论等通知</span></template>
          <van-switch v-model="settings.bbsNotification" size="20" @change="handleBbsNotification" />
        </van-cell>
      </van-cell-group>

      <!-- 退出登录 -->
      <div class="logout-section">
        <van-button type="default" size="large" round @click="handleLogout">
          退出登录
        </van-button>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import { logout as apiLogout } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const settings = ref({
  taskNotification: true,
  bbsNotification: true,
})

function goEditProfile() {
  router.push('/mine/profile/edit')
}

function handleTaskNotification(val: boolean) {
  showToast(val ? '任务通知已开启' : '任务通知已关闭')
}

function handleBbsNotification(val: boolean) {
  showToast(val ? '社区通知已开启' : '社区通知已关闭')
}

async function handleLogout() {
  // 1. 通知后端（无状态：丢弃令牌；后续可接 Redis 黑名单）
  await apiLogout()
  // 2. 清本地 token + 用户态
  localStorage.removeItem('token')
  userStore.logout()
  showToast('已退出登录')
  // 3. 回登录页，避免留在需登录页触发 401 空白
  router.replace('/login')
}
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: var(--bg-primary);
}

.menu-icon {
  margin-right: var(--spacing-sm);
  font-size: 18px;
  color: var(--text-tertiary);
}

.notification-group {
  margin-top: var(--spacing-lg);
}

.setting-desc {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.logout-section {
  margin-top: var(--spacing-2xl);
  padding: 0 var(--spacing-xl);
}
</style>