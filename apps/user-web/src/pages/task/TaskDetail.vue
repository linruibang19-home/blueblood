<template>
  <SubPageLayout title="任务详情" :bottom-bar="true">
    <div class="task-detail" v-if="task">
      <!-- 任务头部 -->
      <div class="task-header">
        <div class="task-tags">
          <van-tag type="primary">{{ task.category }}</van-tag>
          <StatusBadge :label="statusLabel" :type="statusType" />
        </div>
        <h2 class="task-title">{{ task.title }}</h2>
        <div class="task-reward">
          <span class="reward-value">¥{{ task.reward.toLocaleString() }}</span>
          <span class="reward-label">到手金额</span>
        </div>
      </div>

      <!-- 状态流 -->
      <div class="status-flow">
        <div
          v-for="(step, i) in statusSteps"
          :key="step"
          class="status-step"
          :class="{ 'active': i <= currentStepIndex, 'current': i === currentStepIndex }"
        >
          <div class="step-dot" />
          <span class="step-label">{{ step }}</span>
        </div>
      </div>

      <!-- 任务信息 -->
      <div class="task-info">
        <div class="info-row">
          <van-icon name="location-o" />
          <span class="info-label">企业</span>
          <span class="info-value">{{ task.employerName }}</span>
        </div>
        <div class="info-row">
          <van-icon name="clock-o" />
          <span class="info-label">截止时间</span>
          <span class="info-value">{{ task.deadline }}</span>
        </div>
        <div class="info-row">
          <van-icon name="user-o" />
          <span class="info-label">等级要求</span>
          <span class="info-value accent">LV{{ task.levelRequired }}</span>
        </div>
        <div class="info-row">
          <van-icon name="friends-o" />
          <span class="info-label">剩余名额</span>
          <span class="info-value">{{ task.slotsLeft }}/{{ task.totalSlots }}</span>
        </div>
      </div>

      <!-- 任务描述 -->
      <div class="task-desc">
        <h3 class="section-title">任务描述</h3>
        <p class="desc-content">{{ task.description }}</p>
      </div>

      <!-- 技能要求 -->
      <div class="task-skills">
        <h3 class="section-title">技能要求</h3>
        <div class="skills-list">
          <SkillTag v-for="skill in task.skills" :key="skill" :text="skill" />
        </div>
      </div>

      <!-- 底部接单按钮 -->
      <div class="bottom-bar" v-if="['approved', 'recruiting', 'in_progress'].includes(task.status)">
        <van-button
          v-if="task.accepted"
          type="success"
          size="large"
          round
          block
          @click="goExecute"
        >
          已接单 · 去做任务
        </van-button>
        <van-button
          v-else
          type="primary"
          size="large"
          round
          block
          :disabled="task.slotsLeft === 0"
          @click="showConfirmSheet = true"
        >
          {{ task.slotsLeft === 0 ? '名额已满' : '确认接单' }}
        </van-button>
      </div>

      <!-- 接单确认弹窗 -->
      <ConfirmSheet
        v-model:modelValue="showConfirmSheet"
        :title="'确认接单'"
        :desc="`确认接取任务「${task.title}」？完成后可获得 ¥${task.reward.toLocaleString()} 报酬。`"
        confirm-text="确认接单"
        cancel-text="我再看看"
        @confirm="handleConfirmAccept"
        @cancel="showConfirmSheet = false"
      />
    </div>
    <div v-else class="loading-wrap">
      <van-loading type="spinner" />
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import SkillTag from '@/components/SkillTag.vue'
import ConfirmSheet from '@/components/ConfirmSheet.vue'
import { getTaskDetail, acceptTask } from '@/api/task'
import type { Task } from '@/types/task'

const route = useRoute()
const router = useRouter()
const task = ref<Task | null>(null)
const showConfirmSheet = ref(false)

const statusSteps = ['待审核', '已通过', '报名中', '执行中', '已完成']

const statusLabel = computed(() => {
  const map: Record<string, string> = {
    draft: '草稿',
    pending_review: '待审核',
    approved: '已通过',
    recruiting: '报名中',
    in_progress: '执行中',
    completed: '已完成',
    closed: '已关闭',
  }
  return map[task.value?.status || ''] || ''
})

const statusType = computed(() => {
  const s = task.value?.status
  if (s === 'recruiting') return 'primary'
  if (s === 'in_progress') return 'warning'
  if (s === 'completed') return 'success'
  return 'default'
})

const currentStepIndex = computed(() => {
  const map: Record<string, number> = {
    pending_review: 0,
    approved: 1,
    recruiting: 2,
    in_progress: 3,
    completed: 4,
  }
  return map[task.value?.status || ''] ?? 0
})

onMounted(async () => {
  const id = route.params.id as string
  task.value = await getTaskDetail(id)
})

async function handleConfirmAccept() {
  if (!task.value) return
  showConfirmSheet.value = false
  // 接单后用后端返回的真实订单 id 跳转执行页(不再本地扣 slotsLeft / 跳大厅)
  const orderId = await acceptTask(task.value.id)
  if (orderId) {
    showToast('接单成功')
    router.replace(`/tasks/execution/${orderId}`)
  } else {
    showToast('接单失败,可能名额已满或等级不足')
  }
}

function goExecute() {
  router.push('/tasks')
}
</script>

<style scoped>
.task-detail {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 80px;
}

.task-header {
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.task-tags {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.task-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  line-height: 1.4;
}

.task-reward {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-sm);
}

.reward-value {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--warning);
}

.reward-label {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

.status-flow {
  display: flex;
  justify-content: space-between;
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.status-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  flex: 1;
}

.step-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--border);
  transition: all 0.3s;
}

.status-step.active .step-dot {
  background: var(--primary);
}

.status-step.current .step-dot {
  background: var(--primary);
  box-shadow: 0 0 0 4px var(--primary-alpha);
}

.step-label {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.status-step.active .step-label {
  color: var(--primary);
}

.task-info {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.info-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
  width: 60px;
}

.info-value {
  font-size: var(--font-size-sm);
  color: var(--text-primary);
}

.info-value.accent {
  color: var(--accent);
  font-weight: 600;
}

.task-desc,
.task-skills {
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.desc-content {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.7;
}

.skills-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--bg-card);
  border-top: 1px solid var(--border);
  max-width: var(--max-width);
  margin: 0 auto;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
</style>