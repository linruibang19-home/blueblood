<template>
  <div class="permission-manage">
    <!-- 角色概览卡片 -->
    <el-card shadow="never" class="role-card">
      <template #header>
        <span>角色概览</span>
      </template>
      <div v-loading="roleLoading" class="role-grid">
        <el-card
          v-for="role in roles"
          :key="role.id"
          shadow="hover"
          class="role-item"
          :class="{ active: selectedRoleCode === role.code }"
          @click="selectRole(role.code)"
        >
          <div class="role-code">
            <el-tag :type="role.code === 'ADMIN' ? 'danger' : 'primary'" size="small">
              {{ role.code }}
            </el-tag>
            <el-tag
              v-if="role.status === 'ACTIVE'"
              type="success"
              size="small"
              effect="plain"
            >
              启用
            </el-tag>
            <el-tag v-else type="info" size="small" effect="plain">停用</el-tag>
          </div>
          <div class="role-name">{{ role.name }}</div>
          <div class="role-desc">{{ role.description || '—' }}</div>
          <div class="role-count">
            <el-icon><User /></el-icon>
            <span>{{ role.userCount }} 人</span>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 用户角色分配 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户角色分配</span>
          <div class="header-actions">
            <el-select
              v-model="selectedRoleCode"
              placeholder="选择角色"
              style="width: 180px"
              @change="loadRoleUsers"
            >
              <el-option
                v-for="role in roles"
                :key="role.code"
                :label="`${role.name}(${role.code})`"
                :value="role.code"
              />
            </el-select>
            <el-button type="primary" :icon="Plus" @click="openAssign">
              分配角色
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="userLoading"
        :data="roleUsers"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="昵称" min-width="160">
          <template #default="{ row }">
            {{ row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="用户名" min-width="160">
          <template #default="{ row }">
            {{ row.username || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="danger"
              :disabled="row.userId === currentAdminId"
              @click="confirmRevoke(row)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="该角色下暂无用户" />
        </template>
      </el-table>
    </el-card>

    <!-- 底部说明 -->
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="权限说明"
      description="菜单权限为静态配置（menu.ts），角色决定可访问的 /admin/* 接口（后端 @PreAuthorize）。此处仅管理用户与角色的绑定关系。"
    />

    <!-- 分配角色 -->
    <el-dialog v-model="assignVisible" title="分配角色" width="440px">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input
            v-model.number="assignForm.userId"
            placeholder="请输入用户ID"
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="assignForm.roleCode" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in roles"
              :key="role.code"
              :label="`${role.name}(${role.code})`"
              :value="role.code"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAssign">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, User } from '@element-plus/icons-vue'
import {
  getRoleList,
  getRoleUsers,
  assignRole,
  revokeRole,
  type AdminRoleVO,
  type AdminRoleUserVO,
  type RoleAssignParams,
} from '@/api/admin-role'

const roleLoading = ref(false)
const roles = ref<AdminRoleVO[]>([])
const selectedRoleCode = ref<string>('')

const userLoading = ref(false)
const roleUsers = ref<AdminRoleUserVO[]>([])

// 从 localStorage token 解析当前登录管理员ID（用于禁止撤销自身管理员角色）
const currentAdminId = computed(() => {
  try {
    const token = localStorage.getItem('admin_token') || ''
    const payload = token.split('.')[1]
    if (!payload) return -1
    const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
    return Number(decoded?.userId ?? decoded?.sub ?? -1)
  } catch {
    return -1
  }
})

async function loadRoles() {
  roleLoading.value = true
  try {
    const list = await getRoleList()
    roles.value = list || []
    if (!selectedRoleCode.value && roles.value.length) {
      selectedRoleCode.value = roles.value[0].code
      await loadRoleUsers()
    }
  } catch (e) {
    ElMessage.error((e as Error).message || '加载角色失败')
  } finally {
    roleLoading.value = false
  }
}

async function loadRoleUsers() {
  if (!selectedRoleCode.value) {
    roleUsers.value = []
    return
  }
  userLoading.value = true
  try {
    const list = await getRoleUsers(selectedRoleCode.value)
    roleUsers.value = list || []
  } catch (e) {
    ElMessage.error((e as Error).message || '加载用户失败')
  } finally {
    userLoading.value = false
  }
}

function selectRole(code: string) {
  selectedRoleCode.value = code
  loadRoleUsers()
}

// 撤销角色（二次确认）
async function confirmRevoke(row: AdminRoleUserVO) {
  try {
    await ElMessageBox.confirm(
      `确定撤销用户「${row.nickname || row.username || '#' + row.userId}」的「${selectedRoleCode.value}」角色？`,
      '撤销确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await revokeRole({ userId: row.userId, roleCode: selectedRoleCode.value })
    ElMessage.success('已撤销')
    await Promise.all([loadRoleUsers(), loadRoles()])
  } catch (e) {
    ElMessage.error((e as Error).message || '撤销失败')
  }
}

// 分配角色
const assignVisible = ref(false)
const submitting = ref(false)
const assignFormRef = ref<FormInstance>()
const assignForm = reactive<RoleAssignParams>({
  userId: 0,
  roleCode: '',
})

const assignRules: FormRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

function openAssign() {
  assignForm.userId = 0
  assignForm.roleCode = selectedRoleCode.value || ''
  assignVisible.value = true
}

async function submitAssign() {
  if (!assignFormRef.value) return
  await assignFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await assignRole({
        userId: assignForm.userId,
        roleCode: assignForm.roleCode,
      })
      ElMessage.success('已分配')
      assignVisible.value = false
      // 切换到目标角色并刷新
      selectedRoleCode.value = assignForm.roleCode
      await Promise.all([loadRoleUsers(), loadRoles()])
    } catch (e) {
      ElMessage.error((e as Error).message || '分配失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(loadRoles)
</script>

<style scoped>
.permission-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.role-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}
.role-item {
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.role-item.active {
  border-color: var(--el-color-primary);
}
.role-code {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}
.role-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}
.role-desc {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  min-height: 20px;
  margin-bottom: 8px;
}
.role-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--el-color-primary);
  font-size: 14px;
  font-weight: 600;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
