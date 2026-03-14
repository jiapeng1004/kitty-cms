<template>
  <div class="auth-page">
    <!-- 与登录页一致：全屏渐变 + 光斑 + 柔光 -->
    <div class="auth-page__bg">
      <div class="auth-page__bg-blob auth-page__bg-blob--1"/>
      <div class="auth-page__bg-blob auth-page__bg-blob--2"/>
      <div class="auth-page__bg-blob auth-page__bg-blob--3"/>
      <div class="auth-page__mask"/>
      <div class="auth-page__author">
        <div class="auth-page__brand">
          <img :src="IMG_LOGO" alt="Kitty CMS" class="auth-page__logo"/>
          <h1 class="auth-page__title">Kitty CMS</h1>
          <p class="auth-page__slogan">内容管理 · 一站式智能管理</p>
        </div>
      </div>
    </div>

    <div class="auth-page__form-wrap">
      <div class="auth-page__card">
        <h2 class="auth-page__card-title">用户注册</h2>
        <a-form
          :model="formState"
          name="register"
          autocomplete="off"
          @finish="handleRegister"
        >
          <a-form-item v-if="tenantOptions.length > 0" name="tenantId" class="auth-page__form-item--no-label">
            <a-select
              v-model:value="selectedTenantId"
              placeholder="请选择租户"
              size="large"
              allow-clear
              :loading="tenantLoading"
              :options="tenantOptions"
              :field-names="tenantSelectFieldNames"
              class="auth-page__tenant-select"
              @change="onTenantChange"
            />
          </a-form-item>
          <a-form-item
            name="nickName"
            :rules="[{ required: true, message: '请输入昵称（2-15 位）!' }]"
          >
            <a-input
              v-model:value="formState.nickName"
              placeholder="昵称（2-15 位）"
              size="large"
            >
              <template #prefix>
                <UserOutlined class="auth-page__input-icon"/>
              </template>
            </a-input>
          </a-form-item>

          <a-form-item
            name="pwd"
            :rules="[
              { required: true, message: '请输入密码!' },
              { validator: validatePwd }
            ]"
          >
            <a-input-password
              v-model:value="formState.pwd"
              placeholder="密码"
              size="large"
            >
              <template #prefix>
                <LockOutlined class="auth-page__input-icon"/>
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
            <a-input-password
              v-model:value="formState.confirmPassword"
              placeholder="确认密码"
              size="large"
            >
              <template #prefix>
                <LockOutlined class="auth-page__input-icon"/>
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item name="captcha" :rules="[{ required: true, message: '请输入验证码!' }]">
            <a-row :gutter="8" align="middle">
              <a-col :span="14">
                <a-input
                  v-model:value="formState.captcha"
                  placeholder="验证码"
                  size="large"
                >
                  <template #prefix>
                    <SafetyCertificateOutlined class="auth-page__input-icon"/>
                  </template>
                </a-input>
              </a-col>
              <a-col :span="10" class="captcha-col">
                <template v-if="captchaLoadFailed">
                  <div
                    class="captcha-placeholder"
                    title="点击刷新验证码"
                    @click="refreshCaptcha"
                  >
                    <span class="captcha-placeholder-text">点击刷新</span>
                  </div>
                </template>
                <img
                  v-show="!captchaLoadFailed"
                  :key="captchaSrc"
                  :src="captchaSrc"
                  alt="验证码"
                  class="captcha-img"
                  title="点击刷新验证码"
                  @click="refreshCaptcha"
                  @load="captchaLoadFailed = false"
                  @error="captchaLoadFailed = true"
                />
              </a-col>
            </a-row>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              :loading="loading"
              block
              class="auth-page__submit"
            >
              注册
            </a-button>
            <div class="auth-page__footer-link">
              已有账号？ <a @click="router.push('/login')">立即登录</a>
            </div>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { UserOutlined, LockOutlined, SafetyCertificateOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { setTenantId, getTenantId, getResponseMessage } from '../../utils/api'
import { register, fetchCaptchaImageUrl } from '../../api/auth_api'
import { getTenantList } from '../../api/tenant_api'
import { IMG_LOGO } from '@/assets'

const router = useRouter()
const loading = ref(false)
const captchaSrc = ref('')
const captchaBlobUrlRef = ref<string | null>(null)
const captchaLoadFailed = ref(false)
const tenantLoading = ref(false)
const tenantSelectFieldNames = { label: 'name', value: 'id' }

interface TenantOption {
  id: string
  name: string
}
const tenantOptions = ref<TenantOption[]>([])
const selectedTenantId = ref<string | undefined>(getTenantId() || undefined)

const formState = reactive({
  nickName: '',
  pwd: '',
  confirmPassword: '',
  captcha: ''
})

function onTenantChange(tenantId: string | undefined) {
  setTenantId(tenantId || '')
}

/** 与后端 KtUserServiceImpl 一致：至少 8 位，仅字母与数字，且须包含大写、小写、数字 */
const PWD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/
const PWD_ONLY_ALNUM = /^[a-zA-Z0-9]*$/

function validatePwd(_: unknown, value: string) {
  if (!value) return Promise.resolve()
  if (PWD_REGEX.test(value)) return Promise.resolve()
  if (!PWD_ONLY_ALNUM.test(value)) {
    return Promise.reject(new Error('密码仅允许字母与数字，不能包含符号'))
  }
  return Promise.reject(new Error('密码至少 8 位，且须包含大写、小写和数字'))
}

const validateConfirmPassword = (_: unknown, value: string) => {
  if (!value || formState.pwd === value) {
    return Promise.resolve()
  }
  return Promise.reject(new Error('两次输入的密码不一致!'))
}

async function refreshCaptcha() {
  if (captchaBlobUrlRef.value) {
    URL.revokeObjectURL(captchaBlobUrlRef.value)
    captchaBlobUrlRef.value = null
  }
  captchaLoadFailed.value = false
  captchaSrc.value = ''
  try {
    const blobUrl = await fetchCaptchaImageUrl()
    captchaBlobUrlRef.value = blobUrl
    captchaSrc.value = blobUrl
  } catch (_) {
    captchaLoadFailed.value = true
  }
}

const handleRegister = async () => {
  try {
    loading.value = true
    await register({
      nickName: formState.nickName,
      pwd: formState.pwd,
      captcha: formState.captcha
    })
    message.success('注册成功')
    router.push('/login')
  } catch (error) {
    message.error(getResponseMessage(error))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    tenantLoading.value = true
    const list = await getTenantList()
    tenantOptions.value = list || []
    if (!selectedTenantId.value && list?.length > 0) {
      selectedTenantId.value = list[0].id
      setTenantId(list[0].id)
    }
  } catch (_) {
    tenantOptions.value = []
  } finally {
    tenantLoading.value = false
  }
  await refreshCaptcha()
})
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.auth-page__bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, #7c3aed 0%, #a78bfa 28%, #c4b5fd 55%, #e9d5ff 82%, #f5d0fe 100%);
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 48px 0 48px 12%;
}

.auth-page__bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  pointer-events: none;
}
.auth-page__bg-blob--1 {
  width: min(60vw, 480px);
  height: min(60vw, 480px);
  background: rgba(255, 255, 255, 0.35);
  top: -10%;
  left: -5%;
}
.auth-page__bg-blob--2 {
  width: min(45vw, 360px);
  height: min(45vw, 360px);
  background: rgba(196, 181, 253, 0.6);
  bottom: 10%;
  right: 15%;
}
.auth-page__bg-blob--3 {
  width: min(35vw, 280px);
  height: min(35vw, 280px);
  background: rgba(245, 208, 254, 0.5);
  top: 50%;
  left: 35%;
  transform: translate(-50%, -50%);
}

.auth-page__mask {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 80% 60% at 20% 50%, rgba(255, 255, 255, 0.2) 0%, transparent 50%),
    radial-gradient(ellipse 60% 80% at 85% 20%, rgba(255, 255, 255, 0.08) 0%, transparent 45%);
  pointer-events: none;
}

.auth-page__author {
  position: relative;
  z-index: 1;
  text-align: left;
  color: #fff;
}

.auth-page__brand {
  max-width: 320px;
}

.auth-page__logo {
  width: 72px;
  height: 72px;
  margin: 0 0 20px;
  display: block;
  border-radius: 16px;
  object-fit: contain;
}

.auth-page__title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px;
  letter-spacing: 0.02em;
}

.auth-page__slogan {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.auth-page__form-wrap {
  position: absolute;
  right: 10%;
  top: 50%;
  transform: translateY(-50%);
  width: 100%;
  max-width: 400px;
  padding: 0;
  z-index: 2;
}

.auth-page__card {
  width: 100%;
  max-width: 360px;
  margin-left: auto;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15), 0 0 1px rgba(0, 0, 0, 0.08);
}

.auth-page__card-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 28px;
  color: #1f2937;
}

.auth-page__input-icon {
  color: #94a3b8;
}

.auth-page__card :deep(.ant-input),
.auth-page__card :deep(.ant-input-affix-wrapper) {
  border-radius: 10px;
  border-color: #e2e8f0;
  transition: border-color 0.2s, box-shadow 0.2s;
  min-height: 40px;
  line-height: 1.5715;
  padding-top: 6px;
  padding-bottom: 6px;
  box-sizing: border-box;
}
.auth-page__card :deep(.ant-input-affix-wrapper .ant-input) {
  min-height: 28px;
  line-height: 1.5715;
  padding: 0;
  box-sizing: border-box;
}
.auth-page__card :deep(.ant-input:hover),
.auth-page__card :deep(.ant-input-affix-wrapper:hover:not(.ant-input-affix-wrapper-disabled)) {
  border-color: #cbd5e1;
}
.auth-page__card :deep(.ant-input:focus),
.auth-page__card :deep(.ant-input-affix-wrapper-focused) {
  border-color: #a060e6;
  box-shadow: 0 0 0 2px rgba(160, 96, 230, 0.15);
  outline: none;
}
.auth-page__card :deep(.ant-input::placeholder) {
  color: #94a3b8;
}
.auth-page__card :deep(.ant-input-affix-wrapper) {
  padding-left: 12px;
  padding-right: 12px;
  align-items: stretch;
}
.auth-page__card :deep(.ant-input-affix-wrapper > .ant-input) {
  height: auto;
}
.auth-page__card :deep(.ant-input-password .ant-input) {
  border: none;
  box-shadow: none;
}
.auth-page__card :deep(.ant-input-password.ant-input-affix-wrapper:hover .ant-input),
.auth-page__card :deep(.ant-input-password.ant-input-affix-wrapper-focused .ant-input) {
  border: none;
  box-shadow: none;
}
.auth-page__card :deep(.ant-form-item) {
  margin-bottom: 20px;
}
.auth-page__card :deep(.ant-form-item-explain) {
  line-height: 1.5;
  min-height: 1.5em;
}

.auth-page__submit {
  height: 44px;
  font-size: 15px;
  border-radius: 10px;
}

.auth-page__footer-link {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
  color: #64748b;
}
.auth-page__footer-link a {
  color: #a060e6;
  cursor: pointer;
}
.auth-page__footer-link a:hover {
  text-decoration: underline;
}

.auth-page__form-item--no-label :deep(.ant-form-item-label) {
  display: none;
}
.auth-page__tenant-select :deep(.ant-select-selector) {
  border-radius: 10px;
  border-color: #e2e8f0;
  min-height: 40px;
  line-height: 1.5715;
  padding: 4px 12px;
  box-sizing: border-box;
}
.auth-page__tenant-select :deep(.ant-select-selection-placeholder),
.auth-page__tenant-select :deep(.ant-select-selection__placeholder) {
  color: #94a3b8;
}

.captcha-col {
  display: flex;
  align-items: center;
}
.captcha-img {
  width: 100px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  display: block;
}
.captcha-placeholder {
  width: 100px;
  height: 40px;
  border-radius: 4px;
  background: #f1f5f9;
  border: 1px dashed #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #94a3b8;
  cursor: pointer;
}
.captcha-placeholder:hover {
  background: #e2e8f0;
  border-color: #94a3b8;
  color: #64748b;
}
.captcha-placeholder-text {
  pointer-events: none;
}

@media (max-width: 900px) {
  .auth-page__bg {
    justify-content: center;
    padding: 32px 24px;
  }
  .auth-page__form-wrap {
    position: relative;
    right: auto;
    top: auto;
    transform: none;
    max-width: 400px;
    margin: 0 auto;
    padding: 24px 16px;
  }
  .auth-page__author {
    display: none;
  }
}
</style>
