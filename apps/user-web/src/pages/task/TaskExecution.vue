<template>
  <SubPageLayout title="任务执行">
    <div class="task-execution" v-if="order">
      <!-- 任务信息卡片 -->
      <div class="task-card">
        <div class="task-header">
          <h3 class="task-title">{{ order.taskTitle }}</h3>
          <StatusBadge :label="statusLabel" :type="statusType" />
        </div>
        <div class="task-info-row">
          <span class="info-label">企业</span>
          <span class="info-value">{{ task?.employerName || '-' }}</span>
        </div>
        <div class="task-info-row">
          <span class="info-label">到手金额</span>
          <span class="info-value reward">¥{{ task?.reward?.toLocaleString() || 0 }}</span>
        </div>
      </div>

      <!-- 整体进度 -->
      <div class="progress-section">
        <div class="progress-header">
          <span class="progress-title">整体进度</span>
          <span class="progress-value">{{ order.progress }}%</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: order.progress + '%' }" />
        </div>
      </div>

      <!-- 里程碑时间线 -->
      <div class="milestones-section">
        <h3 class="section-title">里程碑进度</h3>
        <div class="milestone-timeline">
          <div
            v-for="(milestone, i) in order.milestones"
            :key="milestone.id"
            class="milestone-item"
            :class="{ 'current': milestone.status === 'in_progress', 'approved': milestone.status === 'approved' }"
          >
            <div class="milestone-left">
              <div class="milestone-dot" :class="getMilestoneClass(milestone.status)" />
              <div v-if="i < order.milestones.length - 1" class="milestone-line" :class="{ 'filled': milestone.status === 'approved' }" />
            </div>
            <div class="milestone-content">
              <div class="milestone-header">
                <span class="milestone-title">{{ milestone.title }}</span>
                <StatusBadge :label="getMilestoneLabel(milestone.status)" :type="getMilestoneType(milestone.status)" />
              </div>
              <p class="milestone-desc">{{ milestone.description }}</p>
              <div class="milestone-meta">
                <span><van-icon name="clock-o" /> 截止 {{ milestone.dueDate }}</span>
              </div>
              <van-button
                v-if="milestone.status === 'in_progress'"
                type="primary"
                size="small"
                @click="goMilestoneSubmit(order.id, milestone.id)"
              >
                提交里程碑
              </van-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { getMyTaskOrderDetail } from '@/api/task'
import { getTaskDetail } from '@/api/task'
import type { TaskOrder } from '@/types/task'
import type { Task } from '@/types/task'

const route = useRoute()
const router = useRouter()
const order = ref<TaskOrder | null>(null)
const task = ref<Task | null>(null)

onMounted(async () => {
  const id = route.params.id as string
  order.value = await getMyTaskOrderDetail(id)
  if (order.value?.taskId) {
    task.value = await getTaskDetail(order.value.taskId)
  }
})

const statusLabel = computed(() => {
  const map: Record<string, string> = {
    in_progress: '执行中',
    wait_acceptance: '待验收',
    passed: '验收通过',
    settling: '结算中',
    settled: '已结算',
  }
  return map[order.value?.status || ''] || '执行中'
})

const statusType = computed(() => {
  const s = order.value?.status
  if (s === 'passed' || s === 'settled') return 'success'
  if (s === 'wait_acceptance' || s === 'settling') return 'warning'
  return 'primary'
})

function goMilestoneSubmit(orderId: string, milestoneId: string) {
  router.push(`/tasks/milestone/${orderId}/${milestoneId}`)
}

function getMilestoneClass(status: string) {
  const map: Record<string, string> = {
    approved: 'dot-approved',
    in_progress: 'dot-current',
    submitted: 'dot-submitted',
    not_started: 'dot-pending',
  }
  return map[status] || 'dot-pending'
}

function getMilestoneLabel(status: string) {
  const map: Record<string, string> = {
    approved: '已通过',
    in_progress: '进行中',
    submitted: '待审核',
    not_started: '未开始',
    rejected: '已驳回',
  }
  return map[status] || status
}

function getMilestoneType(status: string): 'primary' | 'success' | 'warning' | 'danger' | 'default' {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'default'> = {
    approved: 'success',
    in_progress: 'primary',
    submitted: 'warning',
    not_started: 'default',
    rejected: 'danger',
  }
  return map[status] || 'default'
}
</script>

<style scoped>
.task-execution {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 40px;
}

.task-card {
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-md);
}

.task-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
  margin-right: var(--spacing-sm);
}

.task-info-row {
  display: flex;
  justify-content: space-between;
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-sm);
}

.info-label {
  color: var(--text-tertiary);
}

.info-value {
  color: var(--text-primary);
}

.info-value.reward {
  font-weight: 600;
  color: var(--warning);
}

.progress-section {
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.progress-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
}

.progress-value {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--primary);
}

.progress-bar {
  height: 8px;
  background: var(--bg-tertiary);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), var(--accent));
  border-radius: 4px;
  transition: width 0.3s;
}

.milestones-section {
  padding: var(--spacing-xl);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xl);
}

.milestone-timeline {
  display: flex;
  flex-direction: column;
}

.milestone-item {
  display: flex;
  gap: var(--spacing-md);
}

.milestone-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 20px;
}

.milestone-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--border);
  flex-shrink: 0;
}

.dot-approved { background: var(--success); }
.dot-current { background: var(--primary); box-shadow: 0 0 0 3px var(--primary-alpha); }
.dot-submitted { background: var(--warning); }
.dot-pending { background: var(--border); }

.milestone-line {
  width: 2px;
  flex: 1;
  background: var(--border);
  margin: 4px 0;
}

.milestone-line.filled {
  background: var(--success);
}

.milestone-content {
  flex: 1;
  padding-bottom: var(--spacing-xl);
}

.milestone-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.milestone-title {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--text-primary);
}

.milestone-desc {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
  margin-bottom: var(--spacing-sm);
}

.milestone-meta {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.milestone-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>