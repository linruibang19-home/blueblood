<script setup lang="ts">
// 通用业务占位页：接收 title，展示「XX管理 - 待开发」空状态。
defineProps<{
  title: string
}>()
</script>

<template>
  <div class="placeholder-view">
    <el-card shadow="never" class="placeholder-card">
      <el-empty :description="`${title} - 待开发`">
        <template #image>
          <el-icon :size="64" color="#c0c4cc">
            <DocumentRemove />
          </el-icon>
        </template>
      </el-empty>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.placeholder-view {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 24px;
}

.placeholder-card {
  width: 100%;
  border-radius: 8px;
}
</style>
