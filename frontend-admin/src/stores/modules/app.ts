/**
 * App Store - 侧边栏折叠 + 设备类型
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const device = ref<'desktop' | 'mobile'>('desktop')

  function toggleSidebar(): void {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setDevice(d: 'desktop' | 'mobile'): void {
    device.value = d
  }

  return {
    sidebarCollapsed,
    device,
    toggleSidebar,
    setDevice,
  }
})
