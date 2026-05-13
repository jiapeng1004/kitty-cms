<template>
  <a-config-provider :theme="{ token: { colorPrimary: '#006EFF', borderRadius: 6 } }">
    <router-view />
  </a-config-provider>
</template>

<script setup>
</script>

<style>
:root {
  --kt-primary: #006EFF;
  --kt-bg: #F5F7FA;
  --kt-text: #333;
  --kt-muted: #666;
}
#app { min-height: 100vh; }
body {
  background: var(--kt-bg);
  color: var(--kt-text);
}
</style>
