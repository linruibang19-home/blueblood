<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import TheSidebar from './TheSidebar.vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 侧边栏折叠
const collapse = ref(false)
function toggleCollapse() {
  collapse.value = !collapse.value
}

// 当前页面标题（顶部栏 + 浏览器页签）
const pageTitle = computed(() => (route.meta.title as string) || '管理后台')

// 用户名（优先本地 store，无则回退展示）
const displayName = computed(() => userStore.username || '管理员')

// 退出登录
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消',
    })
  } catch {
    return // 取消
  }
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}

// 刷新页面后恢复用户信息（token 已从 localStorage 恢复）
onMounted(() => {
  if (userStore.isLoggedIn && !userStore.userInfo) {
    userStore.fetchUserInfo().catch(() => {
      // token 失效等：store 会被响应拦截器/守卫处理
    })
  }
})
</script>

<template>
  <div class="admin-layout">
    <TheSidebar :collapse="collapse" />

    <div class="admin-layout__main">
      <!-- 顶部栏 -->
      <header class="topbar">
        <div class="topbar__left">
          <el-icon
            class="topbar__toggle"
            :size="20"
            @click="toggleCollapse"
          >
            <Fold v-if="!collapse" />
            <Expand v-else />
          </el-icon>
          <span class="topbar__title">{{ pageTitle }}</span>
        </div>

        <div class="topbar__right">
          <el-dropdown trigger="click">
            <span class="topbar__user">
              <el-icon><UserFilled /></el-icon>
              <span class="topbar__username">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 主内容区 -->
      <main class="admin-layout__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped lang="scss">
.admin-layout {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;

  &__main {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  &__content {
    flex: 1;
    overflow-y: auto;
    background-color: #f0f2f5;
  }
}

.topbar {
  height: 60px;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;

  &__left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__toggle {
    cursor: pointer;
    color: #5a5e66;

    &:hover {
      color: #2459e6;
    }
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  &__user {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    color: #5a5e66;
    outline: none;
  }

  &__username {
    font-size: 14px;
  }
}
</style>
