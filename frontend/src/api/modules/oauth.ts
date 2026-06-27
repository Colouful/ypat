import { get, post } from '../request'
import { ensureImageDataUrl } from '@/utils/file-base64'
import type { ApiResult, OauthInfo, OauthSubmitParams, OcrResult } from '../types'

// 后端 /oauth/ocr、/oauth/add 需要带 dataURL 头的 base64(见 backend MockTest)。
async function normalizeImage(value: string): Promise<string> {
  return ensureImageDataUrl(value)
}

export async function ocrIdCard(value: string): Promise<ApiResult<OcrResult>> {
  const cardfront = await normalizeImage(value)
  return post('/oauth/ocr', { cardfront })
}

export async function submitAuth(data: OauthSubmitParams): Promise<ApiResult<OauthInfo>> {
  const pics = await Promise.all(data.pics.map(normalizeImage))
  return post('/oauth/add', { name: data.name, certcode: data.certcode, pics })
}

export function getAuthDetail(): Promise<ApiResult<OauthInfo>> {
  return get('/oauth/get')
}

export function getAuthDetailByUserId(id: number): Promise<ApiResult<OauthInfo>> {
  return get('/oauth/detail', { id })
}
