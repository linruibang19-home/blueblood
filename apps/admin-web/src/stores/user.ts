import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, logout as apiLogout, me as apiMe } from '@/api/auth'
import type { LoginParams } from '@/api/auth'
import { TOKEN_KEY } from '@/api/request'

export interface UserInfo {
  userId: number
  username: string
  role: string
}

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 恢复 token
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')

  /** 登录：保存 token + 拉取用户信息 */
  async function login(params: LoginParams): Promise<void> {
    const result = await apiLogin(params)
    token.value = result.token
    localStorage.setItem(TOKEN_KEY, result.token)
    userInfo.value = {
      userId: result.userId,
      username: result.username,
      role: result.role,
    }
  }

  /** 拉取当前用户信息（用于刷新后恢复） */
  async function fetchUserInfo(): Promise<UserInfo | null> {
    if (!token.value) return null
    const data = await apiMe()
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      role: data.role,
    }
    return userInfo.value
  }

  /** 登出：清 token + 用户信息（失败不阻塞跳转） */
  async function logout(): Promise<void> {
    try {
      if (token.value) await apiLogout()
    } catch {
      // 忽略后端登出错误，本地仍要清理
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem(TOKEN_KEY)
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    login,
    fetchUserInfo,
    logout,
  }
})
