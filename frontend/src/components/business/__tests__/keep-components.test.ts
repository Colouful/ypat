// @vitest-environment jsdom
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import KeepFilterSheet from '../KeepFilterSheet.vue'
import KeepState from '../KeepState.vue'
import KeepYpatCard from '../KeepYpatCard.vue'

describe('Keep components', () => {
  it('renders ypat card title and trust badges', () => {
    const wrapper = mount(KeepYpatCard, {
      props: {
        item: {
          id: 1,
          title: '寻找气质女模拍一组复古港风样片',
          targetLabel: '约模特',
          chargeLabel: '希望互勉',
          city: '上海·徐汇',
          name: '陈默 Mo',
          image: '/static/default-cover.png',
          avatar: '/static/default-avatar.png',
          time: '12分钟前',
          applyCount: 36,
          realname: true,
          credit: true,
        },
      },
    })

    expect(wrapper.text()).toContain('寻找气质女模')
    expect(wrapper.text()).toContain('约模特')
    expect(wrapper.text()).toContain('实名认证')
    expect(wrapper.text()).toContain('信用担保')
  })

  it('renders state action button and emits action', async () => {
    const wrapper = mount(KeepState, {
      props: {
        type: 'login',
        title: '登录后查看',
        buttonText: '去登录',
      },
    })

    expect(wrapper.text()).toContain('登录后查看')
    await wrapper.find('button').trigger('tap')
    expect(wrapper.emitted('action')).toHaveLength(1)
  })

  it('confirms selected filter values', async () => {
    const wrapper = mount(KeepFilterSheet, {
      props: {
        visible: true,
        modelValue: { target: ['all'] },
        groups: [{
          key: 'target',
          title: '我想找',
          multiple: false,
          options: [
            { label: '全部', value: 'all' },
            { label: '约摄影师', value: 'photographer' },
          ],
        }],
      },
    })

    await wrapper.findAll('.keep-filter__option')[1].trigger('tap')
    await wrapper.find('.keep-filter__confirm').trigger('tap')
    expect(wrapper.emitted('confirm')?.[0]).toEqual([{ target: ['photographer'] }])
  })
})
