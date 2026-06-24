import { get, post } from '../request'
import type {
  ApiResult,
  OcrResult,
  OauthInfo,
  OauthSubmitParams,
} from '../types'

/**
 * OCR 识别身份证正面。
 * 历史后端接收表单字段 cardfront，值为不含 data:image 前缀的纯 Base64。
 */
export function ocrIdCard(cardfront: string): Promise<ApiResult<OcrResult>> {
  return post('/oauth/ocr', { cardfront })
}

/** 提交当前登录用户实名认证 */
export function submitAuth(data: OauthSubmitParams): Promise<ApiResult<OauthInfo>> {
  return post('/oauth/add', data)
}

/** 获取当前登录用户认证信息 */
export function getAuthDetail(): Promise<ApiResult<OauthInfo>> {
  return get('/oauth/get')
}

/** 管理端或公开场景按用户 ID 获取认证详情 */
export function getAuthDetailByUserId(id: number): Promise<ApiResult<OauthInfo>> {
  return get('/oauth/detail', { id })
}
