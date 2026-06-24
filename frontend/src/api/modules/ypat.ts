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

/**
 * 获取推荐列表
 */
export function getRecommendList(params: YpatListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return get('/ypat/tc/list', params)
}

/**
 * 获取最新列表
 */
export function getLatestList(params: YpatListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return get('/ypat/zx/list', params)
}

/**
 * 获取详情
 */
export function getDetail(id: number, userid?: number): Promise<ApiResult<YpatInfo>> {
  return get('/ypat/get', { id, userid })
}

/**
 * 发布/编辑
 */
export function submit(data: YpatSubmitParams): Promise<ApiResult<YpatInfo>> {
  return post('/ypat/submit', data)
}

/**
 * 增加阅读量
 */
export function addReadCount(id: number): Promise<ApiResult<null>> {
  return put('/ypat/yd/add', { id })
}

/**
 * 报名/申请约拍
 */
export function applyYpat(data: YpatApplyParams): Promise<ApiResult<null>> {
  return put('/my/ypat/rec/add', data)
}

/**
 * 收藏
 */
export function addFavorite(userid: number, ypatid: number): Promise<ApiResult<null>> {
  return put('/my/ypat/sc/add', { userid, ypatid })
}

/**
 * 我发布的列表
 */
export function getMyPublishList(params: YpatMyListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return post('/my/ypat/pub/list', params)
}

/**
 * 我发送的列表
 */
export function getMySentList(params: YpatMyListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return post('/my/ypat/send/list', params)
}

/**
 * 我收到的列表
 */
export function getMyReceivedList(params: YpatMyListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return post('/my/ypat/rec/list', params)
}

/**
 * 我的收藏列表
 */
export function getMyFavoriteList(params: YpatMyListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return post('/my/ypat/sc/list', params)
}

/**
 * 获取未读消息数量
 */
export function getUnreadCount(userid: number): Promise<ApiResult<UnreadCountResult>> {
  return get('/my/ypat/unread/count', { userid })
}
