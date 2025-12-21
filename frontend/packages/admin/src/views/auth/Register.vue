<template>
  <div class="register-container">
    <a-card title="用户注册" class="register-card">
      <a-form
        :model="formState"
        name="register"
        autocomplete="off"
        @finish="handleRegister"
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
          name="confirmPassword"
          :rules="[
            { required: true, message: '请确认密码!' },
            { validator: validateConfirmPassword }
          ]"
        >
          <a-input-password v-model:value="formState.confirmPassword" placeholder="确认密码">
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button 
            type="primary" 
            html-type="submit" 
            :loading="loading"
            block
          >
            注册
          </a-button>
          <div class="login-link">
            已有账号？ <a @click="$router.push('/login')">立即登录</a>
          </div>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import api from '../../utils/api'

const router = useRouter()
const loading = ref(false)

const formState = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_, value) => {
  if (!value || formState.password === value) {
    return Promise.resolve()
  }
  return Promise.reject('两次输入的密码不一致!')
}

const handleRegister = async (values) => {
  try {
    loading.value = true
    const response = await api.post('/api/user/register', {
      username: values.username,
      password: values.password
    })
    message.success('注册成功')
    router.push('/login')
  } catch (error) {
    message.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.register-card {
  width: 400px;
  max-width: 90%;
}

.login-link {
  text-align: center;
  margin-top: 16px;
}
</style>