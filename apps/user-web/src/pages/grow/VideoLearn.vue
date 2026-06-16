<template>
  <SubPageLayout title="视频学习">
    <div class="video-study">
      <!-- 模拟视频区域 -->
      <div class="video-player" @click="togglePlay">
        <div class="video-placeholder">
          <van-icon :name="isPlaying ? 'pause-circle-o' : 'play-circle-o'" class="play-icon" />
          <span class="video-tip">{{ isPlaying ? '点击暂停' : '点击播放' }}</span>
        </div>
        <!-- 进度条 -->
        <div class="video-progress" @click.stop>
          <div class="progress-track">
            <div class="progress-fill" :style="{ width: playProgress + '%' }" />
          </div>
        </div>
      </div>

      <!-- 课程信息 -->
      <div class="course-info">
        <h3 class="chapter-title">{{ currentChapter?.title }}</h3>
        <p class="chapter-desc">本章介绍核心概念与实战应用，建议认真观看并做好笔记。</p>
      </div>

      <!-- 课堂互动 -->
      <div class="interaction-section">
        <h3 class="section-title">💬 课堂互动</h3>
        <div class="message-list">
          <div
            v-for="msg in interactionMsgs"
            :key="msg.id"
            class="interaction-msg"
          >
            <UserAvatar :src="msg.avatar" :size="'sm'" />
            <div class="msg-body">
              <span class="msg-author">{{ msg.author }}</span>
              <p class="msg-content">{{ msg.content }}</p>
            </div>
          </div>
        </div>
        <div class="interaction-input">
          <van-field
            v-model="inputText"
            placeholder="参与课堂讨论..."
            @keyup.enter="sendMessage"
          >
            <template #button>
              <van-button size="small" type="primary" @click="sendMessage">发送</van-button>
            </template>
          </van-field>
        </div>
      </div>

      <!-- 上下节切换 -->
      <div class="chapter-nav">
        <van-button plain size="small" :disabled="isFirstChapter" @click="prevChapter">
          <van-icon name="arrow-left" /> 上一节
        </van-button>
        <span class="chapter-indicator">{{ currentIndex + 1 }}/{{ totalChapters }}</span>
        <van-button plain size="small" :disabled="isLastChapter" @click="nextChapter">
          下一节 <van-icon name="arrow" />
        </van-button>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { getCourseDetail } from '@/api/course'
import type { Course } from '@/types/course'

const route = useRoute()
const router = useRouter()
const course = ref<Course | null>(null)
const isPlaying = ref(false)
const playProgress = ref(0)
const inputText = ref('')
const currentIndex = ref(0)

interface InteractionMsg {
  id: string
  author: string
  avatar: string
  content: string
}

const interactionMsgs = ref<InteractionMsg[]>([
  { id: '1', author: '张明', avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', content: '这个概念很重要，建议记下来' },
  { id: '2', author: '李娜', avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg', content: '老师讲得很清楚' },
])

let progressTimer: any = null

onMounted(async () => {
  const courseId = route.params.courseId as string
  course.value = await getCourseDetail(courseId)
  updateCurrentIndex()
  startProgress()
})

onUnmounted(() => {
  if (progressTimer) clearInterval(progressTimer)
})

const currentChapter = computed(() => course.value?.chapters?.[currentIndex.value])
const totalChapters = computed(() => course.value?.chapters?.length || 0)
const isFirstChapter = computed(() => currentIndex.value === 0)
const isLastChapter = computed(() => currentIndex.value >= totalChapters.value - 1)

function updateCurrentIndex() {
  const chapterId = route.params.chapterId as string
  const idx = course.value?.chapters?.findIndex(ch => ch.id === chapterId) ?? 0
  currentIndex.value = idx < 0 ? 0 : idx
}

function togglePlay() {
  isPlaying.value = !isPlaying.value
  showToast(isPlaying.value ? '播放中' : '已暂停')
}

function startProgress() {
  progressTimer = setInterval(() => {
    if (isPlaying.value && playProgress.value < 100) {
      playProgress.value += 0.5
      if (playProgress.value >= 100) {
        isPlaying.value = false
        showToast('本章学习完成')
      }
    }
  }, 500)
}

function sendMessage() {
  if (!inputText.value.trim()) return
  interactionMsgs.value.push({
    id: `msg_${Date.now()}`,
    author: '林同学',
    avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    content: inputText.value.trim(),
  })
  inputText.value = ''
}

function prevChapter() {
  if (!course.value?.id || !currentChapter.value?.id) return
  if (!isFirstChapter.value) {
    currentIndex.value -= 1
    router.replace(`/grow/video/${course.value.id}/${currentChapter.value?.id}`)
  }
}

function nextChapter() {
  if (!course.value?.id || !currentChapter.value?.id) return
  if (!isLastChapter.value) {
    currentIndex.value += 1
    router.replace(`/grow/video/${course.value.id}/${currentChapter.value?.id}`)
  }
}
</script>

<style scoped>
.video-study {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 80px;
}

.video-player {
  position: relative;
  background: #000;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #fff;
}

.play-icon {
  font-size: 56px;
  opacity: 0.8;
}

.video-tip {
  font-size: var(--font-size-sm);
  opacity: 0.7;
  margin-top: var(--spacing-sm);
}

.video-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--spacing-sm);
}

.progress-track {
  height: 3px;
  background: rgba(255,255,255,0.3);
  border-radius: 2px;
}

.progress-fill {
  height: 100%;
  background: var(--primary);
  border-radius: 2px;
  transition: width 0.3s;
}

.course-info {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.chapter-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.chapter-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.6;
}

.interaction-section {
  padding: var(--spacing-lg);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.message-list {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: var(--spacing-md);
}

.interaction-msg {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.msg-body {
  flex: 1;
}

.msg-author {
  font-size: var(--font-size-xs);
  font-weight: 500;
  color: var(--primary);
}

.msg-content {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  background: var(--bg-tertiary);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  margin-top: 2px;
}

.interaction-input {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.chapter-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-top: 1px solid var(--divider);
}

.chapter-indicator {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}
</style>