<template>
  <div class="level-badge" :class="`level-${level}`">
    <span class="level-text">LV{{ level }}</span>
    <span class="level-name">{{ levelName }}</span>
  </div>
</template>

<script setup lang="ts">
interface Props {
  level: number
  levelName?: string
}

withDefaults(defineProps<Props>(), {
  level: 1,
  levelName: '新锐',
})
</script>

<style scoped>
.level-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: var(--font-size-xs);
  font-weight: 500;
}

.level-1, .level-2 {
  background: var(--primary-alpha);
  color: var(--primary);
}

.level-3, .level-4 {
  background: var(--accent-alpha);
  color: var(--accent);
}

.level-5, .level-6 {
  background: var(--warning-alpha);
  color: var(--warning);
}

.level-7, .level-8 {
  background: linear-gradient(135deg, var(--warning), #FFD700);
  color: #fff;
}

.level-text {
  font-weight: 700;
}
</style>