/**
 * 约拍申请 / 作品 API
 */

import { get, post } from '../request'
import type { ApiResult, PageResult, PageQuery } from '../types'

/** 约拍申请信息 */
export interface YpatInfo {
  id: number
  describ: string
  nickname: string
  mobile: string
  gender: string
  genderTxt: string
  profess: string
  professTxt: string
  target: string
  targetTxt: string
  patstyle?: string
  patstyleTxt?: string
  chargeway?: string
  chargewayTxt?: string
  chargeamt?: number
  province?: string
  city: string
  area?: string
  isNationwide?: string
  workId?: string
  patdate?: string
  patarea?: string
  patslice?: string
  creditflag?: string
  realnameflag?: string
  pubdate: string
  status: string
  statusTxt: string
  recomflag: string
  reason: string
  pics: string[]
  userQo?: {
    id: number
    nickname: string
    openid: string
  }
}

/** 申请列表查询参数 */
export interface YpatListQuery extends PageQuery {
  status?: string
  nickname?: string
  mobile?: string
  city?: string
  recomflag?: string
  target?: string
  patstyle?: string
  chargeway?: string
  workId?: string
}

/** 发布作品表单 */
export interface YpatSubmitForm {
  describ: string
  target: string
  patdate: string
  chargeway: string
  chargeamt?: number
  province: string
  city: string
  area?: string
  patstyle: string
  nickname: string
  gender: string
  profess: string
}

/**
 * 申请列表
 */
export function getYpatList(params: YpatListQuery): Promise<ApiResult<PageResult<YpatInfo>>> {
  return get<PageResult<YpatInfo>>('/admin/ypat/list', params as Record<string, unknown>)
}

/**
 * 申请详情
 */
export function getYpatDetail(id: number): Promise<ApiResult<YpatInfo>> {
  return get<YpatInfo>('/admin/ypat/detail', { id })
}

/**
 * 审核
 */
export function auditYpat(id: number, flag: string, reason?: string): Promise<ApiResult<unknown>> {
  return post('/admin/ypat/audit', undefined, { params: { id, flag, reason } })
}

/**
 * 推荐 / 取消推荐
 */
export function recomYpat(id: number, recomflag: string): Promise<ApiResult<unknown>> {
  return post('/admin/ypat/recom', undefined, { params: { id, recomflag } })
}

/**
 * 后台代发约拍
 */
export function submitYpat(
  data: YpatSubmitForm,
  avatarFile: File | undefined,
  files: File[],
): Promise<ApiResult<unknown>> {
  const formData = new FormData()
  appendFormValue(formData, 'describ', data.describ)
  appendFormValue(formData, 'target', data.target)
  appendFormValue(formData, 'patdate', data.patdate)
  appendFormValue(formData, 'chargeway', data.chargeway)
  appendFormValue(formData, 'chargeamt', data.chargeamt)
  appendFormValue(formData, 'province', data.province)
  appendFormValue(formData, 'city', data.city)
  appendFormValue(formData, 'area', data.area)
  appendFormValue(formData, 'patstyle', data.patstyle)
  appendFormValue(formData, 'nickname', data.nickname)
  appendFormValue(formData, 'gender', data.gender)
  appendFormValue(formData, 'profess', data.profess)
  if (avatarFile) {
    formData.append('file', avatarFile)
  }
  files.forEach((file) => formData.append('files', file))
  return post('/admin/ypat/submit', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

function appendFormValue(formData: FormData, key: string, value: string | number | undefined): void {
  if (value !== undefined && value !== '') {
    formData.append(key, String(value))
  }
}
