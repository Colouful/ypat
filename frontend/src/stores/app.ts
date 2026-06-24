import { defineStore } from 'pinia'
import { ref } from 'vue'

interface SystemInfo {
  platform: string
  model: string
  system: string
  screenWidth: number
  screenHeight: number
  windowWidth: number
  windowHeight: number
  pixelRatio: number
  language: string
}

export const useAppStore = defineStore('app', () => {
  const systemInfo = ref<SystemInfo | null>(null)
  const statusBarHeight = ref<number>(0)
  const navBarHeight = ref<number>(0)
  const safeAreaBottom = ref<number>(0)
  const networkType = ref<string>('unknown')
  const isReady = ref<boolean>(false)

  function initApp() {
    const info = uni.getSystemInfoSync()

    systemInfo.value = {
      platform: info.platform || '',
      model: info.model || '',
      system: info.system || '',
      screenWidth: info.screenWidth || 0,
      screenHeight: info.screenHeight || 0,
      windowWidth: info.windowWidth || 0,
      windowHeight: info.windowHeight || 0,
      pixelRatio: info.pixelRatio || 1,
      language: info.language || 'zh-CN',
    }

    statusBarHeight.value = info.statusBarHeight || 0

    // Calculate nav bar height: status bar + 44px (default nav bar height)
    const isIOS = info.platform === 'ios'
    const defaultNavHeight = isIOS ? 44 : 48
    navBarHeight.value = statusBarHeight.value + defaultNavHeight

    // Safe area bottom inset
    if (info.safeArea) {
      safeAreaBottom.value = info.screenHeight - info.safeArea.bottom
    }

    // Get initial network type
    uni.getNetworkType({
      success: (res) => {
        networkType.value = res.networkType
      },
    })

    // Listen for network status changes
    uni.onNetworkStatusChange((res) => {
      networkType.value = res.networkType
    })

    isReady.value = true
  }

  function updateNetwork(type: string) {
    networkType.value = type
  }

  return {
    systemInfo,
    statusBarHeight,
    navBarHeight,
    safeAreaBottom,
    networkType,
    isReady,
    initApp,
    updateNetwork,
  }
})
