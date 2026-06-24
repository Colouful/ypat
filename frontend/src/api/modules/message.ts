import { get } from '../request'
import type { ApiResult, MessInfo } from '../types'

/**
 * 获取消息详情
 */
export function getMessageDetail(id: number, userid: number): Promise<ApiResult<MessInfo>> {
  return get('/mess/get', { id, userid })
}

/**
 * 获取收到的未读消息数量
 */
export function getRecUnreadCount(type: number, userid: number): Promise<ApiResult<number>> {
  return get('/my/ypat/rec/unread/count', { type, userid })
}

/**
 * 获取发送的未读消息数量
 */
export function getSendUnreadCount(type: number, userid: number): Promise<ApiResult<number>> {
  return get('/my/ypat/send/unread/count', { type, userid })
}
