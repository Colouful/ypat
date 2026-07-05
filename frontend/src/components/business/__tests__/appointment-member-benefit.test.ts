// @vitest-environment jsdom
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AppointmentPublishForm from '../AppointmentPublishForm.vue'

vi.mock('@/api/modules/dict', () => ({
  getWorkTags: vi.fn(() => Promise.resolve({ success: true, data: [] })),
}))

vi.mock('@/stores/member', () => ({
  useMemberStore: () => ({
    submitYpatQuote: {
      scene: 'SUBMIT_YPAT',
      memberActive: true,
      levelCode: 'BASIC',
      originalPpd: 5,
      discountPpd: 2,
      actualPpd: 3,
      ruleEffective: true,
    },
    refreshSubmitYpatQuote: vi.fn(() => Promise.resolve()),
  }),
}))

describe('AppointmentPublishForm member benefit', () => {
  it('shows member discount when quote is active', () => {
    const wrapper = mount(AppointmentPublishForm, {
      props: { target: '0' },
      global: {
        stubs: {
          MediaUploader: { template: '<view />' },
          TagSelector: { template: '<view />' },
          KeepIcon: { template: '<text />' },
          picker: { template: '<view><slot /></view>' },
        },
      },
    })

    expect(wrapper.text()).toContain('原价：5 拍拍豆')
    expect(wrapper.text()).toContain('BASIC 会员优惠：-2 拍拍豆')
    expect(wrapper.text()).toContain('本次实扣：3 拍拍豆')
  })
})
