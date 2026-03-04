<template>
  <div class="kitty-logo" :class="{ compact }">
    <svg class="logo-icon" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="64" height="64" rx="14" fill="#006EFF"/>
      <circle cx="32" cy="36" r="14" fill="#fff" opacity="0.95"/>
      <ellipse cx="26" cy="34" rx="3" ry="4" fill="#006EFF"/>
      <ellipse cx="38" cy="34" rx="3" ry="4" fill="#006EFF"/>
      <path d="M32 40 Q28 44 32 46 Q36 44 32 40" fill="none" stroke="#006EFF" stroke-width="1.5" stroke-linecap="round"/>
      <path d="M18 26 Q14 22 20 24 M46 26 Q50 22 44 24" stroke="#006EFF" stroke-width="2" stroke-linecap="round" fill="none"/>
    </svg>
    <span v-if="!compact" class="logo-text">kitty-transcoder</span>
  </div>
</template>

<script setup>
defineProps({
  compact: { type: Boolean, default: false }
})
</script>

<style scoped>
.kitty-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}
.kitty-logo.compact .logo-icon {
  width: 28px;
  height: 28px;
}
.logo-text {
  font-weight: 600;
  font-size: 17px;
  letter-spacing: -0.02em;
  color: #006EFF;
}
</style>
