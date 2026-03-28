<template>
  <div class="mam-layout">
    <a-layout-header class="mam-header" v-if="!isEmbedded">
      <div class="mam-header-content">
        <div class="mam-logo">
          <span>Kitty CMS</span>
        </div>
        <a-menu
          mode="horizontal"
          :selected-keys="[currentRoute]"
          class="mam-nav-menu"
        >
          <a-menu-item key="catalog-tree">
            <router-link to="/catalog-tree">栏目树</router-link>
          </a-menu-item>
          <a-menu-item key="permissions">
            <router-link to="/permissions">栏目权限</router-link>
          </a-menu-item>
          <a-menu-item key="resources">
            <router-link to="/resources">资源管理</router-link>
          </a-menu-item>
          <a-menu-item key="metadata">
            <router-link to="/metadata">编目管理</router-link>
          </a-menu-item>
          <a-menu-item key="storage-route">
            <router-link to="/storage-route">存储路由</router-link>
          </a-menu-item>
          <a-menu-item key="tasks">
            <router-link to="/tasks">任务中心</router-link>
          </a-menu-item>
          <a-menu-item key="transcode-policy">
            <router-link to="/transcode-policy">转码策略</router-link>
          </a-menu-item>
          <a-menu-item key="messages">
            <router-link to="/messages">站内信</router-link>
          </a-menu-item>
          <a-menu-item key="review">
            <router-link to="/review">审核</router-link>
          </a-menu-item>
        </a-menu>
      </div>
    </a-layout-header>
    <a-layout-content class="mam-content">
      <router-view />
    </a-layout-content>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const isEmbedded = computed(() => route.path.startsWith('/embed/'))
const currentRoute = computed(() => route.path.replace('/embed/', '').replace('/', ''))
</script>

<style scoped>
.mam-layout {
  min-height: 100vh;
  background: #f0f2f5;
}

.mam-header {
  background: #001529;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.mam-header-content {
  max-width: 1440px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 100%;
}

.mam-logo {
  color: white;
  font-size: 18px;
  font-weight: 600;
  padding: 0 24px;
  white-space: nowrap;
}

.mam-nav-menu {
  flex: 1;
  background: transparent;
  border: none;
}

.mam-nav-menu :deep(.ant-menu-item) {
  color: rgba(255, 255, 255, 0.85);
}

.mam-nav-menu :deep(.ant-menu-item:hover) {
  color: white;
}

.mam-nav-menu :deep(.ant-menu-item-selected) {
  color: white;
}

.mam-nav-menu :deep(.ant-menu-item a) {
  color: inherit;
}

.mam-nav-menu :deep(.ant-menu-item a:hover) {
  color: inherit;
}

.mam-content {
  padding: 24px;
  max-width: 1440px;
  margin: 0 auto;
}
</style>
