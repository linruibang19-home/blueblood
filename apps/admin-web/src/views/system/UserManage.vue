<template>
  <div class="user-manage">
    <!-- 筛选表单 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="昵称/用户名/手机号"
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
            <el-option label="正常" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
            <el-option label="封禁" value="BANNED" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证">
          <el-select
            v-model="filter.verified"
            placeholder="全部"
            clearable
            style="width: 140px"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="未认证" :value="0" />
            <el-option label="已认证" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户表格 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.avatar" />
              <span>{{ row.nickname || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="等级" width="120">
          <template #default="{ row }">
            <span>Lv{{ row.level }} · {{ row.levelName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="90" />
        <el-table-column prop="creditScore" label="信誉分" width="90" />
        <el-table-column prop="completedTasks" label="已完成任务" width="100" />
        <el-table-column label="认证" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.verified === 1" type="success" size="small">
              已认证
            </el-tag>
            <el-tag v-else type="info" size="small">未认证</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="160">
          <template #default="{ row }">
            {{ row.lastLoginAt || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">
              详情
            </el-button>
            <el-dropdown trigger="click" @command="(c: UserStatus) => confirmStatus(row, c)">
              <el-button link type="warning">状态<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="ACTIVE">设为正常</el-dropdown-item>
                  <el-dropdown-item command="INACTIVE">设为停用</el-dropdown-item>
                  <el-dropdown-item command="BANNED">设为封禁</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button link type="primary" @click="openAdjust(row)">
              调整
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

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="用户详情" size="420px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="ID">{{ current.id }}</el-descriptions-item>
        <el-descriptions-item label="头像/昵称">
          <div class="user-cell">
            <el-avatar :size="32" :src="current.avatar" />
            <span>{{ current.nickname }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="用户名">{{ current.username }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ current.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ current.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ current.gender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="等级">Lv{{ current.level }} · {{ current.levelName }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ current.points }}</el-descriptions-item>
        <el-descriptions-item label="信誉分">{{ current.creditScore }}</el-descriptions-item>
        <el-descriptions-item label="已完成任务">{{ current.completedTasks }}</el-descriptions-item>
        <el-descriptions-item label="认证">
          {{ current.verified === 1 ? '已认证' : '未认证' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(current.status) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ current.lastLoginAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ current.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <!-- 调整等级/积分 -->
    <el-dialog v-model="adjustVisible" title="调整等级/积分" width="440px">
      <el-form :model="adjustForm" label-width="92px">
        <el-form-item label="等级">
          <el-input-number v-model="adjustForm.level" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="等级名称">
          <el-input v-model="adjustForm.levelName" placeholder="如 黄金会员" />
        </el-form-item>
        <el-form-item label="积分变动">
          <el-input-number
            v-model="adjustForm.pointsDelta"
            controls-position="right"
            placeholder="正数增加，负数扣减"
          />
        </el-form-item>
        <el-form-item label="原因">
          <el-input
            v-model="adjustForm.reason"
            type="textarea"
            :rows="2"
            placeholder="调整原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAdjust">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import {
  getUserList,
  updateUserStatus,
  adjustUser,
  type AdminUserVO,
  type UserStatus,
  type AdjustUserParams,
} from '@/api/admin-user'

const loading = ref(false)
const list = ref<AdminUserVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{ keyword?: string; status?: UserStatus; verified?: number }>({
  keyword: undefined,
  status: undefined,
  verified: undefined,
})

async function loadList() {
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: filter.keyword || undefined,
      status: filter.status,
      verified: filter.verified,
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
  filter.verified = undefined
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

function statusLabel(s: UserStatus) {
  return s === 'ACTIVE' ? '正常' : s === 'INACTIVE' ? '停用' : '封禁'
}

function statusTagType(s: UserStatus): 'success' | 'info' | 'danger' {
  return s === 'ACTIVE' ? 'success' : s === 'INACTIVE' ? 'info' : 'danger'
}

// 详情
const detailVisible = ref(false)
const current = ref<AdminUserVO | null>(null)

function openDetail(row: AdminUserVO) {
  current.value = row
  detailVisible.value = true
}

// 状态切换
async function confirmStatus(row: AdminUserVO, next: UserStatus) {
  try {
    await ElMessageBox.confirm(
      `确定将用户「${row.nickname || row.username}」状态改为「${statusLabel(next)}」？`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await updateUserStatus(row.id, next)
    row.status = next
    ElMessage.success('状态已更新')
  } catch (e) {
    ElMessage.error((e as Error).message || '更新失败')
  }
}

// 调整等级/积分
const adjustVisible = ref(false)
const submitting = ref(false)
const adjustForm = reactive<AdjustUserParams & { id: number }>({
  id: 0,
  level: 0,
  levelName: '',
  pointsDelta: 0,
  reason: '',
})

function openAdjust(row: AdminUserVO) {
  adjustForm.id = row.id
  adjustForm.level = row.level
  adjustForm.levelName = row.levelName
  adjustForm.pointsDelta = 0
  adjustForm.reason = ''
  adjustVisible.value = true
}

async function submitAdjust() {
  submitting.value = true
  try {
    await adjustUser(adjustForm.id, {
      level: adjustForm.level,
      levelName: adjustForm.levelName,
      pointsDelta: adjustForm.pointsDelta,
      reason: adjustForm.reason,
    })
    ElMessage.success('调整成功')
    adjustVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error((e as Error).message || '调整失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.user-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
