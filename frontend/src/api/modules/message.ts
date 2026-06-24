import { get } from '../request'
import type { ApiResult, MessInfo } from '../types'

export function getMessageDetail(id: number, userid: number): Promise<ApiResult<MessInfo>> {
  return get('/mess/get', { id, userid })
}

export function getRecUnreadCount(type: string, userid: number): Promise<ApiResult<number>> {
  return get('/my/ypat/rec/unread/count', { type, userid })
}

export function getSendUnreadCount(type: string, userid: number): Promise<ApiResult<number>> {
  return get('/my/ypat/send/unread/count', { type, userid })
}
