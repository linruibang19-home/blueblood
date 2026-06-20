<template>
  <div class="hackathon-manage">
    <!-- 筛选表单 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="黑客松标题"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filter.status"
            placeholder="全部"
            clearable
            style="width: 160px"
          >
            <el-option label="报名中" value="signup" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已结束" value="ended" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 黑客松表格 -->
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate">
          新增黑客松
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
        <el-table-column label="标题" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.title || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="奖金池" width="130">
          <template #default="{ row }">
            {{ formatPrize(row.prizePool) }}
          </template>
        </el-table-column>
        <el-table-column label="时间" width="220">
          <template #default="{ row }">
            <div class="time-cell">
              <span>开始：{{ row.startTime || '-' }}</span>
              <span>结束：{{ row.endTime || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="120">
          <template #default="{ row }">{{ row.location || '-' }}</template>
        </el-table-column>
        <el-table-column label="队伍" width="120">
          <template #default="{ row }">
            {{ row.currentTeams ?? 0 }} / {{ row.maxTeams ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布者" width="120">
          <template #default="{ row }">
            {{ row.publisherNickname || row.publishedBy || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="confirmDelete(row)">
              删除
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

    <!-- 新增/编辑黑客松 -->
    <el-dialog
      v-model="formVisible"
      :title="formMode === 'create' ? '新增黑客松' : '编辑黑客松'"
      width="640px"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="黑客松标题" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="封面图 URL" />
        </el-form-item>
        <el-form-item label="奖金池">
          <el-input v-model="prizeInput" placeholder="如 100000">
            <template #append>元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="报名截止">
          <el-date-picker
            v-model="form.signupDeadline"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="报名截止时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location" placeholder="如 线上 / 北京" />
        </el-form-item>
        <el-form-item label="最大队伍数">
          <el-input-number
            v-model="form.maxTeams"
            :min="0"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="当前队伍数">
          <el-input-number
            v-model="form.currentTeams"
            :min="0"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 160px">
            <el-option label="报名中" value="signup" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已结束" value="ended" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布者ID">
          <el-input-number
            v-model="form.publishedBy"
            :min="1"
            controls-position="right"
            placeholder="发布用户ID"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="活动描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getHackathonList,
  createHackathon,
  updateHackathon,
  deleteHackathon,
  type AdminHackathonVO,
  type HackathonStatus,
  type HackathonRequest,
} from '@/api/admin-hackathon'

const loading = ref(false)
const list = ref<AdminHackathonVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{ keyword?: string; status?: HackathonStatus }>({
  keyword: undefined,
  status: undefined,
})

async function loadList() {
  loading.value = true
  try {
    const res = await getHackathonList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: filter.keyword || undefined,
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

function statusLabel(s: HackathonStatus) {
  return s === 'signup' ? '报名中' : s === 'ongoing' ? '进行中' : '已结束'
}

function statusTagType(s: HackathonStatus): 'warning' | 'success' | 'info' {
  return s === 'signup' ? 'warning' : s === 'ongoing' ? 'success' : 'info'
}

function formatPrize(p?: string | number): string {
  if (p === undefined || p === null || p === '') return '-'
  const n = Number(p)
  if (Number.isNaN(n)) return String(p)
  return `￥${n.toLocaleString('zh-CN')}`
}

// ==================== 新增/编辑 ====================
const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const submitting = ref(false)
const formRef = ref<FormInstance>()
const prizeInput = ref('')

const form = reactive<HackathonRequest & { id?: number }>({
  id: undefined,
  title: '',
  description: '',
  coverImage: '',
  prizePool: undefined,
  startTime: '',
  endTime: '',
  signupDeadline: '',
  location: '',
  maxTeams: undefined,
  currentTeams: undefined,
  status: 'signup',
  publishedBy: undefined,
})

const formRules: FormRules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
}

function resetForm() {
  form.id = undefined
  form.title = ''
  form.description = ''
  form.coverImage = ''
  form.prizePool = undefined
  form.startTime = ''
  form.endTime = ''
  form.signupDeadline = ''
  form.location = ''
  form.maxTeams = undefined
  form.currentTeams = undefined
  form.status = 'signup'
  form.publishedBy = undefined
  prizeInput.value = ''
}

function openCreate() {
  resetForm()
  formMode.value = 'create'
  formVisible.value = true
}

function openEdit(row: AdminHackathonVO) {
  resetForm()
  formMode.value = 'edit'
  form.id = row.id
  form.title = row.title
  form.description = row.description || ''
  form.coverImage = row.coverImage || ''
  form.prizePool = row.prizePool
  form.startTime = row.startTime || ''
  form.endTime = row.endTime || ''
  form.signupDeadline = row.signupDeadline || ''
  form.location = row.location || ''
  form.maxTeams = row.maxTeams
  form.currentTeams = row.currentTeams
  form.status = row.status
  form.publishedBy = row.publishedBy
  prizeInput.value =
    row.prizePool === undefined || row.prizePool === null ? '' : String(row.prizePool)
  formVisible.value = true
}

function buildRequest(): HackathonRequest {
  const prizeStr = prizeInput.value.trim()
  return {
    title: form.title,
    description: form.description,
    coverImage: form.coverImage,
    prizePool: prizeStr === '' ? undefined : prizeStr,
    startTime: form.startTime,
    endTime: form.endTime,
    signupDeadline: form.signupDeadline,
    location: form.location,
    maxTeams: form.maxTeams,
    currentTeams: form.currentTeams,
    status: form.status,
    publishedBy: form.publishedBy,
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
      await createHackathon(buildRequest())
      ElMessage.success('新增成功')
    } else if (form.id) {
      await updateHackathon(form.id, buildRequest())
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

// ==================== 删除 ====================
async function confirmDelete(row: AdminHackathonVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除黑客松「${row.title}」？删除后不可恢复。`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteHackathon(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.hackathon-manage {
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
.time-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  line-height: 1.4;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
