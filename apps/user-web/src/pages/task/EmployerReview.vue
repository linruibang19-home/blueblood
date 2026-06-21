<template>
  <SubPageLayout title="待审核里程碑">
    <div class="review-page">
      <van-loading v-if="loading" type="spinner" class="page-loading" />
      <template v-else>
        <div v-for="m in items" :key="m.milestoneId" class="review-card">
          <div class="card-head">
            <div class="head-left">
              <h4 class="card-title">{{ m.milestoneTitle }}</h4>
              <span class="card-sub">{{ m.taskTitle }} · 接单者 {{ m.workerName }}</span>
            </div>
            <span class="card-reward">¥{{ m.reward }}</span>
          </div>
          <div class="card-body">
            <div v-if="m.githubUrl" class="line">
              <van-icon name="link-o" />
              <span>{{ m.githubUrl }}</span>
            </div>
            <div v-if="m.submissionDesc" class="line desc">{{ m.submissionDesc }}</div>
            <div v-if="parsedAttachments(m.attachments).length" class="attach-list">
              <van-icon name="attachment" />
              <a v-for="(a, i) in parsedAttachments(m.attachments)" :key="i" :href="a" target="_blank" class="attach-link">附件{{ i + 1 }}</a>
            </div>
          </div>
          <div class="card-actions">
            <van-button size="small" type="success" :loading="loadingId === m.milestoneId" @click="onApprove(m)">通过并结算</van-button>
            <van-button size="small" type="danger" plain @click="openReject(m)">驳回</van-button>
          </div>
        </div>
        <van-empty v-if="!items.length" description="暂无待审核的里程碑" />
      </template>
    </div>

    <!-- 驳回反馈对话框 -->
    <van-dialog
      v-model:show="rejectShow"
      title="驳回原因"
      show-cancel-button
      :confirm-button-color="'var(--danger)'"
      @confirm="confirmReject"
    >
      <van-field
        v-model="rejectFeedback"
        type="textarea"
        placeholder="请填写驳回原因(将反馈给接单者)"
        rows="3"
        autosize
      />
    </van-dialog>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated } from 'vue'
import { showToast, showSuccessToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import { getMilestonesForReview, reviewMilestone } from '@/api/task'
import type { MilestoneReviewItem } from '@/types/task'

const items = ref<MilestoneReviewItem[]>([])
const loading = ref(false)
const loadingId = ref<string>('')

const rejectShow = ref(false)
const rejectFeedback = ref('')
const rejectTarget = ref<MilestoneReviewItem | null>(null)

async function load() {
  loading.value = true
  try {
    items.value = await getMilestonesForReview()
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
onActivated(load)

function parsedAttachments(raw: string): string[] {
  if (!raw) return []
  try {
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr.filter(Boolean) : []
  } catch {
    return []
  }
}

async function onApprove(m: MilestoneReviewItem) {
  loadingId.value = m.milestoneId
  try {
    await reviewMilestone(m.milestoneId, { result: 'APPROVED' })
    showSuccessToast('已通过,酬金已结算给接单者')
    await load()
  } catch (e) {
    showToast((e as Error).message || '操作失败')
  } finally {
    loadingId.value = ''
  }
}

function openReject(m: MilestoneReviewItem) {
  rejectTarget.value = m
  rejectFeedback.value = ''
  rejectShow.value = true
}

async function confirmReject() {
  const m = rejectTarget.value
  if (!m) return
  loadingId.value = m.milestoneId
  try {
    await reviewMilestone(m.milestoneId, { result: 'REJECTED', feedback: rejectFeedback.value || '未通过验收,请修改后重新提交' })
    showSuccessToast('已驳回')
    await load()
  } catch (e) {
    showToast((e as Error).message || '操作失败')
  } finally {
    loadingId.value = ''
  }
}
</script>

<style scoped>
.review-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 40px;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.review-card {
  margin: var(--spacing-md) var(--spacing-xl);
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-sm);
}

.head-left {
  flex: 1;
  margin-right: var(--spacing-sm);
}

.card-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.card-sub {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.card-reward {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: var(--warning);
}

.card-body {
  padding: var(--spacing-sm) 0;
}

.line {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xs);
  word-break: break-all;
}

.line.desc {
  color: var(--text-tertiary);
  line-height: 1.6;
  white-space: pre-wrap;
}

.attach-list {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-xs);
  color: var(--primary);
}

.attach-link {
  color: var(--primary);
}

.card-actions {
  display: flex;
  gap: var(--spacing-sm);
  justify-content: flex-end;
  margin-top: var(--spacing-sm);
}
</style>
