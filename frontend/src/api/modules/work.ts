/**
 * 作品 API 模块
 */
import { get, post, put } from '../request-adapter'
import type {
  WorkListParams, WorkListResult, WorkDetail, WorkSubmitParams,
  WorkLikeParams, WorkFavoriteParams, WorkComplainParams, WorkQuickApplyParams, WorkQuickApplyResult,
} from '../types/work'
import type { ApiResult } from '../types'
import { normalizeImageUrl } from '../adapters'

/** 列表 */
export function getList(params: WorkListParams): Promise<ApiResult<WorkListResult>> {
  return post<WorkListResult>('/work/list', params)
}

/** 详情 */
export function getDetail(id: number): Promise<ApiResult<WorkDetail>> {
  return get<WorkDetail>('/work/get', { id }).then((result) => ({
    ...result,
    data: normalizeWorkDetail(result.data),
  }))
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
export function quickApply(params: WorkQuickApplyParams): Promise<ApiResult<WorkQuickApplyResult>> {
  return post<WorkQuickApplyResult>('/work/quick-apply', params)
}

function isOn(value: unknown): boolean {
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    return normalized === '1' || normalized === 'true' || normalized === 'y'
  }
  return value === true || value === 1
}

function normalizeWorkDetail(data: WorkDetail | null | undefined): WorkDetail {
  const raw = (data || {}) as WorkDetail & Record<string, any>
  const userRaw = raw.user || raw.userQo || {}
  const mediasRaw = Array.isArray(raw.medias)
    ? raw.medias
    : Array.isArray(raw.mediaList)
      ? raw.mediaList
      : Array.isArray(raw.pics)
        ? raw.pics.map((url: string, index: number) => ({ id: index, type: 'IMAGE', url }))
        : []

  const normalizedMedias = mediasRaw
    .map((item: any, index: number) => {
      if (typeof item === 'string') return { id: index, type: 'IMAGE' as const, url: normalizeImageUrl(item) }
      const url = normalizeImageUrl(item.url || item.imgpath || item.path || item.fileUrl)
      return {
        ...item,
        id: item.id || index,
        type: item.type || item.mediaType || 'IMAGE',
        url,
      }
    })
    .filter((item: { url?: string }) => item.url)

  return {
    ...raw,
    description: raw.description || raw.describ || '',
    readCount: Number(raw.readCount || raw.readtimes || 0),
    likeCount: Number(raw.likeCount || raw.liketimes || 0),
    favoriteCount: Number(raw.favoriteCount || raw.coltimes || 0),
    publishTime: raw.publishTime || raw.pubdate || raw.credate,
    medias: normalizedMedias,
    tags: Array.isArray(raw.tags) ? raw.tags : [],
    user: {
      ...userRaw,
      id: Number(userRaw.id || raw.userid || raw.userId || 0),
      avatar: normalizeImageUrl(userRaw.avatar || userRaw.imgpath || userRaw.avatarurl),
      profession: userRaw.profession || userRaw.profess,
    },
    isLiked: isOn(raw.isLiked) || isOn(raw.likeflag) || isOn(raw.liked),
    isFavorited: isOn(raw.isFavorited) || isOn(raw.colflag) || isOn(raw.favoriteflag) || isOn(raw.favoriteFlag) || isOn(raw.favorited),
    isOwner: isOn(raw.isOwner) || isOn(raw.ownerFlag),
  } as WorkDetail
}
