/**
 * Permission Store - 动态路由 + 权限标识
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { menuConfig, type MenuItem } from '@/constants/menu'

export const usePermissionStore = defineStore('permission', () => {
  /** 动态路由是否已加载 */
  const isDynamicRouteLoaded = ref(false)
  /** 动态路由列表 */
  const dynamicRoutes = ref<RouteRecordRaw[]>([])

  /**
   * 生成动态路由
   *
   * 从静态菜单配置生成路由，使用 import.meta.glob 安全映射组件
   */
  function generateRoutes(): RouteRecordRaw[] {
    // 安全的组件映射（不直接信任后端字符串执行动态 import）
    const viewModules = import.meta.glob('../views/**/*.vue')

    const routes: RouteRecordRaw[] = []

    for (const group of menuConfig) {
      for (const item of group.children) {
        const componentPath = `../views/${item.component}.vue`

        // 检查组件是否存在
        if (!(componentPath in viewModules)) {
          console.warn(`[router] 组件不存在: ${item.component}，使用占位组件`)
          routes.push(createPlaceholderRoute(item))
          continue
        }

        routes.push({
          path: item.path,
          name: item.path.replace(/\//g, '-').slice(1),
          component: viewModules[componentPath],
          meta: {
            title: item.title,
            icon: item.icon,
            hidden: item.hidden,
          },
        })
      }
    }

    dynamicRoutes.value = routes
    isDynamicRouteLoaded.value = true
    return routes
  }

  /**
   * 创建占位路由（组件不存在时）
   */
  function createPlaceholderRoute(item: MenuItem): RouteRecordRaw {
    return {
      path: item.path,
      name: item.path.replace(/\//g, '-').slice(1),
      component: () => import('@/components/common/PagePlaceholder.vue'),
      meta: {
        title: item.title,
        icon: item.icon,
        hidden: item.hidden,
        placeholder: true,
      },
    }
  }

  /**
   * 重置动态路由
   */
  function resetRoutes(): void {
    isDynamicRouteLoaded.value = false
    dynamicRoutes.value = []
  }

  return {
    isDynamicRouteLoaded,
    dynamicRoutes,
    generateRoutes,
    resetRoutes,
  }
})
