/**
 * 用户管理 API（实名审核模块）
 */

import { get, post } from '../request'
import type { ApiResult, PageResult, PageQuery } from '../types'

/** 实名认证用户信息（对应后端 OauthQo） */
export interface OauthQo {
  /** 用户分页接口返回 id，实名详情接口返回 userid。 */
  id?: number
  userid?: number
  nickname?: string
  mobile?: string
  name?: string
  certcode?: string
  pics?: string[]
  status?: string
  statusTxt?: string
  gender?: string
  genderTxt?: string
  profess?: string
  professTxt?: string
  ppd?: number
  avatarurl?: string
  imgpath?: string
  realnameflag?: string
  creditflag?: string
  pubtimes?: number
  rectimes?: number
  coltimes?: number
  recmobile?: string
  province?: string
  city?: string
  area?: string
  wx?: string
  qq?: string
  wb?: string
  birthday?: string
  channel?: string
  channelTxt?: string
  dataFlag?: string
  internalBatchNo?: string
  regisdate?: string
}

/** 用户列表查询参数 */
export interface UserListQuery extends PageQuery {
  status?: string
  nickname?: string
  mobile?: string
  regisdate?: string
  gender?: string
  id?: number
  dataFlag?: string
}

/**
 * 获取实名认证用户列表
 * 对应旧后台：GET /manage/user/list
 */
export function getUserList(
  params: UserListQuery,
): Promise<ApiResult<PageResult<OauthQo>>> {
  return get<PageResult<OauthQo>>('/admin/user/list', params as Record<string, unknown>)
}

/**
 * 获取实名认证详情
 * 对应旧后台：GET /manage/user/detail
 */
export function getUserDetail(id: number): Promise<ApiResult<OauthQo>> {
  return get<OauthQo>('/admin/user/detail', { id })
}

/**
 * 实名审核
 * 对应旧后台：GET /manage/user/audit（旧版用 GET，新版改为 POST 更安全）
 *
 * @param id   用户 ID
 * @param flag 审核标志（2审核通过/3审核未通过）
 */
export function auditUser(id: number, flag: string): Promise<ApiResult<{ success: boolean; data: unknown }>> {
  return post('/admin/user/audit', undefined, { params: { id, flag } })
}
