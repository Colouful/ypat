import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { staticRoutes, layoutRoute } from './modules/static'
import { setupRouterGuards } from './guards'

const routes: RouteRecordRaw[] = [
  ...staticRoutes,
  layoutRoute,
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', hidden: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 安装路由守卫
setupRouterGuards(router)

export default router
