<template>
  <div class="banner-card" @click="$emit('click')">
    <van-swipe :autoplay="3000" :show-indicators="showIndicators" class="banner-swipe">
      <van-swipe-item v-for="(item, index) in banners" :key="index">
        <div class="banner-item" :style="{ background: item.bg }">
          <div class="banner-content">
            <span class="banner-tag" v-if="item.tag">{{ item.tag }}</span>
            <h3 class="banner-title">{{ item.title }}</h3>
            <p class="banner-desc" v-if="item.desc">{{ item.desc }}</p>
          </div>
        </div>
      </van-swipe-item>
    </van-swipe>
  </div>
</template>

<script setup lang="ts">
export interface BannerItem {
  title: string
  desc?: string
  tag?: string
  bg?: string
  image?: string
}

interface Props {
  banners: BannerItem[]
  showIndicators?: boolean
  card?: boolean
}

withDefaults(defineProps<Props>(), {
  showIndicators: true,
  card: false,
})

defineEmits(['click'])
</script>

<style scoped>
.banner-card {
  width: 100%;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.banner-swipe {
  width: 100%;
  height: 140px;
}

.banner-item {
  width: 100%;
  height: 140px;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  display: flex;
  align-items: center;
  padding: var(--spacing-xl);
}

.banner-content {
  color: #fff;
}

.banner-tag {
  font-size: var(--font-size-xs);
  background: rgba(255,255,255,0.25);
  padding: 2px 8px;
  border-radius: 10px;
}

.banner-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  margin: var(--spacing-sm) 0;
}

.banner-desc {
  font-size: var(--font-size-sm);
  opacity: 0.85;
}
</style>