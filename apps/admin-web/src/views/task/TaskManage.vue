<template>
  <div class="task-manage">
    <!-- 筛选表单 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="标题/雇主"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="filter.categoryId"
            placeholder="全部"
            clearable
            style="width: 160px"
          >
            <el-option
              v-for="c in categories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filter.status"
            placeholder="全部"
            clearable
            style="width: 150px"
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

    <!-- 任务表格 -->
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate">
          发布任务
        </el-button>
      </div>
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">{{ row.category || '-' }}</template>
        </el-table-column>
        <el-table-column label="雇主" width="130">
          <template #default="{ row }">{{ row.employerName || '-' }}</template>
        </el-table-column>
        <el-table-column label="酬金" width="100">
          <template #default="{ row }">{{ row.reward ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="levelRequired" label="等级" width="70">
          <template #default="{ row }">{{ row.levelRequired ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="名额" width="110">
          <template #default="{ row }">
            {{ row.slotsLeft ?? 0 }} / {{ row.totalSlots ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              link
              type="warning"
              :disabled="row.status === 'CLOSED'"
              @click="confirmClose(row)"
            >
              关闭
            </el-button>
            <el-button link type="primary" @click="openOrders(row)">接单记录</el-button>
            <el-button link type="danger" @click="confirmDelete(row)">删除</el-button>
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

    <!-- 新增/编辑任务 -->
    <el-dialog
      v-model="formVisible"
      :title="formMode === 'create' ? '发布任务' : '编辑任务'"
      width="600px"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="任务标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="form.categoryId"
            placeholder="选择分类"
            clearable
            style="width: 100%"
            @change="onCategoryChange"
          >
            <el-option
              v-for="c in categories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="雇主">
          <el-input v-model="form.employerName" placeholder="雇主名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="酬金">
          <el-input-number
            v-model="form.reward"
            :min="0"
            :precision="2"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="所需等级">
          <el-input-number v-model="form.levelRequired" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="总名额">
          <el-input-number v-model="form.totalSlots" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
            v-model="form.deadline"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 200px">
            <el-option
              v-for="opt in statusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 接单记录 -->
    <el-dialog
      v-model="orderVisible"
      :title="`接单记录 - ${currentTask?.title ?? ''}`"
      width="860px"
    >
      <el-table
        v-loading="orderLoading"
        :data="orders"
        stripe
        border
        size="small"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" min-width="150">
          <template #default="{ row }">
            {{ row.nickname || row.username || row.userId }}
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="90">
          <template #default="{ row }">{{ row.progress ?? 0 }}%</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="orderStatusTagType(row.status)">
              {{ orderStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ row.createdAt || '-' }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pager"
        :current-page="orderPage"
        :page-size="orderPageSize"
        :total="orderTotal"
        layout="total, prev, pager, next"
        background
        @current-change="onOrderPageChange"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
} from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getTaskList,
  createTask,
  updateTask,
  updateTaskStatus,
  deleteTask,
  listTaskCategories,
  getOrderList,
  type AdminTaskVO,
  type TaskRequest,
  type TaskStatus,
  type TaskCategoryVO,
  type AdminTaskOrderVO,
  type OrderStatus,
} from '@/api/admin-task'

// ==================== 状态选项 ====================
const statusOptions: { value: TaskStatus; label: string }[] = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PENDING_REVIEW', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'RECRUITING', label: '招募中' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CLOSED', label: '已关闭' },
]

function statusLabel(s: TaskStatus) {
  return statusOptions.find((o) => o.value === s)?.label || s
}

function statusTagType(s: TaskStatus): 'success' | 'info' | 'warning' | 'danger' | 'primary' {
  switch (s) {
    case 'RECRUITING':
      return 'success'
    case 'IN_PROGRESS':
      return 'primary'
    case 'COMPLETED':
      return 'info'
    case 'CLOSED':
      return 'danger'
    case 'PENDING_REVIEW':
      return 'warning'
    default:
      return 'info'
  }
}

function orderStatusLabel(s: OrderStatus) {
  const map: Record<OrderStatus, string> = {
    applied: '已申请',
    accepted: '已接单',
    in_progress: '进行中',
    wait_acceptance: '待验收',
    passed: '已通过',
    rejected: '已驳回',
    settling: '结算中',
    settled: '已结算',
  }
  return map[s] || s
}

function orderStatusTagType(s: OrderStatus): 'success' | 'info' | 'warning' | 'danger' {
  if (s === 'passed' || s === 'settled') return 'success'
  if (s === 'rejected') return 'danger'
  if (s === 'wait_acceptance' || s === 'settling') return 'warning'
  return 'info'
}

// ==================== 分类 ====================
const categories = ref<TaskCategoryVO[]>([])
async function loadCategories() {
  try {
    categories.value = await listTaskCategories()
  } catch {
    categories.value = []
  }
}

// ==================== 列表 ====================
const loading = ref(false)
const list = ref<AdminTaskVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{ keyword?: string; categoryId?: number; status?: TaskStatus }>({
  keyword: undefined,
  categoryId: undefined,
  status: undefined,
})

async function loadList() {
  loading.value = true
  try {
    const res = await getTaskList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: filter.keyword || undefined,
      category: filter.categoryId,
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
  filter.keyword = undefined
  filter.categoryId = undefined
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

// ==================== 新增/编辑 ====================
const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<TaskRequest & { id?: number }>({
  id: undefined,
  title: '',
  categoryId: undefined,
  category: undefined,
  employerName: '',
  description: '',
  reward: 0,
  levelRequired: 0,
  totalSlots: 0,
  deadline: '',
  status: 'APPROVED',
})

const formRules: FormRules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
}

function resetForm() {
  form.id = undefined
  form.title = ''
  form.categoryId = undefined
  form.category = undefined
  form.employerName = ''
  form.description = ''
  form.reward = 0
  form.levelRequired = 0
  form.totalSlots = 0
  form.deadline = ''
  form.status = 'APPROVED'
}

function openCreate() {
  resetForm()
  formMode.value = 'create'
  formVisible.value = true
}

function openEdit(row: AdminTaskVO) {
  resetForm()
  formMode.value = 'edit'
  form.id = row.id
  form.title = row.title
  form.categoryId = row.categoryId
  form.category = row.category
  form.employerName = row.employerName || ''
  form.description = row.description || ''
  form.reward = row.reward != null ? Number(row.reward) : 0
  form.levelRequired = row.levelRequired ?? 0
  form.totalSlots = row.totalSlots ?? 0
  form.deadline = row.deadline || ''
  form.status = row.status
  formVisible.value = true
}

function onCategoryChange(id?: number) {
  const c = categories.value.find((x) => x.id === id)
  form.category = c?.name
}

function buildRequest(): TaskRequest {
  return {
    title: form.title,
    categoryId: form.categoryId,
    category: form.category,
    employerName: form.employerName || undefined,
    description: form.description || undefined,
    reward: form.reward,
    levelRequired: form.levelRequired,
    totalSlots: form.totalSlots,
    deadline: form.deadline || undefined,
    status: form.status,
  }
}

async function submitForm() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (formMode.value === 'create') {
      await createTask(buildRequest())
      ElMessage.success('发布成功')
    } else if (form.id) {
      await updateTask(form.id, buildRequest())
      ElMessage.success('更新成功')
    }
    formVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// ==================== 关闭 ====================
async function confirmClose(row: AdminTaskVO) {
  try {
    await ElMessageBox.confirm(`确定关闭任务「${row.title}」？关闭后不再招募。`, '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await updateTaskStatus(row.id, 'CLOSED')
    ElMessage.success('已关闭')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}

// ==================== 删除 ====================
async function confirmDelete(row: AdminTaskVO) {
  try {
    await ElMessageBox.confirm(`确定删除任务「${row.title}」？删除后不可恢复。`, '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteTask(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

// ==================== 接单记录 ====================
const orderVisible = ref(false)
const orderLoading = ref(false)
const orders = ref<AdminTaskOrderVO[]>([])
const orderTotal = ref(0)
const orderPage = ref(1)
const orderPageSize = ref(10)
const currentTask = ref<AdminTaskVO | null>(null)

async function openOrders(row: AdminTaskVO) {
  currentTask.value = row
  orderVisible.value = true
  orderPage.value = 1
  await loadOrders(row.id)
}

async function loadOrders(taskId: number) {
  orderLoading.value = true
  try {
    const res = await getOrderList({
      page: orderPage.value,
      pageSize: orderPageSize.value,
      taskId,
    })
    orders.value = res.list || []
    orderTotal.value = res.total || 0
  } catch (e) {
    ElMessage.error((e as Error).message || '加载接单记录失败')
  } finally {
    orderLoading.value = false
  }
}

function onOrderPageChange(p: number) {
  orderPage.value = p
  if (currentTask.value) {
    loadOrders(currentTask.value.id)
  }
}

onMounted(() => {
  loadCategories()
  loadList()
})
</script>

<style scoped>
.task-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.table-toolbar {
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
