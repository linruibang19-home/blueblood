<template>
  <SubPageLayout title="私信">
    <div class="chat-detail">
      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ 'message-self': msg.senderId === currentUserId }"
        >
          <UserAvatar v-if="msg.senderId !== currentUserId" :src="msg.senderAvatar" :size="'sm'" />
          <div class="message-bubble">
            <p class="message-text">{{ msg.content }}</p>
            <span class="message-time">{{ msg.createdAt }}</span>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="chat-input-bar">
        <van-field
          v-model="inputText"
          placeholder="输入消息..."
          @keyup.enter="handleSend"
        >
          <template #button>
            <van-button size="small" type="primary" @click="handleSend" :disabled="!inputText.trim()">发送</van-button>
          </template>
        </van-field>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import type { ChatMessage } from '@/types/post'

const route = useRoute()
const inputText = ref('')
const currentUserId = 'u001'
const messages = ref<ChatMessage[]>([])
const messageListRef = ref<HTMLElement>()

// 模拟对方信息
const otherUser = {
  id: 'u002',
  name: '张明',
  avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
}

// 模拟回复
const mockReplies = [
  '好的，收到！',
  '这个思路很不错，赞！',
  '具体说说你的方案？',
  '了解了，我看看',
  '有问题随时问我',
]

onMounted(async () => {
  // 初始化一些模拟消息
  messages.value = [
    {
      id: 'msg001',
      sessionId: route.params.id as string,
      senderId: 'u002',
      senderName: otherUser.name,
      senderAvatar: otherUser.avatar,
      content: '你好！看了你的资料，很感兴趣',
      type: 'text',
      createdAt: '10:30',
    },
    {
      id: 'msg002',
      sessionId: route.params.id as string,
      senderId: currentUserId,
      senderName: '林同学',
      senderAvatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
      content: '你好！请问有什么可以帮到你的吗',
      type: 'text',
      createdAt: '10:32',
    },
  ]

  await nextTick()
  scrollToBottom()
})

async function handleSend() {
  if (!inputText.value.trim()) return

  const newMsg: ChatMessage = {
    id: `msg_${Date.now()}`,
    sessionId: route.params.id as string,
    senderId: currentUserId,
    senderName: '林同学',
    senderAvatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
    content: inputText.value.trim(),
    type: 'text',
    createdAt: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  }

  messages.value.push(newMsg)
  inputText.value = ''
  showToast('已发送')
  await nextTick()
  scrollToBottom()

  // 模拟对方回复
  setTimeout(async () => {
    const replyMsg: ChatMessage = {
      id: `msg_${Date.now() + 1}`,
      sessionId: route.params.id as string,
      senderId: otherUser.id,
      senderName: otherUser.name,
      senderAvatar: otherUser.avatar,
      content: mockReplies[Math.floor(Math.random() * mockReplies.length)],
      type: 'text',
      createdAt: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    }
    messages.value.push(replyMsg)
    await nextTick()
    scrollToBottom()
  }, 1000)
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}
</script>

<style scoped>
.chat-detail {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 46px);
  background: var(--bg-primary);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.message-item {
  display: flex;
  gap: var(--spacing-sm);
  max-width: 75%;
}

.message-self {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.message-bubble {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  box-shadow: var(--shadow-sm);
}

.message-self .message-bubble {
  background: var(--primary);
  color: #fff;
}

.message-text {
  font-size: var(--font-size-md);
  line-height: 1.5;
  word-break: break-word;
}

.message-time {
  font-size: var(--font-size-xs);
  opacity: 0.7;
  display: block;
  text-align: right;
  margin-top: 4px;
}

.chat-input-bar {
  background: var(--bg-card);
  border-top: 1px solid var(--border);
  padding: var(--spacing-sm) var(--spacing-lg);
  padding-bottom: calc(var(--spacing-sm) + env(safe-area-inset-bottom));
}
</style>