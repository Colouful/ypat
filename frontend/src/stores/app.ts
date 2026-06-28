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

interface DeviceInfo {
  platform?: string
  model?: string
  system?: string
}

interface SafeArea {
  bottom?: number
}

interface WindowInfo {
  screenWidth?: number
  screenHeight?: number
  windowWidth?: number
  windowHeight?: number
  pixelRatio?: number
  statusBarHeight?: number
  safeArea?: SafeArea
}

interface AppBaseInfo {
  language?: string
}

interface LegacySystemInfo extends DeviceInfo, WindowInfo, AppBaseInfo {}

interface SystemApiHost {
  getDeviceInfo?: () => DeviceInfo
  getWindowInfo?: () => WindowInfo
  getAppBaseInfo?: () => AppBaseInfo
}

interface RuntimeGlobals {
  wx?: SystemApiHost
}

function callSystemApi<T>(api?: () => T): T | undefined {
  try {
    return api?.()
  } catch {
    return undefined
  }
}

function getCompatSystemInfo(): LegacySystemInfo {
  const uniApi = uni as unknown as SystemApiHost
  const wxApi = (globalThis as unknown as RuntimeGlobals).wx

  const deviceInfo = callSystemApi(uniApi.getDeviceInfo) ?? callSystemApi(wxApi?.getDeviceInfo) ?? {}
  const windowInfo = callSystemApi(uniApi.getWindowInfo) ?? callSystemApi(wxApi?.getWindowInfo) ?? {}
  const appBaseInfo = callSystemApi(uniApi.getAppBaseInfo) ?? callSystemApi(wxApi?.getAppBaseInfo) ?? {}

  if (
    deviceInfo.platform ||
    deviceInfo.model ||
    windowInfo.windowWidth ||
    windowInfo.screenWidth ||
    appBaseInfo.language
  ) {
    return {
      ...deviceInfo,
      ...windowInfo,
      ...appBaseInfo,
    }
  }

  return {}
}

export const useAppStore = defineStore('app', () => {
  const systemInfo = ref<SystemInfo | null>(null)
  const statusBarHeight = ref<number>(0)
  const navBarHeight = ref<number>(0)
  const safeAreaBottom = ref<number>(0)
  const networkType = ref<string>('unknown')
  const isReady = ref<boolean>(false)

  function initApp() {
    const info = getCompatSystemInfo()

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

    // 导航栏高度：状态栏 + 默认胶囊栏高度
    const isIOS = info.platform === 'ios'
    const defaultNavHeight = isIOS ? 44 : 48
    navBarHeight.value = statusBarHeight.value + defaultNavHeight

    // 底部安全区
    if (info.screenHeight && info.safeArea?.bottom) {
      safeAreaBottom.value = info.screenHeight - info.safeArea.bottom
    }

    // 初始网络状态
    uni.getNetworkType({
      success: (res) => {
        networkType.value = res.networkType
      },
    })

    // 网络状态变化
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
