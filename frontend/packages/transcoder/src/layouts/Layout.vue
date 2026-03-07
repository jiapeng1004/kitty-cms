<template>
  <a-layout class="layout">
    <a-layout-header class="header">
      <router-link to="/tasks" class="logo-wrap">
        <KittyLogo />
      </router-link>
      <a-menu v-model:selectedKeys="selectedKeys" class="nav-menu" mode="horizontal" :style="{ lineHeight: '64px' }">
        <a-menu-item key="tasks"><router-link to="/tasks">任务</router-link></a-menu-item>
        <a-menu-item key="strategies"><router-link to="/strategies">策略</router-link></a-menu-item>
        <a-menu-item key="access-keys"><router-link to="/access-keys">Access Key</router-link></a-menu-item>
      </a-menu>
      <div class="header-right">
        <a-dropdown v-if="currentAk" placement="bottomRight" trigger="click">
          <div class="ak-trigger">
            <a-avatar :size="32" class="ak-avatar">{{ (currentAk.accessKeyId || '?').slice(0, 2) }}</a-avatar>
            <span class="ak-label">{{ currentAk.accessKeyId }}</span>
            <span class="ak-arrow">▼</span>
          </div>
          <template #overlay>
            <div class="ak-dropdown">
              <div class="ak-dropdown-title">当前 Access Key</div>
              <div class="ak-info">
                <div class="ak-row"><span class="ak-key">Access Key ID</span><span class="ak-val">{{ currentAk.accessKeyId }}</span></div>
                <div v-if="currentAk.name" class="ak-row"><span class="ak-key">名称</span><span class="ak-val">{{ currentAk.name }}</span></div>
                <div class="ak-row"><span class="ak-key">状态</span><a-tag :color="currentAk.status === 'ACTIVE' ? 'success' : 'default'">{{ currentAk.status }}</a-tag></div>
                <div v-if="currentAk.description" class="ak-row"><span class="ak-key">描述</span><span class="ak-val">{{ currentAk.description }}</span></div>
              </div>
              <a-divider style="margin: 8px 0" />
              <a-button type="text" danger block @click="logout">注销本 AK</a-button>
            </div>
          </template>
        </a-dropdown>
        <a v-else class="logout-fallback" @click="logout">退出</a>
      </div>
    </a-layout-header>
    <a-layout-content class="content">
      <div class="inner"><router-view /></div>
    </a-layout-content>
  </a-layout>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import KittyLogo from '../components/KittyLogo.vue'
import { getCurrentAccessKey } from '../api/auth_api'

const router = useRouter()
const route = useRoute()
const selectedKeys = ref([route.path.split('/')[1] || 'tasks'])
const currentAk = ref(null)

watch(() => route.path, (p) => { selectedKeys.value = [p.split('/')[1] || 'tasks'] })

onMounted(() => {
  getCurrentAccessKey().then((data) => { currentAk.value = data }).catch(() => { currentAk.value = null })
})

function logout() {
  localStorage.removeItem('transcoder_token')
  currentAk.value = null
  router.push('/login')
}
</script>

<style scoped>
.layout { min-height: 100vh; background: #F5F7FA; }
.header {
  display: flex;
  align-items: center;
  background: #fff !important;
  padding: 0 32px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.logo-wrap {
  margin-right: 40px;
  text-decoration: none;
}
.nav-menu {
  flex: 1;
  background: transparent !important;
  border: none !important;
}
.nav-menu :deep(.ant-menu-item) {
  color: #333 !important;
}
.nav-menu :deep(.ant-menu-item-selected) {
  color: #006EFF !important;
}
.nav-menu :deep(.ant-menu-item:hover) {
  color: #006EFF !important;
}
.nav-menu :deep(a) {
  color: inherit;
}
.header-right {
  margin-left: auto;
}
.ak-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  height: 64px;
  cursor: pointer;
}
.ak-trigger:hover {
  background: rgba(0, 0, 0, 0.04);
}
.ak-avatar {
  background: #006EFF;
  font-size: 12px;
}
.ak-label {
  font-size: 13px;
  color: #333;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ak-arrow {
  font-size: 10px;
  color: #999;
}
.ak-dropdown {
  min-width: 280px;
  padding: 12px;
}
.ak-dropdown-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
.ak-info {
  font-size: 13px;
}
.ak-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.ak-key {
  color: #666;
  min-width: 90px;
}
.ak-val {
  color: #333;
  word-break: break-all;
}
.logout-fallback {
  padding: 0 16px;
  color: #666;
  cursor: pointer;
}
.logout-fallback:hover {
  color: #006EFF;
}
.content { padding: 24px 48px; }
.inner {
  background: #fff;
  padding: 24px;
  min-height: 400px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
</style>
