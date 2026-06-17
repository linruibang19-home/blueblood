<template>
  <div class="log-manage">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="用户/动作/模块/URL"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="模块">
          <el-input
            v-model="filter.module"
            placeholder="如 USER/TASK"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filter.status"
            placeholder="全部"
            clearable
            style="width: 130px"
          >
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAIL" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" stripe border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="module" label="模块" width="110" />
        <el-table-column prop="action" label="动作" min-width="160" show-overflow-tooltip />
        <el-table-column label="方法" width="90">
          <template #default="{ row }">
            <el-tag :type="methodTagType(row.method)" size="small">
              {{ row.method || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column label="耗时" width="90">
          <template #default="{ row }">{{ row.costMs ?? '-' }} ms</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="160" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
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
    <el-drawer v-model="detailVisible" title="操作日志详情" size="520px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="ID">{{ current.id }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ current.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ current.module || '-' }}</el-descriptions-item>
        <el-descriptions-item label="动作">{{ current.action || '-' }}</el-descriptions-item>
        <el-descriptions-item label="方法">{{ current.method || '-' }}</el-descriptions-item>
        <el-descriptions-item label="URL">{{ current.url || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ current.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ current.costMs ?? '-' }} ms</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="current.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
            {{ current.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="时间">{{ current.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <pre class="log-pre">{{ current.params || '-' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息">
          <pre class="log-pre">{{ current.errorMsg || '-' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLogList, type SysOperationLog } from '@/api/admin-system'

const loading = ref(false)
const list = ref<SysOperationLog[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{ keyword?: string; module?: string; status?: string }>({
  keyword: undefined,
  module: undefined,
  status: undefined,
})

async function loadList() {
  loading.value = true
  try {
    const res = await getLogList({
      page: page.value,
      pageSize: pageSize.value,
      keyword: filter.keyword || undefined,
      module: filter.module || undefined,
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
  filter.module = undefined
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

function methodTagType(m?: string): 'success' | 'warning' | 'danger' | 'info' | 'primary' {
  if (m === 'GET') return 'success'
  if (m === 'POST') return 'warning'
  if (m === 'PUT') return 'primary'
  if (m === 'DELETE') return 'danger'
  return 'info'
}

const detailVisible = ref(false)
const current = ref<SysOperationLog | null>(null)
function openDetail(row: SysOperationLog) {
  current.value = row
  detailVisible.value = true
}

onMounted(loadList)
</script>

<style scoped>
.log-manage {
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
.log-pre {
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  max-height: 200px;
  overflow: auto;
  font-size: 12px;
  background: #f5f7fa;
  padding: 6px 8px;
  border-radius: 4px;
}
</style>
