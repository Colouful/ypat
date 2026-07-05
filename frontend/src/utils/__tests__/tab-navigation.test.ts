import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  goBackOrHome,
  goRootTab,
  isRootTabUrl,
  openMessage,
  openPublish,
} from '../tab-navigation'

const authStorageMock = vi.hoisted(() => ({
  getStoredUserInfo: vi.fn(),
  getToken: vi.fn(),
}))

vi.mock('@/services/auth-storage', () => ({
  getStoredUserInfo: authStorageMock.getStoredUserInfo,
  getToken: authStorageMock.getToken,
}))

describe('tab-navigation', () => {
  beforeEach(() => {
    authStorageMock.getToken.mockReturnValue('')
    authStorageMock.getStoredUserInfo.mockReturnValue(null)
    vi.clearAllMocks()
    ;(globalThis as unknown as { getCurrentPages: () => unknown[] }).getCurrentPages = vi.fn(() => [])
    Object.assign(uni, {
      hideLoading: vi.fn(),
      navigateBack: vi.fn(),
      navigateTo: vi.fn(),
      reLaunch: vi.fn(),
      showLoading: vi.fn(),
    })
  })

  it('treats bottom tab pages as root tabs except protected publish', () => {
    expect(isRootTabUrl('/pages/home/index')).toBe(true)
    expect(isRootTabUrl('/pages/work/index')).toBe(true)
    expect(isRootTabUrl('/pages/message/index')).toBe(true)
    expect(isRootTabUrl('/pages/mine/index')).toBe(true)
    expect(isRootTabUrl('/pages/publish/index')).toBe(false)
    expect(isRootTabUrl('/pages/discover/index')).toBe(false)
  })

  it('switches root tabs with reLaunch', () => {
    goRootTab('/pages/mine/index')

    expect(uni.showLoading).toHaveBeenCalled()
    expect(uni.reLaunch).toHaveBeenCalledWith(expect.objectContaining({ url: '/pages/mine/index' }))
    expect(uni.navigateTo).not.toHaveBeenCalled()
  })

  it('opens publish with login redirect instead of reLaunch', () => {
    openPublish()

    expect(uni.navigateTo).toHaveBeenCalledWith({
      url: `/pages/login/index?redirect=${encodeURIComponent('/pages/publish/index')}`,
    })
    expect(uni.reLaunch).not.toHaveBeenCalled()
  })

  it('opens publish directly when logged in', () => {
    authStorageMock.getToken.mockReturnValue('token')
    authStorageMock.getStoredUserInfo.mockReturnValue({ id: 1 })

    openPublish()

    expect(uni.navigateTo).toHaveBeenCalledWith(expect.objectContaining({ url: '/pages/publish/index' }))
    expect(uni.reLaunch).not.toHaveBeenCalled()
  })

  it('opens message with login redirect instead of reLaunch', () => {
    openMessage()

    expect(uni.navigateTo).toHaveBeenCalledWith({
      url: `/pages/login/index?redirect=${encodeURIComponent('/pages/message/index')}`,
    })
    expect(uni.reLaunch).not.toHaveBeenCalled()
  })

  it('falls back home through root tab navigation', () => {
    goBackOrHome()

    expect(uni.reLaunch).toHaveBeenCalledWith(expect.objectContaining({ url: '/pages/home/index' }))
  })
})
