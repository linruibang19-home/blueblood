<template>
  <van-popup
    v-model:show="showPopup"
    :round="true"
    :closeable="true"
    position="bottom"
    :style="{ height: 'auto', maxWidth: '430px', margin: '0 auto' }"
    @close="handleClose"
  >
    <div class="confirm-sheet">
      <h3 class="sheet-title">{{ title }}</h3>
      <p class="sheet-desc" v-if="desc">{{ desc }}</p>
      <div class="sheet-actions">
        <van-button size="large" round @click="handleCancel">{{ cancelText }}</van-button>
        <van-button type="primary" size="large" round @click="handleConfirm">{{ confirmText }}</van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  title: string
  desc?: string
  confirmText?: string
  cancelText?: string
  modelValue?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  desc: '',
  confirmText: '确认',
  cancelText: '取消',
  modelValue: false,
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const showPopup = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  showPopup.value = val
})

watch(showPopup, (val) => {
  emit('update:modelValue', val)
})

function handleClose() {
  showPopup.value = false
}

function handleConfirm() {
  emit('confirm')
  showPopup.value = false
}

function handleCancel() {
  emit('cancel')
  showPopup.value = false
}
</script>

<style scoped>
.confirm-sheet {
  padding: var(--spacing-xl);
}

.sheet-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  text-align: center;
  margin-bottom: var(--spacing-md);
}

.sheet-desc {
  font-size: var(--font-size-md);
  color: var(--text-secondary);
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.sheet-actions {
  display: flex;
  gap: var(--spacing-md);
}

.sheet-actions .van-button {
  flex: 1;
}
</style>