import { describe, expect, it, vi } from 'vitest'

vi.mock('@/api/request', () => ({
  get: vi.fn((url: string, params?: Record<string, unknown>) => Promise.resolve({ url, params })),
  post: vi.fn((url: string, data?: unknown, config?: unknown) => Promise.resolve({ url, data, config })),
}))

describe('后台作品 API', () => {
  it('应使用后台作品路由', async () => {
    const api = await import('@/api/modules/work')

    await expect(api.getWorkList({ page: 0, size: 10, status: '1' }) as unknown as Promise<unknown>).resolves.toMatchObject({
      url: '/admin/work/list',
    })
    await expect(api.getWorkDetail(12) as unknown as Promise<unknown>).resolves.toMatchObject({
      url: '/admin/work/detail',
      params: { id: 12 },
    })
    await expect(api.auditWork(12, '2', 'ok') as unknown as Promise<unknown>).resolves.toMatchObject({
      url: '/admin/work/audit',
    })
    await expect(api.offlineWork(12, '违规') as unknown as Promise<unknown>).resolves.toMatchObject({
      url: '/admin/work/offline',
    })
  })
})
