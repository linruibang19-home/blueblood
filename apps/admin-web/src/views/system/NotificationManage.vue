<template>
  <div class="notification-manage">
    <!-- 筛选表单 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="接收人ID">
          <el-input
            v-model="filter.userId"
            placeholder="用户ID"
            clearable
            style="width: 140px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select
            v-model="filter.type"
            placeholder="全部"
            clearable
            style="width: 150px"
          >
            <el-option label="里程碑" value="milestone" />
            <el-option label="任务审核" value="task_review" />
            <el-option label="收益" value="income" />
            <el-option label="系统" value="system" />
            <el-option label="小组" value="group" />
            <el-option label="课程" value="course" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="标题/内容"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 通知表格 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>通知列表</span>
          <el-button type="primary" :icon="Plus" @click="openSend">
            发送通知
          </el-button>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="接收人" min-width="150">
          <template #default="{ row }">
            <span>{{ row.recipientNickname || ('#' + row.userId) }}</span>
            <span class="user-id">（ID: {{ row.userId }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTagType(row.type)">
              {{ typeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column label="内容摘要" min-width="220">
          <template #default="{ row }">
            <span>{{ summary(row.content) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="链接" width="120">
          <template #default="{ row }">
            <el-link
              v-if="row.link"
              type="primary"
              :href="row.link"
              target="_blank"
              :underline="false"
            >
              打开
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="发送时间" width="160">
          <template #default="{ row }">
            {{ row.createdAt || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
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

    <!-- 发送通知 -->
    <el-dialog v-model="sendVisible" title="发送通知" width="520px">
      <el-form ref="sendFormRef" :model="sendForm" :rules="sendRules" label-width="92px">
        <el-form-item label="接收用户" prop="userId">
          <el-input
            v-model.number="sendForm.userId"
            placeholder="请输入接收用户ID"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="sendForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="里程碑" value="milestone" />
            <el-option label="任务审核" value="task_review" />
            <el-option label="收益" value="income" />
            <el-option label="系统" value="system" />
            <el-option label="小组" value="group" />
            <el-option label="课程" value="course" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="sendForm.title" placeholder="通知标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="sendForm.content"
            type="textarea"
            :rows="4"
            placeholder="通知内容"
          />
        </el-form-item>
        <el-form-item label="跳转链接" prop="link">
          <el-input v-model="sendForm.link" placeholder="可选，跳转链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitSend">
          发送
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
  getNotificationList,
  sendNotification,
  deleteNotification,
  type AdminNotificationVO,
  type NotificationType,
  type NotificationSendParams,
} from '@/api/admin-notification'

const loading = ref(false)
const list = ref<AdminNotificationVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{
  userId?: number | string
  type?: NotificationType
  keyword?: string
}>({
  userId: undefined,
  type: undefined,
  keyword: undefined,
})

async function loadList() {
  loading.value = true
  try {
    const userIdNum =
      filter.userId === '' || filter.userId === undefined
        ? undefined
        : Number(filter.userId)
    const res = await getNotificationList({
      page: page.value,
      pageSize: pageSize.value,
      userId: Number.isNaN(userIdNum) ? undefined : userIdNum,
      type: filter.type,
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
  filter.userId = undefined
  filter.type = undefined
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

function typeLabel(t: NotificationType) {
  const map: Record<NotificationType, string> = {
    milestone: '里程碑',
    task_review: '任务审核',
    income: '收益',
    system: '系统',
    group: '小组',
    course: '课程',
  }
  return map[t] || t
}

function typeTagType(
  t: NotificationType
): 'primary' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    milestone: 'warning',
    task_review: 'primary',
    income: 'success',
    system: 'danger',
    group: 'info',
    course: 'info',
  }
  return map[t] || 'info'
}

function summary(content?: string) {
  if (!content) return '-'
  return content.length > 40 ? content.slice(0, 40) + '…' : content
}

// 删除（二次确认）
async function confirmDelete(row: AdminNotificationVO) {
  try {
    await ElMessageBox.confirm(
      `确定删除通知「${row.title}」？`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteNotification(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

// 发送通知
const sendVisible = ref(false)
const submitting = ref(false)
const sendFormRef = ref<FormInstance>()
const sendForm = reactive<NotificationSendParams>({
  userId: 0,
  type: 'system',
  title: '',
  content: '',
  link: '',
})

const sendRules: FormRules = {
  userId: [{ required: true, message: '请输入接收用户ID', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}

function openSend() {
  sendForm.userId = 0
  sendForm.type = 'system'
  sendForm.title = ''
  sendForm.content = ''
  sendForm.link = ''
  sendVisible.value = true
}

async function submitSend() {
  if (!sendFormRef.value) return
  await sendFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await sendNotification({
        userId: sendForm.userId,
        type: sendForm.type,
        title: sendForm.title,
        content: sendForm.content,
        link: sendForm.link,
      })
      ElMessage.success('已发送')
      sendVisible.value = false
      page.value = 1
      loadList()
    } catch (e) {
      ElMessage.error((e as Error).message || '发送失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(loadList)
</script>

<style scoped>
.notification-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.user-id {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
