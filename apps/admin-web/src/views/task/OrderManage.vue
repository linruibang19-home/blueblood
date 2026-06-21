<template>
  <div class="order-manage">
    <!-- 筛选 -->
    <el-form :inline="true" :model="query" class="filter-bar">
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务ID">
        <el-input v-model.number="query.taskId" clearable placeholder="任务ID" style="width: 140px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="订单ID" width="80" />
      <el-table-column prop="taskTitle" label="任务" min-width="160" show-overflow-tooltip />
      <el-table-column label="接单者" width="120">
        <template #default="{ row }">{{ row.nickname || row.username || row.userId }}</template>
      </el-table-column>
      <el-table-column label="进度" width="90">
        <template #default="{ row }">{{ row.progress }}%</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="接单时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-if="!isFinal(row.status)" link type="danger" @click="onClose(row)">关闭</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @current-change="load"
      @size-change="load"
      class="pager"
    />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailShow" title="订单详情" width="560px">
      <div v-if="detail" class="detail">
        <div class="d-row"><span class="d-label">任务:</span>{{ detail.taskTitle }}</div>
        <div class="d-row"><span class="d-label">状态:</span>{{ statusLabel(detail.status) }} · 进度 {{ detail.progress }}%</div>
        <h4 class="d-sub">里程碑进度</h4>
        <el-timeline v-if="detail.milestones && detail.milestones.length">
          <el-timeline-item v-for="m in detail.milestones" :key="m.id" :type="msType(m.status)">
            <div class="ms-title">{{ m.title }} <span class="ms-reward">¥{{ m.reward }}</span></div>
            <div class="ms-status">{{ msLabel(m.status) }}</div>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="empty">无里程碑</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderList,
  closeOrder,
  getOrderDetail,
  type AdminTaskOrderVO,
  type OrderStatus,
} from '@/api/admin-task'

const loading = ref(false)
const list = ref<AdminTaskOrderVO[]>([])
const total = ref(0)
const query = reactive({
  page: 1,
  pageSize: 10,
  status: undefined as OrderStatus | undefined,
  taskId: undefined as number | undefined,
})

const statusOptions = [
  { value: 'in_progress', label: '执行中' },
  { value: 'wait_acceptance', label: '待验收' },
  { value: 'settled', label: '已结算' },
  { value: 'rejected', label: '已关闭/驳回' },
]

const STATUS_LABEL: Record<string, string> = {
  applied: '已报名',
  accepted: '已接单',
  in_progress: '执行中',
  wait_acceptance: '待验收',
  passed: '验收通过',
  rejected: '已关闭',
  settling: '结算中',
  settled: '已结算',
}
function statusLabel(s: string) {
  return STATUS_LABEL[s] || s
}
function statusType(s: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  if (['settled', 'passed'].includes(s)) return 'success'
  if (['rejected'].includes(s)) return 'danger'
  if (['wait_acceptance', 'settling'].includes(s)) return 'warning'
  return 'primary'
}
function isFinal(s: string) {
  return ['settled', 'rejected', 'passed'].includes(s)
}

const MS_LABEL: Record<string, string> = {
  NOT_STARTED: '未开始',
  SUBMITTED: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回',
}
function msLabel(s: string) {
  return MS_LABEL[s] || s
}
function msType(s: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  if (s === 'APPROVED') return 'success'
  if (s === 'REJECTED') return 'danger'
  if (s === 'SUBMITTED') return 'warning'
  return 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await getOrderList({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status,
      taskId: query.taskId,
    })
    list.value = res.list
    total.value = res.total
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}
onMounted(load)

function onSearch() {
  query.page = 1
  load()
}
function onReset() {
  query.status = undefined
  query.taskId = undefined
  query.page = 1
  load()
}

const detailShow = ref(false)
const detail = ref<any>(null)
async function openDetail(row: AdminTaskOrderVO) {
  try {
    detail.value = await getOrderDetail(row.id)
    detailShow.value = true
  } catch (e: any) {
    ElMessage.error(e.message || '详情加载失败')
  }
}

async function onClose(row: AdminTaskOrderVO) {
  try {
    await ElMessageBox.confirm(
      `确认强制关闭订单 #${row.id}(${row.taskTitle})?关闭后接单者将无法继续。`,
      '关闭订单',
      { type: 'warning' },
    )
    await closeOrder(row.id)
    ElMessage.success('已关闭')
    load()
  } catch {
    // 取消
  }
}
</script>

<style scoped>
.order-manage {
  padding: 16px;
}
.filter-bar {
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.detail .d-row {
  margin-bottom: 8px;
  font-size: 14px;
}
.d-label {
  color: #909399;
  margin-right: 8px;
}
.d-sub {
  margin: 16px 0 12px;
  font-size: 14px;
}
.ms-title {
  font-weight: 600;
}
.ms-reward {
  color: #e6a23c;
  margin-left: 8px;
  font-weight: normal;
}
.ms-status {
  color: #909399;
  font-size: 12px;
}
.empty {
  color: #c0c4cc;
  text-align: center;
  padding: 20px;
}
</style>
