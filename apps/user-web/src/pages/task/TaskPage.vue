<template>
  <MobileTabLayout>
    <template #default>
      <div class="task-page">
        <!-- Tab 切换 -->
        <van-tabs v-model:active="activeTab" sticky offset-top="0">
          <van-tab title="任务大厅" name="hall">
            <div class="tab-content">
              <!-- 搜索框 -->
              <div class="search-section">
                <SearchBar placeholder="搜索任务..." @click="handleSearch" />
              </div>

              <!-- 分类筛选 -->
              <div class="category-filter">
                <div
                  v-for="cat in categories"
                  :key="cat.id"
                  class="category-item"
                  :class="{ 'active': selectedCategory === cat.name }"
                  @click="selectCategory(cat.name)"
                >
                  <van-icon :name="cat.icon" />
                  <span>{{ cat.name }}</span>
                </div>
              </div>

              <!-- 任务列表 -->
              <div class="task-list">
                <div
                  v-for="task in filteredTasks"
                  :key="task.id"
                  class="task-card"
                  @click="goTaskDetail(task.id)"
                >
                  <div class="task-header">
                    <van-tag :type="getCategoryColor(task.category)">{{ task.category }}</van-tag>
                    <span class="task-reward">¥{{ task.reward.toLocaleString() }}</span>
                  </div>
                  <h4 class="task-title">{{ task.title }}</h4>
                  <p class="task-desc text-ellipsis-2">{{ task.description }}</p>
                  <div class="task-tags">
                    <van-tag v-for="skill in task.skills.slice(0, 3)" :key="skill" type="primary">{{ skill }}</van-tag>
                  </div>
                  <div class="task-footer">
                    <div class="task-meta">
                      <span class="meta-item">
                        <van-icon name="user-o" /> {{ task.employerName }}
                      </span>
                      <span class="meta-item">
                        <van-icon name="clock-o" /> {{ task.deadline }}
                      </span>
                    </div>
                    <div class="task-slots">
                      <span class="slots-num">{{ task.slotsLeft }}</span>/{{ task.totalSlots }}名额
                    </div>
                  </div>
                  <div class="task-level-bar">
                    <span>LV{{ task.levelRequired }}可接</span>
                    <div class="level-bar">
                      <div class="level-fill" :style="{ width: '30%' }" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </van-tab>

          <van-tab title="我的任务" name="my">
            <div class="tab-content">
              <div v-if="myTaskOrders.length" class="my-task-list">
                <div
                  v-for="order in myTaskOrders"
                  :key="order.id"
                  class="my-task-card"
                  @click="goTaskExecution(order.id)"
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
            </div>
          </van-tab>
        </van-tabs>
      </div>
    </template>
  </MobileTabLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MobileTabLayout from '@/layouts/MobileTabLayout.vue'
import SearchBar from '@/components/SearchBar.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { getTaskList, getTaskCategories, getMyTaskOrders } from '@/api/task'
import type { Task, TaskCategory } from '@/types/task'
import type { TaskOrder } from '@/types/task'

const router = useRouter()
const activeTab = ref('hall')
const tasks = ref<Task[]>([])
const categories = ref<TaskCategory[]>([])
const selectedCategory = ref('全部')
const myTaskOrders = ref<TaskOrder[]>([])

const filteredTasks = computed(() => {
  if (selectedCategory.value === '全部') return tasks.value
  return tasks.value.filter(t => t.category === selectedCategory.value)
})

onMounted(async () => {
  tasks.value = await getTaskList({})
  categories.value = await getTaskCategories()
  myTaskOrders.value = await getMyTaskOrders()
})

function selectCategory(name: string) {
  selectedCategory.value = name
}

function goTaskDetail(id: string) {
  router.push(`/tasks/detail/${id}`)
}

function goTaskExecution(id: string) {
  router.push(`/tasks/execution/${id}`)
}

function handleSearch() {
  // 搜索功能后续实现
}

function getCategoryColor(category: string) {
  const map: Record<string, string> = {
    'Agent配置': 'primary',
    '自动化脚本': 'success',
    '流程梳理': 'warning',
    '报告生成': 'danger',
    '数据处理': 'primary',
  }
  return (map[category] as any) || 'primary'
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

function getOrderStatusType(status: string) {
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
.task-page {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 60px;
}

.tab-content {
  padding: var(--spacing-lg);
}

.search-section {
  margin-bottom: var(--spacing-md);
}

.category-filter {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  padding-bottom: var(--spacing-md);
}

.category-filter::-webkit-scrollbar { display: none; }

.category-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-card);
  border-radius: 20px;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s;
}

.category-item.active {
  background: var(--primary);
  color: #fff;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.task-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.task-reward {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--warning);
}

.task-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.task-desc {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
  line-height: 1.5;
  margin-bottom: var(--spacing-sm);
}

.task-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-md);
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.task-meta {
  display: flex;
  gap: var(--spacing-md);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.task-slots {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.slots-num {
  font-weight: 600;
  color: var(--primary);
}

.task-level-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.level-bar {
  flex: 1;
  height: 4px;
  background: var(--bg-tertiary);
  border-radius: 2px;
  overflow: hidden;
}

.level-fill {
  height: 100%;
  background: var(--accent);
  border-radius: 2px;
}

/* 我的任务 */
.my-task-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
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