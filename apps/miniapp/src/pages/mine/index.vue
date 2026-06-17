<template>
  <view class="page">
    <view class="title">我的</view>

    <view class="card">
      <view class="label">当前用户</view>
      <view class="value" v-if="userStore.userInfo?.username">
        {{ userStore.userInfo.nickname || userStore.userInfo.username }}
      </view>
      <view class="value muted" v-else>未登录</view>
    </view>

    <button class="btn primary" @tap="onLogin" v-if="!userStore.token">测试登录 lin / 123456</button>
    <button class="btn" @tap="onLogout" v-else>退出登录</button>

    <view class="api-tip" v-if="apiText">{{ apiText }}</view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const apiText = ref('')

async function onLogin() {
  try {
    const res = await userStore.login('lin', '123456')
    apiText.value = `登录成功，token 已存储；用户名 ${res.username}`
    await userStore.fetchMe()
  } catch (e: any) {
    apiText.value = `登录失败：${e.message || e}`
  }
}

async function onLogout() {
  await userStore.logout()
  apiText.value = '已退出登录'
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  padding: 32rpx;
  background: #f7f8fc;
  box-sizing: border-box;
}
.title {
  font-size: 44rpx;
  font-weight: 600;
  color: #2c3345;
  margin-bottom: 32rpx;
}
.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(44, 51, 69, 0.06);
}
.label {
  font-size: 24rpx;
  color: #909bad;
  margin-bottom: 12rpx;
}
.value {
  font-size: 32rpx;
  color: #2c3345;
  font-weight: 600;
}
.value.muted {
  color: #b8bfcc;
  font-weight: 400;
}
.btn {
  margin-top: 16rpx;
  border-radius: 16rpx;
  background: #f0f2f7;
  color: #5a6478;
}
.btn.primary {
  background: #4a90e2;
  color: #fff;
}
.api-tip {
  margin-top: 32rpx;
  font-size: 26rpx;
  color: #5a6478;
}
</style>
