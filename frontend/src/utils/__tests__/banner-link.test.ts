import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { Banner } from '@/api/types'
import {
  copyUrl,
  getBannerWebViewUrl,
  openBannerAction,
  resolveBannerAction,
} from '../banner-link'

function banner(overrides: Partial<Banner>): Banner {
  return {
    id: 1,
    imgpath: '/static/banner.png',
    ...overrides,
  }
}

describe('resolveBannerAction', () => {
  it('previews when jumpflag is disabled', () => {
    expect(resolveBannerAction(banner({ jumpflag: '0' }))).toEqual({ type: 'preview' })
  })

  it('opens enabled miniapp page targets', () => {
    expect(resolveBannerAction(banner({
      jumpflag: '1',
      jumptype: 'miniapp',
      jumpurl: '/pages/work/index',
    }))).toEqual({ type: 'miniapp', url: '/pages/work/index' })
  })

  it('previews enabled miniapp targets outside allowed page roots', () => {
    expect(resolveBannerAction(banner({
      jumpflag: '1',
      jumptype: 'miniapp',
      jumpurl: '/work/index',
    }))).toEqual({ type: 'preview' })
  })

  it('opens enabled web targets with lowercase protocol', () => {
    expect(resolveBannerAction(banner({
      jumpflag: '1',
      jumptype: 'web',
      jumpurl: 'https://example.com/a',
    }))).toEqual({ type: 'web', url: 'https://example.com/a' })
  })

  it('opens enabled web targets with uppercase protocol', () => {
    expect(resolveBannerAction(banner({
      jumpflag: '1',
      jumptype: 'web',
      jumpurl: 'HTTPS://example.com/a',
    }))).toEqual({ type: 'web', url: 'HTTPS://example.com/a' })
  })

  it('copies enabled web targets with unsupported protocol', () => {
    expect(resolveBannerAction(banner({
      jumpflag: '1',
      jumptype: 'web',
      jumpurl: 'ftp://example.com/a',
    }))).toEqual({ type: 'copy', url: 'ftp://example.com/a' })
  })

  it('copies enabled web targets that look like miniapp pages', () => {
    expect(resolveBannerAction(banner({
      jumpflag: '1',
      jumptype: 'web',
      jumpurl: '/pages/work/index',
    }))).toEqual({ type: 'copy', url: '/pages/work/index' })
  })

  it('copies empty enabled web targets', () => {
    expect(resolveBannerAction(banner({
      jumpflag: '1',
      jumptype: 'web',
      jumpurl: '',
    }))).toEqual({ type: 'copy', url: '' })
  })

  it('keeps legacy jumpUrl miniapp compatibility when jumpflag is missing', () => {
    expect(resolveBannerAction(banner({ jumpUrl: '/pages/work/index' }))).toEqual({
      type: 'miniapp',
      url: '/pages/work/index',
    })
  })
})

describe('banner link helpers', () => {
  beforeEach(() => {
    Object.assign(uni, {
      navigateTo: vi.fn(),
      setClipboardData: vi.fn(),
      showToast: vi.fn(),
      switchTab: vi.fn(),
    })
  })

  it('builds encoded web-view urls', () => {
    expect(getBannerWebViewUrl('HTTPS://example.com/a?x=1&y=二')).toBe(
      `/pages-sub/content/web-view?url=${encodeURIComponent('HTTPS://example.com/a?x=1&y=二')}`,
    )
  })

  it('runs preview callback for preview actions', () => {
    const preview = vi.fn()

    openBannerAction({ type: 'preview' }, preview)

    expect(preview).toHaveBeenCalledTimes(1)
  })

  it('copies copy actions without previewing', () => {
    const preview = vi.fn()

    openBannerAction({ type: 'copy', url: 'https://example.com/a' }, preview)

    expect(uni.setClipboardData).toHaveBeenCalledWith(expect.objectContaining({
      data: 'https://example.com/a',
    }))
    expect(preview).not.toHaveBeenCalled()
  })

  it('uses switchTab for tab miniapp pages', () => {
    openBannerAction({ type: 'miniapp', url: '/pages/work/index' }, vi.fn())

    expect(uni.switchTab).toHaveBeenCalledWith(expect.objectContaining({ url: '/pages/work/index' }))
    expect(uni.navigateTo).not.toHaveBeenCalled()
  })

  it('previews when tab miniapp navigation fails', () => {
    const preview = vi.fn()
    vi.mocked(uni.switchTab).mockImplementationOnce((options: UniApp.SwitchTabOptions) => {
      options.fail?.({ errMsg: 'fail' })
    })

    openBannerAction({ type: 'miniapp', url: '/pages/home/index' }, preview)

    expect(uni.switchTab).toHaveBeenCalledWith(expect.objectContaining({
      url: '/pages/home/index',
    }))
    expect(preview).toHaveBeenCalledTimes(1)
  })

  it('uses navigateTo for non-tab miniapp pages and previews on failure', () => {
    const preview = vi.fn()
    vi.mocked(uni.navigateTo).mockImplementationOnce((options: UniApp.NavigateToOptions) => {
      options.fail?.({ errMsg: 'fail' })
    })

    openBannerAction({ type: 'miniapp', url: '/pages-sub/content/detail?id=1' }, preview)

    expect(uni.navigateTo).toHaveBeenCalledWith(expect.objectContaining({
      url: '/pages-sub/content/detail?id=1',
    }))
    expect(preview).toHaveBeenCalledTimes(1)
  })

  it('opens web links through web-view and copies on failure', () => {
    vi.mocked(uni.navigateTo).mockImplementationOnce((options: UniApp.NavigateToOptions) => {
      options.fail?.({ errMsg: 'fail' })
    })

    openBannerAction({ type: 'web', url: 'https://example.com/a' }, vi.fn())

    expect(uni.navigateTo).toHaveBeenCalledWith(expect.objectContaining({
      url: getBannerWebViewUrl('https://example.com/a'),
    }))
    expect(uni.setClipboardData).toHaveBeenCalledWith(expect.objectContaining({
      data: 'https://example.com/a',
    }))
  })

  it('copies url and shows success feedback', () => {
    vi.mocked(uni.setClipboardData).mockImplementationOnce((options: UniApp.SetClipboardDataOptions) => {
      options.success?.({ errMsg: 'ok' })
    })

    copyUrl('ftp://example.com/a')

    expect(uni.setClipboardData).toHaveBeenCalledWith(expect.objectContaining({
      data: 'ftp://example.com/a',
    }))
    expect(uni.showToast).toHaveBeenCalledWith({ title: '链接已复制，请在浏览器打开', icon: 'none' })
  })

  it('shows failure feedback when copy fails', () => {
    vi.mocked(uni.setClipboardData).mockImplementationOnce((options: UniApp.SetClipboardDataOptions) => {
      options.fail?.({ errMsg: 'fail' })
    })

    copyUrl('ftp://example.com/a')

    expect(uni.showToast).toHaveBeenCalledWith({ title: '复制失败，请稍后重试', icon: 'none' })
  })
})
