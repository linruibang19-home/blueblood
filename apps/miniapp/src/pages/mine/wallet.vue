<template>
  <view class="page">
    <!-- 概要卡 -->
    <view class="summary-card">
      <view class="balance">
        <text class="bal-label">可提现余额（元）</text>
        <text class="bal-num">{{ wallet.balance.toFixed(2) }}</text>
      </view>
      <view class="sub-row">
        <view class="sub-item">
          <text class="sub-num">{{ wallet.pendingAmount.toFixed(2) }}</text>
          <text class="sub-label">待结算</text>
        </view>
        <view class="sub-item">
          <text class="sub-num">{{ wallet.totalEarned.toFixed(2) }}</text>
          <text class="sub-label">累计收益</text>
        </view>
        <view class="sub-item">
          <text class="sub-num">{{ wallet.withdrawnAmount.toFixed(2) }}</text>
          <text class="sub-label">已提现</text>
        </view>
      </view>
    </view>

    <!-- 流水 -->
    <view class="section-title">收益流水</view>
    <view v-if="records.length" class="record-list">
      <view class="record-item" v-for="r in records" :key="r.id">
        <view class="r-left">
          <text class="r-title">{{ r.title }}</text>
          <text class="r-time">{{ r.createdAt }}</text>
        </view>
        <text class="r-amount" :class="{ in: r.amount >= 0 }">
          {{ r.amount >= 0 ? '+' : '' }}{{ r.amount.toFixed(2) }}
        </text>
      </view>
    </view>
    <view v-else class="empty">
      <text class="empty-ic">💰</text>
      <text class="empty-tx">暂无收益记录</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getWalletSummary, getWalletRecords, type WalletSummary, type WalletRecord } from '@/api/wallet'

const wallet = ref<WalletSummary>({ balance: 0, pendingAmount: 0, withdrawnAmount: 0, totalEarned: 0 })
const records = ref<WalletRecord[]>([])

async function load() {
  const [w, r] = await Promise.all([
    getWalletSummary(),
    getWalletRecords().catch(() => [] as WalletRecord[]),
  ])
  wallet.value = w
  records.value = r
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
.summary-card {
  background: linear-gradient(135deg, #4a90e2 0%, #7ab3f0 100%);
  padding: 48rpx 32rpx;
  box-shadow: 0 6rpx 18rpx rgba(74, 144, 226, 0.25);
}
.balance {
  text-align: center;
}
.bal-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
  display: block;
}
.bal-num {
  font-size: 64rpx;
  font-weight: 700;
  color: #fff;
  margin-top: 12rpx;
  display: block;
}
.sub-row {
  display: flex;
  margin-top: 36rpx;
  padding-top: 28rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.25);
}
.sub-item {
  flex: 1;
  text-align: center;
}
.sub-num {
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
  display: block;
}
.sub-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 6rpx;
  display: block;
}
.section-title {
  padding: 32rpx 32rpx 12rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #2c3345;
}
.record-list {
  margin: 0 24rpx;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(44, 51, 69, 0.06);
}
.record-item {
  display: flex;
  align-items: center;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #f0f2f7;
}
.record-item:last-child {
  border-bottom: none;
}
.r-left {
  flex: 1;
}
.r-title {
  font-size: 28rpx;
  color: #2c3345;
  display: block;
}
.r-time {
  font-size: 22rpx;
  color: #b8bfcc;
  margin-top: 8rpx;
  display: block;
}
.r-amount {
  font-size: 32rpx;
  font-weight: 600;
  color: #2c3345;
}
.r-amount.in {
  color: #52c9a4;
}
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 160rpx;
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
