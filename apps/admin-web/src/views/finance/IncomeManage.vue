<template>
  <div class="income-manage">
    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" @submit.prevent>
        <el-form-item label="用户ID">
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
            style="width: 140px"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="收入" value="income" />
            <el-option label="支出" value="expense" />
            <el-option label="提现" value="withdraw" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filter.status"
            placeholder="全部"
            clearable
            style="width: 140px"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="待结算" value="pending" />
            <el-option label="可用" value="available" />
            <el-option label="提现中" value="withdrawing" />
            <el-option label="已提现" value="withdrawn" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" min-width="140">
          <template #default="{ row }">
            {{ row.nickname || row.username || row.userId }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">
              {{ typeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span :class="row.type === 'income' ? 'amt-income' : 'amt-expense'">
              {{ row.type === 'income' ? '+' : '-' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bizType" label="业务类型" width="120" show-overflow-tooltip />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ row.createdAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openWallet(row)">
              查看钱包
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

    <!-- 用户钱包抽屉 -->
    <el-drawer v-model="walletVisible" title="用户钱包" size="420px">
      <el-descriptions v-if="wallet" :column="1" border>
        <el-descriptions-item label="用户">{{ wallet.nickname || wallet.userId }}</el-descriptions-item>
        <el-descriptions-item label="可用余额">{{ wallet.balance }}</el-descriptions-item>
        <el-descriptions-item label="待入账">{{ wallet.pendingAmount }}</el-descriptions-item>
        <el-descriptions-item label="已提现">{{ wallet.withdrawnAmount }}</el-descriptions-item>
        <el-descriptions-item label="累计收益">{{ wallet.totalEarned }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="wallet.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ wallet.status === 'ACTIVE' ? '正常' : '冻结' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无数据" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getWalletRecordList,
  getWalletAccount,
  type AdminWalletRecordVO,
  type AdminWalletAccountVO,
  type WalletRecordType,
  type WalletRecordStatus,
} from '@/api/admin-wallet'

const loading = ref(false)
const list = ref<AdminWalletRecordVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filter = reactive<{
  userId?: string
  type?: WalletRecordType
  status?: WalletRecordStatus
}>({ userId: undefined, type: undefined, status: undefined })

async function loadList() {
  loading.value = true
  try {
    const userIdNum = filter.userId ? Number(filter.userId) : undefined
    const res = await getWalletRecordList({
      page: page.value,
      pageSize: pageSize.value,
      userId: userIdNum && !Number.isNaN(userIdNum) ? userIdNum : undefined,
      type: filter.type,
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
  filter.userId = undefined
  filter.type = undefined
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

function typeLabel(t: WalletRecordType) {
  return t === 'income' ? '收入' : t === 'expense' ? '支出' : '提现'
}

function typeTagType(t: WalletRecordType): 'success' | 'warning' | 'danger' {
  return t === 'income' ? 'success' : t === 'expense' ? 'warning' : 'danger'
}

function statusLabel(s: WalletRecordStatus) {
  const map: Record<string, string> = {
    pending: '待结算',
    available: '可用',
    withdrawing: '提现中',
    withdrawn: '已提现',
    failed: '失败',
  }
  return map[s] || s
}

function statusTagType(s: WalletRecordStatus): 'warning' | 'success' | 'info' | 'danger' {
  if (s === 'pending') return 'warning'
  if (s === 'available') return 'success'
  if (s === 'withdrawing') return 'warning'
  if (s === 'withdrawn') return 'info'
  return 'danger'
}

// 用户钱包抽屉
const walletVisible = ref(false)
const wallet = ref<AdminWalletAccountVO | null>(null)

async function openWallet(row: AdminWalletRecordVO) {
  wallet.value = null
  walletVisible.value = true
  try {
    wallet.value = await getWalletAccount(row.userId)
  } catch (e) {
    ElMessage.error((e as Error).message || '加载钱包失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
.income-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.amt-income {
  color: var(--el-color-success);
  font-weight: 600;
}
.amt-expense {
  color: var(--el-color-danger);
  font-weight: 600;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
