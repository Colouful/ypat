import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/modules/auth'
import { usePermissionStore } from '@/stores/modules/permission'
import { useTabsStore } from '@/stores/modules/tabs'

/** 白名单路径 */
const WHITE_LIST = ['/login', '/403', '/404']

export function setupRouterGuards(router: Router): void {
  router.beforeEach(async (to, _from, next) => {
    const authStore = useAuthStore()
    const permissionStore = usePermissionStore()

    // 白名单直接放行
    if (WHITE_LIST.includes(to.path)) {
      // 已登录访问登录页，跳转首页
      if (to.path === '/login' && authStore.token) {
        next('/dashboard')
        return
      }
      next()
      return
    }

    // 无 Token 跳转登录
    if (!authStore.token) {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      return
    }

    // 有 Token 但动态路由未加载
    if (!permissionStore.isDynamicRouteLoaded) {
      try {
        // 获取用户信息
        if (!authStore.adminInfo) {
          await authStore.fetchAdminInfo()
        }

        // 生成动态路由
        const dynamicRoutes = permissionStore.generateRoutes()

        // 动态添加路由
        dynamicRoutes.forEach((route) => {
          router.addRoute(route)
        })

        // 重新导航（确保动态路由已生效）
        next({ ...to, replace: true })
        return
      } catch {
        // 获取用户信息失败（Token 失效等）
        authStore.resetState()
        permissionStore.resetRoutes()
        next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
        return
      }
    }

    next()
  })

  router.afterEach((to) => {
    const tabsStore = useTabsStore()
    tabsStore.addTab(to)
  })
}
