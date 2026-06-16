<template>
  <div class="user-avatar" :class="[`size-${size}`, { 'avatar-verified': verified }]">
    <van-image
      :src="src || defaultAvatar"
      :width="sizeMap[size]"
      :height="sizeMap[size]"
      round
      fit="cover"
      @click="$emit('click')"
    />
    <span v-if="verified" class="verified-badge">
      <van-icon name="passed" />
    </span>
  </div>
</template>

<script setup lang="ts">
interface Props {
  src?: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
  verified?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  src: '',
  size: 'md',
  verified: false,
})

const sizeMap = {
  sm: 32,
  md: 44,
  lg: 60,
  xl: 80,
}

const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

defineEmits(['click'])
</script>

<style scoped>
.user-avatar {
  position: relative;
  display: inline-block;
}

.avatar-verified .verified-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 16px;
  height: 16px;
  background: var(--primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 10px;
  border: 2px solid var(--bg-secondary);
}

.size-sm .verified-badge { width: 14px; height: 14px; font-size: 9px; }
.size-xl .verified-badge { width: 20px; height: 20px; font-size: 12px; }
</style>