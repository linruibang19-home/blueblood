import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, logout as apiLogout, me as apiMe, type LoginUser } from '@/api/auth'
import { TOKEN_KEY } from '@/config'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(uni.getStorageSync(TOKEN_KEY) || '')
  const userInfo = ref<LoginUser | null>(null)

  async function login(username: string, password: string) {
    const res = await apiLogin({ username, password })
    token.value = res.token
    uni.setStorageSync(TOKEN_KEY, res.token)
    return res
  }

  async function fetchMe() {
    if (!token.value) {
      userInfo.value = null
      return null
    }
    try {
      const user = await apiMe()
      userInfo.value = user
      return user
    } catch (e) {
      userInfo.value = null
      return null
    }
  }

  async function logout() {
    try {
      await apiLogout()
    } catch {
      // ignore
    }
    token.value = ''
    userInfo.value = null
    uni.removeStorageSync(TOKEN_KEY)
  }

  return { token, userInfo, login, fetchMe, logout }
})
