<template>
  <view class="page">
    <view class="title">发现</view>
    <view class="desc">蓝血菁英 · 发现页占位（doc03 迁移业务）</view>
    <view class="api-tip" v-if="apiText">接口连通测试：{{ apiText }}</view>
    <button class="btn" @tap="testApi">测试请求 /auth/me</button>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { me } from '@/api/auth'

const apiText = ref('')

async function testApi() {
  try {
    const u = await me()
    apiText.value = u?.username ? `当前用户：${u.username}` : '请求成功（未登录或无用户信息）'
  } catch (e: any) {
    apiText.value = `请求结果：${e.message || e}`
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  padding: 32rpx;
  background: #f7f8fc;
  box-sizing: border-box;
}
.title {
  font-size: 44rpx;
  font-weight: 600;
  color: #2c3345;
  margin-bottom: 16rpx;
}
.desc {
  font-size: 26rpx;
  color: #909bad;
  margin-bottom: 40rpx;
}
.api-tip {
  font-size: 26rpx;
  color: #5a6478;
  margin-bottom: 24rpx;
}
.btn {
  background: #4a90e2;
  color: #fff;
  border-radius: 16rpx;
}
</style>
