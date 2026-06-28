import { describe, expect, it, vi } from 'vitest'
import { resolveMessageNavigation } from '../message-navigation'
import type { MessInfo } from '@/api/types'

describe('resolveMessageNavigation', () => {
  it('opens received items by message id', async () => {
    const result = await resolveMessageNavigation({
      tab: 'received',
      item: { id: 10, ypatid: 99, sendperid: 1, recperid: 2 },
      getMessageDetail: vi.fn(),
    })

    expect(result).toEqual({ type: 'navigate', url: '/pages-sub/content/message-detail?id=10' })
  })

  it('opens sent items by ypatid, not message id', async () => {
    const result = await resolveMessageNavigation({
      tab: 'sent',
      item: { id: 10, ypatid: 99, sendperid: 1, recperid: 2 },
      getMessageDetail: vi.fn(),
    })

    expect(result).toEqual({ type: 'navigate', url: '/pages-sub/ypat/detail?id=99' })
  })

  it('looks up message detail when sent item misses ypatid', async () => {
    const getMessageDetail = vi.fn<[number], Promise<MessInfo>>()
      .mockResolvedValue({ id: 10, ypatid: 88, sendperid: 1, recperid: 2 })

    const result = await resolveMessageNavigation({
      tab: 'sent',
      item: { id: 10, sendperid: 1, recperid: 2 },
      getMessageDetail,
    })

    expect(getMessageDetail).toHaveBeenCalledWith(10)
    expect(result).toEqual({ type: 'navigate', url: '/pages-sub/ypat/detail?id=88' })
  })

  it('does not fall back to message id when ypatid is still missing', async () => {
    const result = await resolveMessageNavigation({
      tab: 'sent',
      item: { id: 10, sendperid: 1, recperid: 2 },
      getMessageDetail: vi.fn().mockResolvedValue({ id: 10, sendperid: 1, recperid: 2 }),
    })

    expect(result).toEqual({ type: 'toast', message: '关联约拍不存在或已下架' })
  })

  it('shows load failure without navigating when detail lookup fails', async () => {
    const result = await resolveMessageNavigation({
      tab: 'sent',
      item: { id: 10, sendperid: 1, recperid: 2 },
      getMessageDetail: vi.fn().mockRejectedValue(new Error('消息加载失败')),
    })

    expect(result).toEqual({ type: 'toast', message: '消息加载失败' })
  })
})
