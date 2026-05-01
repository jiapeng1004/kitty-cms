<template>
  <div class="mam-layout">
    <!--
      全站不展示原「Kitty CMS + 横向管理菜单」顶栏；管理入口在素材工作台的设置齿轮、各子页自带返回/面包屑。
    -->
    <a-layout-content :class="['mam-content', { 'mam-content--flush': isFlush }]">
      <router-view />
    </a-layout-content>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isFlush = computed(() => route.meta.flush === true)
</script>

<style scoped>
.mam-layout {
  flex: 1 1 auto;
  min-height: 0;
  height: 100%;
  /* 让 flush 子页面（素材工作台等）能拿到「剩余视口高度」，内部才能把分页栏压到底部 */
  display: flex;
  flex-direction: column;
  background: var(--mam-page-bg);
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
