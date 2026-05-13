<template>
  <div class="login-wrap">
    <div class="login-brand">
      <KittyLogo />
      <p class="brand-desc">视频转码服务</p>
    </div>
    <a-card class="card">
      <template #title>
        <span class="card-title">AK/SK 登录</span>
      </template>
      <a-form :model="form" layout="vertical" @finish="onSubmit">
        <a-form-item label="Access Key ID" name="accessKeyId" :rules="[{ required: true }]">
          <a-input v-model:value="form.accessKeyId" placeholder="AK..." @blur="form.accessKeyId = (form.accessKeyId || '').trim()" />
        </a-form-item>
        <a-form-item label="Secret Key" name="secretKey" :rules="[{ required: true }]">
          <a-input-password v-model:value="form.secretKey" placeholder="SK..." @blur="form.secretKey = (form.secretKey || '').trim()" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" block class="login-btn">登录</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import KittyLogo from '../components/KittyLogo.vue'
import { message } from 'ant-design-vue'
import { login } from '../api/auth_api'
import { showApiErrorDialog } from '../utils/errorDialog'

const router = useRouter()
const loading = ref(false)
const form = reactive({ accessKeyId: '', secretKey: '' })

async function onSubmit() {
  try {
    loading.value = true
    const res = await login({ accessKeyId: (form.accessKeyId || '').trim(), secretKey: (form.secretKey || '').trim() })
    localStorage.setItem('transcoder_token', res.token)
    message.success('登录成功')
    router.push('/')
  } catch (e) {
    showApiErrorDialog(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #F5F7FA;
  padding: 24px;
}
.login-brand {
  text-align: center;
  margin-bottom: 40px;
}
.login-brand :deep(.kitty-logo) {
  justify-content: center;
}
.login-brand :deep(.logo-icon) {
  width: 48px;
  height: 48px;
}
.login-brand :deep(.logo-text) {
  font-size: 22px;
}
.brand-desc {
  margin: 12px 0 0;
  font-size: 14px;
  color: #666;
  letter-spacing: 0.02em;
}
.card {
  width: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid #eee;
}
.card-title {
  font-weight: 600;
  color: #333;
}
.login-btn {
  background: #006EFF !important;
  border: none !important;
  height: 40px;
  font-weight: 500;
}
.login-btn:hover {
  background: #0052CC !important;
}
</style>
