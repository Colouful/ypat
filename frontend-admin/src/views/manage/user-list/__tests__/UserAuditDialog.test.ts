import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
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
  const source = readFileSync(resolve(__dirname, '../UserAuditDialog.vue'), 'utf-8')

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
  })

  it('不在图片项外层绑定无效预览点击', () => {
    expect(source).not.toContain('@click="handlePreview(pic)"')
    expect(source).not.toContain('previewVisible')
    expect(source).not.toContain('previewSrc')
    expect(source).not.toContain('function handlePreview')
  })
})
