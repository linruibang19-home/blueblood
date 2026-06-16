<template>
  <SubPageLayout title="批改结果" :bottom-bar="true">
    <div class="assignment-result">
      <!-- 得分卡片 -->
      <div class="score-card">
        <div class="score-circle">
          <span class="score-value">{{ assignment?.score }}</span>
          <span class="score-unit">分</span>
        </div>
        <div class="score-status">
          <StatusBadge
            :label="scoreLabel"
            :type="scoreType"
          />
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="info-section">
        <h3 class="section-title">{{ assignment?.title }}</h3>
        <p class="course-name">{{ assignment?.courseName }} · {{ assignment?.chapterName }}</p>
      </div>

      <!-- 批改意见 -->
      <div class="feedback-section" v-if="assignment?.feedback">
        <h3 class="section-title">📝 批改意见</h3>
        <div class="feedback-content">
          <p>{{ assignment.feedback }}</p>
        </div>
      </div>

      <!-- 参考答案 -->
      <div class="answer-section" v-if="assignment?.answer">
        <h3 class="section-title">✅ 参考答案</h3>
        <div class="answer-content">
          <p>{{ assignment.answer }}</p>
        </div>
      </div>

      <!-- 返回按钮 -->
      <div class="bottom-bar">
        <van-button type="primary" size="large" round block @click="router.back()">
          返回课程
        </van-button>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { getAssignmentDetail } from '@/api/course'
import type { Assignment } from '@/types/course'

const route = useRoute()
const router = useRouter()
const assignment = ref<Assignment | null>(null)

onMounted(async () => {
  const id = route.params.id as string
  assignment.value = await getAssignmentDetail(id)
})

const scoreLabel = computed(() => {
  const score = assignment.value?.score || 0
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 60) return '及格'
  return '不及格'
})

const scoreType = computed(() => {
  const score = assignment.value?.score || 0
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
})
</script>

<style scoped>
.assignment-result {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 80px;
}

.score-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-2xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.score-circle {
  display: flex;
  align-items: baseline;
  margin-bottom: var(--spacing-lg);
}

.score-value {
  font-size: 56px;
  font-weight: 700;
  color: var(--primary);
}

.score-unit {
  font-size: var(--font-size-lg);
  color: var(--text-tertiary);
  margin-left: 4px;
}

.info-section {
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.course-name {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

.feedback-section,
.answer-section {
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.feedback-content p,
.answer-content p {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.7;
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
</style>