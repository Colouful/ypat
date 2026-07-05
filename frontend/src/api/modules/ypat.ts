import { get, post, put } from '../request'
import { PHOTO_STYLES } from '@/constants/enums'
import type {
  ApiResult,
  MessInfo,
  PageResult,
  YpatInfo,
  YpatListParams,
  YpatSubmitParams,
  YpatApplyParams,
  YpatMyListParams,
  UnreadCountResult,
} from '../types'

function normalizePatstyle(patstyle?: string): string | undefined {
  if (!patstyle) return patstyle
  return patstyle
    .split(',')
    .map((item) => {
      const value = item.trim()
      const index = PHOTO_STYLES.indexOf(value)
      return index >= 0 ? String(index) : value
    })
    .filter(Boolean)
    .join(',')
}

function normalizeListParams(params: YpatListParams): YpatListParams {
  return {
    ...params,
    patstyle: normalizePatstyle(params.patstyle),
  }
}

export function getRecommendList(params: YpatListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return get('/ypat/tc/list', { ...normalizeListParams(params) })
}

export function getLatestList(params: YpatListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return get('/ypat/zx/list', { ...normalizeListParams(params) })
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

// 后端 MypatInfoController 这些列表均为 @GetMapping(query 绑定),必须用 GET,
// 否则 405。见 docs/migration/frontend-parity/03-api-contract-matrix.md (GAP-API-01)。
export function getMyPublishList(params: YpatMyListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return get('/my/ypat/pub/list', { ...params })
}

export function getMySentList(params: YpatMyListParams): Promise<ApiResult<PageResult<MessInfo>>> {
  return get('/my/ypat/send/list', { ...params })
}

export function getMyReceivedList(params: YpatMyListParams): Promise<ApiResult<PageResult<MessInfo>>> {
  return get('/my/ypat/rec/list', { ...params })
}

export function getMyFavoriteList(params: YpatMyListParams): Promise<ApiResult<PageResult<YpatInfo>>> {
  return get('/my/ypat/sc/list', { ...params })
}

export function getUnreadCount(userid: number): Promise<ApiResult<UnreadCountResult>> {
  return get('/my/ypat/unread/count', { userid })
}
