<template>
  <div class="mobile-tab-layout">
    <div class="layout-container">
      <!-- 顶部安全区 -->
      <div class="safe-area-top" />
      <!-- 内容区域 -->
      <div class="layout-content">
        <slot />
      </div>
      <!-- 底部 Tab -->
      <div class="layout-tabbar safe-area-bottom">
        <AppTabBar />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AppTabBar from '@/components/AppTabBar.vue'
</script>

<style scoped>
.mobile-tab-layout {
  width: 100%;
  min-height: 100vh;
  background-color: var(--bg-primary);
}

.layout-container {
  width: 100%;
  max-width: var(--max-width);
  margin: 0 auto;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-primary);
}

.safe-area-top {
  height: env(safe-area-inset-top);
  background-color: var(--bg-secondary);
}

.layout-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background-color: var(--bg-primary);
}

.layout-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.layout-tabbar > :deep(*) {
  max-width: var(--max-width);
  margin: 0 auto;
}
</style>