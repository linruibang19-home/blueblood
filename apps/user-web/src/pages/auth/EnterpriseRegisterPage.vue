<template>
  <div class="register-page">
    <van-nav-bar title="企业注册" left-arrow @click-left="onBack" />

    <div class="register-card">
      <van-form @submit="handleSubmit">
        <van-cell-group inset>
          <div class="section-title">企业信息</div>
          <van-field
            v-model="form.companyName"
            label="公司名称"
            placeholder="请输入公司全称"
            :rules="[{ required: true, message: '请输入公司名称' }]"
          />
          <van-field
            v-model="form.creditCode"
            label="统一信用代码"
            placeholder="请输入18位统一社会信用代码"
            :rules="[{ required: true, message: '请输入统一信用代码' }]"
          />
          <van-field label="营业执照" :rules="[{ required: true, message: '请上传营业执照' }]" input-align="right">
            <template #input>
              <van-uploader
                v-model="licenseFileList"
                :max-count="1"
                :after-read="onLicenseAfterRead"
                accept="image/*"
              />
            </template>
          </van-field>

          <div class="section-title">联系人信息</div>
          <van-field
            v-model="form.contactName"
            label="联系人"
            placeholder="请输入联系人姓名"
            :rules="[{ required: true, message: '请输入联系人' }]"
          />
          <van-field
            v-model="form.contactPhone"
            label="联系电话"
            type="tel"
            placeholder="请输入联系电话"
            :rules="[{ required: true, message: '请输入联系电话' }, { pattern: /^1\d{10}$/, message: '请输入正确的手机号' }]"
          />

          <div class="section-title">账号信息</div>
          <van-field
            v-model="form.username"
            label="账号"
            placeholder="用于登录的用户名"
            :rules="[{ required: true, message: '请输入账号' }]"
          />
          <van-field
            v-model="form.password"
            type="password"
            label="密码"
            placeholder="请设置登录密码"
            :rules="[{ required: true, message: '请输入密码' }, { pattern: /^.{6,}$/, message: '密码至少6位' }]"
          />
        </van-cell-group>

        <div class="submit-wrap">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            提交注册申请
          </van-button>
        </div>

        <p class="hint">提交后需等待平台审核通过，方可发布岗位与黑客松。</p>
      </van-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, type UploaderFileListItem } from 'vant'
import { register, uploadFile } from '@/api/auth'
import { submitEnterpriseApplication } from '@/api/enterprise'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  companyName: '',
  creditCode: '',
  licenseUrl: '',
  contactName: '',
  contactPhone: '',
  username: '',
  password: '',
})

const licenseFileList = ref<UploaderFileListItem[]>([])

function onBack() {
  router.back()
}

async function onLicenseAfterRead(item: UploaderFileListItem | UploaderFileListItem[]) {
  const file = Array.isArray(item) ? item[0] : item
  if (!file || !file.file) return
  try {
    file.status = 'uploading'
    file.message = '上传中'
    const url = await uploadFile(file.file, 'license')
    if (!url) throw new Error('上传失败')
    form.licenseUrl = url
    file.status = 'done'
    file.message = ''
  } catch (e) {
    file.status = 'failed'
    file.message = '上传失败'
    showToast((e as Error).message || '营业执照上传失败')
  }
}

async function handleSubmit() {
  if (!form.licenseUrl) {
    showToast('请上传营业执照')
    return
  }
  loading.value = true
  try {
    // 1. 创建企业账号
    await register({
      username: form.username,
      password: form.password,
      userType: 'enterprise',
      phone: form.contactPhone,
    })
    // 2. 提交企业认证申请（此时无需 token，或后端允许新注册企业用户提交）
    await submitEnterpriseApplication({
      companyName: form.companyName,
      creditCode: form.creditCode,
      licenseUrl: form.licenseUrl,
      contactName: form.contactName,
      contactPhone: form.contactPhone,
    })
    showSuccessToast('申请已提交，待审核')
    setTimeout(() => router.replace('/login'), 1200)
  } catch (e) {
    showToast((e as Error).message || '提交失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: var(--bg-primary, #f7f8fa);
}

.register-card {
  padding: var(--spacing-lg, 16px) 0 var(--spacing-xl, 24px);
}

.section-title {
  font-size: var(--font-size-sm, 13px);
  color: var(--text-tertiary, #969799);
  padding: var(--spacing-md, 12px) var(--spacing-md, 12px) var(--spacing-xs, 4px);
}

.submit-wrap {
  margin: 20px var(--spacing-xl, 16px) 0;
}

.hint {
  text-align: center;
  font-size: var(--font-size-xs, 12px);
  color: var(--text-tertiary, #969799);
  margin-top: 16px;
  padding: 0 var(--spacing-xl, 16px);
}
</style>
