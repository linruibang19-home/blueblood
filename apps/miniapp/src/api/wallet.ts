import http from '@/utils/request'

export interface WalletSummary {
  balance: number
  pendingAmount: number
  withdrawnAmount: number
  totalEarned: number
}

export interface WalletRecord {
  id: string
  type: string
  amount: number
  title: string
  status: string
  createdAt: string
}

interface PageResp {
  list?: any[]
}

/** GET /wallet/summary */
export async function getWalletSummary(): Promise<WalletSummary> {
  const data = await http.get<any>('/wallet/summary').catch(() => ({}))
  return {
    balance: Number(data?.balance ?? 0),
    pendingAmount: Number(data?.pendingAmount ?? 0),
    withdrawnAmount: Number(data?.withdrawnAmount ?? 0),
    totalEarned: Number(data?.totalEarned ?? 0),
  }
}

/** GET /wallet/records */
export async function getWalletRecords(): Promise<WalletRecord[]> {
  const data = await http.get<any>('/wallet/records').catch(() => ({} as any))
  const list = Array.isArray(data) ? data : (data as PageResp)?.list ?? []
  return list.map((r: any) => ({
    id: String(r?.id ?? ''),
    type: r?.type || 'income',
    amount: Number(r?.amount ?? 0),
    title: r?.title || r?.remark || '收益',
    status: r?.status || '',
    createdAt: r?.createdAt || '',
  }))
}
