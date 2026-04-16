<template>
  <div class="app-root">
    <router-view />
  </div>
</template>

<script setup lang="ts">
</script>

<!-- 无 #app 高度时 flex:1 无法分配「剩余高度」，素材库分页会一直跟在短列表后面 -->
<style>
html,
body {
  height: 100%;
  margin: 0;
}

#app {
  height: 100%;
}
</style>

<style scoped>
.app-root {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.app-root > :deep(*) {
  flex: 1 1 auto;
  min-height: 0;
}
</style>
