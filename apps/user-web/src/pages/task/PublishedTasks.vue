<template>
  <SubPageLayout title="我发布的任务">
    <div class="published-page">
      <div class="top-actions">
        <van-button size="small" type="primary" icon="plus" @click="router.push('/tasks/publish')">发布任务</van-button>
        <van-button size="small" type="warning" plain icon="eye-o" @click="router.push('/tasks/review')">待审核里程碑</van-button>
      </div>

      <van-loading v-if="loading" type="spinner" class="page-loading" />
      <template v-else>
        <div v-for="t in tasks" :key="t.id" class="task-card">
          <div class="card-head">
            <h4 class="card-title" @click="router.push(`/tasks/detail/${t.id}`)">{{ t.title }}</h4>
            <StatusBadge :label="statusLabel(t.status)" :type="statusType(t.status)" />
          </div>
          <div class="card-meta">
            <span class="meta-reward">¥{{ t.reward }}</span>
            <span class="meta-item">名额 {{ t.slotsLeft }}/{{ t.totalSlots }}</span>
            <span class="meta-item">{{ t.category || '未分类' }}</span>
          </div>
          <div class="card-skills">
            <van-tag v-for="s in t.skills.slice(0, 4)" :key="s" plain type="primary">{{ s }}</van-tag>
          </div>
          <div class="card-actions">
            <van-button
              v-if="['approved', 'recruiting'].includes(t.status)"
              size="small"
              type="danger"
              plain
              @click="onClose(t.id)"
            >下架</van-button>
          </div>
        </div>
        <van-empty v-if="!tasks.length" description="还没有发布过任务">
          <van-button type="primary" round size="small" @click="router.push('/tasks/publish')">去发布</van-button>
        </van-empty>
      </template>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { getPublishedTasks, closeTask } from '@/api/task'
import type { Task } from '@/types/task'

const router = useRouter()
const tasks = ref<Task[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    tasks.value = await getPublishedTasks()
  } catch {
    tasks.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
onActivated(load)

function statusLabel(s: string) {
  const m: Record<string, string> = {
    approved: '已上架',
    recruiting: '招募中',
    in_progress: '进行中',
    completed: '已完成',
    closed: '已下架',
  }
  return m[s] || s
}

function statusType(s: string): 'primary' | 'success' | 'warning' | 'danger' | 'default' {
  if (s === 'completed') return 'success'
  if (s === 'closed') return 'default'
  if (s === 'in_progress') return 'warning'
  return 'primary'
}

async function onClose(id: string) {
  try {
    await showConfirmDialog({ title: '确认下架', message: '下架后用户将无法接单' })
    await closeTask(id)
    showToast('已下架')
    load()
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.published-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 40px;
}

.top-actions {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.task-card {
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

.card-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  flex: 1;
  margin-right: var(--spacing-sm);
}

.card-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  margin-bottom: var(--spacing-sm);
}

.meta-reward {
  font-size: var(--font-size-md);
  font-weight: 700;
  color: var(--warning);
}

.card-skills {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-sm);
}

.card-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
