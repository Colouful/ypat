import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import UserAuditDialog from '../UserAuditDialog.vue'

const { getUserDetailMock } = vi.hoisted(() => ({
  getUserDetailMock: vi.fn(),
}))

vi.mock('@/api/modules/user', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/modules/user')>()
  return {
    ...actual,
    getUserDetail: getUserDetailMock,
  }
})

describe('UserAuditDialog', () => {
  beforeEach(() => {
    getUserDetailMock.mockResolvedValue({
      data: {
        userid: 12,
        name: '张三',
        certcode: '330100199001011234',
        status: '1',
        pics: ['front.jpg', 'back.jpg', 'hand.jpg'],
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
    expect(text).toContain('身份证正面')
    expect(text).toContain('身份证反面')
    expect(text).toContain('手持身份证')
    expect(text).toContain('审核通过')
    expect(text).toContain('审核不通过')
  })
})
