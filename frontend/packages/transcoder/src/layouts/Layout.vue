<template>
  <a-layout class="layout">
    <a-layout-header class="header">
      <div class="logo">转码服务</div>
      <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="horizontal" :style="{ lineHeight: '64px' }">
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
.layout { min-height: 100vh; }
.header { display: flex; align-items: center; }
.logo { color: #fff; font-weight: bold; margin-right: 24px; }
.content { padding: 24px 48px; }
.inner { background: #fff; padding: 24px; min-height: 400px; border-radius: 8px; }
</style>
