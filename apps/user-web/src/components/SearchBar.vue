<template>
  <div class="search-bar" :class="{ 'search-bar-card': card }">
    <van-search
      v-model="keyword"
      :placeholder="placeholder"
      :shape="card ? 'round' : 'round'"
      :background="card ? 'var(--bg-card)' : 'var(--bg-tertiary)'"
      :readonly="readonly"
      @click="$emit('click', keyword)"
      @update:model-value="$emit('update:keyword', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  placeholder?: string
  card?: boolean
  readonly?: boolean
  modelValue?: string
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '搜索关键词',
  card: false,
  readonly: true,
  modelValue: '',
})

const emit = defineEmits<{
  (e: 'click', value: string): void
  (e: 'update:keyword', value: string): void
}>()

const keyword = ref(props.modelValue)
</script>

<style scoped>
.search-bar {
  width: 100%;
}

.search-bar-card {
  padding: var(--spacing-sm);
  background: var(--bg-card);
  border-radius: var(--radius-md);
}
</style>