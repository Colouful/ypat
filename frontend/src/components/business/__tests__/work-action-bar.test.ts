// @vitest-environment jsdom
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import WorkActionBar from '../WorkActionBar.vue'
import type { WorkDetail } from '@/api/types/work'

function createWork(overrides: Partial<WorkDetail> = {}): WorkDetail {
  return {
    id: 1,
    description: '测试作品',
    mediaType: '1',
    readCount: 0,
    likeCount: 0,
    favoriteCount: 0,
    medias: [],
    tags: [],
    user: { id: 2 },
    isLiked: false,
    isFavorited: false,
    isApplied: false,
    isOwner: false,
    ...overrides,
  }
}

describe('WorkActionBar', () => {
  it('renders action labels under the three icons', () => {
    const wrapper = mount(WorkActionBar, {
      props: {
        work: createWork(),
      },
    })

    expect(wrapper.text()).toContain('点赞')
    expect(wrapper.text()).toContain('收藏')
    expect(wrapper.text()).toContain('分享')
    expect(wrapper.findAll('.work-action-bar__label')).toHaveLength(3)
  })

  it('uses selected labels and active state after like and favorite', () => {
    const wrapper = mount(WorkActionBar, {
      props: {
        work: createWork({ isLiked: true, isFavorited: true }),
      },
    })

    expect(wrapper.text()).toContain('已赞')
    expect(wrapper.text()).toContain('已收藏')
    expect(wrapper.findAll('.work-action-bar__btn--active')).toHaveLength(2)
  })

  it('emits the original action events from labelled buttons', async () => {
    const wrapper = mount(WorkActionBar, {
      props: {
        work: createWork(),
      },
    })

    const buttons = wrapper.findAll('.work-action-bar__btn')
    await buttons[0].trigger('tap')

    expect(wrapper.emitted('like')).toHaveLength(1)
  })

  it('emits apply only when the work has not been applied to', async () => {
    const wrapper = mount(WorkActionBar, {
      props: { work: createWork() },
    })

    await wrapper.find('.work-action-bar__primary').trigger('tap')

    expect(wrapper.emitted('apply')).toHaveLength(1)
  })

  it('shows a disabled applied state without emitting apply', async () => {
    const wrapper = mount(WorkActionBar, {
      props: { work: createWork({ isApplied: true }) },
    })

    const primary = wrapper.find('.work-action-bar__primary')
    expect(primary.text()).toBe('已约拍')
    expect(primary.classes()).toContain('work-action-bar__primary--disabled')
    await primary.trigger('tap')
    expect(wrapper.emitted('apply')).toBeUndefined()
  })
})
