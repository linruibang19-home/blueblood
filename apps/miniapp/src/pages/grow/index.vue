<template>
  <view class="page">
    <view class="header">
      <text class="h-title">成长中心</text>
      <text class="h-sub">学习 · 实战 · 进阶</text>
    </view>

    <view v-if="loading" class="loading">加载中...</view>

    <view v-else-if="courses.length" class="list">
      <view class="course-card" v-for="c in courses" :key="c.id" @tap="goDetail(c.id)">
        <image class="cover" :src="c.coverImage || defaultCover" mode="aspectFill" />
        <view class="c-body">
          <view class="c-title">{{ c.title }}</view>
          <view class="c-sub" v-if="c.subtitle">{{ c.subtitle }}</view>
          <view class="c-meta">
            <text class="instructor">👨‍🏫 {{ c.instructor || '蓝血导师' }}</text>
            <text class="students" v-if="c.students">{{ c.students }}人在学</text>
          </view>
          <view class="c-progress-row" v-if="c.totalChapters">
            <view class="c-progress">
              <view class="c-progress-bar" :style="{ width: c.progress + '%' }"></view>
            </view>
            <text class="c-progress-num">{{ c.completedChapters }}/{{ c.totalChapters }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="empty">
      <text class="empty-ic">📚</text>
      <text class="empty-tx">暂无课程</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCourseList, type Course } from '@/api/course'

const courses = ref<Course[]>([])
const loading = ref(true)
const defaultCover = 'https://cdn.uviewui.com/uview/album/2.jpg'

async function load() {
  loading.value = true
  try {
    courses.value = await getCourseList()
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goDetail(id: string) {
  uni.showToast({ title: '课程详情待接入', icon: 'none' })
}

onShow(load)
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f7f8fc;
  box-sizing: border-box;
  padding-bottom: 40rpx;
}
.header {
  padding: 40rpx 32rpx 24rpx;
  background: #fff;
}
.h-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #2c3345;
  display: block;
}
.h-sub {
  font-size: 26rpx;
  color: #8a8f9c;
  margin-top: 8rpx;
  display: block;
}
.loading {
  text-align: center;
  color: #8a8f9c;
  padding-top: 160rpx;
}
.list {
  padding: 20rpx 24rpx;
}
.course-card {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(44, 51, 69, 0.06);
}
.cover {
  width: 200rpx;
  height: 150rpx;
  border-radius: 12rpx;
  background: #f0f2f7;
  flex-shrink: 0;
}
.c-body {
  flex: 1;
  margin-left: 24rpx;
  display: flex;
  flex-direction: column;
}
.c-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2c3345;
}
.c-sub {
  font-size: 24rpx;
  color: #8a8f9c;
  margin-top: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.c-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 12rpx;
}
.instructor {
  font-size: 24rpx;
  color: #5a6478;
}
.students {
  font-size: 24rpx;
  color: #8a8f9c;
}
.c-progress-row {
  display: flex;
  align-items: center;
  margin-top: auto;
  padding-top: 12rpx;
}
.c-progress {
  flex: 1;
  height: 10rpx;
  background: #f0f2f7;
  border-radius: 5rpx;
  overflow: hidden;
  margin-right: 12rpx;
}
.c-progress-bar {
  height: 100%;
  background: #4a90e2;
  border-radius: 5rpx;
}
.c-progress-num {
  font-size: 22rpx;
  color: #4a90e2;
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
