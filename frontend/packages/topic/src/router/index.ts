import { createRouter, createWebHistory } from 'vue-router'
import TopicList from '@/views/topic/TopicList.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: TopicList
    }
  ]
})

export default router

