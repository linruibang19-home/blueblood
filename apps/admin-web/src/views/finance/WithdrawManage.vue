<template>
  <div class="withdraw-manage">
    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="状态">
          <el-select
            v-model="filter.status"
            placeholder="全部"
            clearable
            style="width: 160px"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="completed" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" min-width="140">
          <template #default="{ row }">
            {{ row.nickname || row.username || row.userId }}
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span class="amt">{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bankName" label="银行" width="140" show-overflow-tooltip />
        <el-table-column prop="bankAccount" label="账号" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="170">
          <template #default="{ row }">{{ row.createdAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <template v-if="row.status === 'pending'">
              <el-button link type="success" @click="confirmApprove(row)">通过</el-button>
              <el-button link type="danger" @click="openReject(row)">驳回</el-button>
            </template>
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
    <el-drawer v-model="detailVisible" title="提现详情" size="440px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="ID">{{ current.id }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ current.nickname || current.username || current.userId }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ current.amount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(current.status)" size="small">
            {{ statusLabel(current.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="银行">{{ current.bankName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ current.bankAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="失败原因">{{ current.failureReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ current.processedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ current.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <!-- 驳回弹框 -->
    <el-dialog v-model="rejectVisible" title="驳回提现" width="440px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="失败原因">
          <el-input
            v-model="rejectForm.failureReason"
            type="textarea"
            :rows="3"
            placeholder="请填写驳回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitReject">
          确认驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getWithdrawList,
  reviewWithdraw,
  type AdminWithdrawVO,
  type WithdrawStatus,
} from '@/api/admin-wallet'

const loading = ref(false)
const list = ref<AdminWithdrawVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

// 默认筛选 pending
const filter = reactive<{ status?: WithdrawStatus }>({ status: 'pending' })

async function loadList() {
  loading.value = true
  try {
    const res = await getWithdrawList({
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
  filter.status = undefined
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

function statusLabel(s: WithdrawStatus) {
  const map: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成',
    failed: '失败',
  }
  return map[s] || s
}

function statusTagType(s: WithdrawStatus): 'warning' | 'success' | 'danger' | 'info' {
  if (s === 'pending') return 'warning'
  if (s === 'processing') return 'warning'
  if (s === 'completed') return 'success'
  return 'danger'
}

// 详情
const detailVisible = ref(false)
const current = ref<AdminWithdrawVO | null>(null)

function openDetail(row: AdminWithdrawVO) {
  current.value = row
  detailVisible.value = true
}

// 通过
async function confirmApprove(row: AdminWithdrawVO) {
  try {
    await ElMessageBox.confirm(
      `确认通过「${row.nickname || row.username || row.userId}」的提现申请（金额 ${row.amount}）？该操作将置为已完成。`,
      '确认通过',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await reviewWithdraw(row.id, { action: 'APPROVED' })
    ElMessage.success('已通过')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}

// 驳回
const rejectVisible = ref(false)
const submitting = ref(false)
const rejectForm = reactive<{ id: number; failureReason: string }>({
  id: 0,
  failureReason: '',
})

function openReject(row: AdminWithdrawVO) {
  rejectForm.id = row.id
  rejectForm.failureReason = ''
  rejectVisible.value = true
}

async function submitReject() {
  if (!rejectForm.failureReason.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  submitting.value = true
  try {
    await reviewWithdraw(rejectForm.id, {
      action: 'REJECTED',
      failureReason: rejectForm.failureReason,
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
.withdraw-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.amt {
  font-weight: 600;
  color: var(--el-color-danger);
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
