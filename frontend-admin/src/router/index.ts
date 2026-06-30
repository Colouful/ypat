import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { staticRoutes, layoutRoute } from './modules/static'
import { setupRouterGuards } from './guards'

const routes: RouteRecordRaw[] = [
  ...staticRoutes,
  layoutRoute,
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 安装路由守卫
setupRouterGuards(router)

export default router
