<template>
  <video
    v-if="blobUrl"
    :src="blobUrl"
    controls
    playsinline
    preload="auto"
    class="preview-video"
  />
  <video
    v-else-if="failed"
    :src="url"
    controls
    playsinline
    preload="metadata"
    class="preview-video"
  />
  <div v-else class="preview-video-loading">加载中…</div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'

const props = defineProps({ url: { type: String, required: true } })

const blobUrl = ref('')
const failed = ref(false)

async function loadBlob() {
  blobUrl.value = ''
  failed.value = false
  try {
    const res = await fetch(props.url)
    if (!res.ok) throw new Error(res.status)
    const blob = await res.blob()
    blobUrl.value = URL.createObjectURL(blob)
  } catch {
    failed.value = true
  }
}

watch(() => props.url, loadBlob, { immediate: true })

onBeforeUnmount(() => {
  if (blobUrl.value) URL.revokeObjectURL(blobUrl.value)
})
</script>

<style scoped>
.preview-video { width: 100%; height: 100%; display: block; object-fit: contain; }
.preview-video-loading { min-height: 120px; display: flex; align-items: center; justify-content: center; color: #999; }
</style>
