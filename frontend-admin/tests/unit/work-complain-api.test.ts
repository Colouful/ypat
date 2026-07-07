import { beforeEach, describe, expect, it, vi } from 'vitest'

const getMock = vi.fn((url: string, params?: Record<string, unknown>) => Promise.resolve({ url, params }))
const postMock = vi.fn((url: string, data?: unknown, config?: unknown) => Promise.resolve({ url, data, config }))

vi.mock('@/api/request', () => ({
  get: getMock,
  post: postMock,
}))

describe('后台作品投诉 API', () => {
  beforeEach(() => {
    getMock.mockClear()
    postMock.mockClear()
  })

  it('应请求投诉列表与详情', async () => {
    const api = await import('@/api/modules/work-complain')

    await expect(
      api.getWorkComplainList({ page: 0, size: 10, status: '0', workId: '88', userId: '66' }) as unknown as Promise<unknown>,
    ).resolves.toMatchObject({
      url: '/admin/work/complain/list',
      params: { page: 0, size: 10, status: '0', workId: '88', userId: '66' },
    })
    expect(getMock).toHaveBeenNthCalledWith(1, '/admin/work/complain/list', {
      page: 0,
      size: 10,
      status: '0',
      workId: '88',
      userId: '66',
    })

    await expect(api.getWorkComplainDetail(12) as unknown as Promise<unknown>).resolves.toMatchObject({
      url: '/admin/work/complain/detail',
      params: { id: 12 },
    })
    expect(getMock).toHaveBeenNthCalledWith(2, '/admin/work/complain/detail', { id: 12 })
  })

  it('应提交投诉处理参数', async () => {
    const api = await import('@/api/modules/work-complain')

    await expect(api.handleWorkComplain(12, '1', '证据属实', true) as unknown as Promise<unknown>).resolves.toMatchObject({
      url: '/admin/work/complain/handle',
      data: undefined,
      config: { params: { id: 12, status: '1', reason: '证据属实', offlineWork: true } },
    })
    expect(postMock).toHaveBeenNthCalledWith(1, '/admin/work/complain/handle', undefined, {
      params: { id: 12, status: '1', reason: '证据属实', offlineWork: true },
    })
  })
})
