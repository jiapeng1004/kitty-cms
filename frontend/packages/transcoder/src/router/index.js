import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../layouts/Layout.vue'
import TaskList from '../views/TaskList.vue'
import TaskCreate from '../views/TaskCreate.vue'
import TaskDetail from '../views/TaskDetail.vue'
import StrategyList from '../views/StrategyList.vue'
import AccessKey from '../views/AccessKey.vue'

const routes = [
  { path: '/login', name: 'Login', component: Login },
  {
    path: '/',
    component: Layout,
    children: [
      { path: '', redirect: '/tasks' },
      { path: 'tasks', name: 'TaskList', component: TaskList },
      { path: 'tasks/create', name: 'TaskCreate', component: TaskCreate },
      { path: 'tasks/:id', name: 'TaskDetail', component: TaskDetail },
      { path: 'strategies', name: 'StrategyList', component: StrategyList },
      { path: 'access-keys', name: 'AccessKey', component: AccessKey }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('transcoder_token')
  if (to.name !== 'Login' && !token) next({ name: 'Login' })
  else next()
})

export default router
