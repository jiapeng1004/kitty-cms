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
}

.catalog-page-title {
  margin-bottom: 12px;
  color: #111827;
  font-size: 22px;
  font-weight: 600;
  line-height: 1.2;
}

.catalog-tree-shell {
  width: 340px;
  flex-shrink: 0;
}

.catalog-tree-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  min-height: 28px;
}

.catalog-refresh-btn {
  border-color: #d0d7de;
  color: #155eef;
  height: 28px;
  padding: 0 12px;
}

.catalog-current-label {
  color: #4b5563;
  font-size: 13px;
  line-height: 28px;
}
</style>
