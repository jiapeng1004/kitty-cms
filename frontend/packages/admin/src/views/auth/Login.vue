<template>
  <div class="login-container">
    <a-card title="用户登录" class="login-card">
      <a-form
        :model="formState"
        name="login"
        autocomplete="off"
        @finish="handleLogin"
      >
        <a-form-item
          name="username"
          :rules="[{ required: true, message: '请输入用户名!' }]"
        >
          <a-input v-model:value="formState.username" placeholder="用户名">
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="password"
          :rules="[{ required: true, message: '请输入密码!' }]"
        >
          <a-input-password v-model:value="formState.password" placeholder="密码">
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item
          name="captcha"
          :rules="[{ required: true, message: '请输入验证码!' }]"
        >
          <a-row :gutter="[8, 0]">
            <a-col :span="14">
              <a-input v-model:value="formState.captcha" placeholder="验证码">
                <template #prefix>
                  <SafetyCertificateOutlined />
                </template>
              </a-input>
            </a-col>
            <a-col :span="10">
              <img 
                :src="captchaUrl" 
                alt="验证码" 
                class="captcha-img" 
                @click="refreshCaptcha"
              />
            </a-col>
          </a-row>
        </a-form-item>

        <a-form-item>
          <a-button 
            type="primary" 
            html-type="submit" 
            :loading="loading"
            block
          >
            登录
          </a-button>
          <div class="register-link">
            还没有账号？ <a @click="$router.push('/register')">立即注册</a>
          </div>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { UserOutlined, LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import api from '../../utils/api'

const router = useRouter()
const loading = ref(false)
const captchaUrl = ref('/api/user/captcha')

const formState = reactive({
  username: '',
  password: '',
  captcha: ''
})

onMounted(() => {
  refreshCaptcha()
})

const refreshCaptcha = () => {
  captchaUrl.value = `/api/user/captcha?t=${Date.now()}`
}

const handleLogin = async (values) => {
  try {
    loading.value = true
    const response = await api.post('/api/user/login', values)
    message.success('登录成功')
    // TODO: 保存token到store
    router.push('/')
  } catch (error) {
    message.error(error.message || '登录失败')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.login-card {
  width: 400px;
  max-width: 90%;
}

.captcha-img {
  width: 100%;
  height: 32px;
  cursor: pointer;
  border-radius: 4px;
}

.register-link {
  text-align: center;
  margin-top: 16px;
}
</style>