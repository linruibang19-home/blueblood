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
        <text class="label">附件</text>
        <view class="attach-grid">
          <view
            class="attach-item"
            v-for="(a, i) in attachments"
            :key="i"
            @tap="onPreview(i)"
          >
            <image class="attach-img" :src="a.url" mode="aspectFill" />
            <view class="attach-del" @tap.stop="onRemove(i)">×</view>
          </view>
          <view class="attach-add" v-if="attachments.length < 6" @tap="onChoose">
            <text class="add-ic">+</text>
            <text class="add-tx">选择附件</text>
          </view>
        </view>
        <view class="attach-tip" v-if="uploading">上传中...</view>
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
import { uploadFile } from '@/utils/upload'
import { requestSubscribe } from '@/utils/subscribe'

/** 审核结果通知模板（占位 ID，需在小程序后台配置后替换） */
const TMPL_AUDIT_RESULT = 'tmpl_audit_result_placeholder'

const orderId = ref('')
const milestoneId = ref('')
const submitting = ref(false)
const uploading = ref(false)

interface Attachment {
  url: string
  name?: string
}
const attachments = ref<Attachment[]>([])

const form = reactive({
  githubUrl: '',
  description: '',
})

/** 选择附件并上传 */
async function onChoose() {
  try {
    const choose: any = await uni.chooseImage({
      count: 6 - attachments.value.length,
      sizeType: ['compressed', 'original'],
    })
    const temps: string[] = choose?.tempFilePaths || []
    if (!temps.length) return
    uploading.value = true
    for (const p of temps) {
      const up = await uploadFile({ filePath: p, bizType: 'milestone' })
      attachments.value.push({ url: up.url })
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '上传失败', icon: 'none' })
  } finally {
    uploading.value = false
  }
}

function onRemove(idx: number) {
  attachments.value.splice(idx, 1)
}

function onPreview(idx: number) {
  uni.previewImage({
    current: attachments.value[idx]?.url,
    urls: attachments.value.map((a) => a.url),
  })
}

async function onSubmit() {
  if (!form.githubUrl && !form.description && attachments.value.length === 0) {
    uni.showToast({ title: '请填写成果说明、链接或附件', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await submitMilestone(orderId.value, milestoneId.value, {
      githubUrl: form.githubUrl,
      description: form.description,
      attachments: attachments.value,
    })
    // 提交成功后可选订阅「审核结果通知」模板（前端订阅动作）
    requestSubscribe([TMPL_AUDIT_RESULT]).catch(() => {
      /* 用户拒绝或环境不支持，忽略 */
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
.attach-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}
.attach-item {
  position: relative;
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  overflow: hidden;
}
.attach-img {
  width: 100%;
  height: 100%;
}
.attach-del {
  position: absolute;
  top: 0;
  right: 0;
  width: 40rpx;
  height: 40rpx;
  line-height: 36rpx;
  text-align: center;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 32rpx;
  border-bottom-left-radius: 12rpx;
}
.attach-add {
  width: 180rpx;
  height: 180rpx;
  background: #f7f8fc;
  border: 2rpx dashed #d8dce5;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.add-ic {
  font-size: 56rpx;
  color: #b5bcc9;
  line-height: 1;
}
.add-tx {
  font-size: 22rpx;
  color: #8a8f9c;
  margin-top: 8rpx;
}
.attach-tip {
  font-size: 24rpx;
  color: #8a8f9c;
  margin-top: 12rpx;
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
