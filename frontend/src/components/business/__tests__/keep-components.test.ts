// @vitest-environment jsdom
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import KeepFilterSheet from '../KeepFilterSheet.vue'
import KeepIcon from '../KeepIcon.vue'
import KeepState from '../KeepState.vue'
import KeepYpatCard from '../KeepYpatCard.vue'

describe('Keep components', () => {
  it('renders ypat card trust states and member badge', () => {
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
          memberActive: true,
          memberLevel: 'BASIC',
        },
      },
    })

    expect(wrapper.text()).toContain('寻找气质女模')
    expect(wrapper.text()).toContain('约模特')
    expect(wrapper.text()).toContain('已认证')
    expect(wrapper.text()).toContain('已缴担保金')
    expect(wrapper.text()).toContain('VIP')

    expect(wrapper.findAll('.keep-ypat-card__label-text')).toHaveLength(4)

    const memberBadge = wrapper.find('.keep-ypat-card__member')
    expect(memberBadge.exists()).toBe(true)
    expect(memberBadge.findComponent(KeepIcon).props('name')).toBe('gem')
  })

  it('renders negative ypat card trust states without member badge', () => {
    const wrapper = mount(KeepYpatCard, {
      props: {
        item: {
          id: 2,
          title: '周末约一组城市街拍',
          targetLabel: '约摄影师',
          chargeLabel: '费用协商',
          city: '杭州',
          name: '匿名用户',
          image: '/static/default-cover.png',
          avatar: '/static/default-avatar.png',
          time: '刚刚',
          applyCount: 0,
          realname: false,
          credit: false,
          memberActive: false,
        },
      },
    })

    expect(wrapper.text()).toContain('未认证')
    expect(wrapper.text()).toContain('未缴担保金')
    expect(wrapper.text()).not.toContain('VIP')
    expect(wrapper.find('.keep-ypat-card__member').exists()).toBe(false)
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
