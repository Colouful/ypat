import { get } from '../request'
import type { ApiResult, MessagePushLog, MessagePushLogStats, PageQuery, PageResult } from '../types'

export interface MessagePushLogQuery extends PageQuery {
  eventType?: string
  businessType?: string
  success?: string
  messageId?: number
  ypatid?: number
  sendperid?: number
  recperid?: number
  touserOpenid?: string
  dateStart?: string
  dateEnd?: string
}

export function getMessagePushLogs(params: MessagePushLogQuery): Promise<ApiResult<PageResult<MessagePushLog>>> {
  return get<PageResult<MessagePushLog>>('/admin/message-push-log/list', params as Record<string, unknown>)
}

export function getMessagePushLogStats(params: MessagePushLogQuery): Promise<ApiResult<MessagePushLogStats>> {
  return get<MessagePushLogStats>('/admin/message-push-log/stats', params as Record<string, unknown>)
}
