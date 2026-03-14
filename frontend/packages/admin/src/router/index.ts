import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/api'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import Layout from '../layouts/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import ConfigList from '../views/config/ConfigList.vue'
import ConfigClassList from '../views/config-class/ConfigClassList.vue'
import TenantList from '../views/tenant/TenantList.vue'
import UserList from '../views/user/UserList.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { public: true }
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Dashboard', component: Dashboard },
      { path: '/config', name: 'ConfigList', component: ConfigList },
      { path: '/config-class', name: 'ConfigClassList', component: ConfigClassList },
      { path: '/tenant', name: 'TenantList', component: TenantList },
      { path: '/user', name: 'UserList', component: UserList }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = getToken()
  const isPublic = to.matched.some((r) => r.meta?.public)
  if (to.meta?.requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (to.path === '/login' && token) {
    next({ path: '/' })
    return
  }
  next()
})

export default router
