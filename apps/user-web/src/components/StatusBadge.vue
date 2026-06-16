<template>
  <span class="status-badge" :class="`status-${type}`">
    {{ label }}
  </span>
</template>

<script setup lang="ts">
interface Props {
  label: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'default'
}

withDefaults(defineProps<Props>(), {
  type: 'default',
})
</script>

<style scoped>
.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: var(--font-size-xs);
  font-weight: 500;
}

.status-primary {
  background: var(--primary-alpha);
  color: var(--primary);
}

.status-success {
  background: var(--success-alpha);
  color: var(--success);
}

.status-warning {
  background: var(--warning-alpha);
  color: var(--warning);
}

.status-danger {
  background: var(--danger-alpha);
  color: var(--danger);
}

.status-default {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}
</style>