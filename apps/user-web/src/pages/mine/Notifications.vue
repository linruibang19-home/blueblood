<template>
  <SubPageLayout title="消息通知">
    <div class="notifications">
      <div class="tab-bar">
        <van-tabs v-model:active="activeTab" sticky>
          <van-tab title="全部" name="all">
            <NotificationList :list="allNotifications" @read="handleRead" />
          </van-tab>
          <van-tab title="未读" name="unread">
            <NotificationList :list="unreadNotifications" @read="handleRead" />
          </van-tab>
        </van-tabs>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import NotificationList from '@/components/NotificationList.vue'
import { getNotifications, markAsRead } from '@/api/notification'
import type { Notification } from '@/types/notification'

const notifications = ref<Notification[]>([])
const activeTab = ref('all')

onMounted(async () => {
  notifications.value = await getNotifications()
})

const allNotifications = computed(() => notifications.value)
const unreadNotifications = computed(() => notifications.value.filter(n => !n.read))

async function handleRead(id: string) {
  await markAsRead(id)
  const notification = notifications.value.find(n => n.id === id)
  if (notification) notification.read = true
  showToast('已标记为已读')
}
</script>

<style scoped>
.notifications {
  min-height: 100vh;
  background: var(--bg-primary);
}
</style>