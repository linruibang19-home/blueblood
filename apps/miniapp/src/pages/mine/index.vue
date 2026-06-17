<template>
  <view class="page">
    <!-- 未登录 -->
    <view v-if="!loggedIn" class="login-wrap">
      <view class="login-card">
        <image class="logo" src="/static/logo.png" mode="aspectFit" />
        <view class="login-title">蓝血菁英</view>
        <view class="login-sub">登录后开启你的成长之旅</view>
        <button class="btn primary" :loading="logining" @tap="onWxLogin">微信一键登录</button>
        <button class="btn outline" :loading="logining" @tap="onLogin">测试登录 lin / 123456</button>
        <view class="api-tip" v-if="apiText">{{ apiText }}</view>
      </view>
    </view>

    <!-- 已登录 -->
    <view v-else>
      <!-- 用户信息卡 -->
      <view class="profile-card">
        <image class="avatar" :src="user?.avatar || defaultAvatar" mode="aspectFill" @tap="onChangeAvatar" />
        <view class="info">
          <view class="name-row">
            <text class="name">{{ user?.name || '蓝血用户' }}</text>
            <text class="lv-tag" v-if="user?.levelName">{{ user.levelName }}</text>
          </view>
          <view class="sub">{{ user?.school || '蓝血菁英平台' }}<text v-if="user?.major"> · {{ user.major }}</text></view>
        </view>
      </view>

      <!-- 数据统计 -->
      <view class="stat-card">
        <view class="stat-item">
          <text class="stat-num">{{ user?.completedTasks ?? 0 }}</text>
          <text class="stat-label">完成任务</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ user?.points ?? 0 }}</text>
          <text class="stat-label">积分</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ user?.creditScore ?? 0 }}</text>
          <text class="stat-label">信誉</text>
        </view>
      </view>

      <!-- 功能入口 -->
      <view class="menu-card">
        <view class="menu-item" @tap="go('/pages/mine/wallet/index')">
          <text class="menu-ic">💰</text>
          <text class="menu-tx">我的收益</text>
          <text class="menu-extra">¥{{ wallet.balance.toFixed(2) }}</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-item" @tap="go('/pages/mine/notifications/index')">
          <text class="menu-ic">🔔</text>
          <text class="menu-tx">消息通知</text>
          <text class="badge" v-if="unread > 0">{{ unread > 99 ? '99+' : unread }}</text>
          <text class="arrow">›</text>
        </view>
        <view class="menu-item" @tap="goOrders">
          <text class="menu-ic">📋</text>
          <text class="menu-tx">我的任务</text>
          <text class="arrow">›</text>
        </view>
      </view>

      <button class="btn logout" @tap="onLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { getCurrentUser, updateProfile, type User } from '@/api/user'
import { getWalletSummary, type WalletSummary } from '@/api/wallet'
import { getUnreadCount } from '@/api/notification'
import { uploadFile } from '@/utils/upload'

const userStore = useUserStore()
const defaultAvatar = 'https://cdn.uviewui.com/uview/album/1.jpg'

const user = ref<User | null>(null)
const wallet = ref<WalletSummary>({ balance: 0, pendingAmount: 0, withdrawnAmount: 0, totalEarned: 0 })
const unread = ref(0)
const logining = ref(false)
const apiText = ref('')

const loggedIn = computed(() => !!userStore.token)

async function loadAll() {
  if (!loggedIn.value) return
  try {
    const [u, w, n] = await Promise.all([
      getCurrentUser(),
      getWalletSummary().catch(() => wallet.value),
      getUnreadCount().catch(() => 0),
    ])
    user.value = u
    wallet.value = w
    unread.value = n
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  }
}

/** 微信一键登录：uni.login 拿 code → wxLogin → 存 token → 刷新 */
async function onWxLogin() {
  logining.value = true
  try {
    const loginRes: any = await uni.login({ provider: 'weixin' })
    const code = loginRes?.code
    if (!code) throw new Error('未获取到微信 code')
    await userStore.wxLogin(code)
    await userStore.fetchMe()
    await loadAll()
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (e: any) {
    apiText.value = `微信登录失败：${e.message || e}`
    uni.showToast({ title: '微信登录失败，可使用测试登录', icon: 'none' })
  } finally {
    logining.value = false
  }
}

/** 兜底测试登录 */
async function onLogin() {
  logining.value = true
  try {
    await userStore.login('lin', '123456')
    await userStore.fetchMe()
    await loadAll()
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (e: any) {
    apiText.value = `登录失败：${e.message || e}`
    uni.showToast({ title: '登录失败', icon: 'none' })
  } finally {
    logining.value = false
  }
}

/** 点击头像 → 选图 → 上传 → 更新资料 */
async function onChangeAvatar() {
  try {
    const choose: any = await uni.chooseImage({ count: 1, sizeType: ['compressed', 'original'] })
    const temp = (choose?.tempFilePaths?.[0] || choose?.tempFiles?.[0]?.path) as string | undefined
    if (!temp) return
    uni.showLoading({ title: '上传中...' })
    const up = await uploadFile({ filePath: temp, bizType: 'avatar' })
    await updateProfile({ avatar: up.url })
    if (user.value) user.value.avatar = up.url
    uni.hideLoading()
    uni.showToast({ title: '头像已更新', icon: 'success' })
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '更新头像失败', icon: 'none' })
  }
}

async function onLogout() {
  await userStore.logout()
  user.value = null
  unread.value = 0
}

function go(url: string) {
  uni.navigateTo({ url })
}

function goOrders() {
  uni.switchTab({ url: '/pages/task/index' })
}

onShow(() => {
  loadAll()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  padding: 32rpx;
  background: #f7f8fc;
  box-sizing: border-box;
}

/* 登录 */
.login-wrap {
  padding-top: 160rpx;
}
.login-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 64rpx 40rpx;
  text-align: center;
  box-shadow: 0 4rpx 16rpx rgba(44, 51, 69, 0.06);
}
.logo {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 24rpx;
}
.login-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #2c3345;
}
.login-sub {
  font-size: 26rpx;
  color: #8a8f9c;
  margin-top: 12rpx;
  margin-bottom: 40rpx;
}
.api-tip {
  margin-top: 24rpx;
  font-size: 24rpx;
  color: #e57474;
}

.btn {
  border-radius: 16rpx;
  font-size: 30rpx;
}
.btn.primary {
  background: #4a90e2;
  color: #fff;
}
.btn + .btn {
  margin-top: 20rpx;
}
.btn.outline {
  background: #fff;
  color: #4a90e2;
  border: 1rpx solid #4a90e2;
}
.btn.logout {
  margin-top: 40rpx;
  background: #fff;
  color: #5a6478;
  border: 1rpx solid #e8ebf0;
}

/* 用户卡 */
.profile-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #4a90e2 0%, #7ab3f0 100%);
  border-radius: 24rpx;
  padding: 36rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 6rpx 18rpx rgba(74, 144, 226, 0.25);
}
.avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
  background: #fff;
}
.info {
  margin-left: 28rpx;
  flex: 1;
}
.name-row {
  display: flex;
  align-items: center;
}
.name {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
}
.lv-tag {
  margin-left: 16rpx;
  font-size: 22rpx;
  color: #4a90e2;
  background: #fff;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}
.sub {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 10rpx;
}

/* 统计 */
.stat-card {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  padding: 36rpx 0;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(44, 51, 69, 0.06);
}
.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-right: 1rpx solid #eef0f5;
}
.stat-item:last-child {
  border-right: none;
}
.stat-num {
  font-size: 40rpx;
  font-weight: 700;
  color: #2c3345;
}
.stat-label {
  font-size: 24rpx;
  color: #8a8f9c;
  margin-top: 8rpx;
}

/* 菜单 */
.menu-card {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(44, 51, 69, 0.06);
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f0f2f7;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-ic {
  font-size: 36rpx;
  margin-right: 20rpx;
}
.menu-tx {
  flex: 1;
  font-size: 30rpx;
  color: #2c3345;
}
.menu-extra {
  font-size: 28rpx;
  color: #4a90e2;
  font-weight: 600;
  margin-right: 12rpx;
}
.badge {
  min-width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  padding: 0 10rpx;
  border-radius: 18rpx;
  background: #e57474;
  color: #fff;
  font-size: 22rpx;
  text-align: center;
  margin-right: 12rpx;
}
.arrow {
  font-size: 36rpx;
  color: #c0c6d0;
}
</style>
