<template>
  <a-sub-menu v-if="isDir" :key="item.menuKey">
    <template #title>
      <component :is="iconComp" v-if="iconComp" />
      <span>{{ item.menuName }}</span>
    </template>
    <SidebarMenuItem
      v-for="child in item.children || []"
      :key="child.menuKey"
      :item="child"
    />
  </a-sub-menu>

  <a-menu-item v-else-if="isExternalLink" :key="item.menuKey">
    <component :is="iconComp" v-if="iconComp" />
    <a :href="item.linkUrl" target="_blank" rel="noopener noreferrer">
      <span>{{ item.menuName }}</span>
    </a>
  </a-menu-item>

  <a-menu-item v-else-if="isInternalLink" :key="item.menuKey">
    <component :is="iconComp" v-if="iconComp" />
    <router-link :to="item.linkUrl || '/'">
      <span>{{ item.menuName }}</span>
    </router-link>
  </a-menu-item>

  <a-menu-item v-else :key="item.menuKey">
    <component :is="iconComp" v-if="iconComp" />
    <router-link :to="item.path || '/'">
      <span>{{ item.menuName }}</span>
    </router-link>
  </a-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { MenuTreeItem } from '@/api/menu_api'
import { BUILTIN_MENU_ICONS } from '@/assets'

defineOptions({
  name: 'SidebarMenuItem'
})

const props = defineProps<{
  item: MenuTreeItem
}>()

const iconComp = computed(() => {
  const name = props.item.icon
  if (!name) return null
  return BUILTIN_MENU_ICONS[name as keyof typeof BUILTIN_MENU_ICONS] ?? null
})

const isDir = computed(() => props.item.menuType === 'DIR')
const isInternalLink = computed(() => props.item.menuType === 'LINK' && props.item.linkType === 'INTERNAL')
const isExternalLink = computed(() => props.item.menuType === 'LINK' && props.item.linkType === 'EXTERNAL')
</script>

