<template>
  <div class="enterprise-review">
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
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="公司名/信用代码"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
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
        <el-table-column label="申请人" width="140">
          <template #default="{ row }">
            {{ row.nickname || row.userId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="companyName" label="公司名" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.companyName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="creditCode" label="信用代码" width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.creditCode || '-' }}</template>
        </el-table-column>
        <el-table-column prop="contact" label="联系人" width="140">
          <template #default="{ row }">{{ row.contact || '-' }}</template>
        </el-table-column>
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
    <el-drawer v-model="detailVisible" title="企业认证详情" size="480px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="ID">{{ current.id }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ current.nickname || current.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公司名">{{ current.companyName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="统一信用代码">{{ current.creditCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ current.contact || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(current.status)" size="small">
            {{ statusLabel(current.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="营业执照">
          <div v-if="current.licenseUrl" class="license-wrap">
            <el-image
              :src="current.licenseUrl"
              :preview-src-list="[current.licenseUrl]"
              fit="contain"
              class="license-img"
              :preview-teleported="true"
            />
            <a
              :href="current.licenseUrl"
              target="_blank"
              rel="noopener"
              class="license-link"
            >
              查看原图
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
      <div v-if="current && current.status === 'PENDING'" class="drawer-actions">
        <el-button type="success" @click="confirmApprove(current)">通过</el-button>
        <el-button type="danger" @click="openReject(current)">驳回</el-button>
      </div>
    </el-drawer>

    <!-- 驳回弹框 -->
    <el-dialog v-model="rejectVisible" title="驳回企业认证" width="440px">
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getEnterpriseList,
  reviewEnterprise,
  type AdminEnterpriseVO,
  type EnterpriseStatus,
} from '@/api/admin-enterprise'

const loading = ref(false)
const list = ref<AdminEnterpriseVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{ status?: EnterpriseStatus; keyword?: string }>({
  status: undefined,
  keyword: undefined,
})

async function loadList() {
  loading.value = true
  try {
    const res = await getEnterpriseList({
      page: page.value,
      pageSize: pageSize.value,
      status: filter.status,
      keyword: filter.keyword || undefined,
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
  filter.keyword = undefined
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

function statusLabel(s: EnterpriseStatus) {
  return s === 'PENDING' ? '待审核' : s === 'APPROVED' ? '已通过' : '已驳回'
}

function statusTagType(s: EnterpriseStatus): 'warning' | 'success' | 'danger' {
  return s === 'PENDING' ? 'warning' : s === 'APPROVED' ? 'success' : 'danger'
}

// 详情
const detailVisible = ref(false)
const current = ref<AdminEnterpriseVO | null>(null)

function openDetail(row: AdminEnterpriseVO) {
  current.value = row
  detailVisible.value = true
}

// 通过
async function confirmApprove(row: AdminEnterpriseVO) {
  try {
    await ElMessageBox.confirm(
      `确定通过「${row.companyName || row.nickname || row.id}」的企业认证？`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await reviewEnterprise(row.id, { status: 'APPROVED' })
    ElMessage.success('已通过')
    detailVisible.value = false
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

function openReject(row: AdminEnterpriseVO) {
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
    await reviewEnterprise(rejectForm.id, {
      status: 'REJECTED',
      rejectReason: rejectForm.rejectReason,
    })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    detailVisible.value = false
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
.enterprise-review {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.license-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}
.license-img {
  width: 100%;
  max-width: 360px;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
}
.license-link {
  color: var(--el-color-primary);
  text-decoration: none;
  word-break: break-all;
}
.license-link:hover {
  text-decoration: underline;
}
.drawer-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
