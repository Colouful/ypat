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

export function showNavigationLoading(title = '加载中...'): void {
  uni.showLoading({ title, mask: true })
}

export function hideNavigationLoading(): void {
  setTimeout(() => {
    uni.hideLoading()
  }, 180)
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
  uni.navigateTo({ url })
}

export function goRootTab(url: RootTabUrl): void {
  if (currentPath() === url) return
  uni.reLaunch({ url })
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
