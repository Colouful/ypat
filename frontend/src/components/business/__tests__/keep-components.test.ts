// @vitest-environment jsdom
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import KeepFilterSheet from '../KeepFilterSheet.vue'
import KeepIcon from '../KeepIcon.vue'
import KeepState from '../KeepState.vue'
import KeepYpatCard from '../KeepYpatCard.vue'

describe('Keep components', () => {
  it('renders ypat card trust states and member badge label from memberLevel', () => {
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
          memberLevel: 'PLUS',
        },
      },
    })

    expect(wrapper.text()).toContain('寻找气质女模')
    expect(wrapper.text()).toContain('约模特')
    expect(wrapper.text()).toContain('已认证')
    expect(wrapper.text()).toContain('已缴担保金')
    expect(wrapper.text()).toContain('VIP+')

    expect(wrapper.findAll('.keep-ypat-card__label-text')).toHaveLength(4)
    expect(wrapper.find('.keep-ypat-card__badge--real').exists()).toBe(true)
    expect(wrapper.find('.keep-ypat-card__badge--credit').exists()).toBe(true)
    expect(wrapper.find('.keep-ypat-card__body + .keep-ypat-card__tags').exists()).toBe(true)

    const memberBadge = wrapper.find('.keep-ypat-card__member')
    expect(memberBadge.exists()).toBe(true)
    expect(memberBadge.text()).toContain('VIP+')
    expect(memberBadge.findComponent(KeepIcon).props('name')).toBe('gem')
  })

  it('hides negative ypat card trust states without member badge', () => {
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

    expect(wrapper.text()).not.toContain('未认证')
    expect(wrapper.text()).not.toContain('未缴担保金')
    expect(wrapper.find('.keep-ypat-card__tags').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('VIP')
    expect(wrapper.find('.keep-ypat-card__member').exists()).toBe(false)
    expect(wrapper.findAll('.keep-ypat-card__badge--muted')).toHaveLength(0)
  })

  it('keeps label structure and member badge visible with long text content', () => {
    const longTargetLabel = '约超长超长超长超长超长超长超长超长标签'
    const longChargeLabel = '收费说明超长超长超长超长超长超长超长'
    const longName = '这是一个很长很长很长很长很长很长的会员昵称展示'

    const wrapper = mount(KeepYpatCard, {
      props: {
        item: {
          id: 3,
          title: '长文本布局保护用例',
          targetLabel: longTargetLabel,
          chargeLabel: longChargeLabel,
          city: '北京',
          name: longName,
          image: '/static/default-cover.png',
          avatar: '/static/default-avatar.png',
          time: '1小时前',
          applyCount: 8,
          realname: true,
          credit: false,
          memberActive: true,
          memberLevel: 'PRO',
        },
      },
    })

    const nameRow = wrapper.find('.keep-ypat-card__name-row')
    const memberBadge = nameRow.find('.keep-ypat-card__member')
    const targetLabel = wrapper.find('.keep-ypat-card__tag--main .keep-ypat-card__label-text')
    const chargeLabel = wrapper.find('.keep-ypat-card__tag--way .keep-ypat-card__label-text')

    expect(wrapper.findAll('.keep-ypat-card__label-text')).toHaveLength(3)
    expect(targetLabel.exists()).toBe(true)
    expect(targetLabel.text()).toBe(longTargetLabel)
    expect(chargeLabel.exists()).toBe(true)
    expect(chargeLabel.text()).toBe(longChargeLabel)
    expect(nameRow.find('.keep-ypat-card__name').exists()).toBe(true)
    expect(nameRow.find('.keep-ypat-card__name').text()).toContain(longName)
    expect(memberBadge.exists()).toBe(true)
    expect(memberBadge.text()).toContain('PRO')
    expect(memberBadge.findComponent(KeepIcon).props('name')).toBe('gem')
  })

  it('falls back to VIP badge when memberLevel is empty or unknown', () => {
    const baseItem = {
      id: 4,
      title: '会员徽标默认文案',
      targetLabel: '约造型师',
      chargeLabel: '费用自理',
      city: '成都',
      name: '默认会员',
      image: '/static/default-cover.png',
      avatar: '/static/default-avatar.png',
      time: '2小时前',
      applyCount: 5,
      realname: true,
      credit: true,
      memberActive: true,
    }

    const emptyLevelWrapper = mount(KeepYpatCard, {
      props: {
        item: {
          ...baseItem,
          memberLevel: '',
        },
      },
    })

    const unknownLevelWrapper = mount(KeepYpatCard, {
      props: {
        item: {
          ...baseItem,
          id: 5,
          memberLevel: 'gold',
        },
      },
    })

    expect(emptyLevelWrapper.find('.keep-ypat-card__member').exists()).toBe(true)
    expect(emptyLevelWrapper.find('.keep-ypat-card__member').text()).toContain('VIP')
    expect(unknownLevelWrapper.find('.keep-ypat-card__member').exists()).toBe(true)
    expect(unknownLevelWrapper.find('.keep-ypat-card__member').text()).toContain('VIP')
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
