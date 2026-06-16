// 钱包类型定义

export interface WalletSummary {
  balance: number
  pendingAmount: number
  withdrawnAmount: number
  totalEarned: number
}

export interface WalletRecord {
  id: string
  type: 'income' | 'expense' | 'withdraw'
  amount: number
  status: 'pending' | 'available' | 'withdrawing' | 'withdrawn' | 'failed'
  title: string
  description: string
  createdAt: string
}

export interface WithdrawRecord {
  id: string
  amount: number
  status: 'pending' | 'processing' | 'completed' | 'failed'
  bankName: string
  bankAccount: string
  createdAt: string
  processedAt?: string
  failureReason?: string
}