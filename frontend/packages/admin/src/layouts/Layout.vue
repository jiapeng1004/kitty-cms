<template>
  <a-layout class="layout">
    <a-layout-header class="header">
      <div class="logo">Kitty CMS</div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="horizontal"
        :style="{ lineHeight: '64px' }"
      >
        <a-menu-item key="dashboard">
          <router-link to="/">Dashboard</router-link>
        </a-menu-item>
        <a-menu-item key="config">
          <router-link to="/config">配置管理</router-link>
        </a-menu-item>
        <a-menu-item key="config-class">
          <router-link to="/config-class">配置分类</router-link>
        </a-menu-item>
      </a-menu>
    </a-layout-header>
    <a-layout-content class="content">
      <div class="site-layout-content">
        <router-view />
      </div>
    </a-layout-content>
    <a-layout-footer class="footer">
      Kitty CMS ©2025 Created by Jia Peng
    </a-layout-footer>
  </a-layout>
</template>

<script setup>
import { ref } from 'vue'

const selectedKeys = ref(['dashboard'])
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  position: relative;
  display: flex;
  align-items: center;
}

.logo {
  color: #ffffff;
  font-weight: bold;
  font-size: 18px;
  margin-right: 20px;
}

.content {
  padding: 0 50px;
  margin-top: 20px;
}

.site-layout-content {
  background: #fff;
  padding: 24px;
  min-height: 280px;
  border-radius: 4px;
}

.footer {
  text-align: center;
}
</style>