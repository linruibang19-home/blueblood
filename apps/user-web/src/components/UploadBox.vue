<template>
  <div class="upload-box" :class="{ 'upload-draggable': draggable }" @click="handleClick">
    <van-uploader
      :after-read="handleAfterRead"
      :before-read="handleBeforeRead"
      :max-count="maxCount"
      :accept="accept"
      :multiple="multiple"
      @click-upload="handleClick"
    >
      <div v-if="!files.length" class="upload-placeholder">
        <van-icon name="plus" class="upload-icon" />
        <span class="upload-text">{{ placeholder }}</span>
      </div>
    </van-uploader>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { showToast } from 'vant'

interface Props {
  placeholder?: string
  maxCount?: number
  accept?: string
  multiple?: boolean
  draggable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '点击上传',
  maxCount: 1,
  accept: '*',
  multiple: false,
  draggable: false,
})

const files = ref<any[]>([])

const emit = defineEmits<{
  (e: 'change', files: any[]): void
}>()

function handleClick() {
  // trigger upload
}

function handleBeforeRead() {
  return true
}

function handleAfterRead(file: any) {
  files.value.push(file)
  emit('change', files.value)
  showToast('上传成功')
}
</script>

<style scoped>
.upload-box {
  width: 100%;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 100px;
  border: 1px dashed var(--border);
  border-radius: var(--radius-md);
  background: var(--bg-tertiary);
  cursor: pointer;
  transition: all 0.2s;
}

.upload-placeholder:hover {
  border-color: var(--primary);
  background: var(--primary-alpha);
}

.upload-icon {
  font-size: 28px;
  color: var(--text-tertiary);
  margin-bottom: var(--spacing-sm);
}

.upload-text {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}
</style>