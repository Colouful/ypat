import type { MessInfo } from '@/api/types'

export type MessageTab = 'received' | 'sent'

export type MessageNavigationResult =
  | { type: 'navigate'; url: string }
  | { type: 'toast'; message: string }

export interface ResolveMessageNavigationInput {
  tab: MessageTab
  item: MessInfo
  getMessageDetail: (messageId: number) => Promise<MessInfo>
}

function detailUrl(ypatid: number): MessageNavigationResult {
  return { type: 'navigate', url: `/pages-sub/ypat/detail?id=${ypatid}` }
}

export async function resolveMessageNavigation(
  input: ResolveMessageNavigationInput,
): Promise<MessageNavigationResult> {
  const { tab, item, getMessageDetail } = input
  if (tab === 'received') {
    return { type: 'navigate', url: `/pages-sub/content/message-detail?id=${item.id}` }
  }

  if (item.ypatid) return detailUrl(item.ypatid)

  try {
    const message = await getMessageDetail(item.id)
    if (message.ypatid) return detailUrl(message.ypatid)
    return { type: 'toast', message: '关联约拍不存在或已下架' }
  } catch (error) {
    return {
      type: 'toast',
      message: error instanceof Error ? error.message : '消息加载失败',
    }
  }
}
