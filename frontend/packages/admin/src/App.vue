<template>
  <div class="app-root">
    <router-view />
  </div>
</template>

<script setup lang="ts">
</script>

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
