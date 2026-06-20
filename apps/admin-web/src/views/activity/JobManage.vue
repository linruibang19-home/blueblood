<template>
  <div class="job-manage">
    <!-- 筛选表单 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="岗位标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="公司">
          <el-input
            v-model="filter.company"
            placeholder="公司名"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filter.status"
            placeholder="全部"
            clearable
            style="width: 140px"
          >
            <el-option label="招聘中" value="ACTIVE" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select
            v-model="filter.type"
            placeholder="全部"
            clearable
            style="width: 140px"
          >
            <el-option label="实习" value="实习" />
            <el-option label="兼职" value="兼职" />
            <el-option label="全职" value="全职" />
            <el-option label="外包" value="外包" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 岗位表格 -->
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate">
          新增岗位
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
        <el-table-column prop="company" label="公司" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.company || '-' }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="110">
          <template #default="{ row }">{{ row.location || '-' }}</template>
        </el-table-column>
        <el-table-column prop="salary" label="薪资" width="120">
          <template #default="{ row }">{{ row.salary || '-' }}</template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">{{ row.type || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
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
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button
              link
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="confirmToggle(row)"
            >
              {{ row.status === 'ACTIVE' ? '关闭' : '开启' }}
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

    <!-- 新增/编辑岗位 -->
    <el-dialog
      v-model="formVisible"
      :title="formMode === 'create' ? '新增岗位' : '编辑岗位'"
      width="600px"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="92px">
        <el-form-item label="岗位标题" prop="title">
          <el-input v-model="form.title" placeholder="岗位标题" />
        </el-form-item>
        <el-form-item label="公司">
          <el-input v-model="form.company" placeholder="公司名" />
        </el-form-item>
        <el-form-item label="公司Logo">
          <el-input v-model="form.companyLogo" placeholder="Logo URL" />
        </el-form-item>
        <el-form-item label="工作地点">
          <el-input v-model="form.location" placeholder="如 北京/海淀" />
        </el-form-item>
        <el-form-item label="薪资">
          <el-input v-model="form.salary" placeholder="如 15-25K·14薪" />
        </el-form-item>
        <el-form-item label="岗位类型">
          <el-select v-model="form.type" style="width: 160px" clearable>
            <el-option label="实习" value="实习" />
            <el-option label="兼职" value="兼职" />
            <el-option label="全职" value="全职" />
            <el-option label="外包" value="外包" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input
            v-model="tagsInput"
            placeholder="多个标签用逗号分隔，如 Java,应届,远程"
          />
        </el-form-item>
        <el-form-item label="岗位描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="岗位描述"
          />
        </el-form-item>
        <el-form-item label="任职要求">
          <el-input
            v-model="requirementsInput"
            type="textarea"
            :rows="3"
            placeholder="每行一条，自动转为列表"
          />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contact" placeholder="邮箱/电话" />
        </el-form-item>
        <el-form-item label="发布者ID">
          <el-input-number
            v-model="form.publishedBy"
            :min="1"
            controls-position="right"
            placeholder="发布用户ID"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 160px">
            <el-option label="招聘中" value="ACTIVE" />
            <el-option label="已关闭" value="CLOSED" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getJobList,
  createJob,
  updateJob,
  deleteJob,
  type AdminJobVO,
  type JobStatus,
  type JobType,
  type JobRequest,
} from '@/api/admin-job'

const loading = ref(false)
const list = ref<AdminJobVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{
  keyword?: string
  company?: string
  status?: JobStatus
  type?: JobType
}>({
  keyword: undefined,
  company: undefined,
  status: undefined,
  type: undefined,
})

async function loadList() {
  loading.value = true
  try {
    const res = await getJobList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: filter.keyword || undefined,
      company: filter.company || undefined,
      status: filter.status,
      type: filter.type,
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
  filter.company = undefined
  filter.status = undefined
  filter.type = undefined
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

function statusLabel(s: JobStatus) {
  return s === 'ACTIVE' ? '招聘中' : '已关闭'
}

function statusTagType(s: JobStatus): 'success' | 'info' {
  return s === 'ACTIVE' ? 'success' : 'info'
}

// ==================== 新增/编辑 ====================
const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const submitting = ref(false)
const formRef = ref<FormInstance>()
const tagsInput = ref('')
const requirementsInput = ref('')

const form = reactive<JobRequest & { id?: number }>({
  id: undefined,
  title: '',
  company: '',
  companyLogo: '',
  location: '',
  salary: '',
  type: undefined,
  tags: '',
  description: '',
  requirements: '',
  contact: '',
  status: 'ACTIVE',
  publishedBy: undefined,
})

const formRules: FormRules = {
  title: [{ required: true, message: '岗位标题不能为空', trigger: 'blur' }],
}

function splitToJson(raw: string): string {
  const arr = raw
    .split(/[\n,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
  return JSON.stringify(arr)
}

function joinFromJson(raw?: string): string {
  if (!raw) return ''
  try {
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr.join('\n') : raw
  } catch {
    return raw
  }
}

function resetForm() {
  form.id = undefined
  form.title = ''
  form.company = ''
  form.companyLogo = ''
  form.location = ''
  form.salary = ''
  form.type = undefined
  form.tags = ''
  form.description = ''
  form.requirements = ''
  form.contact = ''
  form.status = 'ACTIVE'
  form.publishedBy = undefined
  tagsInput.value = ''
  requirementsInput.value = ''
}

function openCreate() {
  resetForm()
  formMode.value = 'create'
  formVisible.value = true
}

function openEdit(row: AdminJobVO) {
  resetForm()
  formMode.value = 'edit'
  form.id = row.id
  form.title = row.title
  form.company = row.company || ''
  form.companyLogo = row.companyLogo || ''
  form.location = row.location || ''
  form.salary = row.salary || ''
  form.type = row.type
  form.tags = row.tags || ''
  form.description = row.description || ''
  form.requirements = row.requirements || ''
  form.contact = row.contact || ''
  form.status = row.status
  form.publishedBy = row.publishedBy
  tagsInput.value = joinFromJson(row.tags)
  requirementsInput.value = joinFromJson(row.requirements)
  formVisible.value = true
}

function buildRequest(): JobRequest {
  return {
    title: form.title,
    company: form.company,
    companyLogo: form.companyLogo,
    location: form.location,
    salary: form.salary,
    type: form.type,
    tags: splitToJson(tagsInput.value),
    description: form.description,
    requirements: splitToJson(requirementsInput.value),
    contact: form.contact,
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
      await createJob(buildRequest())
      ElMessage.success('新增成功')
    } else if (form.id) {
      await updateJob(form.id, buildRequest())
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

// ==================== 开启/关闭 ====================
async function confirmToggle(row: AdminJobVO) {
  const next: JobStatus = row.status === 'ACTIVE' ? 'CLOSED' : 'ACTIVE'
  try {
    await ElMessageBox.confirm(
      `确定将岗位「${row.title}」${next === 'ACTIVE' ? '开启' : '关闭'}？`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await updateJob(row.id, { ...buildRowRequest(row), status: next })
    await loadList()
    ElMessage.success('状态已更新')
  } catch (e) {
    ElMessage.error((e as Error).message || '更新失败')
  }
}

/** 用表格行构造请求体（关闭/开启只改 status，其余字段原样回填） */
function buildRowRequest(row: AdminJobVO): JobRequest {
  return {
    title: row.title,
    company: row.company,
    companyLogo: row.companyLogo,
    location: row.location,
    salary: row.salary,
    type: row.type,
    tags: row.tags,
    description: row.description,
    requirements: row.requirements,
    contact: row.contact,
    status: row.status,
    publishedBy: row.publishedBy,
  }
}

// ==================== 删除 ====================
async function confirmDelete(row: AdminJobVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除岗位「${row.title}」？删除后不可恢复。`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteJob(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.job-manage {
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
