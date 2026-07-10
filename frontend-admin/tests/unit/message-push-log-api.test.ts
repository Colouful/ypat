import { beforeEach, describe, expect, it, vi } from 'vitest'

const getMock = vi.fn((url: string, params?: Record<string, unknown>) => Promise.resolve({ url, params }))

vi.mock('@/api/request', () => ({
  get: getMock,
}))

describe('后台推送记录 API', () => {
  beforeEach(() => {
    getMock.mockClear()
  })

  it('应请求推送记录列表', async () => {
    const api = await import('@/api/modules/message-push-log')

    await expect(
      api.getMessagePushLogs({
        eventType: 'WECHAT_SUBSCRIBE_SENT',
        success: '0',
        ypatid: 12,
        page: 0,
        size: 10,
      }) as unknown as Promise<unknown>,
    ).resolves.toMatchObject({
      url: '/admin/message-push-log/list',
      params: {
        eventType: 'WECHAT_SUBSCRIBE_SENT',
        success: '0',
        ypatid: 12,
        page: 0,
        size: 10,
      },
    })
  })

  it('应请求推送记录统计', async () => {
    const api = await import('@/api/modules/message-push-log')

    await expect(
      api.getMessagePushLogStats({
        eventType: 'IN_APP_CREATED',
        recperid: 8,
      }) as unknown as Promise<unknown>,
    ).resolves.toMatchObject({
      url: '/admin/message-push-log/stats',
      params: {
        eventType: 'IN_APP_CREATED',
        recperid: 8,
      },
    })
  })
})
