import { useUserStore } from '@/stores/user'
import {
  goTab,
  hideNavigationLoading,
  isBottomTabUrl,
  showNavigationLoading,
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
    if (!checkLoginInterception(fullUrl)) return

    showNavigationLoading()
    uni.navigateTo({
      url: fullUrl,
      fail: () => {
        const path = fullUrl.split('?')[0]
        if (isBottomTabUrl(path)) goTab(path)
      },
      complete: hideNavigationLoading,
    })
  }

  function redirectTo(url: string, params?: Record<string, string | number | boolean | undefined | null>) {
    const fullUrl = buildUrl(url, params)
    if (!checkLoginInterception(fullUrl)) return

    showNavigationLoading()
    uni.redirectTo({ url: fullUrl, complete: hideNavigationLoading })
  }

  function switchTab(url: string) {
    if (isBottomTabUrl(url)) goTab(url)
  }

  function reLaunch(url: string) {
    showNavigationLoading()
    uni.reLaunch({ url, complete: hideNavigationLoading })
  }

  function goBack(delta: number = 1) {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      uni.navigateBack({ delta })
    } else {
      goTab('/pages/home/index')
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
