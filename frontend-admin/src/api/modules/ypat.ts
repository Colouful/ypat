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
  userid?: number
  imgpath?: string
  dataFlag?: string
  internalBatchNo?: string
  patdate?: string
  patarea?: string
  patslice?: string
  creditflag?: string
  realnameflag?: string
  pubdate: string
  readtimes?: number
  pattimes?: number
  coltimes?: number
  status: string
  statusTxt: string
  recomflag: string
  reason: string
  pics: string[]
  userQo?: {
    id: number
    nickname?: string
    mobile?: string
    gender?: string
    genderTxt?: string
    profess?: string
    professTxt?: string
    openid?: string
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
  workId?: string
  patstyle: string
  nickname: string
  gender: string
  profess: string
}

/**
 * 申请列表
 */
export function getYpatList(params: YpatListQuery): Promise<ApiResult<PageResult<YpatInfo>>> {
  return get<PageResult<YpatInfo>>('/admin/ypat/list', params as Record<string, unknown>).then((res) => ({
    ...res,
    data: normalizeYpatListPage(res.data),
  }))
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
  appendFormValue(formData, 'workId', data.workId)
  appendFormValue(formData, 'patstyle', data.patstyle)
  appendFormValue(formData, 'nickname', data.nickname)
  appendFormValue(formData, 'gender', data.gender)
  appendFormValue(formData, 'profess', data.profess)
  if (avatarFile) {
    formData.append('file', avatarFile)
  }
  files.filter((file) => file.size > 0).forEach((file) => formData.append('files', file))
  return post('/admin/ypat/submit', formData)
}

function appendFormValue(formData: FormData, key: string, value: string | number | undefined): void {
  if (value !== undefined && value !== '') {
    formData.append(key, String(value))
  }
}

export function normalizeYpatListPage(page: PageResult<YpatInfo>): PageResult<YpatInfo> {
  return {
    ...page,
    content: (page.content || []).map(normalizeYpatInfo),
  }
}

function normalizeYpatInfo(item: YpatInfo): YpatInfo {
  const user = item.userQo
  if (!user) return fillYpatEnumText(item)

  const gender = item.gender || user.gender || ''
  const profess = item.profess || user.profess || ''
  return fillYpatEnumText({
    ...item,
    nickname: item.nickname || user.nickname || '',
    mobile: item.mobile || user.mobile || '',
    gender,
    genderTxt: item.genderTxt || user.genderTxt || getGenderText(gender),
    profess,
    professTxt: item.professTxt || user.professTxt || getProfessText(profess),
  })
}

function fillYpatEnumText(item: YpatInfo): YpatInfo {
  return {
    ...item,
    targetTxt: item.targetTxt || getTargetText(item.target),
    chargewayTxt: item.chargewayTxt || getChargeWayText(item.chargeway),
    patstyleTxt: item.patstyleTxt || getPatstyleText(item.patstyle),
  }
}

function getGenderText(value?: string): string {
  const genderMap: Record<string, string> = {
    '1': '男',
    '2': '女',
  }
  return value ? genderMap[value] || value : ''
}

function getProfessText(value?: string): string {
  const professMap: Record<string, string> = {
    '0': '摄影师',
    '1': '模特',
    '2': '化妆师',
    '3': '修图师',
    '4': '个人',
    '5': '演员',
    '6': '商家',
    '7': '其他',
    '8': '素人模特',
    '9': '摄像师',
  }
  return value ? professMap[value] || value : ''
}

function getTargetText(value?: string): string {
  const targetMap: Record<string, string> = {
    '0': '约摄影师',
    '1': '约模特',
    '2': '约摄像师',
    '3': '约商家',
    '4': '约化妆师',
    '5': '约修图师',
  }
  return value ? targetMap[value] || value : ''
}

function getChargeWayText(value?: string): string {
  const chargeWayMap: Record<string, string> = {
    '0': '希望互勉',
    '1': '我要收费',
    '2': '可付费',
    '3': '费用协商',
  }
  return value ? chargeWayMap[value] || value : ''
}

function getPatstyleText(value?: string): string {
  const styleMap: Record<string, string> = {
    '0': '复古',
    '1': 'INS',
    '2': '胶片',
    '3': '少女',
    '4': '暗黑',
    '5': '情绪',
    '6': '夜景',
    '7': '欧美',
    '8': '商务',
    '9': '韩系',
    '10': '日系',
    '11': '情侣',
    '12': '样片',
  }
  return value
    ? value
      .split(',')
      .map((item) => styleMap[item.trim()] || item.trim())
      .filter(Boolean)
      .join('、')
    : ''
}
