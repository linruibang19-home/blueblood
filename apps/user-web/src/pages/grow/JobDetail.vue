<template>
  <SubPageLayout title="岗位详情" :bottom-bar="true">
    <div class="job-detail" v-if="job">
      <!-- 岗位头部 -->
      <div class="job-header">
        <img :src="job.companyLogo" class="company-logo" />
        <div class="job-info">
          <h2 class="job-title">{{ job.title }}</h2>
          <p class="job-company">{{ job.company }}</p>
          <div class="job-tags">
            <van-tag type="primary">{{ job.type }}</van-tag>
            <van-tag v-for="tag in job.tags.slice(0, 3)" :key="tag">{{ tag }}</van-tag>
          </div>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="info-section">
        <div class="info-row">
          <van-icon name="location-o" />
          <span>{{ job.location }}</span>
        </div>
        <div class="info-row">
          <van-icon name="cash-o" />
          <span class="salary-text">{{ job.salary }}</span>
        </div>
        <div class="info-row">
          <van-icon name="clock-o" />
          <span>发布于 {{ job.postedAt }}</span>
        </div>
      </div>

      <!-- 岗位描述 -->
      <div class="desc-section">
        <h3 class="section-title">岗位描述</h3>
        <p class="desc-content">{{ job.description }}</p>
      </div>

      <!-- 任职要求 -->
      <div class="requirements-section">
        <h3 class="section-title">任职要求</h3>
        <ul class="requirements-list">
          <li v-for="(req, i) in job.requirements" :key="i" class="requirement-item">
            {{ req }}
          </li>
        </ul>
      </div>

      <!-- 底部投递按钮 -->
      <div class="bottom-bar">
        <van-button type="primary" size="large" round block @click="handleApply">
          立即投递
        </van-button>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import { mockJobs } from '@/mock/jobs'
import type { Job } from '@/types/course'

const route = useRoute()
const job = ref<Job | null>(null)

onMounted(async () => {
  const id = route.params.id as string
  job.value = mockJobs.find(j => j.id === id) || mockJobs[0]
})

function handleApply() {
  showToast('投递成功，请等待 HR 联系')
}
</script>

<style scoped>
.job-detail {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 80px;
}

.job-header {
  display: flex;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.company-logo {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-md);
  object-fit: cover;
  flex-shrink: 0;
}

.job-info {
  flex: 1;
}

.job-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.job-company {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-sm);
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.info-section {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.info-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-md);
}

.info-row:last-child {
  margin-bottom: 0;
}

.salary-text {
  font-weight: 600;
  color: var(--warning);
}

.desc-section,
.requirements-section {
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.desc-content {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.7;
}

.requirements-list {
  list-style: none;
  padding: 0;
}

.requirement-item {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: var(--spacing-sm);
  padding-left: var(--spacing-lg);
  position: relative;
}

.requirement-item::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--primary);
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