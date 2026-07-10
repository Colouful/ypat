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
})
