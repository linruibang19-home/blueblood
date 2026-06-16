<template>
  <SubPageLayout title="收益明细">
    <div class="wallet-detail">
      <!-- 账户概览 -->
      <div class="account-overview">
        <div class="balance-card">
          <span class="balance-label">账户余额（元）</span>
          <span class="balance-value">¥{{ summary.balance.toFixed(2) }}</span>
        </div>
        <div class="balance-detail">
          <div class="detail-item">
            <span class="detail-label">待结算</span>
            <span class="detail-value pending">¥{{ summary.pendingAmount.toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">已提现</span>
            <span class="detail-value">¥{{ summary.withdrawnAmount.toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">累计收益</span>
            <span class="detail-value">¥{{ summary.totalEarned.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 收益流水 -->
      <div class="records-section">
        <h3 class="section-title">收益流水</h3>
        <div class="record-list">
          <div v-for="record in records" :key="record.id" class="record-item">
            <div class="record-left">
              <div class="record-icon" :class="record.type">
                <van-icon :name="record.type === 'income' ? 'arrow-up' : 'arrow-down'" />
              </div>
              <div class="record-info">
                <span class="record-title">{{ record.title }}</span>
                <span class="record-desc">{{ record.description }}</span>
              </div>
            </div>
            <div class="record-right">
              <span class="record-amount" :class="record.type">
                {{ record.type === 'income' ? '+' : '-' }}¥{{ record.amount.toFixed(2) }}
              </span>
              <span class="record-time">{{ record.createdAt }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import { getWalletSummary, getWalletRecords } from '@/api/wallet'
import type { WalletSummary, WalletRecord } from '@/types/wallet'

const summary = ref<WalletSummary>({ balance: 0, pendingAmount: 0, withdrawnAmount: 0, totalEarned: 0 })
const records = ref<WalletRecord[]>([])

onMounted(async () => {
  summary.value = await getWalletSummary()
  records.value = await getWalletRecords()
})
</script>

<style scoped>
.wallet-detail {
  min-height: 100vh;
  background: var(--bg-primary);
}

.account-overview {
  background: linear-gradient(135deg, var(--primary), var(--accent));
  padding: var(--spacing-xl);
  color: #fff;
}

.balance-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-xl) 0;
  border-bottom: 1px solid rgba(255,255,255,0.2);
  margin-bottom: var(--spacing-lg);
}

.balance-label {
  font-size: var(--font-size-sm);
  opacity: 0.8;
  margin-bottom: var(--spacing-sm);
}

.balance-value {
  font-size: 36px;
  font-weight: 700;
}

.balance-detail {
  display: flex;
  justify-content: space-around;
}

.detail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.detail-label {
  font-size: var(--font-size-xs);
  opacity: 0.7;
}

.detail-value {
  font-size: var(--font-size-md);
  font-weight: 600;
}

.detail-value.pending {
  color: var(--warning-light);
}

.records-section {
  padding: var(--spacing-xl);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.record-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.record-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.record-icon.income {
  background: var(--success-alpha);
  color: var(--success);
}

.record-icon.withdraw {
  background: var(--primary-alpha);
  color: var(--primary);
}

.record-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.record-title {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.record-desc {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.record-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.record-amount {
  font-size: var(--font-size-md);
  font-weight: 600;
}

.record-amount.income {
  color: var(--success);
}

.record-amount.withdraw {
  color: var(--text-primary);
}

.record-time {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}
</style>