// API - 钱包模块
import type { WalletSummary, WalletRecord } from '@/types/wallet'
import { mockWalletSummary, mockWalletRecords } from '@/mock/wallets'

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

export async function getWalletSummary(): Promise<WalletSummary> {
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