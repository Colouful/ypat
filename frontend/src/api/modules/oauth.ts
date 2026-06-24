import { get, post, upload } from '../request'
import type {
  ApiResult,
  OcrResult,
  OauthSubmitParams,
  OauthInfo,
} from '../types'

/**
 * OCR 识别身份证（文件上传）
 */
export function ocrIdCard(filePath: string): Promise<ApiResult<OcrResult>> {
  return upload({
    url: '/oauth/ocr',
    filePath,
    name: 'file',
    showLoading: true,
  })
}

/**
 * 提交实名认证
 */
export function submitAuth(data: OauthSubmitParams): Promise<ApiResult<null>> {
  return post('/oauth/add', data)
}

/**
 * 获取认证状态
 */
export function getAuthStatus(id: number): Promise<ApiResult<{ status: number; reason: string }>> {
  return get('/oauth/get', { id })
}

/**
 * 获取认证详情
 */
export function getAuthDetail(id: number): Promise<ApiResult<OauthInfo>> {
  return get('/oauth/getAuth', { id })
}
