export const BOTTOM_TAB_URLS = [
  '/pages/home/index',
  '/pages/discover/index',
  '/pages/publish/index',
  '/pages/message/index',
  '/pages/mine/index',
] as const

export type BottomTabUrl = typeof BOTTOM_TAB_URLS[number]

export function isBottomTabUrl(url: string): url is BottomTabUrl {
  return BOTTOM_TAB_URLS.includes(url.split('?')[0] as BottomTabUrl)
}

export function showNavigationLoading(title = '加载中...'): void {
  uni.showLoading({ title, mask: true })
}

export function hideNavigationLoading(): void {
  setTimeout(() => {
    uni.hideLoading()
  }, 180)
}

export function goTab(url: BottomTabUrl): void {
  showNavigationLoading()
  uni.reLaunch({
    url,
    complete: hideNavigationLoading,
  })
}

export function goBackOrHome(): void {
  if (getCurrentPages().length > 1) {
    uni.navigateBack()
    return
  }
  goTab('/pages/home/index')
}
