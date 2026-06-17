<template>
  <div class="milestone-review">
    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="状态">
          <el-select
            v-model="filter.status"
            placeholder="里程碑状态"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="opt in statusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 提交列表 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="taskTitle" label="任务" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.taskTitle || row.taskId }}</template>
        </el-table-column>
        <el-table-column prop="milestoneTitle" label="里程碑" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.milestoneTitle || row.milestoneId }}</template>
        </el-table-column>
        <el-table-column label="提交用户" width="130">
          <template #default="{ row }">{{ row.userNickname || row.userId }}</template>
        </el-table-column>
        <el-table-column label="GitHub" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link
              v-if="row.githubUrl"
              :href="row.githubUrl"
              type="primary"
              target="_blank"
            >
              {{ row.githubUrl }}
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="170">
          <template #default="{ row }">{{ row.submittedAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
              link
              type="success"
              :disabled="row.status !== 'SUBMITTED'"
              @click="confirmApprove(row)"
            >
              通过
            </el-button>
            <el-button
              link
              type="danger"
              :disabled="row.status !== 'SUBMITTED'"
              @click="openReject(row)"
            >
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        :current-page="page"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      :title="`提交详情 - ${current?.milestoneTitle ?? ''}`"
      size="520px"
    >
      <div v-if="current" class="detail">
        <div class="row"><span class="label">任务：</span>{{ current.taskTitle || current.taskId }}</div>
        <div class="row"><span class="label">里程碑：</span>{{ current.milestoneTitle || current.milestoneId }}</div>
        <div class="row"><span class="label">提交用户：</span>{{ current.userNickname || current.userId }}</div>
        <div class="row"><span class="label">提交时间：</span>{{ current.submittedAt || '-' }}</div>
        <div class="row">
          <span class="label">GitHub：</span>
          <el-link v-if="current.githubUrl" :href="current.githubUrl" type="primary" target="_blank">
            {{ current.githubUrl }}
          </el-link>
          <span v-else>-</span>
        </div>
        <div class="row"><span class="label">说明：</span>{{ current.description || '-' }}</div>
        <div class="row attachments">
          <span class="label">附件：</span>
          <div v-if="parsedAttachments.length">
            <el-link
              v-for="(a, i) in parsedAttachments"
              :key="i"
              :href="a"
              type="primary"
              target="_blank"
              class="att-link"
            >
              {{ a }}
            </el-link>
          </div>
          <span v-else>无</span>
        </div>
      </div>
    </el-drawer>

    <!-- 驳回弹框 -->
    <el-dialog v-model="rejectVisible" title="驳回里程碑" width="480px">
      <el-form :model="rejectForm" label-width="90px">
        <el-form-item label="审核反馈">
          <el-input
            v-model="rejectForm.feedback"
            type="textarea"
            :rows="4"
            placeholder="请填写驳回原因 / 修改建议"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReject">
          确认驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMilestoneSubmissionList,
  reviewMilestone,
  type AdminMilestoneSubmissionVO,
  type MilestoneStatus,
} from '@/api/admin-task'

// ==================== 状态选项 ====================
const statusOptions: { value: MilestoneStatus; label: string }[] = [
  { value: 'NOT_STARTED', label: '未开始' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'SUBMITTED', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'OVERDUE', label: '已逾期' },
]

function statusLabel(s: MilestoneStatus) {
  return statusOptions.find((o) => o.value === s)?.label || s
}

function statusTagType(s: MilestoneStatus): 'success' | 'info' | 'warning' | 'danger' | 'primary' {
  switch (s) {
    case 'APPROVED':
      return 'success'
    case 'SUBMITTED':
      return 'warning'
    case 'REJECTED':
      return 'danger'
    case 'IN_PROGRESS':
      return 'primary'
    default:
      return 'info'
  }
}

// ==================== 列表 ====================
const loading = ref(false)
const list = ref<AdminMilestoneSubmissionVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{ status: MilestoneStatus }>({ status: 'SUBMITTED' })

async function loadList() {
  loading.value = true
  try {
    const res = await getMilestoneSubmissionList({
      page: page.value,
      pageSize: pageSize.value,
      status: filter.status,
    })
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error((e as Error).message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadList()
}

function handleReset() {
  filter.status = 'SUBMITTED'
  page.value = 1
  loadList()
}

function onPageChange(p: number) {
  page.value = p
  loadList()
}

function onSizeChange(s: number) {
  pageSize.value = s
  page.value = 1
  loadList()
}

// ==================== 详情 ====================
const detailVisible = ref(false)
const current = ref<AdminMilestoneSubmissionVO | null>(null)

const parsedAttachments = computed<string[]>(() => {
  const raw = current.value?.attachments
  if (!raw) return []
  try {
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr.filter((x) => typeof x === 'string') : []
  } catch {
    return []
  }
})

function openDetail(row: AdminMilestoneSubmissionVO) {
  current.value = row
  detailVisible.value = true
}

// ==================== 通过 ====================
async function confirmApprove(row: AdminMilestoneSubmissionVO) {
  try {
    await ElMessageBox.confirm(
      `确定通过里程碑「${row.milestoneTitle || row.milestoneId}」的提交？`,
      '确认',
      { type: 'success' }
    )
  } catch {
    return
  }
  try {
    await reviewMilestone(row.milestoneId, { result: 'APPROVED', feedback: '' })
    ElMessage.success('已通过')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}

// ==================== 驳回 ====================
const rejectVisible = ref(false)
const submitting = ref(false)
const rejectForm = reactive<{ milestoneId?: number; feedback: string }>({
  milestoneId: undefined,
  feedback: '',
})

function openReject(row: AdminMilestoneSubmissionVO) {
  rejectForm.milestoneId = row.milestoneId
  rejectForm.feedback = ''
  rejectVisible.value = true
}

async function submitReject() {
  if (!rejectForm.milestoneId) return
  submitting.value = true
  try {
    await reviewMilestone(rejectForm.milestoneId, {
      result: 'REJECTED',
      feedback: rejectForm.feedback || undefined,
    })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.milestone-review {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.detail .row {
  margin-bottom: 12px;
  line-height: 1.6;
  word-break: break-all;
}
.detail .label {
  color: #909399;
  margin-right: 4px;
}
.detail .att-link {
  display: block;
  margin-bottom: 4px;
}
</style>
