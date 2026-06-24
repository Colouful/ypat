import { get, post, put } from '../request'
import type {
  ApiResult,
  PageResult,
  YpatInfo,
  YpatListParams,
  YpatSubmitParams,
  YpatApplyParams,
  YpatMyListParams,
  UnreadCountResult,
} from '../types'

export function getRecommendList(params: YpatListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return get('/ypat/tc/list', { ...params })
}

export function getLatestList(params: YpatListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return get('/ypat/zx/list', { ...params })
}

export function getDetail(id: number, userid?: number): Promise<ApiResult<YpatInfo>> {
  return get('/ypat/get', { id, userid })
}

export function submit(data: YpatSubmitParams): Promise<ApiResult<YpatInfo>> {
  return post('/ypat/submit', data)
}

export function addReadCount(ypatid: number): Promise<ApiResult<null>> {
  return put('/ypat/yd/add', { ypatid })
}

export function applyYpat(data: YpatApplyParams): Promise<ApiResult<null>> {
  return put('/my/ypat/rec/add', data)
}

export function addFavorite(userid: number, ypatid: number): Promise<ApiResult<null>> {
  return put('/my/ypat/sc/add', { userid, ypatid })
}

export function getMyPublishList(params: YpatMyListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return post('/my/ypat/pub/list', params)
}

export function getMySentList(params: YpatMyListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return post('/my/ypat/send/list', params)
}

export function getMyReceivedList(params: YpatMyListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return post('/my/ypat/rec/list', params)
}

export function getMyFavoriteList(params: YpatMyListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return post('/my/ypat/sc/list', params)
}

export function getUnreadCount(userid: number): Promise<ApiResult<UnreadCountResult>> {
  return get('/my/ypat/unread/count', { userid })
}
