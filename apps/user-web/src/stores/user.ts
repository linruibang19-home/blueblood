import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface UserInfo {
  id: string
  name: string
  avatar: string
  school: string
  major: string
  level: number
  levelName: string
  creditScore: number
  completedTasks: number
  currentPoints: number
  balance: number
  pendingAmount: number
  verified: boolean
  bio: string
  github: string
  skills: string[]
  tags: string[]
}

export const useUserStore = defineStore('user', () => {
  const isLoggedIn = ref(false)
  const userInfo = ref<UserInfo | null>(null)

  function setUser(info: UserInfo) {
    userInfo.value = info
    isLoggedIn.value = true
  }

  function logout() {
    userInfo.value = null
    isLoggedIn.value = false
  }

  return {
    isLoggedIn,
    userInfo,
    setUser,
    logout,
  }
})