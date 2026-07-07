/**
 * 后台作品投诉治理 API
 */

import { get, post } from '../request'
import type { ApiResult, PageQuery, PageResult } from '../types'

export interface WorkComplainInfo {
  id: number
  workId?: number | string
  userId?: number | string
  reason?: string
  contact?: string
  status?: number | string
  statusText?: string
  createdAt?: string
  workDescription?: string
  targetUserId?: number | string
  targetNickname?: string
  userNickname?: string
  content?: string
  pics?: string[] | string
  handleReason?: string
}

export interface WorkComplainListQuery extends PageQuery {
  status?: string
  workId?: string
  userId?: string
}

export function getWorkComplainList(
  params: WorkComplainListQuery,
): Promise<ApiResult<PageResult<WorkComplainInfo>>> {
  return get<PageResult<WorkComplainInfo>>(
    '/admin/work/complain/list',
    params as Record<string, unknown>,
  )
}

export function getWorkComplainDetail(id: number): Promise<ApiResult<WorkComplainInfo>> {
  return get<WorkComplainInfo>('/admin/work/complain/detail', { id })
}

export function handleWorkComplain(
  id: number,
  status: number | string,
  reason?: string,
  offlineWork = false,
): Promise<ApiResult<unknown>> {
  return post('/admin/work/complain/handle', undefined, {
    params: { id, status, reason, offlineWork },
  })
}
