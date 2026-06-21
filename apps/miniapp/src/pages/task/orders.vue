<template>
  <view class="page">
    <view class="header-bar">
      <text class="h-title">我的任务</text>
    </view>

    <view v-if="loading" class="loading">加载中...</view>

    <view v-else-if="orders.length" class="list">
      <view class="order-card" v-for="o in orders" :key="o.id" @tap="goDetail(o.id)">
        <view class="o-head">
          <text class="o-title">{{ o.taskTitle }}</text>
          <text class="o-status" :class="statusClass(o.status)">{{ statusText(o.status) }}</text>
        </view>
        <view class="o-progress-row">
          <view class="o-progress">
            <view class="o-progress-bar" :style="{ width: o.progress + '%' }"></view>
          </view>
          <text class="o-progress-num">{{ o.progress }}%</text>
        </view>
        <view class="o-foot">
          <text class="o-meta">里程碑 {{ o.milestones.length }} 个</text>
          <text class="o-arrow">查看详情 ›</text>
        </view>
      </view>
    </view>

    <view v-else class="empty">
      <text class="empty-ic">📋</text>
      <text class="empty-tx">暂无接单任务</text>
      <button class="btn-go" @tap="goHall">去任务大厅</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyTaskOrders, type TaskOrder } from '@/api/task'

const orders = ref<TaskOrder[]>([])
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    orders.value = await getMyTaskOrders()
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function statusText(s: string) {
  const map: Record<string, string> = {
    applied: '已报名',
    accepted: '已接单',
    in_progress: '进行中',
    wait_acceptance: '待验收',
    passed: '验收通过',
    rejected: '已驳回',
    settling: '结算中',
    settled: '已结算',
  }
  return map[s] || s || '进行中'
}
function statusClass(s: string) {
  if (s === 'completed' || s === 'settled' || s === 'passed') return 'ok'
  if (s === 'submitted' || s === 'wait_acceptance' || s === 'settling') return 'warn'
  if (s === 'rejected') return 'err'
  return ''
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/task/order-detail?id=${id}` })
}
function goHall() {
  uni.switchTab({ url: '/pages/task/index' })
}

onShow(load)
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f7f8fc;
  box-sizing: border-box;
}
.header-bar {
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
.order-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(44, 51, 69, 0.06);
}
.o-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.o-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2c3345;
  flex: 1;
}
.o-status {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  background: #eaf2fc;
  color: #4a90e2;
}
.o-status.ok {
  background: #e8f8f1;
  color: #52c9a4;
}
.o-status.warn {
  background: #fff4e6;
  color: #f5a623;
}
.o-status.err {
  background: #fdeaea;
  color: #e57474;
}
.o-progress-row {
  display: flex;
  align-items: center;
  margin: 24rpx 0 20rpx;
}
.o-progress {
  flex: 1;
  height: 12rpx;
  background: #f0f2f7;
  border-radius: 6rpx;
  overflow: hidden;
  margin-right: 16rpx;
}
.o-progress-bar {
  height: 100%;
  background: #4a90e2;
  border-radius: 6rpx;
  transition: width 0.3s;
}
.o-progress-num {
  font-size: 24rpx;
  color: #4a90e2;
  font-weight: 600;
}
.o-foot {
  display: flex;
  justify-content: space-between;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f2f7;
}
.o-meta {
  font-size: 24rpx;
  color: #8a8f9c;
}
.o-arrow {
  font-size: 24rpx;
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
.btn-go {
  margin-top: 40rpx;
  background: #4a90e2;
  color: #fff;
  font-size: 28rpx;
  border-radius: 16rpx;
  padding: 0 48rpx;
}
</style>
