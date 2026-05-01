<script setup lang="ts">
import { ref } from 'vue'
import type { MaterialCatalogNode } from '@/api/mam_catalog_api'
import MaterialCatalogTreePanel from '@/components/MaterialCatalogTreePanel.vue'

const selectedCatalogId = ref<string>()
const panelRef = ref<InstanceType<typeof MaterialCatalogTreePanel>>()
const currentCatalog = ref<MaterialCatalogNode | undefined>()

function onCatalogSelect(node: MaterialCatalogNode | undefined) {
  currentCatalog.value = node
}

const refreshing = ref(false)
async function refresh() {
  refreshing.value = true
  try {
    await panelRef.value?.load()
  } finally {
    refreshing.value = false
  }
}
</script>

<template>
  <div class="catalog-tree-page">
    <div class="catalog-page-title">栏目树（全量）</div>

    <div class="catalog-tree-shell">
      <div class="catalog-tree-toolbar">
        <a-button class="catalog-refresh-btn" :loading="refreshing" @click="refresh">刷新</a-button>
        <span class="catalog-current-label">当前栏目：{{ currentCatalog?.name || '未选择' }}</span>
      </div>

      <MaterialCatalogTreePanel
        ref="panelRef"
        v-model:selected-catalog-id="selectedCatalogId"
        @catalog-select="onCatalogSelect"
      />
    </div>
  </div>
</template>

<style scoped lang="less">
.catalog-tree-page {
  padding: 20px;
  min-height: calc(100vh - 64px);
  background: var(--mam-page-bg);
}

.catalog-page-title {
  margin-bottom: 12px;
  color: var(--mam-text);
  font-size: 22px;
  font-weight: 600;
  line-height: 1.2;
}

.catalog-tree-shell {
  width: 340px;
  flex-shrink: 0;
  background: var(--mam-surface);
  border: 1px solid var(--mam-border);
  border-radius: var(--mam-radius-lg);
  box-shadow: var(--mam-shadow-sm);
  padding: 20px;
}

.catalog-tree-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  min-height: 28px;
}

.catalog-refresh-btn {
  border-color: var(--mam-border);
  color: var(--mam-primary);
  height: 28px;
  padding: 0 12px;
}

.catalog-refresh-btn:hover {
  border-color: var(--mam-primary);
  color: var(--mam-primary-hover);
}

.catalog-current-label {
  color: var(--mam-text-secondary);
  font-size: 13px;
  line-height: 28px;
}
</style>
