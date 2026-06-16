<template>
  <div class="progress-bar">
    <div class="progress-track">
      <div class="progress-fill" :style="{ width: `${percentage}%`, background: color }" />
    </div>
    <span v-if="showText" class="progress-text">{{ percentage }}%</span>
  </div>
</template>

<script setup lang="ts">
interface Props {
  percentage: number
  showText?: boolean
  color?: string
}

withDefaults(defineProps<Props>(), {
  showText: true,
  color: 'var(--primary)',
})
</script>

<style scoped>
.progress-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.progress-track {
  flex: 1;
  height: 6px;
  background: var(--bg-tertiary);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  min-width: 36px;
  text-align: right;
}
</style>