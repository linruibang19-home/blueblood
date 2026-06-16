<template>
  <SubPageLayout title="作业提交" :bottom-bar="true">
    <div class="assignment-submit">
      <!-- 作业信息 -->
      <div class="assignment-info">
        <h2 class="assignment-title">{{ assignment?.title }}</h2>
        <div class="assignment-meta">
          <van-tag type="primary">{{ assignment?.courseName }}</van-tag>
          <span class="deadline">
            <van-icon name="clock-o" /> 截止 {{ assignment?.deadline }}
          </span>
        </div>
      </div>

      <!-- 作业说明 -->
      <div class="assignment-desc">
        <h3 class="desc-title">作业说明</h3>
        <div class="desc-content">
          <p v-for="(line, i) in assignment?.description.split('\n')" :key="i">{{ line }}</p>
        </div>
      </div>

      <!-- 代码编辑区域 -->
      <div class="code-editor">
        <h3 class="editor-title">代码提交</h3>
        <van-field
          v-model="codeContent"
          type="textarea"
          placeholder="请输入代码..."
          rows="10"
          autosize
          class="code-textarea"
        />
      </div>

      <!-- 附件上传 -->
      <div class="upload-section">
        <h3 class="upload-title">附件上传</h3>
        <UploadBox placeholder="点击上传附件（可选）" />
      </div>

      <!-- 提交按钮 -->
      <div class="submit-bar">
        <van-button type="primary" size="large" round block @click="handleSubmit" :loading="submitting">
          提交作业
        </van-button>
      </div>
    </div>
  </SubPageLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import SubPageLayout from '@/layouts/SubPageLayout.vue'
import UploadBox from '@/components/UploadBox.vue'
import { getAssignmentDetail, submitAssignment } from '@/api/course'
import type { Assignment } from '@/types/course'

const route = useRoute()
const router = useRouter()
const assignment = ref<Assignment | null>(null)
const codeContent = ref('')
const submitting = ref(false)

onMounted(async () => {
  const id = route.params.id as string
  assignment.value = await getAssignmentDetail(id)
})

async function handleSubmit() {
  if (!codeContent.value.trim()) {
    showToast('请填写代码内容')
    return
  }
  submitting.value = true
  try {
    await submitAssignment(route.params.id as string, { code: codeContent.value })
    showToast('提交成功')
    router.back()
  } catch {
    showToast('提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.assignment-submit {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: 100px;
}

.assignment-info {
  padding: var(--spacing-xl);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.assignment-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.assignment-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.deadline {
  font-size: var(--font-size-sm);
  color: var(--danger);
  display: flex;
  align-items: center;
  gap: 4px;
}

.assignment-desc,
.code-editor,
.upload-section {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-bottom: 1px solid var(--divider);
}

.desc-title,
.editor-title,
.upload-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.desc-content p {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: var(--spacing-sm);
}

.code-textarea {
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  font-family: 'Courier New', monospace;
  font-size: var(--font-size-sm);
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