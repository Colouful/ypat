import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const envMock = vi.hoisted(() => ({ envConfig: { env: 'production', apiBaseUrl: '', imageBaseUrl: '' } }))
const requestMocks = vi.hoisted(() => ({ get: vi.fn(), post: vi.fn() }))
const storageMocks = vi.hoisted(() => ({
  clearAuth: vi.fn(),
  getStoredUserInfo: vi.fn(),
  getToken: vi.fn(),
  registerAuthResetHandler: vi.fn(),
  setStoredUserInfo: vi.fn(),
  setToken: vi.fn(),
}))

vi.mock('@/config/env', () => envMock)
vi.mock('@/api/request', () => requestMocks)
vi.mock('@/api/modules/user', () => ({ sendH5LoginCode: vi.fn() }))
vi.mock('@/services/auth-storage', () => storageMocks)

import { useUserStore } from '../user'

describe('user store login (GAP-AUTH-01)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    delete (globalThis as typeof globalThis & { wx?: unknown }).wx
    requestMocks.get.mockReset()
    requestMocks.post.mockReset()
    requestMocks.get.mockImplementation((url: string) => {
      if (url === '/user/code') {
        return Promise.resolve({
          success: true,
          data: { openid: 'openid-1', session_key: 'session-1' },
          code: '200',
          message: '',
        })
      }
      return Promise.resolve({ success: true, data: { id: 1, token: 't' }, code: '200', message: '' })
    })
  })

  it('production WeChat login resolves openid before login and never uses the test account', async () => {
    envMock.envConfig.env = 'production'
    requestMocks.post.mockResolvedValue({
      success: true,
      data: { id: 1, token: 'real-token' },
      code: '200',
      message: '',
    })
    const store = useUserStore()
    await store.login({ code: 'wx-code', encryptedData: 'enc', iv: 'iv', channel: '0' })

    expect(requestMocks.get).toHaveBeenCalledWith('/user/code', { code: 'wx-code' }, { withToken: false, showError: false })
    const [url, payload] = requestMocks.post.mock.calls[0]
    expect(url).toBe('/user/login')
    expect(payload).toMatchObject({
      openid: 'openid-1',
      sessionKey: 'session-1',
      encryptedData: 'enc',
      iv: 'iv',
      channel: '0',
    })
    // 关键: 生产环境绝不退化为硬编码测试账号
    expect(payload.code).toBeUndefined()
    expect(payload.mobile).toBeUndefined()
    expect(JSON.stringify(payload)).not.toContain('18888888888')
  })

  it('development login uses the backend test account for offline 联调', async () => {
    envMock.envConfig.env = 'development'
    requestMocks.post.mockResolvedValue({
      success: true,
      data: { id: 2, token: 'dev-token' },
      code: '200',
      message: '',
    })
    const store = useUserStore()
    await store.login({ code: 'x', encryptedData: 'y', iv: 'z' })

    const [, payload] = requestMocks.post.mock.calls[0]
    expect(payload).toMatchObject({ mobile: '18888888888', smsCode: '888888', channel: '2' })
  })

  it('development WeChat mini-program runtime still resolves openid for payment flows', async () => {
    envMock.envConfig.env = 'development'
    ;(globalThis as typeof globalThis & { wx?: { login: () => void } }).wx = { login: vi.fn() }
    requestMocks.post.mockResolvedValue({
      success: true,
      data: { id: 9, token: 'real-token' },
      code: '200',
      message: '',
    })
    const store = useUserStore()
    await store.login({ code: 'wx-code', encryptedData: 'enc', iv: 'iv' })

    const [, payload] = requestMocks.post.mock.calls[0]
    expect(payload).toMatchObject({ openid: 'openid-1', sessionKey: 'session-1', channel: '0' })
    expect(payload.mobile).toBeUndefined()
  })

  it('H5 phone login sends channel 2', async () => {
    requestMocks.post.mockResolvedValue({
      success: true,
      data: { id: 3, token: 'h5-token' },
      code: '200',
      message: '',
    })
    const store = useUserStore()
    await store.loginByPhone({ mobile: '13800138000', smsCode: '123456' })

    const [url, payload] = requestMocks.post.mock.calls[0]
    expect(url).toBe('/user/login')
    expect(payload).toMatchObject({ mobile: '13800138000', smsCode: '123456', channel: '2' })
    expect(payload).not.toHaveProperty('recmobile')
  })

  it('H5 phone login forwards recmobile when invitee differs from inviter', async () => {
    requestMocks.post.mockResolvedValue({
      success: true,
      data: { id: 4, token: 'h5-token' },
      code: '200',
      message: '',
    })
    const store = useUserStore()
    await store.loginByPhone({ mobile: '13900139000', smsCode: '123456', recmobile: '13800138000' })

    const [, payload] = requestMocks.post.mock.calls[0]
    expect(payload).toMatchObject({ mobile: '13900139000', recmobile: '13800138000' })
  })

  it('H5 phone login drops recmobile when it equals the login mobile (self-invite)', async () => {
    requestMocks.post.mockResolvedValue({
      success: true,
      data: { id: 5, token: 'h5-token' },
      code: '200',
      message: '',
    })
    const store = useUserStore()
    await store.loginByPhone({ mobile: '13800138000', smsCode: '123456', recmobile: '13800138000' })

    const [, payload] = requestMocks.post.mock.calls[0]
    expect(payload).not.toHaveProperty('recmobile')
  })

  it('production WeChat login forwards recmobile when supplied', async () => {
    envMock.envConfig.env = 'production'
    requestMocks.post.mockResolvedValue({
      success: true,
      data: { id: 6, token: 'real-token' },
      code: '200',
      message: '',
    })
    const store = useUserStore()
    await store.login({
      code: 'wx-code',
      encryptedData: 'enc',
      iv: 'iv',
      channel: '0',
      recmobile: '13800138000',
    })

    const [, payload] = requestMocks.post.mock.calls[0]
    expect(payload).toMatchObject({ recmobile: '13800138000' })
  })

  it('production WeChat login forwards inviteCode with priority over recmobile', async () => {
    envMock.envConfig.env = 'production'
    requestMocks.post.mockResolvedValue({
      success: true,
      data: { id: 7, token: 'real-token' },
      code: '200',
      message: '',
    })
    const store = useUserStore()
    await store.login({
      code: 'wx-code',
      encryptedData: 'enc',
      iv: 'iv',
      channel: '0',
      recmobile: '13800138000',
      inviteCode: 'IV3F',
      inviteSource: 'share',
    })

    const [, payload] = requestMocks.post.mock.calls[0]
    expect(payload).toMatchObject({ inviteCode: 'IV3F', inviteSource: 'share' })
  })

  it('H5 phone login forwards inviteCode without recmobile', async () => {
    requestMocks.post.mockResolvedValue({
      success: true,
      data: { id: 8, token: 'h5-token' },
      code: '200',
      message: '',
    })
    const store = useUserStore()
    await store.loginByPhone({ mobile: '13900139000', smsCode: '123456', inviteCode: 'IV2A', inviteSource: 'qr' })

    const [, payload] = requestMocks.post.mock.calls[0]
    expect(payload).toMatchObject({ inviteCode: 'IV2A', inviteSource: 'qr' })
    expect(payload).not.toHaveProperty('recmobile')
  })

  it('deduplicates concurrent unread count refreshes from app and page onShow', async () => {
    const store = useUserStore()
    store.token = 'token'
    store.userInfo = { id: 2 }
    requestMocks.get.mockImplementation((url: string) =>
      Promise.resolve({
        success: true,
        data: url.includes('/rec/') ? 1 : 2,
        code: '200',
        message: '',
      }),
    )

    await Promise.all([
      store.refreshUnreadCount(),
      store.refreshUnreadCount(),
    ])

    expect(requestMocks.get).toHaveBeenCalledTimes(2)
    expect(requestMocks.get.mock.calls.map(([url]) => url)).toEqual([
      '/my/ypat/rec/unread/count',
      '/my/ypat/send/unread/count',
    ])
    expect(store.unreadCount).toBe(3)
  })
})
