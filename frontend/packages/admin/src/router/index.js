import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import Layout from '../layouts/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import ConfigList from '../views/config/ConfigList.vue'
import ConfigClassList from '../views/config-class/ConfigClassList.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: Dashboard
      },
      {
        path: '/config',
        name: 'ConfigList',
        component: ConfigList
      },
      {
        path: '/config-class',
        name: 'ConfigClassList',
        component: ConfigClassList
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router