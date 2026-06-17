<template>
  <view class="page">
    <view class="header">
      <text class="h-title">消息通知</text>
    </view>

    <view v-if="loading" class="loading">加载中...</view>

    <view v-else-if="list.length" class="list">
      <view class="notice-card" v-for="n in list" :key="n.id" :class="{ unread: !n.read }">
        <view class="n-head">
          <text class="n-title">{{ n.title }}</text>
          <view class="dot" v-if="!n.read"></view>
        </view>
        <view class="n-content">{{ n.content }}</view>
        <view class="n-time" v-if="n.createdAt">{{ n.createdAt }}</view>
      </view>
    </view>

    <view v-else class="empty">
      <text class="empty-ic">🔔</text>
      <text class="empty-tx">暂无通知</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getNotifications, type AppNotification } from '@/api/notification'

const list = ref<AppNotification[]>([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    list.value = await getNotifications()
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onShow(load)
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f7f8fc;
  box-sizing: border-box;
}
.header {
  padding: 32rpx;
  background: #fff;
}
.h-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #2c3345;
}
.loading {
  text-align: center;
  color: #8a8f9c;
  padding-top: 160rpx;
}
.list {
  padding: 20rpx 24rpx;
}
.notice-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(44, 51, 69, 0.06);
  position: relative;
}
.notice-card.unread {
  border-left: 6rpx solid #4a90e2;
}
.n-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.n-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2c3345;
  flex: 1;
}
.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #e57474;
}
.n-content {
  font-size: 26rpx;
  color: #5a6478;
  line-height: 1.6;
  margin-top: 12rpx;
}
.n-time {
  font-size: 22rpx;
  color: #b8bfcc;
  margin-top: 16rpx;
}
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}
.empty-ic {
  font-size: 100rpx;
}
.empty-tx {
  font-size: 28rpx;
  color: #8a8f9c;
  margin-top: 24rpx;
}
</style>
