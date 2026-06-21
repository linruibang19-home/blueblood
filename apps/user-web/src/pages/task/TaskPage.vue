<template>
  <MobileTabLayout>
    <template #default>
      <div class="task-page">
        <van-tabs v-model:active="activeTab" sticky offset-top="0">
          <!-- 任务大厅 -->
          <van-tab title="任务大厅" name="hall">
            <div class="tab-content">
              <div class="quick-actions">
                <van-button size="small" type="primary" plain icon="edit" @click="router.push('/tasks/publish')">发布任务</van-button>
                <van-button size="small" type="default" plain icon="manager-o" @click="router.push('/tasks/published')">我发布的</van-button>
                <van-button size="small" type="default" plain icon="orders-o" @click="router.push('/tasks/my')">我的任务</van-button>
              </div>
              <div class="search-section">
                <van-search v-model="keyword" placeholder="搜索任务标题..." shape="round" @search="onSearch" />
              </div>
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
              <div class="task-list">
                <van-loading v-if="loading" type="spinner" class="page-loading" />
                <template v-else>
                  <div
                    v-for="task in tasks"
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
                        <span class="meta-item"><van-icon name="user-o" /> {{ task.employerName }}</span>
                        <span class="meta-item"><van-icon name="clock-o" /> {{ task.deadline }}</span>
                      </div>
                      <div class="task-slots"><span class="slots-num">{{ task.slotsLeft }}</span>/{{ task.totalSlots }}名额</div>
                    </div>
                  </div>
                  <van-empty v-if="!tasks.length" description="暂无任务" />
                </template>
              </div>
            </div>
          </van-tab>

          <!-- 黑客松 -->
          <van-tab title="黑客松" name="hackathon">
            <div class="tab-content">
              <div v-if="hackathons.length" class="hackathon-list">
                <div v-for="h in hackathons" :key="h.id" class="hackathon-card" @click="goHackathonDetail(h.id)">
                  <img :src="h.coverImage" class="hackathon-cover" />
                  <div class="hackathon-info">
                    <div class="hackathon-header">
                      <h4 class="hackathon-title">{{ h.title }}</h4>
                      <StatusBadge :label="hackathonStatusLabel(h.status)" :type="hackathonStatusType(h.status)" />
                    </div>
                    <p class="hackathon-desc">{{ h.description }}</p>
                    <div class="hackathon-meta">
                      <span class="meta-item"><van-icon name="award-o" /> 奖金 ¥{{ (h.prizePool / 1000).toFixed(0) }}k</span>
                      <span class="meta-item"><van-icon name="friends-o" /> {{ h.currentTeams }}/{{ h.maxTeams }}队</span>
                    </div>
                  </div>
                </div>
              </div>
              <van-empty v-else description="暂无赛事" />
            </div>
          </van-tab>

          <!-- AI 岗位 -->
          <van-tab title="AI岗位" name="job">
            <div class="tab-content">
              <div v-if="jobs.length" class="job-list">
                <div v-for="j in jobs" :key="j.id" class="job-card" @click="goJobDetail(j.id)">
                  <div class="job-header">
                    <img :src="j.companyLogo" class="job-logo" />
                    <div class="job-info">
                      <h4 class="job-title">{{ j.title }}</h4>
                      <span class="job-company">{{ j.company }}</span>
                    </div>
                  </div>
                  <div class="job-tags">
                    <van-tag v-for="tag in j.tags" :key="tag" plain>{{ tag }}</van-tag>
                  </div>
                  <div class="job-footer">
                    <span class="job-salary">{{ j.salary }}</span>
                    <span class="meta-item"><van-icon name="location-o" /> {{ j.location }}</span>
                  </div>
                </div>
              </div>
              <van-empty v-else description="暂无岗位" />
            </div>
          </van-tab>
        </van-tabs>
      </div>
    </template>
  </MobileTabLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MobileTabLayout from '@/layouts/MobileTabLayout.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { getTaskList, getTaskCategories } from '@/api/task'
import { mockHackathons } from '@/mock/hackathons'
import { mockJobs } from '@/mock/jobs'
import type { Task, TaskCategory } from '@/types/task'
import type { Hackathon, Job } from '@/types/course'

const router = useRouter()
const activeTab = ref('hall')

// 任务大厅
const tasks = ref<Task[]>([])
const categories = ref<TaskCategory[]>([])
const selectedCategory = ref('全部')
const keyword = ref('')
const loading = ref(false)

async function fetchTasks() {
  loading.value = true
  try {
    tasks.value = await getTaskList({
      category: selectedCategory.value === '全部' ? undefined : selectedCategory.value,
      keyword: keyword.value.trim() || undefined,
    })
  } catch {
    tasks.value = []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([
    fetchTasks(),
    (async () => { categories.value = await getTaskCategories() })(),
  ])
})

function selectCategory(name: string) {
  selectedCategory.value = name
  fetchTasks()
}
function onSearch() { fetchTasks() }
function goTaskDetail(id: string) { router.push(`/tasks/detail/${id}`) }

function getCategoryColor(category: string) {
  const map: Record<string, string> = {
    'Agent配置': 'primary', '自动化脚本': 'success', '流程梳理': 'warning',
    '报告生成': 'danger', '数据处理': 'primary',
  }
  return (map[category] as any) || 'primary'
}

// 黑客松 / 岗位(机会域,数据源同原成长页;后续可接企业发布列表)
const hackathons = ref<Hackathon[]>(mockHackathons)
const jobs = ref<Job[]>(mockJobs)
function goHackathonDetail(id: string) { router.push(`/grow/hackathon/${id}`) }
function goJobDetail(id: string) { router.push(`/grow/job/${id}`) }

function hackathonStatusLabel(s: string) {
  return s === 'signup' ? '报名中' : s === 'ongoing' ? '进行中' : '已结束'
}
function hackathonStatusType(s: string): 'primary' | 'success' | 'default' {
  return s === 'signup' ? 'primary' : s === 'ongoing' ? 'success' : 'default'
}
</script>

<style scoped>
.task-page {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 60px;
}
.tab-content { padding: var(--spacing-lg); }

.quick-actions { display: flex; gap: var(--spacing-sm); margin-bottom: var(--spacing-md); }
.search-section { margin-bottom: var(--spacing-md); }
.page-loading { display: flex; justify-content: center; padding: 40px 0; }

.category-filter { display: flex; flex-wrap: wrap; gap: var(--spacing-sm); padding-bottom: var(--spacing-md); }
.category-item {
  display: flex; align-items: center; gap: 4px;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-card); border-radius: 20px;
  font-size: var(--font-size-sm); color: var(--text-secondary);
  white-space: nowrap; cursor: pointer; transition: all 0.2s;
}
.category-item.active { background: var(--primary); color: #fff; }

.task-list { display: flex; flex-direction: column; gap: var(--spacing-md); }
.task-card {
  background: var(--bg-card); border-radius: var(--radius-md);
  padding: var(--spacing-lg); box-shadow: var(--shadow-sm); cursor: pointer;
}
.task-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--spacing-sm); }
.task-reward { font-size: var(--font-size-lg); font-weight: 700; color: var(--warning); }
.task-title {
  font-size: var(--font-size-md); font-weight: 600; color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
  overflow: hidden; text-overflow: ellipsis;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.task-desc { font-size: var(--font-size-sm); color: var(--text-tertiary); line-height: 1.5; margin-bottom: var(--spacing-sm); }
.task-tags { display: flex; flex-wrap: wrap; gap: var(--spacing-xs); margin-bottom: var(--spacing-md); }
.task-footer { display: flex; justify-content: space-between; align-items: center; }
.task-meta { display: flex; gap: var(--spacing-md); }
.meta-item { display: flex; align-items: center; gap: 4px; font-size: var(--font-size-xs); color: var(--text-tertiary); }
.task-slots { font-size: var(--font-size-xs); color: var(--text-tertiary); }
.slots-num { font-weight: 600; color: var(--primary); }

/* 黑客松 */
.hackathon-list { display: flex; flex-direction: column; gap: var(--spacing-md); }
.hackathon-card { background: var(--bg-card); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.hackathon-cover { width: 100%; height: 140px; object-fit: cover; }
.hackathon-info { padding: var(--spacing-lg); }
.hackathon-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--spacing-sm); }
.hackathon-title { font-size: var(--font-size-md); font-weight: 600; color: var(--text-primary); }
.hackathon-desc { font-size: var(--font-size-sm); color: var(--text-secondary); margin-bottom: var(--spacing-md); }
.hackathon-meta { display: flex; gap: var(--spacing-lg); }

/* 岗位 */
.job-list { display: flex; flex-direction: column; gap: var(--spacing-md); }
.job-card { background: var(--bg-card); border-radius: var(--radius-md); padding: var(--spacing-lg); box-shadow: var(--shadow-sm); }
.job-header { display: flex; align-items: center; gap: var(--spacing-md); margin-bottom: var(--spacing-md); }
.job-logo { width: 40px; height: 40px; border-radius: var(--radius-sm); object-fit: cover; }
.job-info { flex: 1; }
.job-title { font-size: var(--font-size-md); font-weight: 500; color: var(--text-primary); }
.job-company { font-size: var(--font-size-xs); color: var(--text-tertiary); }
.job-tags { display: flex; flex-wrap: wrap; gap: var(--spacing-xs); margin-bottom: var(--spacing-md); }
.job-footer { display: flex; justify-content: space-between; align-items: center; }
.job-salary { font-size: var(--font-size-md); font-weight: 600; color: var(--warning); }
</style>
