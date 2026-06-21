<template>
  <MobileTabLayout>
    <template #default>
      <div class="grow-page">
        <!-- 顶部 Banner -->
        <div class="grow-banner">
          <div class="banner-content">
            <h2 class="banner-title">超级个体成长中心</h2>
            <p class="banner-desc">学习 · 提升</p>
          </div>
        </div>

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

          <!-- 精品课程 -->
          <div class="section">
            <div class="section-header">
              <span class="section-title">精品课程</span>
              <span class="section-more" @click="router.push('/grow/courses')">更多 <van-icon name="arrow" /></span>
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
              <van-empty v-if="!courses.length" description="暂无课程" />
            </div>
          </div>
        </div>
      </div>
    </template>
  </MobileTabLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MobileTabLayout from '@/layouts/MobileTabLayout.vue'
import ProgressBar from '@/components/ProgressBar.vue'
import { getCourseList, getMyAssignments } from '@/api/course'
import type { Course } from '@/types/course'
import type { Assignment } from '@/types/course'

const router = useRouter()
const courses = ref<Course[]>([])
const assignments = ref<Assignment[]>([])

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
  color: var(--primary);
  display: flex;
  align-items: center;
}

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

.assignment-info { flex: 1; }
.assignment-title { font-size: var(--font-size-md); font-weight: 500; color: var(--text-primary); margin-bottom: 4px; }
.assignment-course { font-size: var(--font-size-xs); color: var(--text-tertiary); display: block; margin-bottom: 4px; }
.assignment-deadline { font-size: var(--font-size-xs); color: var(--danger); display: flex; align-items: center; gap: 4px; }

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

.course-cover { width: 100px; height: 120px; object-fit: cover; flex-shrink: 0; }
.course-info { flex: 1; padding: var(--spacing-md) var(--spacing-md) var(--spacing-md) 0; display: flex; flex-direction: column; gap: 4px; }
.course-title { font-size: var(--font-size-md); font-weight: 500; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.course-subtitle { font-size: var(--font-size-xs); color: var(--text-tertiary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.course-meta { display: flex; align-items: center; gap: var(--spacing-sm); margin-top: var(--spacing-xs); }
.course-instructor { font-size: var(--font-size-xs); color: var(--text-secondary); width: 50px; }
.course-progress { font-size: var(--font-size-xs); color: var(--primary); width: 30px; text-align: right; }
.course-tags { display: flex; gap: var(--spacing-xs); flex-wrap: wrap; }
</style>
