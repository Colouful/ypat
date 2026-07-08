import type { Banner } from '@/api/types'
import { isRootTabUrl } from './tab-navigation'

export type BannerAction =
  | { type: 'preview' }
  | { type: 'miniapp'; url: string }
  | { type: 'web'; url: string }
  | { type: 'copy'; url: string }

const LEGACY_URL_FIELDS = ['jumpUrl', 'linkUrl', 'linkurl', 'url', 'path'] as const

function toText(value: unknown): string {
  return typeof value === 'string' ? value.trim() : ''
}

function isMiniappPageUrl(url: string): boolean {
  return url.startsWith('/pages/') || url.startsWith('/pages-sub/')
}

function isWebUrl(url: string): boolean {
  return /^https?:\/\//i.test(url)
}

function getLegacyUrl(item: Banner): string {
  const record = item as unknown as Record<string, unknown>
  return LEGACY_URL_FIELDS
    .map((field) => toText(record[field]))
    .find(Boolean) || ''
}

function inferActionFromUrl(url: string): BannerAction {
  if (!url) return { type: 'preview' }
  if (isMiniappPageUrl(url)) return { type: 'miniapp', url }
  if (isWebUrl(url)) return { type: 'web', url }
  return { type: 'copy', url }
}

export function resolveBannerAction(item: Banner): BannerAction {
  const jumpflag = toText(item.jumpflag)
  if (jumpflag === '0') return { type: 'preview' }

  const jumpurl = toText(item.jumpurl)

  if (jumpflag === '1') {
    const jumptype = toText(item.jumptype).toLowerCase()

    if (jumptype === 'miniapp') {
      return isMiniappPageUrl(jumpurl) ? { type: 'miniapp', url: jumpurl } : { type: 'preview' }
    }

    if (jumptype === 'web') {
      return isWebUrl(jumpurl) ? { type: 'web', url: jumpurl } : { type: 'copy', url: jumpurl }
    }

    return { type: 'preview' }
  }

  return inferActionFromUrl(jumpurl || getLegacyUrl(item))
}

export function getBannerWebViewUrl(url: string): string {
  return `/pages-sub/content/web-view?url=${encodeURIComponent(url)}`
}

export function openBannerAction(action: BannerAction, preview: () => void): void {
  if (action.type === 'preview') {
    preview()
    return
  }

  if (action.type === 'copy') {
    copyUrl(action.url)
    return
  }

  if (action.type === 'web') {
    uni.navigateTo({
      url: getBannerWebViewUrl(action.url),
      fail: () => copyUrl(action.url),
    })
    return
  }

  const navigateOptions = {
    url: action.url,
    fail: () => preview(),
  }

  if (isRootTabUrl(action.url)) {
    uni.switchTab(navigateOptions)
    return
  }

  uni.navigateTo(navigateOptions)
}

export function copyUrl(url: string): void {
  uni.setClipboardData({
    data: url,
    success: () => {
      uni.showToast({ title: '链接已复制，请在浏览器打开', icon: 'none' })
    },
    fail: () => {
      uni.showToast({ title: '复制失败，请稍后重试', icon: 'none' })
    },
  })
}
