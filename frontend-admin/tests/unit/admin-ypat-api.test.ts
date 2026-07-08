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

  it('列表数据应从 userQo 补齐用户展示字段', async () => {
    const api = await import('@/api/modules/ypat')

    const page = api.normalizeYpatListPage({
      content: [
        {
          id: 2,
          describ: '测试约拍',
          nickname: '',
          mobile: '',
          gender: '',
          genderTxt: '',
          profess: '',
          professTxt: '',
          target: '0',
          targetTxt: '约摄影师',
          city: '上海',
          pubdate: '2026-07-07',
          status: '1',
          statusTxt: '待审核',
          recomflag: '0',
          reason: '',
          pics: [],
          userQo: {
            id: 8,
            nickname: '内测摄影师',
            gender: '1',
            genderTxt: '男',
            profess: '0',
            professTxt: '摄影师',
            mobile: '13800000000',
            openid: 'openid',
          },
        },
      ],
      totalElements: 1,
    })

    expect(page.content[0]).toMatchObject({
      nickname: '内测摄影师',
      mobile: '13800000000',
      gender: '1',
      genderTxt: '男',
      profess: '0',
      professTxt: '摄影师',
    })
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
        workId: '88',
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
    expect(formData.get('workId')).toBe('88')
    expect(formData.get('patstyle')).toBe('1,2')
    expect(formData.get('file')).toBe(avatar)
    expect(formData.getAll('files')).toEqual([workImage])
    expect(config).toBeUndefined()
  })
})
