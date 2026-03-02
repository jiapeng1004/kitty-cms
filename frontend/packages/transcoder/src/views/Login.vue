<template>
  <div class="login-wrap">
    <a-card title="转码服务登录（AK/SK）" class="card">
      <a-form :model="form" layout="vertical" @finish="onSubmit">
        <a-form-item label="Access Key ID" name="accessKeyId" :rules="[{ required: true }]">
          <a-input v-model:value="form.accessKeyId" placeholder="AK..." @blur="form.accessKeyId = (form.accessKeyId || '').trim()" />
        </a-form-item>
        <a-form-item label="Secret Key" name="secretKey" :rules="[{ required: true }]">
          <a-input-password v-model:value="form.secretKey" placeholder="SK..." @blur="form.secretKey = (form.secretKey || '').trim()" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" block>登录</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { login } from '../api/auth_api'

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
    message.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f0f2f5; }
.card { width: 400px; }
</style>
