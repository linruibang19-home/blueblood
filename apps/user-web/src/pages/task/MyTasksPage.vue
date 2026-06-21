<template>
  <SubPageLayout title="我的任务">
    <div class="my-tasks-page">
      <van-loading v-if="loading" type="spinner" class="page-loading" />
      <template v-else>
        <div v-if="orders.length" class="my-task-list">
          <div
            v-for="order in orders"
            :key="order.id"
            class="my-task-card"
            @click="goExecution(order.id)"
          >
            <div class="my-task-header">
              <h4 class="my-task-title">{{ order.taskTitle }}</h4>
              <StatusBadge :label="getOrderStatusLabel(order.status)" :type="getOrderStatusType(order.status)" />
            </div>
            <div class="my-task-progress">
              <span class="progress-label">整体进度</span>
              <div class="progress-bar-wrap">
                <div class="progress-bar-track">
                  <div class="progress-bar-fill" :style="{ width: order.progress + '%' }" />
                </div>
                <span class="progress-value">{{ order.progress }}%</span>
              </div>
            </div>
            <div class="milestone-brief">
              <span class="brief-label">里程碑</span>
              <div class="milestone-dots">
                <span
                  v-for="m in order.milestones"
                  :key="m.id"
                  class="milestone-dot"
                  :class="getMilestoneStatusClass(m.status)"
                  :title="m.title"
                />
              </div>
            </div>
            <div class="my-task-footer">
              <span class="my-task-enter">点击查看详情 <van-icon name="arrow" /></span>
            </div>
          </div>
        </div>
        <van-empty v-else description="暂无进行中的任务" />
      </template>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { getMyTaskOrders } from '@/api/task'
import type { TaskOrder } from '@/types/task'

const router = useRouter()
const orders = ref<TaskOrder[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    orders.value = await getMyTaskOrders()
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
onActivated(load)

function goExecution(id: string) {
  router.push(`/tasks/execution/${id}`)
}

function getOrderStatusLabel(status: string) {
  const map: Record<string, string> = {
    applied: '已报名',
    accepted: '已接单',
    in_progress: '执行中',
    wait_acceptance: '待验收',
    passed: '验收通过',
    rejected: '已驳回',
    settling: '结算中',
    settled: '已结算',
  }
  return map[status] || status
}

function getOrderStatusType(status: string): 'primary' | 'success' | 'warning' | 'danger' | 'default' {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'default'> = {
    in_progress: 'primary',
    wait_acceptance: 'warning',
    passed: 'success',
    rejected: 'danger',
    settling: 'warning',
    settled: 'success',
  }
  return map[status] || 'default'
}

function getMilestoneStatusClass(status: string) {
  const map: Record<string, string> = {
    approved: 'dot-approved',
    in_progress: 'dot-progress',
    submitted: 'dot-submitted',
    not_started: 'dot-pending',
    rejected: 'dot-rejected',
  }
  return map[status] || 'dot-pending'
}
</script>

<style scoped>
.my-tasks-page {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 40px;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.my-task-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
}

.my-task-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
}

.my-task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.my-task-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: var(--spacing-sm);
}

.my-task-progress {
  margin-bottom: var(--spacing-md);
}

.progress-label {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  display: block;
  margin-bottom: var(--spacing-xs);
}

.progress-bar-wrap {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.progress-bar-track {
  flex: 1;
  height: 6px;
  background: var(--bg-tertiary);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), var(--accent));
  border-radius: 3px;
}

.progress-value {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--primary);
  width: 40px;
  text-align: right;
}

.milestone-brief {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.brief-label {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.milestone-dots {
  display: flex;
  gap: 6px;
}

.milestone-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--bg-tertiary);
}

.dot-approved { background: var(--success); }
.dot-progress { background: var(--primary); }
.dot-submitted { background: var(--warning); }
.dot-rejected { background: var(--danger); }

.my-task-footer {
  display: flex;
  justify-content: flex-end;
}

.my-task-enter {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
}
</style>
