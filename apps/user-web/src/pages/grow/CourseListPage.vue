<template>
  <SubPageLayout title="全部课程">
    <div class="course-list-page">
      <van-search v-model="keyword" placeholder="搜索课程标题..." shape="round" @search="load" />

      <van-loading v-if="loading" type="spinner" class="page-loading" />
      <template v-else>
        <div class="course-list">
          <div
            v-for="course in filtered"
            :key="course.id"
            class="course-card"
            @click="goDetail(course.id)"
          >
            <img :src="course.coverImage" class="course-cover" />
            <div class="course-info">
              <h4 class="course-title">{{ course.title }}</h4>
              <p class="course-subtitle">{{ course.subtitle }}</p>
              <div class="course-meta">
                <span class="meta-instructor">
                  <van-icon name="user-o" /> {{ course.instructor }}
                </span>
                <span class="meta-chapters">
                  <van-icon name="bookmark-o" /> {{ course.chapters.length }}章
                </span>
                <span v-if="course.rewardPoints" class="meta-reward">
                  <van-icon name="gem-o" /> +{{ course.rewardPoints }}
                </span>
              </div>
              <div v-if="course.progress > 0" class="course-progress">
                <ProgressBar :percentage="course.progress" :show-text="false" />
                <span class="progress-num">{{ course.progress }}%</span>
              </div>
            </div>
          </div>
        </div>
        <van-empty v-if="!filtered.length" description="暂无课程" />
      </template>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import ProgressBar from '@/components/ProgressBar.vue'
import { getCourseList } from '@/api/course'
import type { Course } from '@/types/course'

const router = useRouter()
const courses = ref<Course[]>([])
const keyword = ref('')
const loading = ref(false)

const filtered = computed(() => {
  if (!keyword.value.trim()) return courses.value
  const kw = keyword.value.toLowerCase()
  return courses.value.filter(
    c => c.title.toLowerCase().includes(kw) || c.subtitle.toLowerCase().includes(kw),
  )
})

async function load() {
  loading.value = true
  try {
    courses.value = await getCourseList()
  } catch {
    courses.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)

function goDetail(id: string) {
  router.push(`/grow/course/${id}`)
}
</script>

<style scoped>
.course-list-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 40px;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
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
  gap: var(--spacing-md);
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  margin-top: 2px;
}

.course-meta span {
  display: flex;
  align-items: center;
  gap: 3px;
}

.meta-reward {
  color: var(--warning);
}

.course-progress {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-top: auto;
}

.progress-num {
  font-size: var(--font-size-xs);
  color: var(--primary);
  width: 32px;
  text-align: right;
}
</style>
