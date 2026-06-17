<script setup lang="ts">
import { menuGroups } from '@/config/menu'

defineProps<{
  /** 侧边栏折叠状态 */
  collapse: boolean
}>()

// 当前激活菜单高亮：使用路由 path 匹配
</script>

<template>
  <aside class="sidebar" :class="{ 'is-collapse': collapse }">
    <!-- 品牌 Logo 区 -->
    <div class="sidebar__logo">
      <el-icon :size="24" color="#fff"><Platform /></el-icon>
      <span v-show="!collapse" class="sidebar__title">蓝血菁英平台</span>
    </div>

    <!-- 菜单：分组渲染 -->
    <el-scrollbar class="sidebar__scroll">
      <el-menu
        :default-active="$route.path"
        :collapse="collapse"
        :collapse-transition="false"
        router
        background-color="#001529"
        text-color="#b7c0cd"
        active-text-color="#ffffff"
        class="sidebar__menu"
      >
        <template v-for="group in menuGroups" :key="group.title">
          <el-menu-item-group
            :title="collapse ? '' : group.title"
            class="sidebar__group"
          >
            <el-menu-item
              v-for="item in group.children"
              :key="item.path"
              :index="item.path"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </el-menu-item-group>
        </template>
      </el-menu>
    </el-scrollbar>
  </aside>
</template>

<style scoped lang="scss">
.sidebar {
  width: 220px;
  height: 100vh;
  background-color: #001529;
  display: flex;
  flex-direction: column;
  transition: width 0.28s ease;

  &.is-collapse {
    width: 64px;
  }

  &__logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    flex-shrink: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  }

  &__title {
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    white-space: nowrap;
    letter-spacing: 1px;
  }

  &__scroll {
    flex: 1;
    overflow: hidden;
  }

  &__menu {
    border-right: none;
  }

  &__group {
    :deep(.el-menu-item-group__title) {
      color: #5a6678;
      font-size: 12px;
      padding-left: 20px;
      padding-top: 12px;
      padding-bottom: 4px;
    }
  }

  // 去除 el-menu 折叠时的右边框
  :deep(.el-menu--collapse) {
    width: 64px;
  }
}
</style>
