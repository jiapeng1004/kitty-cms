import {createRouter, createWebHistory, type RouteRecordRaw} from 'vue-router'
import {getToken} from '@/utils/api'
import {getCurrentMenuTree, type MenuTreeItem} from '@/api/menu_api'
import Login from '@/views/auth/Login.vue'
import Register from '@/views/auth/Register.vue'
import Layout from '@/layouts/Layout.vue'
import Dashboard from '@/views/Dashboard.vue'

// 扫描所有视图组件，用于根据菜单中配置的 component 字段动态加载
const viewModules = import.meta.glob('../views/**/*.vue')

/**
 * 基础路由：仅包含登录、注册和 Layout 壳子，业务路由按菜单和权限动态挂载
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
            // 首页固定挂在 Layout 下
            {path: '', name: 'Dashboard', component: Dashboard}
            // 其余业务路由（/config、/user、/role 等）在登录后根据菜单动态 addRoute
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes: constantRoutes
})

/**
 * 从菜单节点的 component 字段解析出实际组件
 * 例如 menu.component = 'config/ConfigList' -> '../views/config/ConfigList.vue'
 */
function resolveViewComponent(component?: string) {
    if (!component) return undefined
    const key = `../views/${component}.vue`
    return viewModules[key]
}

let dynamicRoutesInited = false

router.beforeEach(async (to, _from, next) => {
    const token = getToken()
    const isPublic = to.matched.some((r) => r.meta?.public)

    // 未登录且访问受保护路由 → 强制跳登录
    if (!isPublic && !token) {
        next({path: '/login', query: {redirect: to.fullPath}})
        return
    }

    // 已登录访问登录页 → 跳首页
    if (to.path === '/login' && token) {
        next({path: '/'})
        return
    }

    // 登录后首次路由跳转时，根据当前菜单动态注册业务路由
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
                        if (!router.getRoutes().some((r) => r.path === item.path)) {
                            router.addRoute('RootLayout', {
                                path: item.path,
                                name: item.menuKey || item.path,
                                component: loader
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
            // 忽略菜单解析失败，后续接口层仍有权限兜底
        } finally {
            dynamicRoutesInited = true
        }
    }

    next()
})

export default router
