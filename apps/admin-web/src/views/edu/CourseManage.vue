<template>
  <div class="course-manage">
    <!-- 筛选表单 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="标题/副标题"
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
            style="width: 150px"
          >
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已下架" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 课程表格 -->
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate">
          新增课程
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
        <el-table-column label="课程" min-width="220">
          <template #default="{ row }">
            <div class="course-cell">
              <el-image
                v-if="row.coverImage"
                :src="row.coverImage"
                fit="cover"
                class="cover"
              />
              <div class="course-titles">
                <div>{{ row.title || '-' }}</div>
                <div class="subtitle">{{ row.subtitle }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="instructor" label="讲师" width="120">
          <template #default="{ row }">{{ row.instructor || '-' }}</template>
        </el-table-column>
        <el-table-column prop="totalChapters" label="章节数" width="90" />
        <el-table-column prop="students" label="学员" width="80" />
        <el-table-column label="评分" width="80">
          <template #default="{ row }">{{ row.rating ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              编辑
            </el-button>
            <el-button
              link
              :type="row.status === 'PUBLISHED' ? 'warning' : 'success'"
              @click="confirmToggle(row)"
            >
              {{ row.status === 'PUBLISHED' ? '下架' : '上架' }}
            </el-button>
            <el-button link type="primary" @click="openChapters(row)">
              章节
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

    <!-- 新增/编辑课程 -->
    <el-dialog
      v-model="formVisible"
      :title="formMode === 'create' ? '新增课程' : '编辑课程'"
      width="560px"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="课程标题" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" placeholder="副标题" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="封面图 URL" />
        </el-form-item>
        <el-form-item label="讲师">
          <el-input v-model="form.instructor" placeholder="讲师姓名" />
        </el-form-item>
        <el-form-item label="讲师头像">
          <el-input v-model="form.instructorAvatar" placeholder="讲师头像 URL" />
        </el-form-item>
        <el-form-item label="总章节数">
          <el-input-number
            v-model="form.totalChapters"
            :min="0"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="奖励积分">
          <el-input-number
            v-model="form.rewardPoints"
            :min="0"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="评分">
          <el-input-number
            v-model="form.rating"
            :min="0"
            :max="5"
            :step="0.1"
            :precision="2"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 180px">
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已下架" value="OFFLINE" />
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

    <!-- 章节管理 -->
    <el-dialog
      v-model="chapterVisible"
      :title="`章节管理 - ${currentCourse?.title ?? ''}`"
      width="820px"
    >
      <div class="table-toolbar">
        <el-button type="primary" size="small" :icon="Plus" @click="openChapterCreate">
          新增章节
        </el-button>
      </div>
      <el-table
        v-loading="chapterLoading"
        :data="chapters"
        stripe
        border
        size="small"
        style="width: 100%"
      >
        <el-table-column prop="chapterOrder" label="序号" width="70" />
        <el-table-column prop="title" label="章节标题" min-width="200" />
        <el-table-column prop="duration" label="时长" width="100">
          <template #default="{ row }">{{ row.duration || '-' }}</template>
        </el-table-column>
        <el-table-column prop="videoUrl" label="视频地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.videoUrl || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openChapterEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="confirmChapterDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 新增/编辑章节 -->
      <el-dialog
        v-model="chapterFormVisible"
        :title="chapterFormMode === 'create' ? '新增章节' : '编辑章节'"
        width="480px"
        append-to-body
      >
        <el-form ref="chapterFormRef" :model="chapterForm" :rules="chapterRules" label-width="90px">
          <el-form-item label="章节标题" prop="title">
            <el-input v-model="chapterForm.title" placeholder="章节标题" />
          </el-form-item>
          <el-form-item label="时长">
            <el-input v-model="chapterForm.duration" placeholder="如 12:30" />
          </el-form-item>
          <el-form-item label="视频地址">
            <el-input v-model="chapterForm.videoUrl" placeholder="视频 URL" />
          </el-form-item>
          <el-form-item label="序号">
            <el-input-number v-model="chapterForm.chapterOrder" :min="1" controls-position="right" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="chapterFormVisible = false">取消</el-button>
          <el-button type="primary" :loading="chapterSubmitting" @click="submitChapterForm">
            确定
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
  createCourse,
  updateCourse,
  updateCourseStatus,
  deleteCourse,
  listChapters,
  createChapter,
  updateChapter,
  deleteChapter,
  type AdminCourseVO,
  type CourseStatus,
  type CourseRequest,
  type AdminChapterVO,
  type ChapterRequest,
} from '@/api/admin-course'

// ==================== 列表 ====================
const loading = ref(false)
const list = ref<AdminCourseVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{ keyword?: string; status?: CourseStatus }>({
  keyword: undefined,
  status: undefined,
})

async function loadList() {
  loading.value = true
  try {
    const res = await getCourseList({
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

function statusLabel(s: CourseStatus) {
  return s === 'PUBLISHED' ? '已发布' : s === 'DRAFT' ? '草稿' : '已下架'
}

function statusTagType(s: CourseStatus): 'success' | 'info' | 'warning' {
  return s === 'PUBLISHED' ? 'success' : s === 'DRAFT' ? 'info' : 'warning'
}

// ==================== 新增/编辑 ====================
const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<CourseRequest & { id?: number }>({
  id: undefined,
  title: '',
  subtitle: '',
  coverImage: '',
  instructor: '',
  instructorAvatar: '',
  totalChapters: 0,
  rewardPoints: 0,
  rating: 0,
  status: 'DRAFT',
})

const formRules: FormRules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
}

function resetForm() {
  form.id = undefined
  form.title = ''
  form.subtitle = ''
  form.coverImage = ''
  form.instructor = ''
  form.instructorAvatar = ''
  form.totalChapters = 0
  form.rewardPoints = 0
  form.rating = 0
  form.status = 'DRAFT'
}

function openCreate() {
  resetForm()
  formMode.value = 'create'
  formVisible.value = true
}

function openEdit(row: AdminCourseVO) {
  resetForm()
  formMode.value = 'edit'
  form.id = row.id
  form.title = row.title
  form.subtitle = row.subtitle || ''
  form.coverImage = row.coverImage || ''
  form.instructor = row.instructor || ''
  form.instructorAvatar = row.instructorAvatar || ''
  form.totalChapters = row.totalChapters ?? 0
  form.rewardPoints = row.rewardPoints ?? 0
  form.rating = row.rating != null ? Number(row.rating) : 0
  form.status = row.status
  formVisible.value = true
}

function buildRequest(): CourseRequest {
  return {
    title: form.title,
    subtitle: form.subtitle || undefined,
    coverImage: form.coverImage || undefined,
    instructor: form.instructor || undefined,
    instructorAvatar: form.instructorAvatar || undefined,
    totalChapters: form.totalChapters,
    rewardPoints: form.rewardPoints,
    rating: form.rating,
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
      await createCourse(buildRequest())
      ElMessage.success('新增成功')
    } else if (form.id) {
      await updateCourse(form.id, buildRequest())
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

// ==================== 上下架 ====================
async function confirmToggle(row: AdminCourseVO) {
  const next: CourseStatus = row.status === 'PUBLISHED' ? 'OFFLINE' : 'PUBLISHED'
  try {
    await ElMessageBox.confirm(
      `确定将课程「${row.title}」${next === 'PUBLISHED' ? '上架' : '下架'}？`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await updateCourseStatus(row.id, next)
    row.status = next
    ElMessage.success('状态已更新')
  } catch (e) {
    ElMessage.error((e as Error).message || '更新失败')
  }
}

// ==================== 删除 ====================
async function confirmDelete(row: AdminCourseVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除课程「${row.title}」？删除后不可恢复。`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteCourse(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

// ==================== 章节管理 ====================
const chapterVisible = ref(false)
const chapterLoading = ref(false)
const chapters = ref<AdminChapterVO[]>([])
const currentCourse = ref<AdminCourseVO | null>(null)

const chapterFormVisible = ref(false)
const chapterFormMode = ref<'create' | 'edit'>('create')
const chapterSubmitting = ref(false)
const chapterFormRef = ref<FormInstance>()

const chapterForm = reactive<ChapterRequest & { id?: number }>({
  id: undefined,
  courseId: 0,
  title: '',
  duration: '',
  videoUrl: '',
  chapterOrder: 1,
})

const chapterRules: FormRules = {
  title: [{ required: true, message: '章节标题不能为空', trigger: 'blur' }],
}

async function openChapters(row: AdminCourseVO) {
  currentCourse.value = row
  chapterVisible.value = true
  await loadChapters(row.id)
}

async function loadChapters(courseId: number) {
  chapterLoading.value = true
  try {
    chapters.value = await listChapters(courseId)
  } catch (e) {
    ElMessage.error((e as Error).message || '加载章节失败')
  } finally {
    chapterLoading.value = false
  }
}

function resetChapterForm() {
  chapterForm.id = undefined
  chapterForm.courseId = currentCourse.value?.id ?? 0
  chapterForm.title = ''
  chapterForm.duration = ''
  chapterForm.videoUrl = ''
  chapterForm.chapterOrder = (chapters.value.length || 0) + 1
}

function openChapterCreate() {
  resetChapterForm()
  chapterFormMode.value = 'create'
  chapterFormVisible.value = true
}

function openChapterEdit(row: AdminChapterVO) {
  resetChapterForm()
  chapterFormMode.value = 'edit'
  chapterForm.id = row.id
  chapterForm.title = row.title
  chapterForm.duration = row.duration || ''
  chapterForm.videoUrl = row.videoUrl || ''
  chapterForm.chapterOrder = row.chapterOrder ?? 1
  chapterFormVisible.value = true
}

function buildChapterRequest(): ChapterRequest {
  return {
    courseId: chapterForm.courseId,
    title: chapterForm.title,
    duration: chapterForm.duration || undefined,
    videoUrl: chapterForm.videoUrl || undefined,
    chapterOrder: chapterForm.chapterOrder,
  }
}

async function submitChapterForm() {
  if (!chapterFormRef.value) return
  try {
    await chapterFormRef.value.validate()
  } catch {
    return
  }
  chapterSubmitting.value = true
  try {
    if (chapterFormMode.value === 'create') {
      await createChapter(buildChapterRequest())
      ElMessage.success('新增成功')
    } else if (chapterForm.id) {
      await updateChapter(chapterForm.id, buildChapterRequest())
      ElMessage.success('更新成功')
    }
    chapterFormVisible.value = false
    if (currentCourse.value) {
      await loadChapters(currentCourse.value.id)
    }
  } catch (e) {
    ElMessage.error((e as Error).message || '提交失败')
  } finally {
    chapterSubmitting.value = false
  }
}

async function confirmChapterDelete(row: AdminChapterVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除章节「${row.title}」？`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteChapter(row.id)
    ElMessage.success('已删除')
    if (currentCourse.value) {
      await loadChapters(currentCourse.value.id)
    }
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.course-manage {
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
.course-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.cover {
  width: 48px;
  height: 32px;
  border-radius: 4px;
  flex-shrink: 0;
}
.course-titles .subtitle {
  font-size: 12px;
  color: #909399;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
