<template>
  <view class="page">
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else-if="!task" class="empty">
      <text class="empty-ic">📭</text>
      <text class="empty-tx">任务不存在</text>
    </view>

    <view v-else>
      <!-- 头部信息 -->
      <view class="head-card">
        <view class="h-title">{{ task.title }}</view>
        <view class="h-reward-row">
          <text class="h-reward">¥{{ task.reward }}</text>
          <text class="h-cate">{{ task.category }}</text>
        </view>
        <view class="h-meta">
          <text class="meta-item">发布方：{{ task.employerName || '蓝血平台' }}</text>
          <text class="meta-item">名额：{{ task.slotsLeft }}/{{ task.totalSlots }}</text>
          <text class="meta-item" v-if="task.deadline">截止：{{ task.deadline }}</text>
        </view>
      </view>

      <!-- 描述 -->
      <view class="section">
        <view class="section-title">任务描述</view>
        <view class="section-body">{{ task.description || '暂无详细描述' }}</view>
      </view>

      <!-- 技能 -->
      <view class="section" v-if="task.skills.length">
        <view class="section-title">技能要求</view>
        <view class="skill-list">
          <text class="skill" v-for="(s, i) in task.skills" :key="i">{{ s }}</text>
        </view>
      </view>

      <!-- 门槛 -->
      <view class="section">
        <view class="section-title">参与门槛</view>
        <view class="section-body">需要达到 Lv.{{ task.levelRequired }} 及以上等级</view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="footer" v-if="task">
      <button
        class="btn-accept"
        :loading="accepting"
        :disabled="accepting"
        @tap="onAccept"
      >
        {{ task.myOrderId ? '已接单 · 查看任务' : '立即接单' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { getTaskDetail, acceptTask, type Task } from '@/api/task'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const task = ref<Task | null>(null)
const loading = ref(true)
const accepting = ref(false)
const taskId = ref('')

async function load() {
  loading.value = true
  task.value = await getTaskDetail(taskId.value)
  loading.value = false
}

async function onAccept() {
  if (!userStore.token) {
    uni.showToast({ title: '请先到「我的」登录', icon: 'none' })
    return
  }
  // 已接单：直接跳我的任务
  if (task.value?.myOrderId) {
    uni.navigateTo({ url: '/pages/task/orders' })
    return
  }
  accepting.value = true
  try {
    await acceptTask(taskId.value)
    uni.showToast({ title: '接单成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/task/orders' })
    }, 600)
  } catch (e: any) {
    uni.showToast({ title: e.message || '接单失败', icon: 'none' })
  } finally {
    accepting.value = false
  }
}

onLoad((q: any) => {
  taskId.value = String(q?.id || '')
  load()
})

/** 转发给好友 */
onShareAppMessage(() => {
  const t = task.value
  return {
    title: t ? `${t.title} · ¥${t.reward}` : '蓝血菁英 · 成长任务',
    path: `/pages/task/detail?id=${taskId.value}`,
  }
})

/** 分享到朋友圈 */
onShareTimeline(() => {
  const t = task.value
  return {
    title: t ? `${t.title} · ¥${t.reward}` : '蓝血菁英 · 成长任务',
    query: `id=${taskId.value}`,
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f7f8fc;
  padding-bottom: 160rpx;
  box-sizing: border-box;
}
.loading,
.empty {
  text-align: center;
  padding-top: 240rpx;
  color: #8a8f9c;
}
.empty-ic {
  font-size: 100rpx;
  display: block;
}
.empty-tx {
  font-size: 28rpx;
  margin-top: 24rpx;
  display: block;
}

.head-card {
  background: #fff;
  padding: 32rpx;
  margin-bottom: 20rpx;
}
.h-title {
  font-size: 38rpx;
  font-weight: 700;
  color: #2c3345;
}
.h-reward-row {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
}
.h-reward {
  font-size: 44rpx;
  font-weight: 700;
  color: #f5a623;
}
.h-cate {
  margin-left: 20rpx;
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  background: #eaf2fc;
  color: #4a90e2;
  border-radius: 8rpx;
}
.h-meta {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
}
.meta-item {
  font-size: 26rpx;
  color: #8a8f9c;
  line-height: 2;
}

.section {
  background: #fff;
  padding: 32rpx;
  margin-bottom: 20rpx;
}
.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2c3345;
  margin-bottom: 16rpx;
}
.section-body {
  font-size: 28rpx;
  color: #5a6478;
  line-height: 1.6;
}
.skill-list {
  display: flex;
  flex-wrap: wrap;
}
.skill {
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  background: #f0f2f7;
  color: #5a6478;
  border-radius: 24rpx;
  margin-right: 16rpx;
  margin-bottom: 12rpx;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 32rpx calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 12rpx rgba(44, 51, 69, 0.06);
}
.btn-accept {
  background: #4a90e2;
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
  height: 88rpx;
  line-height: 88rpx;
}
.btn-accept[disabled] {
  opacity: 0.6;
}
</style>
