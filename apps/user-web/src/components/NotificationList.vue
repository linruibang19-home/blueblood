<template>
  <div class="notification-list">
    <div v-if="list.length" class="list-content">
      <div
        v-for="item in list"
        :key="item.id"
        class="notification-item"
        :class="{ 'unread': !item.read }"
        @click="handleClick(item)"
      >
        <div class="item-icon" :class="item.type">
          <van-icon :name="getIcon(item.type)" />
        </div>
        <div class="item-content">
          <div class="item-header">
            <span class="item-title">{{ item.title }}</span>
            <span v-if="!item.read" class="unread-dot" />
          </div>
          <p class="item-desc">{{ item.content }}</p>
          <span class="item-time">{{ item.createdAt }}</span>
        </div>
      </div>
    </div>
    <van-empty v-else description="暂无通知" />
  </div>
</template>

<script setup lang="ts">
import type { Notification } from '@/types/notification'

interface Props {
  list: Notification[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'read', id: string): void
}>()

function getIcon(type: string) {
  const map: Record<string, string> = {
    milestone: 'flag-o',
    task_review: 'orders-o',
    income: 'cash-o',
    system: 'info-o',
    group: 'friends-o',
    course: 'bookmark-o',
  }
  return map[type] || 'bell-o'
}

function handleClick(item: Notification) {
  if (!item.read) {
    emit('read', item.id)
  }
}
</script>

<style scoped>
.notification-list {
  padding: var(--spacing-lg);
}

.list-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.notification-item {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
}

.notification-item.unread {
  border-left: 3px solid var(--primary);
}

.item-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.item-icon.milestone { background: var(--primary-alpha); color: var(--primary); }
.item-icon.task_review { background: var(--warning-alpha); color: var(--warning); }
.item-icon.income { background: var(--success-alpha); color: var(--success); }
.item-icon.system { background: var(--bg-tertiary); color: var(--text-tertiary); }
.item-icon.group { background: var(--accent-alpha); color: var(--accent); }
.item-icon.course { background: var(--primary-alpha); color: var(--primary); }

.item-content {
  flex: 1;
  min-width: 0;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
}

.item-title {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--danger);
  flex-shrink: 0;
}

.item-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: var(--spacing-sm);
}

.item-time {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}
</style>