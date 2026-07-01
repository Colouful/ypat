/**
 * 作品 API 模块
 */
import { get, post, put } from '../request-adapter'
import type {
  WorkListParams, WorkListResult, WorkDetail, WorkSubmitParams,
  WorkLikeParams, WorkFavoriteParams, WorkComplainParams, WorkQuickApplyResult,
} from '../types/work'
import type { ApiResult } from '../types'

/** 列表 */
export function getList(params: WorkListParams): Promise<ApiResult<WorkListResult>> {
  return post<WorkListResult>('/work/list', params)
}

/** 详情 */
export function getDetail(id: number): Promise<ApiResult<WorkDetail>> {
  return get<WorkDetail>('/work/get', { id })
}

/** 发布 */
export function submit(data: WorkSubmitParams): Promise<ApiResult<{ id: number }>> {
  return post<{ id: number }>('/work/submit', data)
}

/** 我的作品 */
export function getMyWorks(params: { page?: number; size?: number; status?: string }): Promise<ApiResult<WorkListResult>> {
  return get<WorkListResult>('/work/my', params)
}

/** 下架 */
export function offline(id: number): Promise<ApiResult<{ msg: string }>> {
  return put<{ msg: string }>('/work/offline', { id })
}

/** 点赞 */
export function like(workId: number): Promise<ApiResult<{ msg: string }>> {
  return put<{ msg: string }>('/work/like/add', { workId })
}

/** 取消点赞 */
export function unlike(workId: number): Promise<ApiResult<{ msg: string }>> {
  return put<{ msg: string }>('/work/like/cancel', { workId })
}

/** 收藏 */
export function favorite(workId: number): Promise<ApiResult<{ msg: string }>> {
  return put<{ msg: string }>('/work/sc/add', { workId })
}

/** 取消收藏 */
export function unfavorite(workId: number): Promise<ApiResult<{ msg: string }>> {
  return put<{ msg: string }>('/work/sc/cancel', { workId })
}

/** 投诉 */
export function complain(params: WorkComplainParams): Promise<ApiResult<{ msg: string }>> {
  return post<{ msg: string }>('/work/complain', params)
}

/** 立即约拍：返回 target / authorId 等 */
export function quickApply(workId: number): Promise<ApiResult<WorkQuickApplyResult>> {
  return post<WorkQuickApplyResult>('/work/quick-apply', { workId })
}
