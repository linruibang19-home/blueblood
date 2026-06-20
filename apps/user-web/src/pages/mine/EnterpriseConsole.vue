<template>
  <SubPageLayout title="企业工作台">
    <div class="console-page">
      <!-- 企业状态卡 -->
      <div class="status-card">
        <div class="status-info">
          <span class="status-label">认证状态</span>
          <van-tag :type="statusTagType">{{ statusText }}</van-tag>
        </div>
        <p v-if="application?.rejectReason" class="reject-reason">
          拒绝原因：{{ application.rejectReason }}
        </p>
        <p v-if="!isEnterpriseUser" class="status-tip">
          当前账号非企业用户。如需发布岗位/黑客松，请完成企业认证。
        </p>
        <van-button
          v-if="!isEnterpriseUser"
          size="small"
          type="primary"
          round
          class="register-btn"
          @click="goRegister"
        >
          前往企业注册
        </van-button>
      </div>

      <!-- 操作区 -->
      <div v-if="canPublish" class="action-bar">
        <van-button type="primary" round icon="edit" @click="openJobDialog()">发布岗位</van-button>
        <van-button type="success" round icon="flag-o" @click="openHackathonDialog()">发布黑客松</van-button>
      </div>

      <!-- 我发布的岗位 -->
      <div class="section">
        <div class="section-header">
          <span class="section-title">我发布的岗位</span>
          <span class="section-count">{{ jobs.length }}</span>
        </div>
        <van-empty v-if="!jobs.length" description="暂无岗位" />
        <van-cell-group v-else inset>
          <van-cell
            v-for="job in jobs"
            :key="job.id"
            :title="job.title"
            :label="`${job.location || ''} · ${job.salary || ''}`"
            is-link
          >
            <template #value>
              <van-button size="mini" plain @click.stop="openJobDialog(job)">编辑</van-button>
              <van-button size="mini" plain type="danger" @click.stop="onRemoveJob(job)">删除</van-button>
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <!-- 我发布的黑客松 -->
      <div class="section">
        <div class="section-header">
          <span class="section-title">我发布的黑客松</span>
          <span class="section-count">{{ hackathons.length }}</span>
        </div>
        <van-empty v-if="!hackathons.length" description="暂无黑客松" />
        <van-cell-group v-else inset>
          <van-cell
            v-for="h in hackathons"
            :key="h.id"
            :title="h.title"
            :label="h.description"
            is-link
          >
            <template #value>
              <van-button size="mini" plain @click.stop="openHackathonDialog(h)">编辑</van-button>
              <van-button size="mini" plain type="danger" @click.stop="onRemoveHackathon(h)">删除</van-button>
            </template>
          </van-cell>
        </van-cell-group>
      </div>
    </div>

    <!-- 岗位表单弹层 -->
    <van-dialog
      v-model:show="jobDialogVisible"
      :title="editingJob?.id ? '编辑岗位' : '发布岗位'"
      show-cancel-button
      :before-close="onJobBeforeClose"
    >
      <div class="dialog-form">
        <van-field v-model="jobForm.title" label="岗位名称" placeholder="如：前端工程师" />
        <van-field v-model="jobForm.company" label="公司" placeholder="公司名称" />
        <van-field v-model="jobForm.location" label="工作地点" placeholder="如：北京" />
        <van-field v-model="jobForm.salary" label="薪资" placeholder="如：15-25K" />
        <van-field v-model="jobForm.type" label="类型" placeholder="如：全职/实习" />
        <van-field v-model="jobTagsInput" label="标签" placeholder="多个用逗号分隔" />
        <van-field
          v-model="jobForm.contact"
          label="联系方式"
          placeholder="邮箱/电话"
        />
        <van-field
          v-model="jobForm.description"
          type="textarea"
          label="描述"
          placeholder="岗位职责"
          rows="2"
          autosize
        />
        <van-field
          v-model="jobRequirementsInput"
          type="textarea"
          label="要求"
          placeholder="多项用换行分隔"
          rows="2"
          autosize
        />
      </div>
    </van-dialog>

    <!-- 黑客松表单弹层 -->
    <van-dialog
      v-model:show="hackathonDialogVisible"
      :title="editingHackathon?.id ? '编辑黑客松' : '发布黑客松'"
      show-cancel-button
      :before-close="onHackathonBeforeClose"
    >
      <div class="dialog-form">
        <van-field v-model="hackathonForm.title" label="名称" placeholder="黑客松名称" />
        <van-field v-model="hackathonForm.location" label="地点" placeholder="线上/线下地址" />
        <van-field v-model="hackathonForm.startDate" label="开始日期" placeholder="YYYY-MM-DD" />
        <van-field v-model="hackathonForm.endDate" label="结束日期" placeholder="YYYY-MM-DD" />
        <van-field v-model="hackathonForm.prize" label="奖金/奖品" placeholder="如：10万奖金池" />
        <van-field v-model="hackathonForm.contact" label="联系方式" placeholder="邮箱/电话" />
        <van-field v-model="hackathonTagsInput" label="标签" placeholder="多个用逗号分隔" />
        <van-field
          v-model="hackathonForm.description"
          type="textarea"
          label="描述"
          rows="2"
          autosize
        />
        <van-field
          v-model="hackathonRequirementsInput"
          type="textarea"
          label="要求"
          placeholder="多项用换行分隔"
          rows="2"
          autosize
        />
      </div>
    </van-dialog>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showSuccessToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import { getCurrentUser } from '@/api/user'
import {
  getMyApplication,
  getMyJobs,
  publishJob,
  updateJob,
  deleteJob,
  getMyHackathons,
  publishHackathon,
  updateHackathon,
  deleteHackathon,
  type Job,
  type Hackathon,
  type EnterpriseApplication,
} from '@/api/enterprise'
import type { User } from '@/types/user'

const router = useRouter()

const user = ref<User | null>(null)
const application = ref<EnterpriseApplication | null>(null)
const jobs = ref<Job[]>([])
const hackathons = ref<Hackathon[]>([])

const isEnterpriseUser = computed(() => user.value?.userType === 'enterprise')
const isApproved = computed(() => application.value?.status === 'APPROVED')
const canPublish = computed(() => isEnterpriseUser.value && isApproved.value)

const statusText = computed(() => {
  if (!isEnterpriseUser.value) return '非企业用户'
  const s = application.value?.status
  if (s === 'PENDING') return '审核中'
  if (s === 'APPROVED') return '已认证'
  if (s === 'REJECTED') return '已拒绝'
  return '未提交'
})

const statusTagType = computed<'default' | 'primary' | 'success' | 'warning' | 'danger'>(() => {
  const s = application.value?.status
  if (s === 'APPROVED') return 'success'
  if (s === 'PENDING') return 'warning'
  if (s === 'REJECTED') return 'danger'
  return 'default'
})

// ===== 岗位表单 =====
const jobDialogVisible = ref(false)
const editingJob = ref<Job | null>(null)
const jobTagsInput = ref('')
const jobRequirementsInput = ref('')
const jobForm = reactive<Job>(emptyJobForm())

function emptyJobForm(): Job {
  return {
    title: '', company: '', companyLogo: '', location: '', salary: '',
    type: '', tags: [], description: '', requirements: [], contact: '',
  }
}

function openJobDialog(job?: Job) {
  editingJob.value = job || null
  const src = job || emptyJobForm()
  Object.assign(jobForm, src)
  jobTagsInput.value = (src.tags || []).join(',')
  jobRequirementsInput.value = (src.requirements || []).join('\n')
  jobDialogVisible.value = true
}

async function onJobBeforeClose(action: string): Promise<boolean> {
  if (action !== 'confirm') return true
  if (!jobForm.title || !jobForm.company) {
    showToast('请填写岗位名称与公司')
    return false
  }
  const payload: Job = {
    ...jobForm,
    tags: jobTagsInput.value.split(/[,，]/).map(s => s.trim()).filter(Boolean),
    requirements: jobRequirementsInput.value.split('\n').map(s => s.trim()).filter(Boolean),
  }
  try {
    if (editingJob.value?.id) {
      await updateJob(editingJob.value.id, payload)
      showSuccessToast('岗位已更新')
    } else {
      await publishJob(payload)
      showSuccessToast('岗位已发布')
    }
    await loadJobs()
    return true
  } catch (e) {
    showToast((e as Error).message || '操作失败')
    return false
  }
}

async function onRemoveJob(job: Job) {
  if (!job.id) return
  try {
    await showConfirmDialog({ title: '确认删除', message: `删除岗位「${job.title}」？` })
  } catch {
    return
  }
  try {
    await deleteJob(job.id)
    showSuccessToast('已删除')
    await loadJobs()
  } catch (e) {
    showToast((e as Error).message || '删除失败')
  }
}

// ===== 黑客松表单 =====
const hackathonDialogVisible = ref(false)
const editingHackathon = ref<Hackathon | null>(null)
const hackathonTagsInput = ref('')
const hackathonRequirementsInput = ref('')
const hackathonForm = reactive<Hackathon>(emptyHackathonForm())

function emptyHackathonForm(): Hackathon {
  return {
    title: '', description: '', location: '', startDate: '', endDate: '',
    prize: '', tags: [], requirements: [], contact: '',
  }
}

function openHackathonDialog(h?: Hackathon) {
  editingHackathon.value = h || null
  const src = h || emptyHackathonForm()
  Object.assign(hackathonForm, src)
  hackathonTagsInput.value = (src.tags || []).join(',')
  hackathonRequirementsInput.value = (src.requirements || []).join('\n')
  hackathonDialogVisible.value = true
}

async function onHackathonBeforeClose(action: string): Promise<boolean> {
  if (action !== 'confirm') return true
  if (!hackathonForm.title) {
    showToast('请填写黑客松名称')
    return false
  }
  const payload: Hackathon = {
    ...hackathonForm,
    tags: hackathonTagsInput.value.split(/[,，]/).map(s => s.trim()).filter(Boolean),
    requirements: hackathonRequirementsInput.value.split('\n').map(s => s.trim()).filter(Boolean),
  }
  try {
    if (editingHackathon.value?.id) {
      await updateHackathon(editingHackathon.value.id, payload)
      showSuccessToast('黑客松已更新')
    } else {
      await publishHackathon(payload)
      showSuccessToast('黑客松已发布')
    }
    await loadHackathons()
    return true
  } catch (e) {
    showToast((e as Error).message || '操作失败')
    return false
  }
}

async function onRemoveHackathon(h: Hackathon) {
  if (!h.id) return
  try {
    await showConfirmDialog({ title: '确认删除', message: `删除黑客松「${h.title}」？` })
  } catch {
    return
  }
  try {
    await deleteHackathon(h.id)
    showSuccessToast('已删除')
    await loadHackathons()
  } catch (e) {
    showToast((e as Error).message || '删除失败')
  }
}

function goRegister() {
  router.push('/enterprise-register')
}

async function loadJobs() {
  try {
    jobs.value = await getMyJobs()
  } catch {
    jobs.value = []
  }
}

async function loadHackathons() {
  try {
    hackathons.value = await getMyHackathons()
  } catch {
    hackathons.value = []
  }
}

onMounted(async () => {
  try {
    user.value = await getCurrentUser()
  } catch {
    user.value = null
  }
  try {
    application.value = await getMyApplication()
  } catch {
    application.value = null
  }
  if (canPublish.value) {
    await Promise.all([loadJobs(), loadHackathons()])
  }
})
</script>

<style scoped>
.console-page {
  padding: var(--spacing-lg, 16px) 0 60px;
}

.status-card {
  margin: var(--spacing-lg, 16px);
  padding: var(--spacing-lg, 16px);
  background: var(--bg-card, #fff);
  border-radius: var(--radius-lg, 12px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.status-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-label {
  font-size: var(--font-size-md, 15px);
  font-weight: 600;
  color: var(--text-primary, #323233);
}

.status-tip {
  font-size: var(--font-size-sm, 13px);
  color: var(--text-tertiary, #969799);
  margin: var(--spacing-sm, 8px) 0;
}

.reject-reason {
  font-size: var(--font-size-sm, 13px);
  color: var(--danger, #ee0a24);
  margin: var(--spacing-sm, 8px) 0;
}

.register-btn {
  margin-top: var(--spacing-sm, 8px);
}

.action-bar {
  display: flex;
  gap: var(--spacing-md, 12px);
  padding: 0 var(--spacing-lg, 16px);
  margin-bottom: var(--spacing-lg, 16px);
}

.action-bar .van-button {
  flex: 1;
}

.section {
  margin-bottom: var(--spacing-lg, 16px);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg, 16px) var(--spacing-sm, 8px);
}

.section-title {
  font-size: var(--font-size-md, 15px);
  font-weight: 600;
  color: var(--text-primary, #323233);
}

.section-count {
  font-size: var(--font-size-sm, 13px);
  color: var(--text-tertiary, #969799);
}

.dialog-form {
  padding: var(--spacing-md, 12px) 0;
  max-height: 60vh;
  overflow-y: auto;
}
</style>
