<template>
  <view class="page">
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else-if="!order" class="empty">
      <text class="empty-tx">订单不存在</text>
    </view>

    <view v-else>
      <view class="head-card">
        <view class="h-title">{{ order.taskTitle }}</view>
        <view class="h-meta">
          <text class="status" :class="statusClass(order.status)">{{ statusText(order.status) }}</text>
          <text class="progress">进度 {{ order.progress }}%</text>
        </view>
      </view>

      <view class="section-title-bar">里程碑</view>
      <view class="milestone-list" v-if="order.milestones.length">
        <view class="milestone-card" v-for="m in order.milestones" :key="m.id">
          <view class="m-head">
            <text class="m-title">{{ m.title }}</text>
            <text class="m-status" :class="msClass(m.status)">{{ msText(m.status) }}</text>
          </view>
          <view class="m-desc" v-if="m.description">{{ m.description }}</view>
          <button
            class="btn-submit"
            v-if="canSubmit(m.status)"
            @tap="goSubmit(m.id)"
          >
            提交成果
          </button>
        </view>
      </view>
      <view v-else class="empty-inline">暂无里程碑</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import { getTaskOrderDetail, type TaskOrder } from '@/api/task'

const order = ref<TaskOrder | null>(null)
const loading = ref(true)
const orderId = ref('')

async function load() {
  loading.value = true
  order.value = await getTaskOrderDetail(orderId.value)
  loading.value = false
}

function statusText(s: string) {
  return ({
    applied: '已报名', accepted: '已接单', in_progress: '进行中',
    wait_acceptance: '待验收', passed: '验收通过', rejected: '已驳回',
    settling: '结算中', settled: '已结算',
  } as any)[s] || s
}
function statusClass(s: string) {
  if (s === 'completed' || s === 'settled' || s === 'passed') return 'ok'
  if (s === 'rejected') return 'err'
  if (s === 'submitted' || s === 'wait_acceptance' || s === 'settling') return 'warn'
  return ''
}
function msText(s: any) {
  return ({
    not_started: '待提交', pending: '待提交', in_progress: '进行中',
    submitted: '待审核', approved: '已通过', rejected: '已驳回',
  } as any)[s] || s || '待提交'
}
function msClass(s: any) {
  if (s === 'approved') return 'ok'
  if (s === 'rejected') return 'err'
  if (s === 'submitted') return 'warn'
  return ''
}
function canSubmit(s: any) {
  return !s || s === 'not_started' || s === 'pending' || s === 'rejected'
}
function goSubmit(mid: string) {
  uni.navigateTo({ url: `/pages/task/milestone-submit?orderId=${orderId.value}&milestoneId=${mid}` })
}

onLoad((q: any) => {
  orderId.value = String(q?.id || q?.orderId || '')
  load()
})

onShareAppMessage(() => {
  const o = order.value
  return {
    title: o ? `任务进度：${o.taskTitle}` : '蓝血菁英 · 任务进度',
    path: `/pages/task/order-detail?id=${orderId.value}`,
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f7f8fc;
  box-sizing: border-box;
  padding-bottom: 60rpx;
}
.loading,
.empty {
  text-align: center;
  color: #8a8f9c;
  padding-top: 200rpx;
}
.empty-tx {
  font-size: 28rpx;
}
.head-card {
  background: #fff;
  padding: 32rpx;
  margin-bottom: 20rpx;
}
.h-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #2c3345;
}
.h-meta {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
}
.status {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  background: #eaf2fc;
  color: #4a90e2;
  margin-right: 16rpx;
}
.status.ok {
  background: #e8f8f1;
  color: #52c9a4;
}
.status.warn {
  background: #fff4e6;
  color: #f5a623;
}
.status.err {
  background: #fdeaea;
  color: #e57474;
}
.progress {
  font-size: 24rpx;
  color: #8a8f9c;
}
.section-title-bar {
  padding: 24rpx 32rpx 8rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #2c3345;
}
.milestone-list {
  padding: 0 24rpx;
}
.milestone-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(44, 51, 69, 0.06);
}
.m-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.m-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2c3345;
  flex: 1;
}
.m-status {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
  background: #f0f2f7;
  color: #5a6478;
}
.m-status.ok {
  background: #e8f8f1;
  color: #52c9a4;
}
.m-status.warn {
  background: #fff4e6;
  color: #f5a623;
}
.m-status.err {
  background: #fdeaea;
  color: #e57474;
}
.m-desc {
  font-size: 26rpx;
  color: #8a8f9c;
  margin-top: 12rpx;
  line-height: 1.5;
}
.btn-submit {
  margin-top: 24rpx;
  background: #4a90e2;
  color: #fff;
  font-size: 26rpx;
  border-radius: 12rpx;
  height: 64rpx;
  line-height: 64rpx;
}
.empty-inline {
  text-align: center;
  color: #b8bfcc;
  font-size: 26rpx;
  padding: 40rpx;
}
</style>
