<template>
  <div class="group-manage">
    <!-- 筛选表单 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="小组名/简介"
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
            style="width: 140px"
          >
            <el-option label="上架" value="ACTIVE" />
            <el-option label="下架" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 小组表格 -->
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" :icon="Plus" @click="openCreate">
          新增小组
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
        <el-table-column label="小组" min-width="200">
          <template #default="{ row }">
            <div class="group-cell">
              <el-image
                v-if="row.coverImage"
                :src="row.coverImage"
                fit="cover"
                class="cover"
              />
              <span>{{ row.name || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="leaderNickname" label="组长" width="120">
          <template #default="{ row }">
            {{ row.leaderNickname || row.leaderId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="成员数" width="90" />
        <el-table-column prop="postCount" label="帖子数" width="90" />
        <el-table-column prop="activityCount" label="活动数" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
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
              {{ row.status === 'ACTIVE' ? '下架' : '上架' }}
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

    <!-- 新增/编辑小组 -->
    <el-dialog
      v-model="formVisible"
      :title="formMode === 'create' ? '新增小组' : '编辑小组'"
      width="520px"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="92px">
        <el-form-item label="小组名称" prop="name">
          <el-input v-model="form.name" placeholder="小组名称" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="小组简介"
          />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="封面图 URL" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="如 AI" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input
            v-model="tagsInput"
            placeholder="多个标签用逗号分隔，如 学习,求职"
          />
        </el-form-item>
        <el-form-item label="组长ID">
          <el-input-number
            v-model="form.leaderId"
            :min="1"
            controls-position="right"
            placeholder="组长用户ID"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 160px">
            <el-option label="上架" value="ACTIVE" />
            <el-option label="下架" value="INACTIVE" />
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
  getGroupList,
  createGroup,
  updateGroup,
  updateGroupStatus,
  deleteGroup,
  type AdminGroupVO,
  type GroupStatus,
  type GroupRequest,
} from '@/api/admin-community'

const loading = ref(false)
const list = ref<AdminGroupVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{ keyword?: string; status?: GroupStatus }>({
  keyword: undefined,
  status: undefined,
})

async function loadList() {
  loading.value = true
  try {
    const res = await getGroupList({
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

function statusLabel(s: GroupStatus) {
  return s === 'ACTIVE' ? '上架' : '下架'
}

function statusTagType(s: GroupStatus): 'success' | 'info' {
  return s === 'ACTIVE' ? 'success' : 'info'
}

// ==================== 新增/编辑 ====================
const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const submitting = ref(false)
const formRef = ref<FormInstance>()
const tagsInput = ref('')

const form = reactive<GroupRequest & { id?: number }>({
  id: undefined,
  name: '',
  description: '',
  coverImage: '',
  category: '',
  tags: '',
  leaderId: undefined,
  status: 'ACTIVE',
})

const formRules: FormRules = {
  name: [{ required: true, message: '小组名称不能为空', trigger: 'blur' }],
}

function parseTags(raw: string): string {
  const arr = raw
    .split(/[,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
  return JSON.stringify(arr)
}

function stringifyTags(raw?: string): string {
  if (!raw) return ''
  try {
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr.join(', ') : raw
  } catch {
    return raw
  }
}

function resetForm() {
  form.id = undefined
  form.name = ''
  form.description = ''
  form.coverImage = ''
  form.category = ''
  form.tags = ''
  form.leaderId = undefined
  form.status = 'ACTIVE'
  tagsInput.value = ''
}

function openCreate() {
  resetForm()
  formMode.value = 'create'
  formVisible.value = true
}

function openEdit(row: AdminGroupVO) {
  resetForm()
  formMode.value = 'edit'
  form.id = row.id
  form.name = row.name
  form.description = row.description || ''
  form.coverImage = row.coverImage || ''
  form.category = row.category || ''
  form.tags = row.tags || ''
  form.leaderId = row.leaderId
  form.status = row.status
  tagsInput.value = stringifyTags(row.tags)
  formVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  form.tags = parseTags(tagsInput.value)
  submitting.value = true
  try {
    if (formMode.value === 'create') {
      await createGroup({
        name: form.name,
        description: form.description,
        coverImage: form.coverImage,
        category: form.category,
        tags: form.tags,
        leaderId: form.leaderId,
        status: form.status,
      })
      ElMessage.success('新增成功')
    } else if (form.id) {
      await updateGroup(form.id, {
        name: form.name,
        description: form.description,
        coverImage: form.coverImage,
        category: form.category,
        tags: form.tags,
        leaderId: form.leaderId,
        status: form.status,
      })
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
async function confirmToggle(row: AdminGroupVO) {
  const next: GroupStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  try {
    await ElMessageBox.confirm(
      `确定将小组「${row.name}」${next === 'ACTIVE' ? '上架' : '下架'}？`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await updateGroupStatus(row.id, next)
    await loadList()
    ElMessage.success('状态已更新')
  } catch (e) {
    ElMessage.error((e as Error).message || '更新失败')
  }
}

// ==================== 删除 ====================
async function confirmDelete(row: AdminGroupVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除小组「${row.name}」？删除后不可恢复。`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteGroup(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.group-manage {
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
.group-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.cover {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  flex-shrink: 0;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
