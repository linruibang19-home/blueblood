<template>
  <SubPageLayout title="黑客松详情">
    <div class="hackathon-detail" v-if="hackathon">
      <!-- 赛事封面 -->
      <div class="hackathon-cover">
        <img :src="hackathon.coverImage" class="cover-img" />
        <div class="cover-overlay">
          <StatusBadge
            :label="statusLabel"
            :type="statusType"
          />
          <h2 class="hackathon-title">{{ hackathon.title }}</h2>
          <p class="hackathon-desc">{{ hackathon.description }}</p>
        </div>
      </div>

      <!-- 赛事信息 -->
      <div class="info-grid">
        <div class="info-item">
          <van-icon name="award-o" class="info-icon" />
          <span class="info-label">奖金池</span>
          <span class="info-value">¥{{ hackathon.prizePool.toLocaleString() }}</span>
        </div>
        <div class="info-item">
          <van-icon name="friends-o" class="info-icon" />
          <span class="info-label">已报名</span>
          <span class="info-value">{{ hackathon.currentTeams }}/{{ hackathon.maxTeams }}队</span>
        </div>
        <div class="info-item">
          <van-icon name="location-o" class="info-icon" />
          <span class="info-label">地点</span>
          <span class="info-value">{{ hackathon.location }}</span>
        </div>
        <div class="info-item">
          <van-icon name="clock-o" class="info-icon" />
          <span class="info-label">报名截止</span>
          <span class="info-value">{{ hackathon.signupDeadline }}</span>
        </div>
      </div>

      <!-- Tab 切换 -->
      <van-tabs v-model:active="activeTab">
        <van-tab title="队伍列表" name="teams">
          <div class="teams-list">
            <div v-for="i in 5" :key="i" class="team-card">
              <div class="team-header">
                <span class="team-name">队伍 {{ String.fromCharCode(64 + i) }} {{ ['星辰队', 'AI先锋', '代码狂人', '未来科技', '创客空间'][i-1] }}</span>
                <span class="team-members">{{ [3, 4, 3, 5, 4][i-1] }}人</span>
              </div>
              <p class="team-project">{{ ['基于RAG的智能客服', 'AI简历优化工具', '自动化测试平台', '知识图谱问答', '代码审查助手'][i-1] }}</p>
              <div class="team-footer">
                <span class="team-leader">
                  <van-icon name="user-o" /> 队长：{{ ['张明', '李娜', '王强', '陈思', '刘洋'][i-1] }}
                </span>
                <van-button size="small" plain type="primary">申请加入</van-button>
              </div>
            </div>
          </div>
        </van-tab>
        <van-tab title="创建队伍" name="create">
          <div class="create-form">
            <van-field v-model="teamName" label="队伍名称" placeholder="请输入队伍名称" />
            <van-field v-model="projectName" label="项目名称" placeholder="请输入项目名称" />
            <van-field v-model="projectDesc" type="textarea" label="项目描述" placeholder="请描述你的项目方案" rows="3" />
            <van-button type="primary" size="large" round block @click="handleCreateTeam" :loading="creating">
              创建队伍
            </van-button>
          </div>
        </van-tab>
        <van-tab title="提交项目" name="submit" v-if="hackathon.myTeamId">
          <div class="submit-form">
            <van-field v-model="submitUrl" label="GitHub 仓库" placeholder="请输入仓库地址" />
            <van-field v-model="submitDesc" type="textarea" label="项目说明" placeholder="请说明项目功能和创新点" rows="4" />
            <van-button type="primary" size="large" round block @click="handleSubmitProject" :loading="submitting">
              提交项目
            </van-button>
          </div>
        </van-tab>
      </van-tabs>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { mockHackathons } from '@/mock/hackathons'
import type { Hackathon } from '@/types/course'

const route = useRoute()
const hackathon = ref<Hackathon | null>(null)
const activeTab = ref('teams')
const teamName = ref('')
const projectName = ref('')
const projectDesc = ref('')
const creating = ref(false)
const submitUrl = ref('')
const submitDesc = ref('')
const submitting = ref(false)

onMounted(async () => {
  const id = route.params.id as string
  hackathon.value = mockHackathons.find(h => h.id === id) || mockHackathons[0]
})

const statusLabel = computed(() => {
  const s = hackathon.value?.status
  return s === 'signup' ? '报名中' : s === 'ongoing' ? '进行中' : '已结束'
})

const statusType = computed(() => {
  const s = hackathon.value?.status
  return s === 'signup' ? 'primary' : s === 'ongoing' ? 'success' : 'default'
})

async function handleCreateTeam() {
  if (!teamName.value.trim()) {
    showToast('请填写队伍名称')
    return
  }
  creating.value = true
  await new Promise(r => setTimeout(r, 1000))
  creating.value = false
  showToast('创建成功')
  teamName.value = ''
  projectName.value = ''
  projectDesc.value = ''
  activeTab.value = 'teams'
}

async function handleSubmitProject() {
  if (!submitUrl.value.trim()) {
    showToast('请填写 GitHub 仓库地址')
    return
  }
  submitting.value = true
  await new Promise(r => setTimeout(r, 1000))
  submitting.value = false
  showToast('提交成功')
  submitUrl.value = ''
  submitDesc.value = ''
}
</script>

<style scoped>
.hackathon-detail {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 40px;
}

.hackathon-cover {
  position: relative;
  height: 220px;
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
  background: linear-gradient(transparent, rgba(0,0,0,0.75));
  color: #fff;
}

.hackathon-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  margin: var(--spacing-sm) 0;
}

.hackathon-desc {
  font-size: var(--font-size-sm);
  opacity: 0.85;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-xl);
  gap: var(--spacing-xs);
}

.info-icon {
  font-size: 24px;
  color: var(--primary);
}

.info-label {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.info-value {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--text-primary);
}

.teams-list {
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.team-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.team-name {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
}

.team-members {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.team-project {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-md);
}

.team-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.team-leader {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.create-form,
.submit-form {
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}
</style>