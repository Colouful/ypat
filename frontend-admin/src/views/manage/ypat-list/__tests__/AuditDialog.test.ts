import { flushPromises, mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { YpatInfo } from '@/api/modules/ypat'
import AuditDialog from '../AuditDialog.vue'

const { getYpatDetailMock, auditYpatMock } = vi.hoisted(() => ({
  getYpatDetailMock: vi.fn(),
  auditYpatMock: vi.fn(),
}))

vi.mock('@/api/modules/ypat', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/modules/ypat')>()
  return {
    ...actual,
    getYpatDetail: getYpatDetailMock,
    auditYpat: auditYpatMock,
  }
})

const listData: YpatInfo = {
  id: 21,
  describ: '列表描述',
  nickname: '测试用户',
  mobile: '13800138000',
  gender: '1',
  genderTxt: '男',
  profess: '0',
  professTxt: '摄影师',
  target: '1',
  targetTxt: '约模特',
  city: '杭州',
  pubdate: '2026-07-12 12:00:00',
  status: '1',
  statusTxt: '待审核',
  recomflag: '0',
  reason: '',
  pics: ['list.jpg'],
}

function mountDialog() {
  return mount(AuditDialog, {
    props: { visible: false, data: listData },
    global: { plugins: [ElementPlus] },
  })
}

describe('Ypat AuditDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getYpatDetailMock.mockResolvedValue({
      data: {
        ...listData,
        describ: '完整详情描述',
        pics: ['detail-a.jpg', 'detail-b.jpg'],
      },
    })
  })

  it('loads complete details and prefers detail images', async () => {
    const wrapper = mountDialog()

    await wrapper.setProps({ visible: true })
    await flushPromises()

    expect(getYpatDetailMock).toHaveBeenCalledWith(21)
    expect(wrapper.text()).toContain('完整详情描述')
    expect(wrapper.findAllComponents({ name: 'ElImage' })).toHaveLength(2)
  })

  it('falls back to list data when detail loading fails', async () => {
    getYpatDetailMock.mockRejectedValueOnce(new Error('load failed'))
    const wrapper = mountDialog()

    await wrapper.setProps({ visible: true })
    await flushPromises()

    expect(getYpatDetailMock).toHaveBeenCalledWith(21)
    expect(wrapper.text()).toContain('列表描述')
    expect(wrapper.findAllComponents({ name: 'ElImage' })).toHaveLength(1)
  })
})
