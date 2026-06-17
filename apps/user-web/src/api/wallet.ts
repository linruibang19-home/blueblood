// API - 钱包模块
import type { WalletSummary, WalletRecord } from '@/types/wallet'
import { mockWalletSummary, mockWalletRecords } from '@/mock/wallets'
import request, { USE_API, ensureReady } from './request'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

export async function getWalletSummary(): Promise<WalletSummary> {
  if (USE_API) {
    await ensureReady()
    const data = await request.get<any, any>('/wallet/summary')
    return {
      balance: Number(data?.balance ?? 0),
      pendingAmount: Number(data?.pendingAmount ?? 0),
      withdrawnAmount: Number(data?.withdrawnAmount ?? 0),
      totalEarned: Number(data?.totalEarned ?? 0),
    }
  }
  await delay(300)
  return mockWalletSummary
}

export async function getWalletRecords(): Promise<WalletRecord[]> {
  await delay(300)
  return mockWalletRecords
}

export async function withdraw(amount: number): Promise<boolean> {
  await delay(500)
  return amount > 0 && amount <= mockWalletSummary.balance
}