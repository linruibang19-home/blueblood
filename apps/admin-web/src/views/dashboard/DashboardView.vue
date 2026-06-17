<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const greetingName = computed(() => userStore.username || '管理员')

// 统计占位数据（后续接入业务接口后替换）
const stats = [
  { label: '注册用户', value: '—', icon: 'User', color: '#2459e6' },
  { label: '活跃小组', value: '—', icon: 'UserFilled', color: '#13c2c2' },
  { label: '进行中任务', value: '—', icon: 'List', color: '#fa8c16' },
  { label: '今日收益(元)', value: '—', icon: 'Money', color: '#52c41a' },
]
</script>

<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <el-card shadow="never" class="dashboard__welcome">
      <div class="welcome">
        <div class="welcome__avatar">
          <el-icon :size="40" color="#fff"><Avatar /></el-icon>
        </div>
        <div class="welcome__text">
          <h2 class="welcome__title">欢迎回来，{{ greetingName }} 👋</h2>
          <p class="welcome__desc">
            这里是蓝血菁英平台管理后台，请通过左侧菜单进入对应管理模块。
          </p>
        </div>
      </div>
    </el-card>

    <!-- 统计占位 -->
    <div class="dashboard__stats">
      <el-card
        v-for="item in stats"
        :key="item.label"
        shadow="hover"
        class="stat-card"
      >
        <div class="stat">
          <div class="stat__icon" :style="{ backgroundColor: item.color }">
            <el-icon :size="22" color="#fff">
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="stat__body">
            <div class="stat__value">{{ item.value }}</div>
            <div class="stat__label">{{ item.label }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 提示 -->
    <el-card shadow="never" class="dashboard__tip">
      <el-alert
        title="工作台为占位页面，统计与图表将在业务接口接入后补全。"
        type="info"
        :closable="false"
        show-icon
      />
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome {
  display: flex;
  align-items: center;
  gap: 20px;

  &__avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: linear-gradient(135deg, #2459e6, #1e3a8a);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__title {
    margin: 0 0 6px;
    font-size: 20px;
    font-weight: 700;
    color: #1d2129;
  }

  &__desc {
    margin: 0;
    font-size: 14px;
    color: #5a5e66;
  }
}

.dashboard__stats {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 14px;

  &__icon {
    width: 48px;
    height: 48px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__value {
    font-size: 24px;
    font-weight: 700;
    color: #1d2129;
    line-height: 1.2;
  }

  &__label {
    font-size: 13px;
    color: #909399;
    margin-top: 2px;
  }
}
</style>
