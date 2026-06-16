<template>
  <van-tabbar
    v-model="active"
    :fixed="false"
    :border="true"
    :safe-area-inset-bottom="true"
    route
    @change="handleChange"
  >
    <van-tabbar-item to="/discover">
      <template #icon>
        <span class="tab-icon">
          <van-icon name="search" />
        </span>
      </template>
      <span class="tab-text">发现</span>
    </van-tabbar-item>
    <van-tabbar-item to="/grow">
      <template #icon>
        <span class="tab-icon">
          <van-icon name="fire" />
        </span>
      </template>
      <span class="tab-text">成长</span>
    </van-tabbar-item>
    <van-tabbar-item to="/tasks">
      <template #icon>
        <span class="tab-icon">
          <van-icon name="orders-o" />
        </span>
      </template>
      <span class="tab-text">任务</span>
    </van-tabbar-item>
    <van-tabbar-item to="/mine">
      <template #icon>
        <span class="tab-icon">
          <van-icon name="user-o" />
        </span>
      </template>
      <span class="tab-text">我的</span>
    </van-tabbar-item>
  </van-tabbar>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const active = ref(0)

const tabMap: Record<string, number> = {
  '/discover': 0,
  '/grow': 1,
  '/tasks': 2,
  '/mine': 3,
}

watch(
  () => route.path,
  (path) => {
    // 按顶级路径前缀匹配，子页面（如 /mine/wallet）也能正确高亮所属 Tab
    const topPath = '/' + (path.split('/')[1] || '')
    active.value = tabMap[topPath] ?? 0
  },
  { immediate: true }
)

function handleChange(index: number) {
  active.value = index
}
</script>

<style scoped>
.van-tabbar {
  background-color: var(--bg-secondary) !important;
  border-top: 1px solid var(--border);
  height: 56px;
}

.van-tabbar-item {
  --van-tabbar-item-text-color: var(--text-tertiary);
  --van-tabbar-item-active-color: var(--primary);
  --van-tabbar-item-icon-size: 22px;
}

.tab-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-text {
  font-size: var(--font-size-xs);
  margin-top: 2px;
}
</style>