<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { TreeProps } from 'ant-design-vue'
import { queryCatalogTree, type MaterialCatalogNode } from '@/api/mam_catalog_api'

const loading = ref(false)
const treeData = ref<TreeProps['treeData']>([])

function buildTree(nodes: MaterialCatalogNode[]): TreeProps['treeData'] {
  return nodes.map((n) => ({
    key: n.id,
    title: `${n.name}${n.virtualRoot ? '（虚拟根）' : ''}`,
    children: n.children?.length ? buildTree(n.children) : undefined
  }))
}

async function load() {
  loading.value = true
  try {
    const nodes = await queryCatalogTree()
    treeData.value = buildTree(nodes)
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } }; message?: string }
    message.error(err?.response?.data?.message || err?.message || '加载栏目树失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div style="padding: 20px;">
    <a-typography-title :level="4">栏目树（全量）</a-typography-title>
    <a-space style="margin-bottom: 12px;">
      <a-button type="primary" :loading="loading" @click="load">刷新</a-button>
    </a-space>
    <a-spin :spinning="loading">
      <a-tree v-if="treeData?.length" :tree-data="treeData" default-expand-all block-node />
      <a-empty v-else description="无数据或加载中" />
    </a-spin>
  </div>
</template>
