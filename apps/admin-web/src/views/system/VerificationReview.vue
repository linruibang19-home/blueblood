<template>
  <div class="verification-review">
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
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
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
        <el-table-column prop="nickname" label="申请人" width="140" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="idNumber" label="证件号" min-width="180" show-overflow-tooltip />
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
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <template v-if="row.status === 'PENDING'">
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
    <el-drawer v-model="detailVisible" title="认证详情" size="440px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="ID">{{ current.id }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ current.nickname }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ current.realName }}</el-descriptions-item>
        <el-descriptions-item label="证件号">{{ current.idNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(current.status)" size="small">
            {{ statusLabel(current.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="认证材料">
          <div v-if="parsedMaterials.length" class="material-list">
            <a
              v-for="(url, i) in parsedMaterials"
              :key="i"
              :href="url"
              target="_blank"
              rel="noopener"
              class="material-link"
            >
              材料{{ i + 1 }}
            </a>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="驳回原因">
          {{ current.rejectReason || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核人">{{ current.reviewer || current.reviewerId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ current.reviewedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ current.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <!-- 驳回弹框 -->
    <el-dialog v-model="rejectVisible" title="驳回认证" width="440px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因">
          <el-input
            v-model="rejectForm.rejectReason"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getVerificationList,
  reviewVerification,
  type AdminVerificationVO,
  type VerificationStatus,
} from '@/api/admin-verification'

const loading = ref(false)
const list = ref<AdminVerificationVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{ status?: VerificationStatus }>({ status: undefined })

async function loadList() {
  loading.value = true
  try {
    const res = await getVerificationList({
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

function statusLabel(s: VerificationStatus) {
  return s === 'PENDING' ? '待审核' : s === 'APPROVED' ? '已通过' : '已驳回'
}

function statusTagType(s: VerificationStatus): 'warning' | 'success' | 'danger' {
  return s === 'PENDING' ? 'warning' : s === 'APPROVED' ? 'success' : 'danger'
}

// 详情
const detailVisible = ref(false)
const current = ref<AdminVerificationVO | null>(null)

const parsedMaterials = computed<string[]>(() => {
  if (!current.value?.materials) return []
  try {
    const parsed = JSON.parse(current.value.materials)
    return Array.isArray(parsed) ? parsed.filter((m) => typeof m === 'string') : []
  } catch {
    // 非 JSON 时按逗号/空白拆分
    return String(current.value.materials)
      .split(/[,\s]+/)
      .map((s) => s.trim())
      .filter(Boolean)
  }
})

function openDetail(row: AdminVerificationVO) {
  current.value = row
  detailVisible.value = true
}

// 通过
async function confirmApprove(row: AdminVerificationVO) {
  try {
    await ElMessageBox.confirm(
      `确定通过「${row.realName || row.nickname}」的认证申请？`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await reviewVerification(row.id, { status: 'APPROVED' })
    ElMessage.success('已通过')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}

// 驳回
const rejectVisible = ref(false)
const submitting = ref(false)
const rejectForm = reactive<{ id: number; rejectReason: string }>({
  id: 0,
  rejectReason: '',
})

function openReject(row: AdminVerificationVO) {
  rejectForm.id = row.id
  rejectForm.rejectReason = ''
  rejectVisible.value = true
}

async function submitReject() {
  if (!rejectForm.rejectReason.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  submitting.value = true
  try {
    await reviewVerification(rejectForm.id, {
      status: 'REJECTED',
      rejectReason: rejectForm.rejectReason,
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
.verification-review {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.material-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.material-link {
  color: var(--el-color-primary);
  text-decoration: none;
  word-break: break-all;
}
.material-link:hover {
  text-decoration: underline;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
