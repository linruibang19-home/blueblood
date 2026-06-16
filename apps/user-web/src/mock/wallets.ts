// Mock 钱包数据
import type { WalletSummary, WalletRecord } from '@/types/wallet'

export const mockWalletSummary: WalletSummary = {
  balance: 3850.00,
  pendingAmount: 1500.00,
  withdrawnAmount: 12500.00,
  totalEarned: 17850.00,
}

export const mockWalletRecords: WalletRecord[] = [
  {
    id: 'wr001',
    type: 'income',
    amount: 1500.00,
    status: 'available',
    title: '任务结算收入',
    description: 'AI 客服 Agent 配置与优化 - 第一期里程碑',
    createdAt: '2024-06-12 09:00',
  },
  {
    id: 'wr002',
    type: 'income',
    amount: 2000.00,
    status: 'pending',
    title: '任务结算收入',
    description: '数据采集自动化脚本开发 - 定金',
    createdAt: '2024-06-08 15:30',
  },
  {
    id: 'wr003',
    type: 'withdraw',
    amount: 3000.00,
    status: 'withdrawn',
    title: '提现',
    description: '提现至 招商银行 **** 4589',
    createdAt: '2024-06-05 10:00',
  },
  {
    id: 'wr004',
    type: 'income',
    amount: 500.00,
    status: 'available',
    title: '课程学习奖励',
    description: '完成 Vue3 企业级实战 课程第二章',
    createdAt: '2024-06-01 12:00',
  },
  {
    id: 'wr005',
    type: 'withdraw',
    amount: 5000.00,
    status: 'withdrawn',
    title: '提现',
    description: '提现至 招商银行 **** 4589',
    createdAt: '2024-05-28 10:00',
  },
]