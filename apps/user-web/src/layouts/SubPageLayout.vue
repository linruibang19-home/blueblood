<template>
  <div class="sub-page-layout">
    <div class="layout-container">
      <!-- 顶部导航栏 -->
      <div class="nav-bar safe-area-top">
        <van-nav-bar
          :title="title"
          :left-arrow="showBack"
          :fixed="true"
          :placeholder="true"
          :z-index="101"
          @click-left="handleBack"
        >
          <template #right>
            <slot name="right" />
          </template>
        </van-nav-bar>
      </div>
      <!-- 内容区域 -->
      <div class="layout-content" :class="{ 'has-bottom-bar': bottomBar }">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

interface Props {
  title?: string
  showBack?: boolean
  // 页面底部是否有固定操作栏（如保存/提交按钮），有则预留 60px 避免遮挡
  bottomBar?: boolean
}

withDefaults(defineProps<Props>(), {
  title: '',
  showBack: true,
  bottomBar: false,
})

const router = useRouter()

function handleBack() {
  router.back()
}
</script>

<style scoped>
.sub-page-layout {
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
  background-color: var(--bg-secondary);
}

.layout-content {
  flex: 1;
  padding-bottom: env(safe-area-inset-bottom);
  overflow-y: auto;
  overflow-x: hidden;
}

/* 底部有固定操作栏时，内容区预留操作栏高度，避免最后内容被遮挡 */
.layout-content.has-bottom-bar {
  padding-bottom: calc(env(safe-area-inset-bottom) + 70px);
}
</style>