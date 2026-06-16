<template>
  <MobileTabLayout>
    <template #default>
      <div class="grow-page">
        <!-- 顶部 Banner -->
        <div class="grow-banner">
          <div class="banner-content">
            <h2 class="banner-title">超级个体成长中心</h2>
            <p class="banner-desc">学习 · 实战 · 提升</p>
          </div>
        </div>

        <!-- Tab 切换 -->
        <van-tabs v-model:active="activeTab">
          <van-tab title="精品课程" name="course">
            <div class="tab-content">
              <!-- 待提交作业 -->
              <div class="section" v-if="pendingAssignments.length">
                <div class="section-header">
                  <span class="section-title">📝 待提交作业</span>
                </div>
                <div class="assignment-list">
                  <div
                    v-for="item in pendingAssignments"
                    :key="item.id"
                    class="assignment-card"
                    @click="goAssignmentSubmit(item.id)"
                  >
                    <div class="assignment-info">
                      <h4 class="assignment-title">{{ item.title }}</h4>
                      <span class="assignment-course">{{ item.courseName }}</span>
                      <div class="assignment-deadline">
                        <van-icon name="clock-o" />
                        截止 {{ item.deadline }}
                      </div>
                    </div>
                    <van-button size="small" type="primary">去提交</van-button>
                  </div>
                </div>
              </div>

              <!-- 全部课程 -->
              <div class="section">
                <div class="section-header">
                  <span class="section-title">全部课程</span>
                  <span class="section-more">更多 <van-icon name="arrow" /></span>
                </div>
                <div class="course-list">
                  <div
                    v-for="course in courses"
                    :key="course.id"
                    class="course-card"
                    @click="goCourseDetail(course.id)"
                  >
                    <img :src="course.coverImage" class="course-cover" />
                    <div class="course-info">
                      <h4 class="course-title">{{ course.title }}</h4>
                      <p class="course-subtitle">{{ course.subtitle }}</p>
                      <div class="course-meta">
                        <span class="course-instructor">{{ course.instructor }}</span>
                        <ProgressBar :percentage="course.progress" :show-text="false" />
                        <span class="course-progress">{{ course.progress }}%</span>
                      </div>
                      <div class="course-tags">
                        <van-tag type="primary">{{ course.chapters.length }}章节</van-tag>
                        <van-tag type="success">+{{ course.rewardPoints }}积分</van-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </van-tab>

          <van-tab title="黑客松" name="hackathon">
            <div class="tab-content">
              <!-- 黑客松赛事 -->
              <div class="section" v-if="hackathons.length">
                <div class="section-header">
                  <span class="section-title">🔥 近期赛事</span>
                </div>
                <div class="hackathon-list">
                  <div
                    v-for="hackathon in hackathons"
                    :key="hackathon.id"
                    class="hackathon-card"
                    @click="goHackathonDetail(hackathon.id)"
                  >
                    <img :src="hackathon.coverImage" class="hackathon-cover" />
                    <div class="hackathon-info">
                      <div class="hackathon-header">
                        <h4 class="hackathon-title">{{ hackathon.title }}</h4>
                        <StatusBadge
                          :label="hackathon.status === 'signup' ? '报名中' : hackathon.status === 'ongoing' ? '进行中' : '已结束'"
                          :type="hackathon.status === 'signup' ? 'primary' : hackathon.status === 'ongoing' ? 'success' : 'default'"
                        />
                      </div>
                      <p class="hackathon-desc">{{ hackathon.description }}</p>
                      <div class="hackathon-meta">
                        <span class="meta-item">
                          <van-icon name="award-o" />
                          奖金 ¥{{ (hackathon.prizePool / 1000).toFixed(0) }}k
                        </span>
                        <span class="meta-item">
                          <van-icon name="friends-o" />
                          {{ hackathon.currentTeams }}/{{ hackathon.maxTeams }}队
                        </span>
                        <span class="meta-item">
                          <van-icon name="location-o" />
                          {{ hackathon.location }}
                        </span>
                      </div>
                      <div class="hackathon-time">
                        <van-icon name="clock-o" />
                        {{ hackathon.startTime }} - {{ hackathon.endTime }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </van-tab>

          <van-tab title="AI岗位" name="job">
            <div class="tab-content">
              <!-- 岗位列表 -->
              <div class="section">
                <div class="section-header">
                  <span class="section-title">💼 AI 相关岗位</span>
                </div>
                <div class="job-list">
                  <div
                    v-for="job in jobs"
                    :key="job.id"
                    class="job-card"
                    @click="goJobDetail(job.id)"
                  >
                    <div class="job-header">
                      <img :src="job.companyLogo" class="job-logo" />
                      <div class="job-info">
                        <h4 class="job-title">{{ job.title }}</h4>
                        <span class="job-company">{{ job.company }}</span>
                      </div>
                      <StatusBadge :label="job.type" type="primary" />
                    </div>
                    <div class="job-tags">
                      <van-tag v-for="tag in job.tags" :key="tag">{{ tag }}</van-tag>
                    </div>
                    <div class="job-footer">
                      <span class="job-salary">{{ job.salary }}</span>
                      <span class="job-location">
                        <van-icon name="location-o" /> {{ job.location }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
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
import ProgressBar from '@/components/ProgressBar.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { getCourseList, getMyAssignments } from '@/api/course'
import type { Course } from '@/types/course'
import type { Assignment } from '@/types/course'
import { mockHackathons } from '@/mock/hackathons'
import { mockJobs } from '@/mock/jobs'
import type { Hackathon } from '@/types/course'
import type { Job } from '@/types/course'

const router = useRouter()
const activeTab = ref('course')
const courses = ref<Course[]>([])
const assignments = ref<Assignment[]>([])
const hackathons = ref<Hackathon[]>(mockHackathons)
const jobs = ref<Job[]>(mockJobs)

const pendingAssignments = computed(() =>
  assignments.value.filter(a => a.status === 'not_submitted')
)

onMounted(async () => {
  courses.value = await getCourseList()
  assignments.value = await getMyAssignments()
})

function goCourseDetail(id: string) {
  router.push(`/grow/course/${id}`)
}

function goAssignmentSubmit(id: string) {
  router.push(`/grow/assignment/${id}/submit`)
}

function goHackathonDetail(id: string) {
  router.push(`/grow/hackathon/${id}`)
}

function goJobDetail(id: string) {
  router.push(`/grow/job/${id}`)
}
</script>

<style scoped>
.grow-page {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 60px;
}

.grow-banner {
  padding: var(--spacing-2xl) var(--spacing-xl);
  background: linear-gradient(135deg, var(--primary), var(--accent));
  color: #fff;
}

.banner-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  margin-bottom: var(--spacing-xs);
}

.banner-desc {
  font-size: var(--font-size-sm);
  opacity: 0.85;
}

.tab-content {
  padding: var(--spacing-lg);
}

.section {
  margin-bottom: var(--spacing-xl);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.section-more {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
}

/* 作业卡片 */
.assignment-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.assignment-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.assignment-info {
  flex: 1;
}

.assignment-title {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.assignment-course {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  display: block;
  margin-bottom: 4px;
}

.assignment-deadline {
  font-size: var(--font-size-xs);
  color: var(--danger);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 课程卡片 */
.course-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.course-card {
  display: flex;
  gap: var(--spacing-md);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.course-cover {
  width: 100px;
  height: 120px;
  object-fit: cover;
  flex-shrink: 0;
}

.course-info {
  flex: 1;
  padding: var(--spacing-md) var(--spacing-md) var(--spacing-md) 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.course-title {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-subtitle {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-xs);
}

.course-instructor {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  width: 50px;
}

.course-progress {
  font-size: var(--font-size-xs);
  color: var(--primary);
  width: 30px;
  text-align: right;
}

.course-tags {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

/* 黑客松卡片 */
.hackathon-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.hackathon-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.hackathon-cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.hackathon-info {
  padding: var(--spacing-lg);
}

.hackathon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.hackathon-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
}

.hackathon-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-md);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.hackathon-meta {
  display: flex;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-sm);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.hackathon-time {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 岗位卡片 */
.job-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.job-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.job-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.job-logo {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.job-info {
  flex: 1;
}

.job-title {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--text-primary);
}

.job-company {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-md);
}

.job-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.job-salary {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--warning);
}

.job-location {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>