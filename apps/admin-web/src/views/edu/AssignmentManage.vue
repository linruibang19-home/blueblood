<template>
  <div class="assignment-manage">
    <!-- 筛选表单 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="所属课程">
          <el-select
            v-model="filter.courseId"
            placeholder="全部课程"
            clearable
            filterable
            style="width: 220px"
          >
            <el-option
              v-for="c in courseOptions"
              :key="c.id"
              :label="c.title"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="标题/描述"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filter.status"
            placeholder="全部"
            clearable
            style="width: 150px"
          >
            <el-option label="未提交" value="not_submitted" />
            <el-option label="已提交" value="submitted" />
            <el-option label="已评分" value="graded" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 作业表格 -->
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate">
          新增作业
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
        <el-table-column prop="title" label="作业标题" min-width="200" />
        <el-table-column prop="courseTitle" label="所属课程" width="200">
          <template #default="{ row }">{{ row.courseTitle || row.courseId }}</template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="170">
          <template #default="{ row }">{{ row.deadline || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="assignStatusTagType(row.status)" size="small">
              {{ assignStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button link type="primary" @click="openSubmissions(row)">
              提交记录
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

    <!-- 新增/编辑作业 -->
    <el-dialog
      v-model="formVisible"
      :title="formMode === 'create' ? '新增作业' : '编辑作业'"
      width="600px"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="所属课程" prop="courseId">
          <el-select
            v-model="form.courseId"
            placeholder="选择课程"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="c in courseOptions"
              :key="c.id"
              :label="c.title"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属章节">
          <el-select
            v-model="form.chapterId"
            placeholder="可空"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="ch in chapterOptions"
              :key="ch.id"
              :label="`#${ch.chapterOrder ?? ''} ${ch.title}`"
              :value="ch.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="form.title" placeholder="作业标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="作业描述" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="参考答案">
          <el-input v-model="form.answer" type="textarea" :rows="4" placeholder="参考答案（仅评分后对用户可见）" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 180px">
            <el-option label="未提交" value="not_submitted" />
            <el-option label="已提交" value="submitted" />
            <el-option label="已评分" value="graded" />
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

    <!-- 提交记录 -->
    <el-dialog
      v-model="submissionVisible"
      :title="`提交记录 - ${currentAssignment?.title ?? ''}`"
      width="920px"
    >
      <div class="submission-filter">
        <el-radio-group v-model="submissionStatusFilter" @change="onSubFilterChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="submitted">待批改</el-radio-button>
          <el-radio-button label="graded">已评分</el-radio-button>
        </el-radio-group>
      </div>
      <el-table
        v-loading="submissionLoading"
        :data="submissions"
        stripe
        border
        size="small"
        style="width: 100%"
      >
        <el-table-column prop="studentNickname" label="学生" width="120">
          <template #default="{ row }">
            {{ row.studentNickname || row.userId }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="提交内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.content || '-' }}</template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="170">
          <template #default="{ row }">{{ row.submittedAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="subStatusTagType(row.status)" size="small">
              {{ subStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分数" width="80">
          <template #default="{ row }">{{ row.score ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openGrade(row)">
              {{ row.status === 'graded' ? '重新批改' : '批改' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pager"
        :current-page="subPage"
        :page-size="subPageSize"
        :total="subTotal"
        layout="total, prev, pager, next"
        background
        @current-change="onSubPageChange"
      />

      <!-- 批改 -->
      <el-dialog
        v-model="gradeVisible"
        title="批改作业"
        width="480px"
        append-to-body
      >
        <el-form ref="gradeFormRef" :model="gradeForm" :rules="gradeRules" label-width="90px">
          <el-form-item label="学生">
            <span>{{ gradingTarget?.studentNickname || gradingTarget?.userId }}</span>
          </el-form-item>
          <el-form-item label="提交内容">
            <div class="grade-content">{{ gradingTarget?.content || '-' }}</div>
          </el-form-item>
          <el-form-item label="分数" prop="score">
            <el-input-number
              v-model="gradeForm.score"
              :min="0"
              :max="100"
              :step="1"
              :precision="2"
              controls-position="right"
            />
          </el-form-item>
          <el-form-item label="批改反馈">
            <el-input v-model="gradeForm.feedback" type="textarea" :rows="3" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="gradeVisible = false">取消</el-button>
          <el-button type="primary" :loading="gradeSubmitting" @click="submitGrade">
            提交批改
          </el-button>
        </template>
      </el-dialog>
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
  getCourseList,
  listChapters,
  getAssignmentList,
  createAssignment,
  updateAssignment,
  deleteAssignment,
  getSubmissionList,
  gradeSubmission,
  type AdminCourseVO,
  type AdminChapterVO,
  type AdminAssignmentVO,
  type AssignmentStatus,
  type AssignmentRequest,
  type AdminSubmissionVO,
  type SubmissionStatus,
} from '@/api/admin-course'

// ==================== 课程下拉 ====================
const courseOptions = ref<AdminCourseVO[]>([])

async function loadCourseOptions() {
  try {
    const res = await getCourseList({ page: 1, pageSize: 100 })
    courseOptions.value = res.list || []
  } catch {
    courseOptions.value = []
  }
}

// ==================== 作业列表 ====================
const loading = ref(false)
const list = ref<AdminAssignmentVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{
  courseId?: number
  keyword?: string
  status?: AssignmentStatus
}>({
  courseId: undefined,
  keyword: undefined,
  status: undefined,
})

async function loadList() {
  loading.value = true
  try {
    const res = await getAssignmentList({
      page: page.value,
      pageSize: pageSize.value,
      courseId: filter.courseId,
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
  filter.courseId = undefined
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

function assignStatusLabel(s?: string) {
  return s === 'submitted'
    ? '已提交'
    : s === 'graded'
      ? '已评分'
      : '未提交'
}

function assignStatusTagType(s?: string): 'success' | 'warning' | 'info' {
  return s === 'graded' ? 'success' : s === 'submitted' ? 'warning' : 'info'
}

// ==================== 新增/编辑 ====================
const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const submitting = ref(false)
const formRef = ref<FormInstance>()
const chapterOptions = ref<AdminChapterVO[]>([])

const form = reactive<AssignmentRequest & { id?: number }>({
  id: undefined,
  courseId: 0,
  chapterId: undefined,
  title: '',
  description: '',
  deadline: '',
  answer: '',
  status: 'not_submitted',
})

const formRules: FormRules = {
  title: [{ required: true, message: '作业标题不能为空', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
}

function resetForm() {
  form.id = undefined
  form.courseId = filter.courseId ?? 0
  form.chapterId = undefined
  form.title = ''
  form.description = ''
  form.deadline = ''
  form.answer = ''
  form.status = 'not_submitted'
  chapterOptions.value = []
}

async function loadChapterOptions(courseId: number) {
  if (!courseId) {
    chapterOptions.value = []
    return
  }
  try {
    chapterOptions.value = await listChapters(courseId)
  } catch {
    chapterOptions.value = []
  }
}

function openCreate() {
  resetForm()
  formMode.value = 'create'
  formVisible.value = true
  if (form.courseId) {
    loadChapterOptions(form.courseId)
  }
}

async function openEdit(row: AdminAssignmentVO) {
  resetForm()
  formMode.value = 'edit'
  form.id = row.id
  form.courseId = row.courseId
  form.chapterId = row.chapterId
  form.title = row.title
  form.description = row.description || ''
  form.deadline = row.deadline || ''
  form.answer = row.answer || ''
  form.status = row.status
  formVisible.value = true
  await loadChapterOptions(row.courseId)
}

function buildRequest(): AssignmentRequest {
  return {
    courseId: form.courseId,
    chapterId: form.chapterId || undefined,
    title: form.title,
    description: form.description || undefined,
    deadline: form.deadline || undefined,
    answer: form.answer || undefined,
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
      await createAssignment(buildRequest())
      ElMessage.success('新增成功')
    } else if (form.id) {
      await updateAssignment(form.id, buildRequest())
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
async function confirmDelete(row: AdminAssignmentVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除作业「${row.title}」？删除后不可恢复。`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteAssignment(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

// ==================== 提交记录 ====================
const submissionVisible = ref(false)
const submissionLoading = ref(false)
const submissions = ref<AdminSubmissionVO[]>([])
const subTotal = ref(0)
const subPage = ref(1)
const subPageSize = ref(10)
const submissionStatusFilter = ref<SubmissionStatus | ''>('')
const currentAssignment = ref<AdminAssignmentVO | null>(null)

async function openSubmissions(row: AdminAssignmentVO) {
  currentAssignment.value = row
  submissionVisible.value = true
  submissionStatusFilter.value = ''
  subPage.value = 1
  await loadSubmissions()
}

async function loadSubmissions() {
  if (!currentAssignment.value) return
  submissionLoading.value = true
  try {
    const res = await getSubmissionList({
      page: subPage.value,
      pageSize: subPageSize.value,
      assignmentId: currentAssignment.value.id,
      status: submissionStatusFilter.value || undefined,
    })
    submissions.value = res.list || []
    subTotal.value = res.total || 0
  } catch (e) {
    ElMessage.error((e as Error).message || '加载提交记录失败')
  } finally {
    submissionLoading.value = false
  }
}

function onSubFilterChange() {
  subPage.value = 1
  loadSubmissions()
}

function onSubPageChange(p: number) {
  subPage.value = p
  loadSubmissions()
}

function subStatusLabel(s: SubmissionStatus) {
  return s === 'graded' ? '已评分' : '待批改'
}

function subStatusTagType(s: SubmissionStatus): 'success' | 'warning' {
  return s === 'graded' ? 'success' : 'warning'
}

// ==================== 批改 ====================
const gradeVisible = ref(false)
const gradeSubmitting = ref(false)
const gradeFormRef = ref<FormInstance>()
const gradingTarget = ref<AdminSubmissionVO | null>(null)

const gradeForm = reactive<{ score: number; feedback: string }>({
  score: 0,
  feedback: '',
})

const gradeRules: FormRules = {
  score: [{ required: true, message: '请输入分数', trigger: 'blur' }],
}

function openGrade(row: AdminSubmissionVO) {
  gradingTarget.value = row
  gradeForm.score = row.score != null ? Number(row.score) : 0
  gradeForm.feedback = row.feedback || ''
  gradeVisible.value = true
}

async function submitGrade() {
  if (!gradeFormRef.value || !gradingTarget.value) return
  try {
    await gradeFormRef.value.validate()
  } catch {
    return
  }
  gradeSubmitting.value = true
  try {
    await gradeSubmission(gradingTarget.value.id, {
      score: gradeForm.score,
      feedback: gradeForm.feedback || undefined,
    })
    ElMessage.success('批改成功')
    gradeVisible.value = false
    await loadSubmissions()
  } catch (e) {
    ElMessage.error((e as Error).message || '批改失败')
  } finally {
    gradeSubmitting.value = false
  }
}

onMounted(() => {
  loadCourseOptions()
  loadList()
})
</script>

<style scoped>
.assignment-manage {
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
.submission-filter {
  margin-bottom: 12px;
}
.grade-content {
  max-height: 120px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  font-size: 13px;
}
</style>
