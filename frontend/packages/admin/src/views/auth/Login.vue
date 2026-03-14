<template>
  <div class="login-page">
    <!-- 全屏背景：渐变 + 柔光 + 装饰光斑 -->
    <div class="login-page__bg">
      <div class="login-page__bg-blob login-page__bg-blob--1"/>
      <div class="login-page__bg-blob login-page__bg-blob--2"/>
      <div class="login-page__bg-blob login-page__bg-blob--3"/>
      <div class="login-page__author-mask"/>
      <div class="login-page__author-content">
        <div class="login-page__brand">
          <img :src="IMG_LOGO" alt="Kitty CMS" class="login-page__logo"/>
          <h1 class="login-page__title">Kitty CMS</h1>
          <p class="login-page__slogan">内容管理 · 一站式智能管理</p>
        </div>
      </div>
    </div>

    <!-- 浮在背景上的登录卡片 -->
    <div class="login-page__form-wrap">
      <div class="login-page__card">
        <h2 class="login-page__card-title">用户登录</h2>
        <a-form
            :model="formState"
            name="login"
            autocomplete="off"
            @finish="handleLogin"
        >
          <a-form-item v-if="tenantOptions.length > 0" name="tenantId" class="login-page__form-item--no-label">
            <a-select
                v-model:value="selectedTenantId"
                placeholder="请选择租户"
                size="large"
                allow-clear
                :loading="tenantLoading"
                :options="tenantOptions"
                :field-names="tenantSelectFieldNames"
                class="login-page__tenant-select"
                @change="onTenantChange"
            />
          </a-form-item>
          <a-form-item name="username" :rules="rules.username">
            <a-input
                v-model:value="formState.username"
                placeholder="用户名（昵称）"
                size="large"
            >
              <template #prefix>
                <UserOutlined class="login-page__input-icon"/>
              </template>
            </a-input>
          </a-form-item>

          <a-form-item name="password" :rules="rules.password">
            <a-input-password
                v-model:value="formState.password"
                placeholder="密码"
                size="large"
            >
              <template #prefix>
                <LockOutlined class="login-page__input-icon"/>
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item name="captcha" :rules="rules.captcha">
            <a-row :gutter="8" align="middle">
              <a-col :span="14">
                <a-input
                    v-model:value="formState.captcha"
                    placeholder="验证码"
                    size="large"
                >
                  <template #prefix>
                    <SafetyCertificateOutlined class="login-page__input-icon"/>
                  </template>
                </a-input>
              </a-col>
              <a-col :span="10" class="captcha-col">
                <template v-if="captchaLoadFailed">
                  <div
                      class="captcha-placeholder"
                      title="点击刷新验证码"
                      @click="handleCaptchaRefresh"
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
                    @error="onCaptchaImgError"
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
                class="login-page__submit"
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 更多登录方式：可折叠 -->
        <div class="login-page__more">
          <a-button
              type="text"
              block
              class="login-page__more-trigger"
              @click="moreOpen = !moreOpen"
          >
            <DownOutlined :class="{ 'login-page__more-arrow': true, 'login-page__more-arrow--open': moreOpen }"/>
            {{ moreOpen ? '收起' : '更多登录方式' }}
          </a-button>
          <Transition name="slide">
            <div v-show="moreOpen" class="login-page__more-content">
              <div class="login-page__oauth-grid">
                <template v-for="item in oauthOptions" :key="item.key">
                  <a
                      v-if="item.oauth2Source"
                      href="javascript:void(0)"
                      class="login-page__oauth-card"
                      :class="`login-page__oauth-card--${item.key}`"
                      @click.prevent="onOAuthClick(item.key)"
                  >
                    <span class="login-page__oauth-icon">
                      <img v-if="item.logo" :src="item.logo" :alt="item.label" class="login-page__oauth-logo"/>
                      <component v-else :is="item.icon"/>
                    </span>
                    <span class="login-page__oauth-label">{{ item.label }}</span>
                  </a>
                  <div
                      v-else
                      class="login-page__oauth-card"
                      :class="`login-page__oauth-card--${item.key}`"
                  >
                    <span class="login-page__oauth-icon">
                      <img v-if="item.logo" :src="item.logo" :alt="item.label" class="login-page__oauth-logo"/>
                      <component v-else :is="item.icon"/>
                    </span>
                    <span class="login-page__oauth-label">{{ item.label }}</span>
                  </div>
                </template>
              </div>
            </div>
          </Transition>
        </div>

        <div class="login-page__register">
          还没有账号？ <a @click="$router.push('/register')">立即注册</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {reactive, ref, onMounted} from 'vue'
import {
  UserOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  DownOutlined
} from '@ant-design/icons-vue'
import {useRoute, useRouter} from 'vue-router'
import {message} from 'ant-design-vue'
import { setToken, setTenantId, setUserName, getTenantId, getResponseMessage } from '../../utils/api'
import {login, fetchCaptchaImageUrl, getOAuth2RenderUrl, checkOpenAuthReady} from '../../api/auth_api'
import {
  IMG_LOGO,
  IMG_FEISHU_LOGO,
  IMG_DINGTALK_LOGO,
  IMG_GITHUB_LOGO,
  IMG_GOOGLE_LOGO,
  IMG_MICROSOFT_LOGO,
  IMG_WECHAT_LOGO
} from '@/assets'
import {getTenantList} from "@/api/tenant_api"

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const captchaSrc = ref('')
const captchaBlobUrlRef = ref<string | null>(null)
const captchaLoadFailed = ref(false)
const moreOpen = ref(false)
const tenantLoading = ref(false)
const tenantSelectFieldNames = {label: 'name', value: 'id'}

interface TenantOption {
  id: string;
  name: string
}

const tenantOptions = ref<TenantOption[]>([])
const selectedTenantId = ref<string | undefined>(getTenantId() || undefined)

const formState = reactive({
  username: '',
  password: '',
  captcha: ''
})

function onTenantChange(tenantId: string | undefined) {
  setTenantId(tenantId || '')
}

const rules = {
  username: [{required: true, message: '请输入用户名!'}],
  password: [{required: true, message: '请输入密码!'}],
  captcha: [{required: true, message: '请输入验证码!'}]
}

// 六方登录：飞书、GitHub、Google、微软、微信、钉钉
const oauthOptions = [
  {key: 'feishu', label: '飞书', logo: IMG_FEISHU_LOGO, oauth2Source: true},
  {key: 'github', label: 'GitHub', logo: IMG_GITHUB_LOGO, oauth2Source: true},
  {key: 'google', label: 'Google', logo: IMG_GOOGLE_LOGO, oauth2Source: true},
  {key: 'microsoft', label: '微软', logo: IMG_MICROSOFT_LOGO, oauth2Source: true},
  {key: 'wechat', label: '微信', logo: IMG_WECHAT_LOGO, oauth2Source: true},
  {key: 'dingtalk', label: '钉钉', logo: IMG_DINGTALK_LOGO, oauth2Source: true}
]

/** 点击第三方登录：先预检该 source，通过后再跳转 render */
async function onOAuthClick(source: string) {
  const ready = await checkOpenAuthReady(source)
  if (ready) {
    window.location.href = getOAuth2RenderUrl(source)
  } else {
    message.warning('该登录方式未配置或不可用')
  }
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

function handleCaptchaRefresh() {
  refreshCaptcha()
}

function onCaptchaImgError() {
  captchaLoadFailed.value = true
}

onMounted(async () => {
  const token = route.query.token
  if (route.query.oauth2 && token) {
    setToken(String(token))
    message.success('第三方登录成功')
    router.replace('/')
    return
  }
  try {
    tenantLoading.value = true
    const list = await getTenantList()
    tenantOptions.value = list || []
    if (!selectedTenantId.value && list?.length > 0) {
      selectedTenantId.value = list[0].id
      setTenantId(list[0].id)
    } else if (getTenantId()) {
      selectedTenantId.value = getTenantId() || undefined
    }
  } catch (_) {
    tenantOptions.value = []
  } finally {
    tenantLoading.value = false
  }
  // 租户确定后再拉验证码，保证请求带 X-Tenant-Id
  await refreshCaptcha()
})

async function handleLogin() {
  try {
    loading.value = true
    const res = await login({
      username: formState.username,
      password: formState.password,
      captcha: formState.captcha
    })
    if (res && res.token) {
      setToken(res.token)
      setUserName(formState.username)
      message.success('登录成功')
      router.push('/')
    } else {
      message.error('登录失败：未返回 token')
      refreshCaptcha()
    }
  } catch (error) {
    message.error(getResponseMessage(error))
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

/* 全屏背景：主渐变 + 光斑装饰 + 柔光蒙层 */
.login-page__bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, #7c3aed 0%, #a78bfa 28%, #c4b5fd 55%, #e9d5ff 82%, #f5d0fe 100%);
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 48px 0 48px 12%;
}

/* 背景装饰：柔和模糊光斑，增加层次 */
.login-page__bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  pointer-events: none;
}
.login-page__bg-blob--1 {
  width:  min(60vw, 480px);
  height: min(60vw, 480px);
  background: rgba(255, 255, 255, 0.35);
  top: -10%;
  left: -5%;
}
.login-page__bg-blob--2 {
  width:  min(45vw, 360px);
  height: min(45vw, 360px);
  background: rgba(196, 181, 253, 0.6);
  bottom: 10%;
  right: 15%;
}
.login-page__bg-blob--3 {
  width:  min(35vw, 280px);
  height: min(35vw, 280px);
  background: rgba(245, 208, 254, 0.5);
  top: 50%;
  left: 35%;
  transform: translate(-50%, -50%);
}

.login-page__author-mask {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 80% 60% at 20% 50%, rgba(255, 255, 255, 0.2) 0%, transparent 50%),
              radial-gradient(ellipse 60% 80% at 85% 20%, rgba(255, 255, 255, 0.08) 0%, transparent 45%);
  pointer-events: none;
}

.login-page__author-content {
  position: relative;
  z-index: 1;
  text-align: left;
  color: #fff;
}

.login-page__brand {
  max-width: 320px;
}

.login-page__logo {
  width: 72px;
  height: 72px;
  margin: 0 0 20px;
  display: block;
  border-radius: 16px;
  object-fit: contain;
}

.login-page__title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px;
  letter-spacing: 0.02em;
}

.login-page__slogan {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

/* 浮在背景上的登录卡片：不占独立色块，仅卡片有白底+阴影 */
.login-page__form-wrap {
  position: absolute;
  right: 10%;
  top: 50%;
  transform: translateY(-50%);
  width: 100%;
  max-width: 400px;
  padding: 0;
  z-index: 2;
}

.login-page__card {
  width: 100%;
  max-width: 360px;
  margin-left: auto;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15), 0 0 1px rgba(0, 0, 0, 0.08);
}

.login-page__card-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 28px;
  color: #1f2937;
}

/* 租户选择：无外置 label，占位符在框内灰色显示 */
.login-page__form-item--no-label :deep(.ant-form-item-label) {
  display: none;
}

.login-page__form-item--no-label :deep(.ant-form-item-control) {
  flex: 1;
}

.login-page__tenant-select :deep(.ant-select-selection-placeholder),
.login-page__tenant-select :deep(.ant-select-selection__placeholder) {
  color: #94a3b8;
}

.login-page__input-icon {
  color: #94a3b8;
}

/* 登录卡片内输入框：参考 NUCT，细浅灰边框、圆角、聚焦/悬停态 */
.login-page__card :deep(.ant-input),
.login-page__card :deep(.ant-input-affix-wrapper),
.login-page__card :deep(.ant-select .ant-select-selector) {
  border-radius: 10px;
  border-color: #e2e8f0;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.login-page__card :deep(.ant-input:hover),
.login-page__card :deep(.ant-input-affix-wrapper:hover:not(.ant-input-affix-wrapper-disabled)),
.login-page__card :deep(.ant-select:not(.ant-select-disabled) .ant-select-selector:hover) {
  border-color: #cbd5e1;
}
.login-page__card :deep(.ant-input:focus),
.login-page__card :deep(.ant-input-affix-wrapper-focused),
.login-page__card :deep(.ant-select-focused .ant-select-selector) {
  border-color: #a060e6;
  box-shadow: 0 0 0 2px rgba(160, 96, 230, 0.15);
  outline: none;
}
.login-page__card :deep(.ant-input::placeholder) {
  color: #94a3b8;
}
.login-page__card :deep(.ant-input-affix-wrapper) {
  padding-left: 12px;
}
.login-page__card :deep(.ant-input-password .ant-input) {
  border: none;
  box-shadow: none;
}
.login-page__card :deep(.ant-input-password.ant-input-affix-wrapper:hover .ant-input),
.login-page__card :deep(.ant-input-password.ant-input-affix-wrapper-focused .ant-input) {
  border: none;
  box-shadow: none;
}
.login-page__card :deep(.ant-select.ant-select-lg .ant-select-selector) {
  border-radius: 10px;
  min-height: 40px;
  padding: 4px 12px;
}
.login-page__card :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.login-page__submit {
  height: 44px;
  font-size: 15px;
  border-radius: 10px;
}

.login-page__more {
  margin-top: 16px;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.login-page__more-trigger {
  color: #64748b;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.login-page__more-arrow {
  transition: transform 0.2s;
}

.login-page__more-arrow--open {
  transform: rotate(180deg);
}

.login-page__more-content {
  margin-top: 12px;
}

.login-page__more-divider {
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.login-page__more-divider::before,
.login-page__more-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e2e8f0;
}

.login-page__more-divider span {
  flex-shrink: 0;
}

/* Notion 风格：圆角卡片 + 图标在上、文字在下 */
.login-page__oauth-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.login-page__oauth-card {
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
  align-items: center;
  justify-content: center;
  padding: 14px 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  cursor: default;
  transition: background 0.2s, border-color 0.2s, box-shadow 0.2s;
}

.login-page__oauth-card:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.login-page__oauth-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  font-size: 22px;
  color: #475569;
}

.login-page__oauth-icon :deep(svg) {
  color: inherit;
  fill: currentColor;
}

.login-page__oauth-logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
  display: block;
}

.login-page__oauth-card--github .login-page__oauth-icon {
  color: #24292f;
}

.login-page__oauth-card--google .login-page__oauth-icon {
  color: #4285f4;
}

.login-page__oauth-card--microsoft .login-page__oauth-icon {
  color: #00a4ef;
}

.login-page__oauth-card--wechat .login-page__oauth-icon {
  color: #07c160;
}

.login-page__oauth-label {
  font-size: 12px;
  font-weight: 500;
  color: #334155;
}

.login-page__register {
  text-align: center;
  margin-top: 24px;
  font-size: 13px;
  color: #64748b;
}

.login-page__register a {
  color: #a060e6;
}

/* 验证码与输入框同高 */
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
  background: #f5f5f5;
  object-fit: contain;
  object-position: center;
  flex-shrink: 0;
}

/* 验证码加载失败时：柔和占位，可点击刷新 */
.captcha-placeholder {
  width: 100px;
  height: 40px;
  border-radius: 4px;
  background: #f1f5f9;
  border: 1px dashed #cbd5e1;
  color: #94a3b8;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s, border-color 0.2s;
}

.captcha-placeholder:hover {
  background: #e2e8f0;
  border-color: #94a3b8;
  color: #64748b;
}

.captcha-placeholder-text {
  pointer-events: none;
}

/* 折叠展开过渡 */
.slide-enter-active,
.slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 900px) {
  .login-page__bg {
    justify-content: center;
    padding: 32px 24px;
  }

  .login-page__form-wrap {
    position: relative;
    right: auto;
    top: auto;
    transform: none;
    max-width: 400px;
    margin: 0 auto;
    padding: 24px 16px;
  }

  .login-page__card {
    margin-left: 0;
  }
}

@media (max-width: 768px) {
  .login-page__bg {
    padding: 24px 16px;
  }

  .login-page__logo {
    width: 56px;
    height: 56px;
    margin-bottom: 12px;
  }

  .login-page__title {
    font-size: 22px;
  }

  .login-page__slogan {
    font-size: 13px;
  }
}

@media (max-width: 400px) {
  .login-page__oauth-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
