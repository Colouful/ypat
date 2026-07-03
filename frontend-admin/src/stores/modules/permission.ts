/**
 * Permission Store - 动态路由 + 权限标识
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Component } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { menuConfig, type MenuItem } from '@/constants/menu'

// 显式导入所有动态路由组件，避免 import.meta.glob 在构建时被优化为空对象
import ManageYpatList from '@/views/manage/ypat-list/index.vue'
import ManageUserList from '@/views/manage/user-list/index.vue'
import ManageProductList from '@/views/manage/product-list/index.vue'
import ManageWorkList from '@/views/manage/work-list/index.vue'
import ArticleList from '@/views/article/list/index.vue'
import BannerList from '@/views/banner/list/index.vue'
import YpatEdit from '@/views/ypat/edit/index.vue'
import QueryUserList from '@/views/query/user-list/index.vue'
import QueryYpatList from '@/views/query/ypat-list/index.vue'
import QueryMessList from '@/views/query/mess-list/index.vue'
import PubeventList from '@/views/pubevent/list/index.vue'
import OrderList from '@/views/order/list/index.vue'

const viewModules: Record<string, Component> = {
  'manage/ypat-list/index': ManageYpatList,
  'manage/user-list/index': ManageUserList,
  'manage/product-list/index': ManageProductList,
  'manage/work-list/index': ManageWorkList,
  'article/list/index': ArticleList,
  'banner/list/index': BannerList,
  'ypat/edit/index': YpatEdit,
  'query/user-list/index': QueryUserList,
  'query/ypat-list/index': QueryYpatList,
  'query/mess-list/index': QueryMessList,
  'pubevent/list/index': PubeventList,
  'order/list/index': OrderList,
}

export const usePermissionStore = defineStore('permission', () => {
  /** 动态路由是否已加载 */
  const isDynamicRouteLoaded = ref(false)
  /** 动态路由列表 */
  const dynamicRoutes = ref<RouteRecordRaw[]>([])

  /**
   * 生成动态路由
   */
  function generateRoutes(): RouteRecordRaw[] {
    const routes: RouteRecordRaw[] = []

    for (const group of menuConfig) {
      for (const item of group.children) {
        const component = viewModules[item.component]

        if (!component) {
          console.warn(`[router] 组件不存在: ${item.component}，使用占位组件`)
          routes.push(createPlaceholderRoute(item))
          continue
        }

        routes.push({
          path: item.path.replace(/^\//, ''),
          name: item.path.replace(/\//g, '-').slice(1),
          component,
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
      path: item.path.replace(/^\//, ''),
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
