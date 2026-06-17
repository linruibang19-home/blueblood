<template>
  <view class="page">
    <view class="form-card">
      <view class="form-title">提交里程碑成果</view>

      <view class="field">
        <text class="label">GitHub 链接</text>
        <input class="input" v-model="form.githubUrl" placeholder="https://github.com/xxx/xxx" />
      </view>

      <view class="field">
        <text class="label">成果说明</text>
        <textarea
          class="textarea"
          v-model="form.description"
          placeholder="请描述本次交付内容、完成情况等"
          maxlength="500"
        />
      </view>

      <view class="field">
        <text class="label">附件（占位）</text>
        <view class="attach-placeholder">
          <text class="attach-ic">📎</text>
          <text class="attach-tx">附件上传将在 doc04 接入 uni.uploadFile</text>
        </view>
      </view>
    </view>

    <view class="footer">
      <button class="btn-submit" :loading="submitting" :disabled="submitting" @tap="onSubmit">
        提交
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { submitMilestone } from '@/api/task'

const orderId = ref('')
const milestoneId = ref('')
const submitting = ref(false)

const form = reactive({
  githubUrl: '',
  description: '',
  attachments: [] as any[],
})

async function onSubmit() {
  if (!form.githubUrl && !form.description) {
    uni.showToast({ title: '请填写成果说明或链接', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await submitMilestone(orderId.value, milestoneId.value, {
      githubUrl: form.githubUrl,
      description: form.description,
      attachments: form.attachments,
    })
    uni.showToast({ title: '提交成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 600)
  } catch (e: any) {
    uni.showToast({ title: e.message || '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onLoad((q: any) => {
  orderId.value = String(q?.orderId || '')
  milestoneId.value = String(q?.milestoneId || '')
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f7f8fc;
  padding: 24rpx;
  padding-bottom: 180rpx;
  box-sizing: border-box;
}
.form-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(44, 51, 69, 0.06);
}
.form-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #2c3345;
  margin-bottom: 24rpx;
}
.field {
  margin-bottom: 32rpx;
}
.label {
  display: block;
  font-size: 28rpx;
  color: #5a6478;
  margin-bottom: 16rpx;
}
.input {
  width: 100%;
  background: #f7f8fc;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
.textarea {
  width: 100%;
  background: #f7f8fc;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  min-height: 200rpx;
  box-sizing: border-box;
}
.attach-placeholder {
  background: #f7f8fc;
  border: 2rpx dashed #d8dce5;
  border-radius: 12rpx;
  padding: 40rpx;
  text-align: center;
}
.attach-ic {
  font-size: 56rpx;
  display: block;
}
.attach-tx {
  font-size: 24rpx;
  color: #8a8f9c;
  margin-top: 12rpx;
  display: block;
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
.btn-submit {
  background: #4a90e2;
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
  height: 88rpx;
  line-height: 88rpx;
}
.btn-submit[disabled] {
  opacity: 0.6;
}
</style>
