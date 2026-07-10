/**
 * 后台作品管理 API
 */

import { get, post } from '../request'
import type { ApiResult, PageQuery, PageResult } from '../types'

export interface WorkAdminInfo {
  id: number
  description: string
  coverUrl?: string
  mediaType: string
  mediaTypeTxt?: string
  status: string
  statusTxt?: string
  auditReason?: string
  readCount?: number
  likeCount?: number
  favoriteCount?: number
  device?: string
  shootLocation?: string
  returnPhotoFlag?: string | number
  publishTime?: string
  userId?: number
  nickname?: string
  mobile?: string
  gender?: string
  profession?: string
  city?: string
  area?: string
  dataFlag?: string
  internalBatchNo?: string
  tags?: string[]
  medias?: Array<{ id: number; type: string; url: string }>
  user?: Record<string, unknown>
}

export interface WorkListQuery extends PageQuery {
  status?: string
  nickname?: string
  mobile?: string
  city?: string
  mediaType?: string
  tagIds?: string
  dataFlag?: string
}

export function getWorkList(params: WorkListQuery): Promise<ApiResult<PageResult<WorkAdminInfo>>> {
  return get<PageResult<WorkAdminInfo>>('/admin/work/list', params as Record<string, unknown>)
}

export function getWorkDetail(id: number): Promise<ApiResult<WorkAdminInfo>> {
  return get<WorkAdminInfo>('/admin/work/detail', { id })
}

export function auditWork(id: number, flag: string, reason?: string): Promise<ApiResult<unknown>> {
  return post('/admin/work/audit', undefined, { params: { id, flag, reason } })
}

export function offlineWork(id: number, reason?: string): Promise<ApiResult<unknown>> {
  return post('/admin/work/offline', undefined, { params: { id, reason } })
}
