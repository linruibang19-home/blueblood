<template>
  <SubPageLayout title="提交里程碑" :bottom-bar="true">
    <div class="milestone-submit">
      <!-- 里程碑信息 -->
      <div class="milestone-info">
        <h3 class="milestone-title">{{ milestone?.title }}</h3>
        <div class="milestone-reward" v-if="milestone?.reward">里程碑酬金 ¥{{ milestone.reward }}</div>
        <p class="milestone-desc">{{ milestone?.description }}</p>
        <div class="milestone-deadline">
          <van-icon name="clock-o" />
          截止日期：{{ milestone?.dueDate }}
        </div>
      </div>

      <!-- GitHub 仓库链接 -->
      <div class="form-section">
        <h3 class="section-title">GitHub 仓库</h3>
        <van-field
          v-model="githubUrl"
          placeholder="请输入 GitHub 仓库地址"
          prefix-icon="link"
        />
      </div>

      <!-- 提交说明 -->
      <div class="form-section">
        <h3 class="section-title">提交说明</h3>
        <van-field
          v-model="description"
          type="textarea"
          placeholder="请描述本次交付的内容和成果..."
          rows="4"
          autosize
        />
      </div>

      <!-- 附件上传 -->
      <div class="form-section">
        <h3 class="section-title">附件上传</h3>
        <van-uploader
          v-model="fileList"
          :max-count="5"
          :after-read="onAfterRead"
          accept="image/*"
        />
      </div>

      <!-- 提交按钮 -->
      <div class="submit-bar">
        <van-button
          type="primary"
          size="large"
          round
          block
          @click="handleSubmit"
          :loading="submitting"
          :disabled="!githubUrl.trim() || !description.trim()"
        >
          提交里程碑
        </van-button>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, type UploaderFileListItem } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import { getMyTaskOrderDetail, submitMilestone } from '@/api/task'
import { uploadFile } from '@/api/auth'
import type { Milestone } from '@/types/task'

const route = useRoute()
const router = useRouter()
const milestone = ref<Milestone | null>(null)
const githubUrl = ref('')
const description = ref('')
const fileList = ref<UploaderFileListItem[]>([])
const attachments = ref<string[]>([])
const submitting = ref(false)

onMounted(async () => {
  const orderId = route.params.orderId as string
  const order = await getMyTaskOrderDetail(orderId)
  milestone.value = order?.milestones?.find(m => m.id === route.params.milestoneId) || null
})

async function onAfterRead(item: UploaderFileListItem | UploaderFileListItem[]) {
  const file = Array.isArray(item) ? item[0] : item
  if (!file?.file) return
  try {
    file.status = 'uploading'
    file.message = '上传中'
    const url = await uploadFile(file.file, 'milestone')
    if (!url) throw new Error('上传失败')
    attachments.value.push(url)
    file.status = 'done'
    file.message = ''
  } catch {
    file.status = 'failed'
    file.message = '上传失败'
    showToast('附件上传失败')
  }
}

async function handleSubmit() {
  if (!githubUrl.value.trim()) {
    showToast('请填写 GitHub 仓库地址')
    return
  }
  if (!description.value.trim()) {
    showToast('请填写提交说明')
    return
  }

  submitting.value = true
  try {
    await submitMilestone(route.params.orderId as string, route.params.milestoneId as string, {
      githubUrl: githubUrl.value.trim(),
      description: description.value.trim(),
      attachments: attachments.value,
    })
    showToast('提交成功')
    router.back()
  } catch (e) {
    showToast((e as Error).message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.milestone-submit {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 100px;
}

.milestone-info {
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.milestone-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.milestone-reward {
  font-size: var(--font-size-md);
  color: var(--warning);
  font-weight: 600;
  margin-bottom: var(--spacing-sm);
}

.milestone-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: var(--spacing-md);
}

.milestone-deadline {
  font-size: var(--font-size-sm);
  color: var(--danger);
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-section {
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--bg-card);
  border-top: 1px solid var(--border);
  max-width: var(--max-width);
  margin: 0 auto;
}
</style>
