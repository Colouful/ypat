import { get, post } from '../request'
import type { ApiResult, PageQuery, PageResult } from '../types'

export interface FeedbackInfo {
  id: number
  userId?: number | string
  userNickname?: string
  type?: string
  typeText?: string
  content?: string
  contact?: string
  pics?: string[] | string
  status?: number | string
  statusText?: string
  handleReason?: string
  handledBy?: number | string
  handledAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface FeedbackListQuery extends PageQuery {
  status?: string
  type?: string
  userId?: string
}

export function getFeedbackList(params: FeedbackListQuery): Promise<ApiResult<PageResult<FeedbackInfo>>> {
  return get<PageResult<FeedbackInfo>>('/admin/feedback/list', params as Record<string, unknown>)
}

export function getFeedbackDetail(id: number): Promise<ApiResult<FeedbackInfo>> {
  return get<FeedbackInfo>('/admin/feedback/detail', { id })
}

export function handleFeedback(id: number, status: number | string, reason?: string): Promise<ApiResult<unknown>> {
  return post('/admin/feedback/handle', undefined, {
    params: { id, status, reason, handleReason: reason },
  })
}
