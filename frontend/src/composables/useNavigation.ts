import { useUserStore } from '@/stores/user'
import {
  goRootTab,
  openMessage,
  openPublish,
  isRootTabUrl,
} from '@/utils/tab-navigation'

// Pages that require login before accessing
const PROTECTED_PAGES = [
  '/pages/publish/index',
  '/pages/user/profile',
  '/pages/user/settings',
  '/pages/message/index',
  '/pages/order/index',
]

/**
 * Serialize parameters object into URL query string
 */
function serializeParams(params?: Record<string, string | number | boolean | undefined | null>): string {
  if (!params) return ''
  const parts: string[] = []
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null) {
      parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    }
  }
  return parts.length > 0 ? `?${parts.join('&')}` : ''
}

/**
 * Build full URL with serialized params
 */
function buildUrl(url: string, params?: Record<string, string | number | boolean | undefined | null>): string {
  const queryString = serializeParams(params)
  // If url already has query params, append with &
  if (queryString) {
    return url.includes('?') ? `${url}&${queryString.slice(1)}` : `${url}${queryString}`
  }
  return url
}

/**
 * Check if the page requires login and handle interception
 */
function checkLoginInterception(url: string): boolean {
  const userStore = useUserStore()
  const path = url.split('?')[0]

  if (PROTECTED_PAGES.some((page) => path.startsWith(page))) {
    if (!userStore.isLoggedIn) {
      uni.navigateTo({
        url: `/pages/login/index?redirect=${encodeURIComponent(url)}`,
      })
      return false
    }
  }
  return true
}

export function useNavigation() {
  function navigateTo(url: string, params?: Record<string, string | number | boolean | undefined | null>) {
    const fullUrl = buildUrl(url, params)
    const path = fullUrl.split('?')[0]
    if (path === '/pages/publish/index') {
      openPublish()
      return
    }
    if (path === '/pages/message/index') {
      openMessage()
      return
    }
    if (isRootTabUrl(path)) {
      goRootTab(path)
      return
    }
    if (!checkLoginInterception(fullUrl)) return

    uni.navigateTo({
      url: fullUrl,
      fail: () => {
        if (isRootTabUrl(path)) goRootTab(path)
      },
    })
  }

  function redirectTo(url: string, params?: Record<string, string | number | boolean | undefined | null>) {
    const fullUrl = buildUrl(url, params)
    if (!checkLoginInterception(fullUrl)) return

    uni.redirectTo({ url: fullUrl })
  }

  function switchTab(url: string) {
    if (isRootTabUrl(url)) goRootTab(url)
  }

  function reLaunch(url: string) {
    uni.reLaunch({ url })
  }

  function goBack(delta: number = 1) {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      uni.navigateBack({ delta })
    } else {
      goRootTab('/pages/home/index')
    }
  }

  return {
    navigateTo,
    redirectTo,
    switchTab,
    reLaunch,
    goBack,
  }
}
