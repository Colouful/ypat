import { getStoredUserInfo, getToken } from '@/services/auth-storage'

export const ROOT_TAB_URLS = [
  '/pages/home/index',
  '/pages/work/index',
  '/pages/message/index',
  '/pages/mine/index',
] as const

export type RootTabUrl = typeof ROOT_TAB_URLS[number]

export function isRootTabUrl(url: string): url is RootTabUrl {
  return ROOT_TAB_URLS.includes(url.split('?')[0] as RootTabUrl)
}

// 幂等：多次 show 只调一次 uni.showLoading；多次 hide 只调一次 uni.hideLoading。
// 收敛所有异常路径（拦截器 complete 与 native complete 回调双触发、promise .then 额外触发等），
// 避免 "showLoading 与 hideLoading 必须配对使用" 警告
let navLoadingActive = false

export function showNavigationLoading(title = '加载中...'): void {
  if (navLoadingActive) return
  navLoadingActive = true
  uni.showLoading({ title, mask: true })
}

export function hideNavigationLoading(): void {
  if (!navLoadingActive) return
  navLoadingActive = false
  uni.hideLoading()
}

function isLoggedIn(): boolean {
  return Boolean(getToken() && getStoredUserInfo()?.id)
}

function currentPath(): string {
  const pages = getCurrentPages() as Array<{ route?: string }>
  const current = pages[pages.length - 1]
  return current?.route ? `/${current.route}` : ''
}

function navigateToProtected(url: string): void {
  if (!isLoggedIn()) {
    uni.navigateTo({ url: `/pages/login/index?redirect=${encodeURIComponent(url)}` })
    return
  }
  if (currentPath() === url) return
  showNavigationLoading()
  uni.navigateTo({
    url,
    complete: hideNavigationLoading,
  })
}

export function goRootTab(url: RootTabUrl): void {
  if (currentPath() === url) return
  showNavigationLoading()
  uni.reLaunch({ url, complete: hideNavigationLoading })
}

export function openPublish(): void {
  navigateToProtected('/pages/publish/index')
}

export function openMessage(): void {
  navigateToProtected('/pages/message/index')
}

export function goBackOrHome(): void {
  if (getCurrentPages().length > 1) {
    uni.navigateBack()
    return
  }
  goRootTab('/pages/home/index')
}
