/**
 * Token 存取测试
 */

import { describe, it, expect, beforeEach } from 'vitest'
import { getToken, setToken, removeToken } from '@/utils/auth'

describe('Token 管理', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('setToken 应将 token 存入 localStorage', () => {
    setToken('test-token-123')
    expect(localStorage.getItem('admin_token')).toBe('test-token-123')
  })

  it('getToken 应从 localStorage 读取 token', () => {
    setToken('my-token')
    expect(getToken()).toBe('my-token')
  })

  it('getToken 无 token 时应返回 null', () => {
    expect(getToken()).toBeNull()
  })

  it('removeToken 应清除 localStorage 中的 token', () => {
    setToken('to-remove')
    removeToken()
    expect(getToken()).toBeNull()
    expect(localStorage.getItem('admin_token')).toBeNull()
  })

  it('setToken 应覆盖已有 token', () => {
    setToken('first')
    setToken('second')
    expect(getToken()).toBe('second')
  })
})
