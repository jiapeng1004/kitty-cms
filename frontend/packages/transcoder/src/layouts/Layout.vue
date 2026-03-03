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
        <a-menu-item key="logout" @click="logout">退出</a-menu-item>
      </a-menu>
    </a-layout-header>
    <a-layout-content class="content">
      <div class="inner"><router-view /></div>
    </a-layout-content>
  </a-layout>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import KittyLogo from '../components/KittyLogo.vue'

const router = useRouter()
const route = useRoute()
const selectedKeys = ref([route.path.split('/')[1] || 'tasks'])

watch(() => route.path, (p) => { selectedKeys.value = [p.split('/')[1] || 'tasks'] })

function logout() {
  localStorage.removeItem('transcoder_token')
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
.content { padding: 24px 48px; }
.inner {
  background: #fff;
  padding: 24px;
  min-height: 400px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
</style>
