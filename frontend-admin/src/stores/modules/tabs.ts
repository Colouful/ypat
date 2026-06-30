/**
 * Tabs Store - 多标签页状态
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteLocationNormalized } from 'vue-router'

export interface TabItem {
  path: string
  title: string
  name: string
  affix?: boolean
}

export const useTabsStore = defineStore('tabs', () => {
  const tabs = ref<TabItem[]>([])
  const activeTab = ref('')

  /**
   * 添加标签
   */
  function addTab(route: RouteLocationNormalized): void {
    if (!route.name || route.meta?.hidden) return

    const exists = tabs.value.find((t) => t.path === route.path)
    if (exists) {
      activeTab.value = route.path
      return
    }

    tabs.value.push({
      path: route.path,
      title: (route.meta?.title as string) || route.name as string,
      name: route.name as string,
      affix: route.meta?.affix as boolean | undefined,
    })
    activeTab.value = route.path
  }

  /**
   * 关闭标签
   */
  function closeTab(path: string): TabItem | null {
    const index = tabs.value.findIndex((t) => t.path === path)
    if (index === -1) return null

    tabs.value.splice(index, 1)

    // 如果关闭的是当前激活的标签，跳转到相邻标签
    if (activeTab.value === path) {
      const next = tabs.value[index] || tabs.value[index - 1] || tabs.value[tabs.value.length - 1]
      activeTab.value = next ? next.path : ''
      return next
    }
    return null
  }

  /**
   * 关闭其他标签
   */
  function closeOtherTabs(path: string): void {
    tabs.value = tabs.value.filter((t) => t.path === path || t.affix)
    activeTab.value = path
  }

  /**
   * 关闭所有标签
   */
  function closeAllTabs(): TabItem | null {
    tabs.value = tabs.value.filter((t) => t.affix)
    const first = tabs.value[0] || null
    activeTab.value = first ? first.path : ''
    return first
  }

  /**
   * 重置
   */
  function reset(): void {
    tabs.value = []
    activeTab.value = ''
  }

  return {
    tabs,
    activeTab,
    addTab,
    closeTab,
    closeOtherTabs,
    closeAllTabs,
    reset,
  }
})
