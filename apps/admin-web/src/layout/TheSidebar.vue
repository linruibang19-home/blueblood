<script setup lang="ts">
import { computed } from 'vue'
import { menuGroups } from '@/config/menu'

defineProps<{
  /** 侧边栏折叠状态 */
  collapse: boolean
}>()

// 默认展开所有分组（折叠模式下不展开）
const openeds = computed(() => menuGroups.map((g) => g.title))
</script>

<template>
  <aside class="sidebar" :class="{ 'is-collapse': collapse }">
    <!-- 品牌 Logo 区 -->
    <div class="sidebar__logo">
      <el-icon :size="24" color="#fff"><Platform /></el-icon>
      <span v-show="!collapse" class="sidebar__title">蓝血菁英平台</span>
    </div>

    <!-- 菜单：父级分组(可展开) → 子级菜单项，层级清晰 -->
    <el-scrollbar class="sidebar__scroll">
      <el-menu
        :default-active="$route.path"
        :default-openeds="openeds"
        :collapse="collapse"
        :collapse-transition="false"
        :unique-opened="false"
        router
        background-color="#001529"
        text-color="#b7c0cd"
        active-text-color="#ffffff"
        class="sidebar__menu"
      >
        <template v-for="group in menuGroups" :key="group.title">
          <!-- 单项分组：直接渲染为菜单项（如工作台） -->
          <el-menu-item
            v-if="group.children.length === 1"
            :index="group.children[0].path"
            class="sidebar__top-item"
          >
            <el-icon><component :is="group.children[0].icon" /></el-icon>
            <template #title>{{ group.children[0].title }}</template>
          </el-menu-item>

          <!-- 多项分组：可展开父级，子级缩进，层级分明 -->
          <el-sub-menu v-else :index="group.title" popper-class="sidebar-popper">
            <template #title>
              <el-icon class="sidebar__group-icon"><Folder /></el-icon>
              <span class="sidebar__group-title">{{ group.title }}</span>
            </template>
            <el-menu-item
              v-for="item in group.children"
              :key="item.path"
              :index="item.path"
              class="sidebar__sub-item"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </el-sub-menu>
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

  // 顶级单菜单项
  &__top-item {
    :deep(.el-sub-menu__title),
    &.el-menu-item {
      height: 50px;
      line-height: 50px;
    }
  }

  // 父级分组标题：更亮、加粗、略大，带分组图标
  :deep(.el-sub-menu__title) {
    color: #dfe4ea !important;
    font-weight: 600;
    font-size: 14px;
    height: 46px;
    line-height: 46px;
    margin-top: 6px;
    border-top: 1px solid rgba(255, 255, 255, 0.06);
  }

  &__group-icon {
    color: #7e8b9e;
    margin-right: 6px;
  }

  // 子级菜单项：左缩进、字号略小、颜色偏暗，与父级拉开层级
  &__sub-item {
    min-width: auto;
    padding-left: 48px !important;
    font-size: 13px;
    color: #8d99ad;
    height: 42px;
    line-height: 42px;

    &:hover {
      color: #fff;
      background-color: rgba(255, 255, 255, 0.04) !important;
    }

    &.is-active {
      color: #fff;
      background-color: var(--el-color-primary) !important;
    }

    .el-icon {
      font-size: 15px;
      color: inherit;
    }
  }
}

// 去除 el-menu 折叠时的右边框
:deep(.el-menu--collapse) {
  width: 64px;
}
</style>
