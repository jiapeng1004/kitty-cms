<template>
  <MamLayout v-if="hideAdminChrome" />
  <a-layout v-else class="admin-layout">
    <!-- 左侧导航：云控制台风格 -->
    <a-layout-sider
        v-model:collapsed="collapsed"
        :trigger="null"
        collapsible
        :width="220"
        class="admin-sider"
    >
      <div class="admin-logo">
        <span class="admin-logo-icon">🐱</span>
        <span v-show="!collapsed" class="admin-logo-text">Kitty CMS</span>
      </div>
      <a-menu
          v-model:selectedKeys="selectedKeys"
          theme="light"
          mode="inline"
          :inline-collapsed="collapsed"
          class="admin-menu"
      >
        <SidebarMenuItem
            v-for="item in menuTree"
            :key="item.menuKey"
            :item="item"
        />
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <!-- 顶栏：面包屑 + 操作区 -->
      <a-layout-header class="admin-header">
        <div class="admin-header-left">
          <MenuUnfoldOutlined
              v-if="collapsed"
              class="admin-trigger"
              @click="collapsed = false"
          />
          <MenuFoldOutlined
              v-else
              class="admin-trigger"
              @click="collapsed = true"
          />
          <a-breadcrumb class="admin-breadcrumb">
            <a-breadcrumb-item>{{ breadcrumbTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="admin-header-right">
          <a-dropdown placement="bottomRight">
            <span class="admin-user-trigger">
              <UserOutlined class="admin-user-icon"/>
              <span class="admin-user-name">{{ displayName }}</span>
              <DownOutlined class="admin-user-arrow"/>
            </span>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined/>
                  <span>退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 主内容区 -->
      <a-layout-content class="admin-content">
        <div class="admin-content-inner">
          <router-view/>
        </div>
        <a-layout-footer class="admin-footer">
          Kitty CMS ©2025 · Created by Jia Peng
        </a-layout-footer>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {
  UserOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  DownOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'
import SidebarMenuItem from '@/components/layout/SidebarMenuItem.vue'
import MamLayout from '@/mam/layouts/Layout.vue'
import {setToken, setUserName, USER_NAME_KEY} from '@/utils/api'
import {getCurrentMenuTree, type MenuTreeItem} from '@/api/menu_api'
import {logout} from '@/api/auth_api'

const route = useRoute()
const router = useRouter()
const hideAdminChrome = computed(() => route.matched.some((r) => r.meta?.hideAdminChrome === true))
const collapsed = ref(false)
const menuTree = ref<MenuTreeItem[]>([])
const displayName = ref(localStorage.getItem(USER_NAME_KEY) || '用户')

async function handleLogout() {
  try {
    await logout()
  } catch (_) {
    // ignore
  } finally {
    setToken(null)
    setUserName(null)
    localStorage.removeItem('kitty_admin_menu_tree')
    router.replace('/login').catch(() => {
      window.location.href = '/login'
    })
  }
}

function flattenMenuTree(list: MenuTreeItem[]): MenuTreeItem[] {
  const result: MenuTreeItem[] = []
  for (const item of list) {
    result.push(item)
    if (item.children?.length) {
      result.push(...flattenMenuTree(item.children))
    }
  }
  return result
}

const selectedKeys = computed(() => {
  const p = route.path || '/'
  const found = flattenMenuTree(menuTree.value).find((m) => m.path === p || m.linkUrl === p)
  return found ? [found.menuKey] : []
})

const breadcrumbTitle = computed(() => {
  const p = route.path || '/'
  const found = flattenMenuTree(menuTree.value).find((m) => m.path === p || m.linkUrl === p)
  return found ? found.menuName : '工作台'
})

onMounted(async () => {
  try {
    const tree = await getCurrentMenuTree()
    menuTree.value = Array.isArray(tree) ? tree : []
    // 将当前菜单树缓存到本地，供路由守卫在登录后动态挂载路由使用
    try {
      localStorage.setItem('kitty_admin_menu_tree', JSON.stringify(menuTree.value))
    } catch {
      // ignore
    }
  } catch (_) {
    menuTree.value = [
      {menuKey: 'dashboard', path: '/', menuName: '工作台', menuType: 'MENU'},
      {
        menuKey: 'system-root',
        menuName: '系统管理',
        menuType: 'DIR',
        children: [
          {menuKey: 'config', path: '/config', menuName: '配置管理', menuType: 'MENU'},
          {menuKey: 'config-class', path: '/config-class', menuName: '配置分类', menuType: 'MENU'}
        ]
      },
      {menuKey: 'tenant', path: '/tenant', menuName: '租户管理', menuType: 'MENU'},
      {menuKey: 'user', path: '/user', menuName: '用户管理', menuType: 'MENU'}
    ]
  }
})
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  font-family: var(--admin-font);
}

.admin-sider {
  overflow: auto;
  height: 100vh;
  left: 0;
  background: var(--admin-sidebar-bg) !important;
  border-right: 1px solid var(--admin-sidebar-border);
}

.admin-sider :deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
}

.admin-sider :deep(.ant-menu-light) {
  background: transparent;
}

.admin-sider :deep(.ant-menu-inline .ant-menu-item-selected) {
  background: var(--admin-sidebar-selected-bg) !important;
  color: var(--admin-sidebar-text-active);
  border-radius: 8px;
  margin: 4px 12px;
  width: calc(100% - 24px);
}

.admin-sider :deep(.ant-menu-inline .ant-menu-item) {
  margin: 4px 12px;
  width: calc(100% - 24px);
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
  color: var(--admin-sidebar-text);
}

.admin-sider :deep(.ant-menu-item a) {
  color: var(--admin-sidebar-text);
}

.admin-sider :deep(.ant-menu-item-selected a),
.admin-sider :deep(.ant-menu-item-selected .anticon) {
  color: var(--admin-sidebar-text-active) !important;
}

.admin-sider :deep(.ant-menu-item:hover a) {
  color: var(--admin-primary);
}

.admin-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid var(--admin-sidebar-border);
}

.admin-logo-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.admin-logo-text {
  color: rgba(0, 0, 0, 0.88);
  font-weight: 600;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
}

.admin-menu {
  flex: 1;
  padding: 12px 0;
  border-right: none !important;
}

.admin-header {
  background: var(--admin-header-bg) !important;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.admin-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-header-right {
  display: flex;
  align-items: center;
}

.admin-user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: rgba(0, 0, 0, 0.88);
  transition: background 0.2s, color 0.2s;
}

.admin-user-trigger:hover {
  background: var(--admin-primary-light, rgba(22, 119, 255, 0.08));
  color: var(--admin-primary);
}

.admin-user-icon {
  font-size: 16px;
}

.admin-user-name {
  font-size: 14px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-user-arrow {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.admin-user-trigger:hover .admin-user-arrow {
  color: var(--admin-primary);
}

.admin-trigger {
  font-size: 18px;
  cursor: pointer;
  color: #666;
  padding: 4px;
  border-radius: 4px;
}

.admin-trigger:hover {
  color: var(--admin-primary);
  background: var(--admin-primary-light);
}

.admin-breadcrumb {
  font-size: 14px;
}

.admin-breadcrumb :deep(.ant-breadcrumb-link) {
  color: rgba(0, 0, 0, 0.88);
}

.admin-content {
  margin: 0;
  min-height: calc(100vh - 64px);
  background: var(--admin-content-bg);
  padding: 24px;
}

.admin-content-inner {
  min-height: 360px;
  background: transparent;
  padding: 0;
}

.admin-footer {
  text-align: center;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
  padding: 16px 0 0;
  background: transparent;
  border: none;
}
</style>
