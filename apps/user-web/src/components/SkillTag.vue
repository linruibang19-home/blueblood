<template>
  <span class="skill-tag" :class="{ 'tag-addable': addable, 'tag-removable': removable }" @click="handleClick">
    {{ text }}
    <van-icon v-if="removable" name="cross" class="tag-icon" />
    <van-icon v-if="addable" name="plus" class="tag-icon" />
  </span>
</template>

<script setup lang="ts">
interface Props {
  text: string
  addable?: boolean
  removable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  addable: false,
  removable: false,
})

const emit = defineEmits<{
  (e: 'click'): void
  (e: 'remove'): void
}>()

function handleClick() {
  if (props.removable) {
    emit('remove')
  } else if (props.addable) {
    emit('click')
  }
}
</script>

<style scoped>
.skill-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  border-radius: 6px;
  font-size: var(--font-size-sm);
  transition: all 0.2s;
}

.tag-removable {
  cursor: pointer;
}

.tag-removable:hover {
  background: var(--danger-alpha);
  color: var(--danger);
}

.tag-addable {
  cursor: pointer;
  border: 1px dashed var(--border);
  background: transparent;
}

.tag-addable:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.tag-icon {
  font-size: 10px;
}
</style>