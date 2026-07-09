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
import ManageWorkComplainList from '@/views/manage/work-complain-list/index.vue'
import ArticleList from '@/views/article/list/index.vue'
import BannerList from '@/views/banner/list/index.vue'
import YpatEdit from '@/views/ypat/edit/index.vue'
import QueryUserList from '@/views/query/user-list/index.vue'
import QueryYpatList from '@/views/query/ypat-list/index.vue'
import QueryMessList from '@/views/query/mess-list/index.vue'
import PubeventList from '@/views/pubevent/list/index.vue'
import CheckinIndex from '@/views/checkin/index.vue'
import OrderList from '@/views/order/list/index.vue'
import PaymentOrder from '@/views/payment/order/index.vue'
import DepositConfig from '@/views/deposit/config/index.vue'
import DepositOrder from '@/views/deposit/order/index.vue'
import MemberPlan from '@/views/member/plan/index.vue'
import MemberRule from '@/views/member/rule/index.vue'
import MemberUser from '@/views/member/user/index.vue'
import MemberOrder from '@/views/member/order/index.vue'
import MemberLog from '@/views/member/log/index.vue'
import InviteConfig from '@/views/invite/config/index.vue'
import InviteRecords from '@/views/invite/records/index.vue'
import InternalTestResource from '@/views/internal-test/resource/index.vue'
import InternalTestGenerator from '@/views/internal-test/generator/index.vue'

const viewModules: Record<string, Component> = {
  'manage/ypat-list/index': ManageYpatList,
  'manage/user-list/index': ManageUserList,
  'manage/product-list/index': ManageProductList,
  'manage/work-list/index': ManageWorkList,
  'manage/work-complain-list/index': ManageWorkComplainList,
  'article/list/index': ArticleList,
  'banner/list/index': BannerList,
  'ypat/edit/index': YpatEdit,
  'query/user-list/index': QueryUserList,
  'query/ypat-list/index': QueryYpatList,
  'query/mess-list/index': QueryMessList,
  'pubevent/list/index': PubeventList,
  'checkin/index': CheckinIndex,
  'order/list/index': OrderList,
  'payment/order/index': PaymentOrder,
  'deposit/config/index': DepositConfig,
  'deposit/order/index': DepositOrder,
  'member/plan/index': MemberPlan,
  'member/rule/index': MemberRule,
  'member/user/index': MemberUser,
  'member/order/index': MemberOrder,
  'member/log/index': MemberLog,
  'invite/config/index': InviteConfig,
  'invite/records/index': InviteRecords,
  'internal-test/resource/index': InternalTestResource,
  'internal-test/generator/index': InternalTestGenerator,
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
