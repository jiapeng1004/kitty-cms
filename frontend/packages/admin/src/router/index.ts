import {createRouter, createWebHistory, type RouteRecordRaw} from 'vue-router'
import {getToken} from '@/utils/api'
import {getCurrentMenuTree, type MenuTreeItem} from '@/api/menu_api'
import Login from '@/views/auth/Login.vue'
import Register from '@/views/auth/Register.vue'
import Layout from '@/layouts/Layout.vue'
import Dashboard from '@/views/Dashboard.vue'
import {embedMamRoutes, mamContentRoutes} from '@/router/mamRoutes'

// 管理端 + MAM 视图，用于菜单 component 动态加载
const viewModules = import.meta.glob([
    '../views/**/*.vue',
    '../mam/views/**/*.vue'
])

/**
 * 基础路由：登录/注册、统一 Layout 壳子；管理端业务路由按菜单动态挂载，MAM 路由静态注册
 */
const constantRoutes: RouteRecordRaw[] = [
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: {public: true}
    },
    {
        path: '/register',
        name: 'Register',
        component: Register,
        meta: {public: true}
    },
    {
        path: '/',
        name: 'RootLayout',
        component: Layout,
        meta: {requiresAuth: true},
        children: [
            {path: '', name: 'Dashboard', component: Dashboard},
            ...mamContentRoutes
        ]
    },
    ...embedMamRoutes
]

const router = createRouter({
    history: createWebHistory(),
    routes: constantRoutes
})

/**
 * 从菜单 component 解析组件：
 * - config/ConfigList -> views/config/ConfigList.vue
 * - mam/MaterialWorkspacePage -> mam/views/MaterialWorkspacePage.vue
 */
function resolveViewComponent(component?: string) {
    if (!component) return undefined
    if (component.startsWith('mam/')) {
        const rest = component.slice(4)
        const mamKey = rest.startsWith('views/')
            ? `../mam/${rest}.vue`
            : `../mam/views/${rest}.vue`
        return viewModules[mamKey]
    }
    const key = `../views/${component}.vue`
    return viewModules[key]
}

let dynamicRoutesInited = false

/** 登录态与 kitty-user Sa-Token 对齐：Authorization + Bearer + uuid，见 getToken() */
router.beforeEach(async (to, _from, next) => {
    const token = getToken()
    const isPublic = to.matched.some((r) => r.meta?.public)

    if (!isPublic && !token) {
        next({path: '/login', query: {redirect: to.fullPath}})
        return
    }

    if (to.path === '/login' && token) {
        next({path: '/'})
        return
    }

    if (token && !dynamicRoutesInited) {
        try {
            const menuTree = await getCurrentMenuTree()
            try {
                localStorage.setItem('kitty_admin_menu_tree', JSON.stringify(menuTree))
            } catch {
                // ignore
            }

            function walk(list: MenuTreeItem[]) {
                for (const item of list) {
                    const loader = resolveViewComponent(item.component)
                    if (item.menuType === 'MENU' && item.path && loader) {
                        const normalizedPath = item.path.replace(/^\//, '')
                        const exists = router.getRoutes().some((r) => {
                            const p = r.path.replace(/^\//, '')
                            return p === normalizedPath || r.path === item.path
                        })
                        if (!exists) {
                            router.addRoute('RootLayout', {
                                path: normalizedPath,
                                name: item.menuKey || item.path,
                                component: loader,
                                meta: item.path.startsWith('/material') ||
                                item.path.startsWith('/resources') ||
                                item.path.startsWith('/storage-manage')
                                    ? {hideAdminChrome: true, flush: true, immersive: true}
                                    : item.component?.startsWith('mam/')
                                      ? {hideAdminChrome: true}
                                      : undefined
                            })
                        }
                    }
                    if (item.children?.length) {
                        walk(item.children)
                    }
                }
            }

            walk(Array.isArray(menuTree) ? menuTree : [])
        } catch {
            // 忽略菜单解析失败
        } finally {
            dynamicRoutesInited = true
        }
        next({...to, replace: true})
        return
    }

    next()
})

export default router
