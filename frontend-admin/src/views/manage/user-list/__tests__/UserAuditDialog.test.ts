import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import UserAuditDialog from '../UserAuditDialog.vue'

const { getUserDetailMock, getPaymentOrdersMock } = vi.hoisted(() => ({
  getUserDetailMock: vi.fn(),
  getPaymentOrdersMock: vi.fn(),
}))

vi.mock('@/api/modules/user', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/modules/user')>()
  return {
    ...actual,
    getUserDetail: getUserDetailMock,
  }
})

vi.mock('@/api/modules/payment', () => ({
  getPaymentOrders: getPaymentOrdersMock,
}))

describe('UserAuditDialog', () => {
  const source = readFileSync(resolve(__dirname, '../UserAuditDialog.vue'), 'utf-8')

  beforeEach(() => {
    vi.clearAllMocks()
    getUserDetailMock.mockResolvedValue({
      data: {
        userid: 12,
        name: '张三',
        certcode: '330100199001011234',
        status: '1',
        pics: ['front.jpg', 'back.jpg', 'hand.jpg'],
      },
    })
    getPaymentOrdersMock.mockResolvedValue({
      data: {
        content: [{
          id: 1,
          paymentNo: 'P001',
          outTradeNo: 'R001',
          businessOrderNo: 'R001',
          businessType: 'REALNAME',
          userId: 12,
          channel: 'MINIAPP',
          amountFen: 2900,
          status: 'PAID',
          createdAt: '2026-07-12 10:00:00',
          paidAt: '2026-07-12 10:01:00',
        }],
        totalElements: 1,
      },
    })
  })

  it('展示三张实名审核照片的语义标签和待审核操作按钮', async () => {
    const wrapper = mount(UserAuditDialog, {
      props: {
        visible: false,
        user: {
          userid: 12,
          status: '1',
        },
        loading: false,
      },
      global: {
        plugins: [ElementPlus],
        stubs: {
          Loading: true,
          Picture: true,
        },
      },
    })

    await wrapper.setProps({ visible: true })
    await flushPromises()

    const text = wrapper.text()
    const items = wrapper.findAll('.image-item')
    const labels = wrapper.findAll('.image-label')
    const images = wrapper.findAllComponents({ name: 'ElImage' })

    expect(text).toContain('身份证正面')
    expect(text).toContain('身份证反面')
    expect(text).toContain('手持身份证')
    expect(items.map((item) => item.attributes('aria-label'))).toEqual([
      '身份证正面',
      '身份证反面',
      '手持身份证',
    ])
    expect(labels).toHaveLength(3)
    expect(images).toHaveLength(3)
    images.forEach((image, index) => {
      expect(image.props('previewSrcList')).toEqual(['front.jpg', 'back.jpg', 'hand.jpg'])
      expect(image.props('initialIndex')).toBe(index)
    })
    expect(text).toContain('审核通过')
    expect(text).toContain('审核不通过')
    expect(getPaymentOrdersMock).toHaveBeenCalledWith({ userId: 12, page: 0, size: 10 })
    expect(text).toContain('充值订单')
    expect(text).toContain('实名认证(REALNAME)')
    expect(text).toContain('¥29.00')
  })

  it('已审核用户只展示详情，不展示审核操作', async () => {
    getUserDetailMock.mockResolvedValueOnce({
      data: {
        userid: 12,
        name: '张三',
        certcode: '330100199001011234',
        status: '2',
        pics: [],
      },
    })

    const wrapper = mount(UserAuditDialog, {
      props: {
        visible: false,
        user: { userid: 12, status: '2' },
        loading: false,
      },
      global: {
        plugins: [ElementPlus],
        stubs: { Loading: true, Picture: true },
      },
    })

    await wrapper.setProps({ visible: true })
    await flushPromises()

    expect(wrapper.text()).toContain('实名审核详情')
    const actionTexts = wrapper.findAllComponents({ name: 'ElButton' }).map((button) => button.text())
    expect(actionTexts).not.toContain('审核通过')
    expect(actionTexts).not.toContain('审核不通过')
  })

  it('不在图片项外层绑定无效预览点击', () => {
    expect(source).not.toContain('@click="handlePreview(pic)"')
    expect(source).not.toContain('previewVisible')
    expect(source).not.toContain('previewSrc')
    expect(source).not.toContain('function handlePreview')
  })
})
