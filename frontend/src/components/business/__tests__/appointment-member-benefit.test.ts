// @vitest-environment jsdom
import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AppointmentPublishForm from '../AppointmentPublishForm.vue'

vi.mock('@/api/modules/dict', () => ({
  getWorkTags: vi.fn(() => Promise.resolve({ success: true, data: [] })),
}))

vi.mock('@/stores/member', () => ({
  useMemberStore: () => ({
    quotes: {
      SUBMIT_YPAT: {
        scene: 'SUBMIT_YPAT',
        sceneName: '发布约拍',
        memberActive: true,
        levelCode: 'BASIC',
        levelName: '基础会员',
        originalPpd: 3,
        discountPpd: 2,
        actualPpd: 1,
        ruleEffective: true,
      },
    },
    refreshBenefitQuote: vi.fn(() =>
      Promise.resolve({
        scene: 'SUBMIT_YPAT',
        sceneName: '发布约拍',
        memberActive: true,
        levelCode: 'BASIC',
        levelName: '基础会员',
        originalPpd: 3,
        discountPpd: 2,
        actualPpd: 1,
        ruleEffective: true,
      }),
    ),
  }),
}))

describe('AppointmentPublishForm member benefit', () => {
  it('shows the member quote in the fixed action bar', async () => {
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
    await flushPromises()

    expect(wrapper.find('.appointment-publish-form__benefit').exists()).toBe(false)
    const actionBar = wrapper.find('.appointment-publish-form__submit')
    expect(actionBar.text()).toContain('本次实扣 1 拍豆')
    expect(actionBar.text()).toContain('原价 3')
    expect(actionBar.text()).toContain('会员优惠 -2')
    expect(actionBar.text()).toContain('发布约拍')
  })
})
