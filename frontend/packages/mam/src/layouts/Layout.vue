<template>
  <div class="mam-layout">
    <a-layout-header class="mam-header" v-if="!isEmbedded && !isImmersive">
      <div class="mam-header-content">
        <div class="mam-logo">
          <span>Kitty CMS</span>
        </div>
        <a-menu
          mode="horizontal"
          :selected-keys="[currentRoute]"
          class="mam-nav-menu"
        >
          <a-menu-item key="material">
            <router-link to="/material">素材库</router-link>
          </a-menu-item>
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
          <a-menu-item key="storage-manage">
            <router-link to="/storage-manage">存储管理</router-link>
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
    <a-layout-content :class="['mam-content', { 'mam-content--flush': isFlush }]">
      <router-view />
    </a-layout-content>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const isEmbedded = computed(() => route.path.startsWith('/embed/'))
const currentRoute = computed(() => {
  const p = route.path.replace('/embed/', '')
  return p.replace(/^\//, '').split('/')[0] || ''
})
const isFlush = computed(() => route.meta.flush === true)
/** 素材库整页工作台：隐藏顶栏，贴近参考站「仅侧栏 + 主区」 */
const isImmersive = computed(() => route.meta.immersive === true)
</script>

<style scoped>
.mam-layout {
  flex: 1 1 auto;
  min-height: 0;
  height: 100%;
  /* 让 flush 子页面（素材工作台等）能拿到「剩余视口高度」，内部才能把分页栏压到底部 */
  display: flex;
  flex-direction: column;
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

.mam-content--flush {
  padding: 0;
  max-width: none;
  margin: 0;
  flex: 1 1 0;
  min-height: 0 !important;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
