<template>
  <view class="page">
    <view class="header">
      <text class="h-title">发现</text>
      <text class="h-sub">小组 · 热门 · 精英</text>
    </view>

    <!-- 我的小组（横滑） -->
    <view class="section">
      <view class="sec-head">
        <text class="sec-title">我的小组</text>
        <text class="sec-more" @tap="moreGroups">全部 ›</text>
      </view>
      <scroll-view class="group-scroll" scroll-x show-scrollbar="false" v-if="groups.length">
        <view class="group-item" v-for="g in groups" :key="g.id">
          <image class="g-avatar" :src="g.avatar || defaultCover" mode="aspectFill" />
          <text class="g-name">{{ g.name }}</text>
          <text class="g-count">{{ g.memberCount }}人</text>
        </view>
      </scroll-view>
      <view v-else class="empty-inline">还没有加入小组</view>
    </view>

    <!-- 热门任务 -->
    <view class="section">
      <view class="sec-head">
        <text class="sec-title">热门任务</text>
        <text class="sec-more" @tap="goHall">去大厅 ›</text>
      </view>
      <view class="hot-list" v-if="hotTasks.length">
        <view class="hot-card" v-for="t in hotTasks" :key="t.id" @tap="goTask(t.id)">
          <view class="hot-left">
            <text class="hot-title">{{ t.title }}</text>
            <text class="hot-meta">{{ t.category }} · ¥{{ t.reward }}</text>
          </view>
          <text class="hot-arrow">›</text>
        </view>
      </view>
      <view v-else class="empty-inline">暂无热门任务</view>
    </view>

    <!-- 超级个体 -->
    <view class="section">
      <view class="sec-head">
        <text class="sec-title">超级个体</text>
      </view>
      <view class="elite-list" v-if="elites.length">
        <view class="elite-item" v-for="u in elites" :key="u.id">
          <image class="e-avatar" :src="u.avatar || defaultCover" mode="aspectFill" />
          <view class="e-info">
            <text class="e-name">{{ u.name }}</text>
            <text class="e-tag">{{ u.levelName || ('Lv.' + u.level) }} · 完成任务 {{ u.completedTasks }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-inline">暂无精英数据</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyGroups, type Group } from '@/api/group'
import { getTaskList, type Task } from '@/api/task'
import { getEliteUsers, type User } from '@/api/user'

const groups = ref<Group[]>([])
const hotTasks = ref<Task[]>([])
const elites = ref<User[]>([])
const defaultCover = 'https://cdn.uviewui.com/uview/album/2.jpg'

async function load() {
  const [g, t, e] = await Promise.all([
    getMyGroups().catch(() => [] as Group[]),
    getTaskList({ page: 1, pageSize: 5 }).catch(() => [] as Task[]),
    getEliteUsers().catch(() => [] as User[]),
  ])
  groups.value = g.slice(0, 8)
  hotTasks.value = t.slice(0, 5)
  elites.value = e.slice(0, 6)
}

function goTask(id: string) {
  uni.navigateTo({ url: `/pages/task/detail?id=${id}` })
}
function goHall() {
  uni.switchTab({ url: '/pages/task/index' })
}
function moreGroups() {
  uni.showToast({ title: '小组广场待接入', icon: 'none' })
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

.section {
  margin-top: 20rpx;
  background: #fff;
  padding: 28rpx 32rpx;
}
.sec-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}
.sec-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #2c3345;
}
.sec-more {
  font-size: 24rpx;
  color: #4a90e2;
}
.empty-inline {
  font-size: 26rpx;
  color: #b8bfcc;
  text-align: center;
  padding: 24rpx 0;
}

/* 小组横滑 */
.group-scroll {
  white-space: nowrap;
}
.group-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  width: 160rpx;
  margin-right: 20rpx;
}
.g-avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 20rpx;
  background: #f0f2f7;
}
.g-name {
  font-size: 24rpx;
  color: #2c3345;
  margin-top: 12rpx;
  max-width: 160rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.g-count {
  font-size: 22rpx;
  color: #8a8f9c;
  margin-top: 4rpx;
}

/* 热门任务 */
.hot-card {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f2f7;
}
.hot-card:last-child {
  border-bottom: none;
}
.hot-left {
  flex: 1;
}
.hot-title {
  font-size: 28rpx;
  color: #2c3345;
  font-weight: 500;
  display: block;
}
.hot-meta {
  font-size: 24rpx;
  color: #8a8f9c;
  margin-top: 8rpx;
  display: block;
}
.hot-arrow {
  font-size: 32rpx;
  color: #c0c6d0;
}

/* 精英 */
.elite-list {
  display: flex;
  flex-wrap: wrap;
}
.elite-item {
  width: 50%;
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  box-sizing: border-box;
}
.e-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #f0f2f7;
}
.e-info {
  margin-left: 16rpx;
  flex: 1;
  overflow: hidden;
}
.e-name {
  font-size: 26rpx;
  color: #2c3345;
  font-weight: 500;
  display: block;
}
.e-tag {
  font-size: 22rpx;
  color: #8a8f9c;
  margin-top: 4rpx;
  display: block;
}
</style>
