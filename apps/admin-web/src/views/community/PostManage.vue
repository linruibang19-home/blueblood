<template>
  <div class="post-manage">
    <!-- 筛选表单 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="标题/正文"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="小组">
          <el-input-number
            v-model="filter.groupId"
            :min="1"
            controls-position="right"
            placeholder="小组ID"
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="filter.tag"
            placeholder="全部"
            clearable
            style="width: 140px"
          >
            <el-option label="话题" value="话题" />
            <el-option label="任务" value="任务" />
            <el-option label="经验分享" value="经验分享" />
            <el-option label="活动" value="活动" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filter.status"
            placeholder="全部"
            clearable
            style="width: 140px"
          >
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已隐藏" value="HIDDEN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 帖子表格 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="标题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.title || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="authorNickname" label="作者" width="120">
          <template #default="{ row }">
            {{ row.authorNickname || row.authorId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="groupName" label="小组" width="140">
          <template #default="{ row }">
            {{ row.groupName || row.groupId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="tag" label="标签" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.tag" size="small">{{ row.tag }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="likes" label="点赞" width="80" />
        <el-table-column prop="comments" label="评论" width="80" />
        <el-table-column prop="views" label="浏览" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="row.status === 'HIDDEN' ? 'success' : 'warning'"
              @click="confirmToggle(row)"
            >
              {{ row.status === 'HIDDEN' ? '显示' : '隐藏' }}
            </el-button>
            <el-button link type="primary" @click="openComments(row)">
              评论
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

    <!-- 评论管理弹窗 -->
    <el-dialog
      v-model="commentVisible"
      :title="`评论管理 - 帖子「${currentPost?.title || ''}」`"
      width="780px"
    >
      <el-table
        v-loading="commentLoading"
        :data="commentList"
        stripe
        border
        size="small"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="authorNickname" label="评论人" width="110">
          <template #default="{ row }">
            {{ row.authorNickname || row.authorId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="likes" label="点赞" width="70" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'NORMAL' ? 'success' : 'info'" size="small">
              {{ row.status === 'NORMAL' ? '正常' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="row.status === 'NORMAL' ? 'warning' : 'success'"
              @click="confirmToggleComment(row)"
            >
              {{ row.status === 'NORMAL' ? '隐藏' : '显示' }}
            </el-button>
            <el-button link type="danger" @click="confirmDeleteComment(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pager"
        :current-page="commentPage"
        :page-size="commentPageSize"
        :page-sizes="[10, 20, 50]"
        :total="commentTotal"
        layout="total, prev, pager, next"
        background
        @current-change="onCommentPageChange"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPostList,
  updatePostStatus,
  deletePost,
  getCommentList,
  updateCommentStatus,
  deleteComment,
  type AdminPostVO,
  type PostStatus,
  type PostTag,
  type AdminCommentVO,
  type CommentStatus,
} from '@/api/admin-community'

// ==================== 帖子列表 ====================
const loading = ref(false)
const list = ref<AdminPostVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{
  keyword?: string
  groupId?: number
  tag?: PostTag
  status?: PostStatus
}>({
  keyword: undefined,
  groupId: undefined,
  tag: undefined,
  status: undefined,
})

async function loadList() {
  loading.value = true
  try {
    const res = await getPostList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: filter.keyword || undefined,
      groupId: filter.groupId || undefined,
      tag: filter.tag,
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
  filter.groupId = undefined
  filter.tag = undefined
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

function statusLabel(s: PostStatus) {
  return s === 'PUBLISHED' ? '已发布' : s === 'HIDDEN' ? '已隐藏' : '草稿'
}

function statusTagType(s: PostStatus): 'success' | 'info' | 'warning' {
  return s === 'PUBLISHED' ? 'success' : s === 'HIDDEN' ? 'info' : 'warning'
}

// ==================== 显示/隐藏 帖子 ====================
async function confirmToggle(row: AdminPostVO) {
  const next: 'HIDDEN' | 'PUBLISHED' = row.status === 'HIDDEN' ? 'PUBLISHED' : 'HIDDEN'
  const verb = next === 'HIDDEN' ? '隐藏' : '显示'
  try {
    await ElMessageBox.confirm(`确定${verb}帖子「${row.title}」？`, '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await updatePostStatus(row.id, next)
    await loadList()
    ElMessage.success(`已${verb}`)
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}

// ==================== 删除 帖子 ====================
async function confirmDelete(row: AdminPostVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除帖子「${row.title}」？删除后不可恢复。`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deletePost(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

// ==================== 评论管理弹窗 ====================
const commentVisible = ref(false)
const commentLoading = ref(false)
const commentList = ref<AdminCommentVO[]>([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentPageSize = ref(10)
const currentPost = ref<AdminPostVO | null>(null)

async function loadComments() {
  if (!currentPost.value) return
  commentLoading.value = true
  try {
    const res = await getCommentList({
      page: commentPage.value,
      pageSize: commentPageSize.value,
      postId: currentPost.value.id,
    })
    commentList.value = res.list || []
    commentTotal.value = res.total || 0
  } catch (e) {
    ElMessage.error((e as Error).message || '评论加载失败')
  } finally {
    commentLoading.value = false
  }
}

function openComments(row: AdminPostVO) {
  currentPost.value = row
  commentPage.value = 1
  commentVisible.value = true
  loadComments()
}

function onCommentPageChange(p: number) {
  commentPage.value = p
  loadComments()
}

async function confirmToggleComment(row: AdminCommentVO) {
  const next: CommentStatus = row.status === 'NORMAL' ? 'DELETED' : 'NORMAL'
  const verb = next === 'NORMAL' ? '显示' : '隐藏'
  try {
    await ElMessageBox.confirm(`确定${verb}该评论？`, '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await updateCommentStatus(row.id, next)
    row.status = next
    ElMessage.success(`已${verb}`)
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  }
}

async function confirmDeleteComment(row: AdminCommentVO) {
  try {
    await ElMessageBox.confirm('确定删除该评论？删除后不可恢复。', '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteComment(row.id)
    ElMessage.success('已删除')
    loadComments()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.post-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
