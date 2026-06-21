<template>
  <div class="login-page">
    <!-- 品牌 -->
    <div class="brand">
      <h1 class="brand-title">蓝血菁英</h1>
      <p class="brand-sub">超级个体 · 成长平台</p>
    </div>

    <!-- 登录表单 -->
    <div class="login-card">
      <van-form @submit="handleLogin">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            name="username"
            label="账号"
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
        </van-cell-group>
        <div class="submit-wrap">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
        </div>
      </van-form>

      <p class="hint">演示账号：lin / zhangming / lina / wangqiang（密码 123456）</p>

      <p class="enterprise-link">
        <router-link to="/register">没有账号？去注册</router-link>
        <span class="link-sep">·</span>
        <router-link to="/enterprise-register">企业注册 →</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { login } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  loading.value = true
  try {
    const res = await login(form.username, form.password)
    localStorage.setItem('token', res.token)
    showToast('登录成功')
    const redirect = (route.query.redirect as string) || '/mine'
    router.replace(redirect)
  } catch (e) {
    showToast((e as Error).message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
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

.login-card {
  width: 100%;
  max-width: 420px;
  background: var(--bg-card);
  border-radius: 16px 16px 0 0;
  padding: 28px 0 40px;
  min-height: 50vh;
}

.submit-wrap {
  margin: 20px var(--spacing-xl) 0;
}

.hint {
  text-align: center;
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
  margin-top: 20px;
  padding: 0 var(--spacing-xl);
}

.enterprise-link {
  text-align: center;
  margin-top: 12px;
}

.enterprise-link a {
  font-size: var(--font-size-sm);
  color: var(--primary);
  text-decoration: none;
}
</style>
