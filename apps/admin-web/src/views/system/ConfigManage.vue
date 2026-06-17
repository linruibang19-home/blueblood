<template>
  <div class="config-manage">
    <el-tabs v-model="activeTab" class="config-tabs">
      <!-- ============ 系统配置 ============ -->
      <el-tab-pane label="系统配置" name="config">
        <el-card shadow="never" class="filter-card">
          <div class="filter-bar">
            <el-form :inline="true" @submit.prevent>
              <el-form-item label="关键字">
                <el-input
                  v-model="configFilter.keyword"
                  placeholder="配置键/名称/备注"
                  clearable
                  style="width: 220px"
                  @keyup.enter="onConfigSearch"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="onConfigSearch">查询</el-button>
                <el-button @click="onConfigReset">重置</el-button>
              </el-form-item>
            </el-form>
            <el-button type="success" @click="openConfigDialog()">新增配置</el-button>
          </div>
        </el-card>

        <el-card shadow="never">
          <el-table v-loading="configLoading" :data="configList" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="configKey" label="配置键" min-width="180" />
            <el-table-column prop="configValue" label="配置值" min-width="160" show-overflow-tooltip />
            <el-table-column prop="configType" label="类型" width="100" />
            <el-table-column prop="label" label="名称" width="140" />
            <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openConfigDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="confirmDeleteConfig(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pager"
            :current-page="configPage"
            :page-size="configPageSize"
            :page-sizes="[10, 20, 50]"
            :total="configTotal"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @current-change="(p: number) => { configPage = p; loadConfig() }"
            @size-change="(s: number) => { configPageSize = s; configPage = 1; loadConfig() }"
          />
        </el-card>
      </el-tab-pane>

      <!-- ============ 字典 ============ -->
      <el-tab-pane label="字典管理" name="dict">
        <el-card shadow="never" class="filter-card">
          <div class="filter-bar">
            <el-form :inline="true" @submit.prevent>
              <el-form-item label="类型">
                <el-select
                  v-model="dictFilter.dictType"
                  placeholder="全部类型"
                  clearable
                  filterable
                  style="width: 180px"
                  @change="onDictSearch"
                >
                  <el-option
                    v-for="t in dictTypes"
                    :key="t"
                    :label="t"
                    :value="t"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="关键字">
                <el-input
                  v-model="dictFilter.keyword"
                  placeholder="键/值/名称"
                  clearable
                  style="width: 200px"
                  @keyup.enter="onDictSearch"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="onDictSearch">查询</el-button>
                <el-button @click="onDictReset">重置</el-button>
              </el-form-item>
            </el-form>
            <el-button type="success" @click="openDictDialog()">新增字典项</el-button>
          </div>
        </el-card>

        <el-card shadow="never">
          <el-table v-loading="dictLoading" :data="dictList" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="dictType" label="类型" width="140" />
            <el-table-column prop="dictKey" label="键" min-width="140" />
            <el-table-column prop="dictValue" label="值" min-width="140" show-overflow-tooltip />
            <el-table-column prop="label" label="名称" width="140" />
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                  {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDictDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="confirmDeleteDict(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pager"
            :current-page="dictPage"
            :page-size="dictPageSize"
            :page-sizes="[10, 20, 50]"
            :total="dictTotal"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @current-change="(p: number) => { dictPage = p; loadDict() }"
            @size-change="(s: number) => { dictPageSize = s; dictPage = 1; loadDict() }"
          />
        </el-card>
      </el-tab-pane>

      <!-- ============ 任务分类 ============ -->
      <el-tab-pane label="任务分类" name="category">
        <el-card shadow="never">
          <div class="filter-bar">
            <span class="title">任务分类列表</span>
            <el-button type="success" @click="openCategoryDialog()">新增分类</el-button>
          </div>
          <el-table v-loading="categoryLoading" :data="categoryList" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="name" label="名称" min-width="160" />
            <el-table-column prop="icon" label="图标" width="120" />
            <el-table-column prop="categoryOrder" label="排序" width="90" />
            <el-table-column prop="taskCount" label="任务数" width="100" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                  {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openCategoryDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="confirmDeleteCategory(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- ============ 技能标签 ============ -->
      <el-tab-pane label="技能标签" name="skill">
        <el-card shadow="never" class="filter-card">
          <el-form :inline="true" @submit.prevent>
            <el-form-item label="关键字">
              <el-input
                v-model="skillFilter.keyword"
                placeholder="技能名/分类"
                clearable
                style="width: 220px"
                @keyup.enter="onSkillSearch"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onSkillSearch">查询</el-button>
              <el-button @click="onSkillReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card shadow="never">
          <el-table v-loading="skillLoading" :data="skillList" stripe border>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="username" label="用户名" width="140" />
            <el-table-column prop="userId" label="用户ID" width="90" />
            <el-table-column prop="name" label="技能" min-width="160" />
            <el-table-column prop="category" label="分类" width="140" />
            <el-table-column label="熟练度" width="100">
              <template #default="{ row }">
                {{ proficiencyLabel(row.proficiency) }}
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="160" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="danger" @click="confirmDeleteSkill(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pager"
            :current-page="skillPage"
            :page-size="skillPageSize"
            :page-sizes="[10, 20, 50]"
            :total="skillTotal"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @current-change="(p: number) => { skillPage = p; loadSkill() }"
            @size-change="(s: number) => { skillPageSize = s; skillPage = 1; loadSkill() }"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 配置编辑弹窗 -->
    <el-dialog v-model="configDialogVisible" :title="configForm.id ? '编辑配置' : '新增配置'" width="520px">
      <el-form :model="configForm" label-width="90px">
        <el-form-item label="配置键" required>
          <el-input v-model="configForm.configKey" placeholder="如 site.title" />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="configForm.configValue" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="configForm.configType" placeholder="选择类型" style="width: 100%">
            <el-option label="string" value="string" />
            <el-option label="number" value="number" />
            <el-option label="boolean" value="boolean" />
            <el-option label="json" value="json" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="configForm.label" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="configForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="configSubmitting" @click="submitConfig">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典编辑弹窗 -->
    <el-dialog v-model="dictDialogVisible" :title="dictForm.id ? '编辑字典项' : '新增字典项'" width="520px">
      <el-form :model="dictForm" label-width="90px">
        <el-form-item label="字典类型" required>
          <el-input v-model="dictForm.dictType" placeholder="如 task_status" />
        </el-form-item>
        <el-form-item label="字典键" required>
          <el-input v-model="dictForm.dictKey" />
        </el-form-item>
        <el-form-item label="字典值">
          <el-input v-model="dictForm.dictValue" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="dictForm.label" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dictForm.sort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="dictForm.status" style="width: 100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dictForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dictSubmitting" @click="submitDict">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分类编辑弹窗 -->
    <el-dialog v-model="categoryDialogVisible" :title="categoryForm.id ? '编辑分类' : '新增分类'" width="480px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="categoryForm.name" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="categoryForm.icon" placeholder="图标 URL 或标识" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.categoryOrder" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="categoryForm.status" style="width: 100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySubmitting" @click="submitCategory">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getConfigList,
  createConfig,
  updateConfig,
  deleteConfig,
  getDictList,
  getDictTypes,
  createDict,
  updateDict,
  deleteDict,
  getCategoryList,
  createCategory,
  updateCategory,
  deleteCategory,
  getSkillList,
  deleteSkill,
  type SysConfig,
  type SysConfigRequest,
  type SysDict,
  type SysDictRequest,
  type TaskCategory,
  type TaskCategoryRequest,
  type AdminSkillVO,
} from '@/api/admin-system'

const activeTab = ref('config')

// ====== 系统配置 ======
const configLoading = ref(false)
const configList = ref<SysConfig[]>([])
const configTotal = ref(0)
const configPage = ref(1)
const configPageSize = ref(10)
const configFilter = reactive<{ keyword?: string }>({ keyword: undefined })

async function loadConfig() {
  configLoading.value = true
  try {
    const res = await getConfigList({
      page: configPage.value,
      pageSize: configPageSize.value,
      keyword: configFilter.keyword || undefined,
    })
    configList.value = res.list || []
    configTotal.value = res.total || 0
  } catch (e) {
    ElMessage.error((e as Error).message || '加载失败')
  } finally {
    configLoading.value = false
  }
}
function onConfigSearch() {
  configPage.value = 1
  loadConfig()
}
function onConfigReset() {
  configFilter.keyword = undefined
  configPage.value = 1
  loadConfig()
}

const configDialogVisible = ref(false)
const configSubmitting = ref(false)
const configForm = reactive<SysConfigRequest & { id?: number }>({
  id: undefined,
  configKey: '',
  configValue: '',
  configType: 'string',
  label: '',
  remark: '',
})
function openConfigDialog(row?: SysConfig) {
  if (row) {
    configForm.id = row.id
    configForm.configKey = row.configKey
    configForm.configValue = row.configValue || ''
    configForm.configType = row.configType || 'string'
    configForm.label = row.label || ''
    configForm.remark = row.remark || ''
  } else {
    configForm.id = undefined
    configForm.configKey = ''
    configForm.configValue = ''
    configForm.configType = 'string'
    configForm.label = ''
    configForm.remark = ''
  }
  configDialogVisible.value = true
}
async function submitConfig() {
  if (!configForm.configKey) {
    ElMessage.warning('请填写配置键')
    return
  }
  configSubmitting.value = true
  try {
    const payload: SysConfigRequest = {
      configKey: configForm.configKey,
      configValue: configForm.configValue,
      configType: configForm.configType,
      label: configForm.label,
      remark: configForm.remark,
    }
    if (configForm.id) {
      await updateConfig(configForm.id, payload)
    } else {
      await createConfig(payload)
    }
    ElMessage.success('保存成功')
    configDialogVisible.value = false
    loadConfig()
  } catch (e) {
    ElMessage.error((e as Error).message || '保存失败')
  } finally {
    configSubmitting.value = false
  }
}
async function confirmDeleteConfig(row: SysConfig) {
  try {
    await ElMessageBox.confirm(`确定删除配置「${row.configKey}」？`, '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteConfig(row.id)
    ElMessage.success('已删除')
    loadConfig()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

// ====== 字典 ======
const dictLoading = ref(false)
const dictList = ref<SysDict[]>([])
const dictTotal = ref(0)
const dictPage = ref(1)
const dictPageSize = ref(10)
const dictTypes = ref<string[]>([])
const dictFilter = reactive<{ dictType?: string; keyword?: string }>({
  dictType: undefined,
  keyword: undefined,
})
const dictLoaded = ref(false)

async function loadDictTypes() {
  try {
    dictTypes.value = (await getDictTypes()) || []
  } catch {
    dictTypes.value = []
  }
}
async function loadDict() {
  dictLoading.value = true
  try {
    const res = await getDictList({
      page: dictPage.value,
      pageSize: dictPageSize.value,
      dictType: dictFilter.dictType,
      keyword: dictFilter.keyword || undefined,
    })
    dictList.value = res.list || []
    dictTotal.value = res.total || 0
  } catch (e) {
    ElMessage.error((e as Error).message || '加载失败')
  } finally {
    dictLoading.value = false
  }
}
function onDictSearch() {
  dictPage.value = 1
  loadDict()
}
function onDictReset() {
  dictFilter.dictType = undefined
  dictFilter.keyword = undefined
  dictPage.value = 1
  loadDict()
}

const dictDialogVisible = ref(false)
const dictSubmitting = ref(false)
const dictForm = reactive<SysDictRequest & { id?: number }>({
  id: undefined,
  dictType: '',
  dictKey: '',
  dictValue: '',
  label: '',
  sort: 0,
  remark: '',
  status: 'ACTIVE',
})
function openDictDialog(row?: SysDict) {
  if (row) {
    dictForm.id = row.id
    dictForm.dictType = row.dictType
    dictForm.dictKey = row.dictKey
    dictForm.dictValue = row.dictValue || ''
    dictForm.label = row.label || ''
    dictForm.sort = row.sort ?? 0
    dictForm.remark = row.remark || ''
    dictForm.status = row.status || 'ACTIVE'
  } else {
    dictForm.id = undefined
    dictForm.dictType = dictFilter.dictType || ''
    dictForm.dictKey = ''
    dictForm.dictValue = ''
    dictForm.label = ''
    dictForm.sort = 0
    dictForm.remark = ''
    dictForm.status = 'ACTIVE'
  }
  dictDialogVisible.value = true
}
async function submitDict() {
  if (!dictForm.dictType || !dictForm.dictKey) {
    ElMessage.warning('请填写字典类型与键')
    return
  }
  dictSubmitting.value = true
  try {
    const payload: SysDictRequest = {
      dictType: dictForm.dictType,
      dictKey: dictForm.dictKey,
      dictValue: dictForm.dictValue,
      label: dictForm.label,
      sort: dictForm.sort,
      remark: dictForm.remark,
      status: dictForm.status,
    }
    if (dictForm.id) {
      await updateDict(dictForm.id, payload)
    } else {
      await createDict(payload)
    }
    ElMessage.success('保存成功')
    dictDialogVisible.value = false
    await loadDictTypes()
    loadDict()
  } catch (e) {
    ElMessage.error((e as Error).message || '保存失败')
  } finally {
    dictSubmitting.value = false
  }
}
async function confirmDeleteDict(row: SysDict) {
  try {
    await ElMessageBox.confirm(`确定删除字典项「${row.dictKey}」？`, '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteDict(row.id)
    ElMessage.success('已删除')
    loadDict()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

// ====== 任务分类 ======
const categoryLoading = ref(false)
const categoryList = ref<TaskCategory[]>([])
const categoryLoaded = ref(false)
async function loadCategory() {
  categoryLoading.value = true
  try {
    categoryList.value = (await getCategoryList()) || []
  } catch (e) {
    ElMessage.error((e as Error).message || '加载失败')
  } finally {
    categoryLoading.value = false
  }
}
const categoryDialogVisible = ref(false)
const categorySubmitting = ref(false)
const categoryForm = reactive<TaskCategoryRequest & { id?: number }>({
  id: undefined,
  name: '',
  icon: '',
  categoryOrder: 0,
  status: 'ACTIVE',
})
function openCategoryDialog(row?: TaskCategory) {
  if (row) {
    categoryForm.id = row.id
    categoryForm.name = row.name
    categoryForm.icon = row.icon || ''
    categoryForm.categoryOrder = row.categoryOrder ?? 0
    categoryForm.status = row.status || 'ACTIVE'
  } else {
    categoryForm.id = undefined
    categoryForm.name = ''
    categoryForm.icon = ''
    categoryForm.categoryOrder = 0
    categoryForm.status = 'ACTIVE'
  }
  categoryDialogVisible.value = true
}
async function submitCategory() {
  if (!categoryForm.name) {
    ElMessage.warning('请填写分类名称')
    return
  }
  categorySubmitting.value = true
  try {
    const payload: TaskCategoryRequest = {
      name: categoryForm.name,
      icon: categoryForm.icon,
      categoryOrder: categoryForm.categoryOrder,
      status: categoryForm.status,
    }
    if (categoryForm.id) {
      await updateCategory(categoryForm.id, payload)
    } else {
      await createCategory(payload)
    }
    ElMessage.success('保存成功')
    categoryDialogVisible.value = false
    loadCategory()
  } catch (e) {
    ElMessage.error((e as Error).message || '保存失败')
  } finally {
    categorySubmitting.value = false
  }
}
async function confirmDeleteCategory(row: TaskCategory) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」？`, '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteCategory(row.id)
    ElMessage.success('已删除')
    loadCategory()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

// ====== 技能标签 ======
const skillLoading = ref(false)
const skillList = ref<AdminSkillVO[]>([])
const skillTotal = ref(0)
const skillPage = ref(1)
const skillPageSize = ref(10)
const skillFilter = reactive<{ keyword?: string }>({ keyword: undefined })
const skillLoaded = ref(false)

async function loadSkill() {
  skillLoading.value = true
  try {
    const res = await getSkillList({
      page: skillPage.value,
      pageSize: skillPageSize.value,
      keyword: skillFilter.keyword || undefined,
    })
    skillList.value = res.list || []
    skillTotal.value = res.total || 0
  } catch (e) {
    ElMessage.error((e as Error).message || '加载失败')
  } finally {
    skillLoading.value = false
  }
}
function onSkillSearch() {
  skillPage.value = 1
  loadSkill()
}
function onSkillReset() {
  skillFilter.keyword = undefined
  skillPage.value = 1
  loadSkill()
}
async function confirmDeleteSkill(row: AdminSkillVO) {
  try {
    await ElMessageBox.confirm(`确定删除技能「${row.name}」？`, '确认', {
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await deleteSkill(row.id)
    ElMessage.success('已删除')
    loadSkill()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

function proficiencyLabel(p?: number) {
  if (p === 0) return '了解'
  if (p === 1) return '掌握'
  if (p === 2) return '精通'
  return '-'
}

// tab 懒加载
watch(activeTab, (tab) => {
  if (tab === 'dict' && !dictLoaded.value) {
    dictLoaded.value = true
    loadDictTypes()
    loadDict()
  } else if (tab === 'category' && !categoryLoaded.value) {
    categoryLoaded.value = true
    loadCategory()
  } else if (tab === 'skill' && !skillLoaded.value) {
    skillLoaded.value = true
    loadSkill()
  }
})

onMounted(loadConfig)
</script>

<style scoped>
.config-manage {
  display: flex;
  flex-direction: column;
}
.config-tabs {
  display: flex;
  flex-direction: column;
}
.filter-card {
  margin-bottom: 16px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 8px;
}
.filter-bar .title {
  font-weight: 600;
  font-size: 15px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
