import { get, post } from '../request'
import { filePathToBase64 } from '@/utils/file-base64'
import type { ApiResult, OauthInfo, OauthSubmitParams, OcrResult } from '../types'

async function normalizeImage(value: string): Promise<string> {
  if (/^[A-Za-z0-9+/=\r\n]+$/.test(value) && value.length > 200) return value
  return filePathToBase64(value)
}

export async function ocrIdCard(value: string): Promise<ApiResult<OcrResult>> {
  return post('/oauth/ocr', { cardfront: await normalizeImage(value) })
}

export async function submitAuth(data: OauthSubmitParams & { userid?: number }): Promise<ApiResult<OauthInfo>> {
  const pics = await Promise.all(data.pics.map(normalizeImage))
  return post('/oauth/add', { name: data.name, certcode: data.certcode, pics })
}

export async function getAuthDetail(_id?: number): Promise<ApiResult<any>> {
  const result = await get<Record<string, unknown>>('/oauth/get')
  if (result.data) result.data = { ...result.data, status: Number(result.data.status || 0) }
  return result as ApiResult<any>
}

export function getAuthDetailByUserId(id: number): Promise<ApiResult<OauthInfo>> {
  return get('/oauth/detail', { id })
}
