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

    await api.getInternalUsers({ page: 0, size: 10, batchNo: 'IT202607060001', city: '上海' })
    expect(getMock).toHaveBeenCalledWith('/admin/internal-test/users', {
      page: 0,
      size: 10,
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

    await api.getInternalBatches({ page: 0, size: 10, batchNo: 'IT202607060001' })
    expect(getMock).toHaveBeenCalledWith('/admin/internal-test/batches', {
      page: 0,
      size: 10,
      batchNo: 'IT202607060001',
    })

    await api.getInternalBatches({ page: 0, size: 20, batchNo: '   ' })
    expect(getMock).toHaveBeenCalledWith('/admin/internal-test/batches', {
      page: 0,
      size: 20,
      batchNo: undefined,
    })

    await api.cleanupInternalData({ batchNo: 'IT202607060001' })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/cleanup', { batchNo: 'IT202607060001' })

    await api.cleanupInternalData({ cleanupAll: true })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/cleanup', { cleanupAll: true })
  })

  it('应使用内测工作台懒人版路由', async () => {
    const api = await import('@/api/modules/internal-test')

    await api.batchCreateInternalResources({ mediaType: 'image', usageType: 'work', urls: ['https://example.com/1.jpg'] })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/resources/batch', {
      mediaType: 'image',
      usageType: 'work',
      urls: ['https://example.com/1.jpg'],
    })

    await api.getInternalResourceGroups({ page: 0, size: 10, usageType: 'work', usedFlag: 0 })
    expect(getMock).toHaveBeenCalledWith('/admin/internal-test/resource-groups', {
      page: 0,
      size: 10,
      usageType: 'work',
      usedFlag: 0,
    })

    await api.generateInternalUsers({ actionType: 'create_users', userCount: 2 })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/generate/users', { actionType: 'create_users', userCount: 2 })

    await api.generateInternalWorks({ actionType: 'create_works', userId: 1, groupNos: ['ITG1'] })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/generate/works', { actionType: 'create_works', userId: 1, groupNos: ['ITG1'] })

    await api.generateInternalYpats({
      actionType: 'create_ypats',
      userId: 1,
      wx: 'wx-test',
      mobile: '13800138000',
      ypatResourceIds: [101, 102],
    })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/generate/ypats', {
      actionType: 'create_ypats',
      userId: 1,
      wx: 'wx-test',
      mobile: '13800138000',
      ypatResourceIds: [101, 102],
    })

    await api.searchInternalUsers({ keyword: '内测', page: 0, size: 20 })
    expect(getMock).toHaveBeenCalledWith('/admin/internal-test/users/search', { keyword: '内测', page: 0, size: 20 })

    await api.grantInternalUserMember(1, { days: 365, reason: '内测数据一键会员' })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/users/1/grant-member', { days: 365, reason: '内测数据一键会员' })

    await api.verifyInternalUser(1, { reason: '内测数据一键认证' })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/users/1/verify', { reason: '内测数据一键认证' })

    await api.markInternalUserDepositPaid(1, { reason: '内测数据一键保证金' })
    expect(postMock).toHaveBeenCalledWith('/admin/internal-test/users/1/deposit-paid', { reason: '内测数据一键保证金' })
  })
})
