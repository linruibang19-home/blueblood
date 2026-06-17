<template>
  <view class="page">
    <!-- 搜索 -->
    <view class="search-bar">
      <view class="search-input">
        <text class="search-ic">🔍</text>
        <input
          class="input"
          v-model="keyword"
          placeholder="搜索任务标题 / 描述"
          confirm-type="search"
          @confirm="onSearch"
        />
      </view>
    </view>

    <!-- 分类筛选 -->
    <scroll-view class="cate-scroll" scroll-x show-scrollbar="false">
      <view class="cate-list">
        <view
          v-for="c in categories"
          :key="c.id"
          class="cate-item"
          :class="{ active: c.id === activeCate }"
          @tap="onCate(c.id)"
        >
          {{ c.name }}
        </view>
      </view>
    </scroll-view>

    <!-- 任务列表 -->
    <view class="list" v-if="tasks.length">
      <view class="task-card" v-for="t in tasks" :key="t.id" @tap="goDetail(t.id)">
        <view class="t-head">
          <text class="t-title">{{ t.title }}</text>
          <text class="t-reward">¥{{ t.reward }}</text>
        </view>
        <view class="t-desc">{{ t.description || '暂无描述' }}</view>
        <view class="t-tags">
          <text class="tag" v-if="t.category">{{ t.category }}</text>
          <text class="tag lv">Lv.{{ t.levelRequired }}+</text>
        </view>
        <view class="t-foot">
          <text class="t-meta">{{ t.employerName || '蓝血平台' }}</text>
          <text class="t-slots">名额 {{ t.slotsLeft }}/{{ t.totalSlots }}</text>
        </view>
      </view>
      <view class="load-tip">{{ loading ? '加载中...' : '没有更多了' }}</view>
    </view>

    <view v-else-if="!loading" class="empty">
      <text class="empty-ic">📭</text>
      <text class="empty-tx">暂无任务</text>
    </view>

    <!-- 我的任务入口 -->
    <view class="fab" @tap="goOrders">我的任务</view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { getTaskList, getTaskCategories, type Task, type TaskCategory } from '@/api/task'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const tasks = ref<Task[]>([])
const categories = ref<TaskCategory[]>([{ id: 'all', name: '全部', icon: '', count: 0 }])
const activeCate = ref('all')
const keyword = ref('')
const loading = ref(false)

async function loadCate() {
  categories.value = await getTaskCategories()
  if (!categories.value.find((c) => c.id === 'all')) {
    categories.value = [{ id: 'all', name: '全部', icon: '', count: 0 }, ...categories.value]
  }
}

async function loadTasks() {
  loading.value = true
  try {
    const params: any = { page: 1, pageSize: 20 }
    if (activeCate.value !== 'all') params.category = activeCate.value
    if (keyword.value) params.keyword = keyword.value
    tasks.value = await getTaskList(params)
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function onCate(id: string) {
  activeCate.value = id
  loadTasks()
}
function onSearch() {
  loadTasks()
}
function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/task/detail?id=${id}` })
}
function goOrders() {
  if (!userStore.token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/task/orders' })
}

onShow(() => {
  loadCate()
  loadTasks()
})
onPullDownRefresh(async () => {
  await loadTasks()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f7f8fc;
  box-sizing: border-box;
  padding-bottom: 120rpx;
}

/* 搜索 */
.search-bar {
  padding: 20rpx 24rpx;
  background: #fff;
}
.search-input {
  display: flex;
  align-items: center;
  background: #f0f2f7;
  border-radius: 36rpx;
  padding: 0 24rpx;
  height: 72rpx;
}
.search-ic {
  font-size: 28rpx;
  margin-right: 12rpx;
}
.input {
  flex: 1;
  font-size: 28rpx;
}

/* 分类 */
.cate-scroll {
  background: #fff;
  white-space: nowrap;
  padding: 16rpx 24rpx 20rpx;
}
.cate-list {
  display: inline-flex;
}
.cate-item {
  display: inline-block;
  padding: 12rpx 28rpx;
  margin-right: 16rpx;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #5a6478;
  background: #f0f2f7;
}
.cate-item.active {
  background: #4a90e2;
  color: #fff;
}

/* 列表 */
.list {
  padding: 20rpx 24rpx;
}
.task-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(44, 51, 69, 0.06);
}
.t-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.t-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #2c3345;
  flex: 1;
}
.t-reward {
  font-size: 34rpx;
  font-weight: 700;
  color: #f5a623;
}
.t-desc {
  font-size: 26rpx;
  color: #8a8f9c;
  margin-top: 12rpx;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.t-tags {
  display: flex;
  margin-top: 16rpx;
}
.tag {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  background: #eaf2fc;
  color: #4a90e2;
  margin-right: 12rpx;
}
.tag.lv {
  background: #fff4e6;
  color: #f5a623;
}
.t-foot {
  display: flex;
  justify-content: space-between;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f2f7;
}
.t-meta {
  font-size: 24rpx;
  color: #8a8f9c;
}
.t-slots {
  font-size: 24rpx;
  color: #52c9a4;
}
.load-tip {
  text-align: center;
  font-size: 24rpx;
  color: #b8bfcc;
  padding: 24rpx 0;
}

/* 空 */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}
.empty-ic {
  font-size: 100rpx;
}
.empty-tx {
  font-size: 28rpx;
  color: #8a8f9c;
  margin-top: 24rpx;
}

/* 浮动按钮 */
.fab {
  position: fixed;
  right: 32rpx;
  bottom: 60rpx;
  background: #4a90e2;
  color: #fff;
  font-size: 26rpx;
  padding: 22rpx 28rpx;
  border-radius: 40rpx;
  box-shadow: 0 6rpx 16rpx rgba(74, 144, 226, 0.4);
  z-index: 10;
}
</style>
