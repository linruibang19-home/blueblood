<template>
  <SubPageLayout title="课程详情">
    <div class="course-detail" v-if="course">
      <!-- 课程封面 -->
      <div class="course-cover">
        <img :src="course.coverImage" class="cover-img" />
        <div class="cover-overlay">
          <h2 class="course-title">{{ course.title }}</h2>
          <p class="course-subtitle">{{ course.subtitle }}</p>
        </div>
      </div>

      <!-- 课程信息 -->
      <div class="course-info-bar">
        <UserAvatar :src="course.instructorAvatar" :size="'sm'" />
        <span class="instructor-name">{{ course.instructor }}</span>
        <van-divider vertical />
        <span class="student-count">{{ course.students }}人在学</span>
        <van-divider vertical />
        <van-icon name="star" color="var(--warning)" />
        <span class="rating">{{ course.rating }}</span>
      </div>

      <!-- 进度信息 -->
      <div class="progress-section">
        <div class="progress-info">
          <span class="progress-label">学习进度</span>
          <span class="progress-value">{{ course.completedChapters }}/{{ course.totalChapters }}章节</span>
        </div>
        <ProgressBar :percentage="course.progress" :show-text="false" />
        <span class="progress-percent">{{ course.progress }}%</span>
      </div>

      <!-- 章节列表 -->
      <div class="chapters-section">
        <h3 class="section-title">课程章节</h3>
        <div class="chapter-list">
          <div
            v-for="chapter in course.chapters"
            :key="chapter.id"
            class="chapter-item"
            :class="{ 'chapter-completed': chapter.completed }"
            @click="goVideoLearn(course.id, chapter.id)"
          >
            <div class="chapter-left">
              <van-icon :name="chapter.completed ? 'passed' : 'play-circle-o'" :class="{ 'completed': chapter.completed }" />
              <div class="chapter-info">
                <span class="chapter-title">{{ chapter.title }}</span>
                <span class="chapter-duration">{{ chapter.duration }}</span>
              </div>
            </div>
            <van-icon v-if="chapter.completed" name="checked" class="check-icon" />
            <van-icon v-else name="arrow" class="arrow-icon" />
          </div>
        </div>
      </div>

      <!-- 底部学习按钮 -->
      <div class="bottom-bar">
        <van-button type="primary" size="large" round block @click="continueLearn">
          {{ course.progress > 0 ? '继续学习' : '开始学习' }}
        </van-button>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import ProgressBar from '@/components/ProgressBar.vue'
import { getCourseDetail } from '@/api/course'
import type { Course } from '@/types/course'

const route = useRoute()
const router = useRouter()
const course = ref<Course | null>(null)

onMounted(async () => {
  const id = route.params.id as string
  course.value = await getCourseDetail(id)
})

function goVideoLearn(courseId: string, chapterId: string) {
  router.push(`/grow/video/${courseId}/${chapterId}`)
}

function continueLearn() {
  if (!course.value?.chapters?.length) return
  const nextChapter = course.value.chapters.find(ch => !ch.completed)
  if (nextChapter) {
    goVideoLearn(course.value.id, nextChapter.id)
  } else {
    goVideoLearn(course.value.id, course.value.chapters[0].id)
  }
}
</script>

<style scoped>
.course-detail {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 80px;
}

.course-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--spacing-xl);
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  color: #fff;
}

.course-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  margin-bottom: var(--spacing-xs);
}

.course-subtitle {
  font-size: var(--font-size-sm);
  opacity: 0.85;
}

.course-info-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg);
  background: var(--bg-card);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.instructor-name {
  font-weight: 500;
}

.student-count, .rating {
  font-size: var(--font-size-sm);
}

.progress-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-top: 1px solid var(--divider);
}

.progress-info {
  display: flex;
  flex-direction: column;
  width: 80px;
}

.progress-label {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.progress-value {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.progress-percent {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--primary);
  width: 40px;
  text-align: right;
}

.chapters-section {
  padding: var(--spacing-lg);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.chapter-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.chapter-item:active {
  background: var(--bg-tertiary);
}

.chapter-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.chapter-left .van-icon {
  font-size: 22px;
  color: var(--primary);
}

.chapter-left .van-icon.completed {
  color: var(--success);
}

.chapter-info {
  display: flex;
  flex-direction: column;
}

.chapter-title {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.chapter-duration {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.check-icon {
  color: var(--success);
  font-size: var(--font-size-md);
}

.arrow-icon {
  color: var(--text-tertiary);
  font-size: var(--font-size-md);
}

.chapter-completed .chapter-title {
  color: var(--text-tertiary);
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