<template>
  <div class="register-page">
    <!-- 品牌 -->
    <div class="brand">
      <h1 class="brand-title">蓝血菁英</h1>
      <p class="brand-sub">超级个体 · 成长平台</p>
    </div>

    <!-- 注册卡片 -->
    <div class="register-card">
      <van-tabs v-model:active="activeTab" shrink>
        <van-tab title="用户名" name="username" />
        <van-tab title="邮箱" name="email" />
        <van-tab title="手机" name="phone" />
      </van-tabs>

      <van-form @submit="handleRegister">
        <van-cell-group inset>
          <!-- 用户名 -->
          <template v-if="activeTab === 'username'">
            <van-field
              v-model="form.username"
              name="username"
              label="用户名"
              placeholder="请输入用户名"
              :rules="[{ required: true, message: '请输入用户名' }]"
            />
            <van-field
              v-model="form.password"
              type="password"
              name="password"
              label="密码"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请输入密码' }]"
            />
            <van-field
              v-model="form.confirmPassword"
              type="password"
              name="confirmPassword"
              label="确认密码"
              placeholder="请再次输入密码"
              :rules="[
                { required: true, message: '请再次输入密码' },
                { validator: validateConfirm, message: '两次密码不一致' },
              ]"
            />
          </template>

          <!-- 邮箱 -->
          <template v-else-if="activeTab === 'email'">
            <van-field
              v-model="form.email"
              name="email"
              label="邮箱"
              placeholder="请输入邮箱"
              :rules="[{ required: true, message: '请输入邮箱' }]"
            />
            <van-field
              v-model="form.code"
              name="code"
              label="验证码"
              placeholder="请输入验证码"
              center
              clearable
            >
              <template #button>
                <van-button
                  size="small"
                  type="primary"
                  :disabled="counting && seconds > 0"
                  :loading="sendingCode"
                  @click.prevent="onSendCode('email')"
                >
                  {{ codeButtonText }}
                </van-button>
              </template>
            </van-field>
            <van-field
              v-model="form.password"
              type="password"
              name="password"
              label="密码"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请输入密码' }]"
            />
          </template>

          <!-- 手机 -->
          <template v-else>
            <van-field
              v-model="form.phone"
              name="phone"
              label="手机号"
              placeholder="请输入手机号"
              :rules="[{ required: true, message: '请输入手机号' }]"
            />
            <van-field
              v-model="form.code"
              name="code"
              label="验证码"
              placeholder="请输入验证码"
              center
              clearable
            >
              <template #button>
                <van-button
                  size="small"
                  type="primary"
                  :disabled="counting && seconds > 0"
                  :loading="sendingCode"
                  @click.prevent="onSendCode('phone')"
                >
                  {{ codeButtonText }}
                </van-button>
              </template>
            </van-field>
            <van-field
              v-model="form.password"
              type="password"
              name="password"
              label="密码"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请输入密码' }]"
            />
          </template>
        </van-cell-group>

        <div class="submit-wrap">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            注册
          </van-button>
        </div>
      </van-form>

      <p class="login-link">
        已有账号？<router-link to="/login">去登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { sendCode, register } from '@/api/auth'

type RegisterType = 'username' | 'email' | 'phone'

const router = useRouter()
const loading = ref(false)
const sendingCode = ref(false)
const activeTab = ref<RegisterType>('username')

const form = reactive({
  username: '',
  email: '',
  phone: '',
  code: '',
  password: '',
  confirmPassword: '',
})

// 倒计时
const counting = ref(false)
const seconds = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const codeButtonText = computed(() =>
  counting.value && seconds.value > 0 ? `${seconds.value}s 后重发` : '获取验证码',
)

function startCountdown() {
  counting.value = true
  seconds.value = 60
  timer = setInterval(() => {
    seconds.value -= 1
    if (seconds.value <= 0) {
      counting.value = false
      if (timer) {
        clearInterval(timer)
        timer = null
      }
    }
  }, 1000)
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})

function validateConfirm(val: string): boolean {
  return val === form.password
}

async function onSendCode(type: 'email' | 'phone') {
  const target = type === 'email' ? form.email.trim() : form.phone.trim()
  if (!target) {
    showToast(type === 'email' ? '请输入邮箱' : '请输入手机号')
    return
  }
  sendingCode.value = true
  try {
    const res = await sendCode(target, type)
    startCountdown()
    // dev 桩：验证码直接返回，自动填入便于测试
    if (res?.code) {
      form.code = String(res.code)
      showToast('验证码已发送')
    } else {
      showToast('验证码已发送，请查收')
    }
  } catch (e) {
    showToast((e as Error).message || '验证码发送失败')
  } finally {
    sendingCode.value = false
  }
}

async function handleRegister() {
  loading.value = true
  try {
    const payload = {
      registerType: activeTab.value,
      password: form.password,
      username: form.username,
      email: form.email,
      phone: form.phone,
      code: form.code,
    }
    await register(payload)
    showToast('注册成功')
    router.replace('/login')
  } catch (e) {
    showToast((e as Error).message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(160deg, var(--primary), var(--accent));
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 18vh;
}

.brand {
  text-align: center;
  color: #fff;
  margin-bottom: 32px;
}

.brand-title {
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 2px;
}

.brand-sub {
  font-size: 14px;
  opacity: 0.85;
  margin-top: 6px;
}

.register-card {
  width: 100%;
  max-width: 420px;
  background: var(--bg-card);
  border-radius: 16px 16px 0 0;
  padding: 16px 0 40px;
  min-height: 50vh;
}

.register-card :deep(.van-tabs) {
  margin-bottom: 12px;
}

.submit-wrap {
  margin: 20px var(--spacing-xl) 0;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

.login-link a {
  color: var(--primary);
  text-decoration: none;
}
</style>
