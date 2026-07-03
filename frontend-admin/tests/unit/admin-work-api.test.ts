import { beforeEach, describe, expect, it, vi } from 'vitest'

const getMock = vi.fn((url: string, params?: Record<string, unknown>) => Promise.resolve({ url, params }))
const postMock = vi.fn((url: string, data?: unknown, config?: unknown) => Promise.resolve({ url, data, config }))

vi.mock('@/api/request', () => ({
  get: getMock,
  post: postMock,
}))

describe('后台作品 API', () => {
  beforeEach(() => {
    getMock.mockClear()
    postMock.mockClear()
  })

  it('应使用后台作品路由', async () => {
    const api = await import('@/api/modules/work')

    await expect(
      api.getWorkList({ page: 0, size: 10, status: '1', category: '0', gender: '1', profession: '2' }) as unknown as Promise<unknown>,
    ).resolves.toMatchObject({
      url: '/admin/work/list',
      params: { page: 0, size: 10, status: '1', category: '0', gender: '1', profession: '2' },
    })
    expect(getMock).toHaveBeenNthCalledWith(1, '/admin/work/list', {
      page: 0,
      size: 10,
      status: '1',
      category: '0',
      gender: '1',
      profession: '2',
    })

    await expect(api.getWorkDetail(12) as unknown as Promise<unknown>).resolves.toMatchObject({
      url: '/admin/work/detail',
      params: { id: 12 },
    })
    expect(getMock).toHaveBeenNthCalledWith(2, '/admin/work/detail', { id: 12 })

    await expect(api.auditWork(12, '2', 'ok') as unknown as Promise<unknown>).resolves.toMatchObject({
      url: '/admin/work/audit',
      data: undefined,
      config: { params: { id: 12, flag: '2', reason: 'ok' } },
    })
    expect(postMock).toHaveBeenNthCalledWith(1, '/admin/work/audit', undefined, {
      params: { id: 12, flag: '2', reason: 'ok' },
    })

    await expect(api.offlineWork(12, '违规') as unknown as Promise<unknown>).resolves.toMatchObject({
      url: '/admin/work/offline',
      data: undefined,
      config: { params: { id: 12, reason: '违规' } },
    })
    expect(postMock).toHaveBeenNthCalledWith(2, '/admin/work/offline', undefined, {
      params: { id: 12, reason: '违规' },
    })
  })
})
