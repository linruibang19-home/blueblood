<template>
  <van-nav-bar
    :title="title"
    :left-text="leftText"
    :right-text="rightText"
    :left-arrow="showArrow"
    :fixed="fixed"
    :placeholder="placeholder"
    :safe-area-inset-top="false"
    @click-left="$emit('click-left')"
    @click-right="$emit('click-right')"
  >
    <template v-if="$slots.right" #right>
      <slot name="right" />
    </template>
  </van-nav-bar>
</template>

<script setup lang="ts">
interface Props {
  title?: string
  leftText?: string
  rightText?: string
  showArrow?: boolean
  fixed?: boolean
  placeholder?: boolean
}

withDefaults(defineProps<Props>(), {
  title: '',
  leftText: '',
  rightText: '',
  showArrow: true,
  fixed: true,
  placeholder: true,
})

defineEmits(['click-left', 'click-right'])
</script>

<style scoped>
.van-nav-bar {
  background-color: var(--bg-secondary);
}
</style>