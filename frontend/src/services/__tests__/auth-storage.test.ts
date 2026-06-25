import { describe, expect, it, vi } from 'vitest'
import {
  clearAuth,
  getStoredUserInfo,
  getToken,
  registerAuthResetHandler,
  removeToken,
  setStoredUserInfo,
  setToken,
} from '../auth-storage'

describe('auth storage', () => {
  it('stores and reads token', () => {
    setToken('token-1')
    expect(getToken()).toBe('token-1')
  })

  it('removes token when setting empty value', () => {
    setToken('token-1')
    setToken('')
    expect(getToken()).toBe('')
  })

  it('removes token explicitly', () => {
    setToken('token-1')
    removeToken()
    expect(getToken()).toBe('')
  })

  it('stores and reads user info', () => {
    setStoredUserInfo({ id: 1, nickname: '测试用户' })
    expect(getStoredUserInfo()).toMatchObject({ id: 1, nickname: '测试用户' })
  })

  it('removes invalid stored JSON', () => {
    uni.setStorageSync('ypat_user_info', '{invalid')
    expect(getStoredUserInfo()).toBeNull()
    expect(uni.getStorageSync('ypat_user_info')).toBeUndefined()
  })

  it('clears token and user info', () => {
    setToken('token-1')
    setStoredUserInfo({ id: 1 })
    clearAuth()
    expect(getToken()).toBe('')
    expect(getStoredUserInfo()).toBeNull()
  })

  it('runs registered reset handler', () => {
    const handler = vi.fn()
    registerAuthResetHandler(handler)
    clearAuth()
    expect(handler).toHaveBeenCalledTimes(1)
  })

  it('unregisters reset handler', () => {
    const handler = vi.fn()
    const unregister = registerAuthResetHandler(handler)
    unregister()
    clearAuth()
    expect(handler).not.toHaveBeenCalled()
  })
})
