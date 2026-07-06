import { beforeEach, describe, expect, it, vi } from 'vitest'

const getMock = vi.fn((url: string, params?: Record<string, unknown>) => Promise.resolve({ url, params }))
const postMock = vi.fn((url: string, data?: unknown, config?: unknown) => Promise.resolve({ url, data, config }))

vi.mock('@/api/request', () => ({ get: getMock, post: postMock }))

describe('内测数据 API', () => {
  beforeEach(() => {
    getMock.mockClear()
    postMock.mockClear()
  })

  it('应使用内测资源和生成路由', async () => {
    const api = await import('@/api/modules/internal-test')

    await api.getInternalResources({ page: 0, size: 10, mediaType: 'image', usageType: 'work' })
    expect(getMock).toHaveBeenCalledWith('/admin/internal-test/resources', {
      page: 0,
      size: 10,
      mediaType: 'image',
      usageType: 'work',
    })

    await api.createInternalResource({ mediaType: 'image', usageType: 'avatar', url: 'https://example.com/a.jpg' })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/resources', {
      mediaType: 'image',
      usageType: 'avatar',
      url: 'https://example.com/a.jpg',
    })

    await api.updateInternalResource({ id: 2, mediaType: 'video', usageType: 'work', url: 'https://example.com/a.mp4' })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/resources/update', {
      id: 2,
      mediaType: 'video',
      usageType: 'work',
      url: 'https://example.com/a.mp4',
    })

    await api.updateInternalResourceStatus(2, 'disabled')
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/resources/status', undefined, {
      params: { id: 2, status: 'disabled' },
    })

    await api.getInternalUsers({ batchNo: 'IT202607060001', city: '上海' })
    expect(getMock).toHaveBeenCalledWith('/admin/internal-test/users', {
      batchNo: 'IT202607060001',
      city: '上海',
    })

    await api.createInternalUsers({ mode: 'create_and_generate', userCount: 2 })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/users/create', {
      mode: 'create_and_generate',
      userCount: 2,
    })

    await api.generateInternalData({ mode: 'create_and_generate', userCount: 2, publishStatus: '1' })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/generate', {
      mode: 'create_and_generate',
      userCount: 2,
      publishStatus: '1',
    })

    await api.getInternalBatches({ batchNo: 'IT202607060001' })
    expect(getMock).toHaveBeenCalledWith('/admin/internal-test/batches', {
      batchNo: 'IT202607060001',
    })

    await api.cleanupInternalData({ batchNo: 'IT202607060001' })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/cleanup', { batchNo: 'IT202607060001' })
  })
})
