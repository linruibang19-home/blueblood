import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  const tabActive = ref('discover')
  const loading = ref(false)

  function setTabActive(name: string) {
    tabActive.value = name
  }

  function setLoading(val: boolean) {
    loading.value = val
  }

  return {
    tabActive,
    loading,
    setTabActive,
    setLoading,
  }
})