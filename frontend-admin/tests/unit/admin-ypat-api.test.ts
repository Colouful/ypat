import { beforeEach, describe, expect, it, vi } from 'vitest'

const getMock = vi.fn((url: string, params?: Record<string, unknown>) => Promise.resolve({ url, params }))
const postMock = vi.fn((url: string, data?: unknown, config?: unknown) => Promise.resolve({ url, data, config }))

vi.mock('@/api/request', () => ({
  get: getMock,
  post: postMock,
}))

describe('后台约拍 API', () => {
  beforeEach(() => {
    getMock.mockClear()
    postMock.mockClear()
  })

  it('代发约拍应使用 multipart 表单提交文件和字段', async () => {
    const api = await import('@/api/modules/ypat')
    const avatar = new File(['avatar'], 'avatar.jpg', { type: 'image/jpeg' })
    const workImage = new File(['work'], 'work.jpg', { type: 'image/jpeg' })

    await api.submitYpat(
      {
        describ: '约拍描述',
        target: '0',
        patdate: '2026-07-03',
        chargeway: '1',
        province: '上海',
        city: '上海',
        area: '徐汇',
        patstyle: '1,2',
        nickname: '摄影师',
        gender: '1',
        profess: '2',
      },
      avatar,
      [workImage],
    )

    expect(postMock).toHaveBeenCalledTimes(1)
    const [, data, config] = postMock.mock.calls[0]
    expect(data).toBeInstanceOf(FormData)
    const formData = data as FormData
    expect(formData.get('describ')).toBe('约拍描述')
    expect(formData.get('target')).toBe('0')
    expect(formData.get('patstyle')).toBe('1,2')
    expect(formData.get('file')).toBe(avatar)
    expect(formData.getAll('files')).toEqual([workImage])
    expect(config).toMatchObject({
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  })
})
