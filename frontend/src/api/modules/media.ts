/**
 * 媒体上传 API
 * 走统一 request-adapter，确保 baseURL、Token 和错误处理与普通接口一致。
 */
import { del, upload } from '../request-adapter'
import { envConfig } from '@/config/env'
import type { MediaUploadResult, UploadProgressEvent } from '../types/media'

interface MediaUploadConfig {
  url: string
  filePath: string
  name: string
  showLoading: boolean
  onProgress?: (e: UploadProgressEvent) => void
}

function unwrapUploadResult(result: { data?: MediaUploadResult | null; message?: string }): MediaUploadResult {
  if (!result.data) throw new Error(result.message || '上传失败')
  return result.data
}

function createUploadConfig(
  url: string,
  filePath: string,
  onProgress?: (e: UploadProgressEvent) => void,
): MediaUploadConfig {
  const config: MediaUploadConfig = {
    url,
    filePath,
    name: 'file',
    showLoading: false,
  }
  if (onProgress) config.onProgress = onProgress
  return config
}

/**
 * 通过 multipart/form-data 上传图片
 */
export function uploadImage(
  filePath: string,
  onProgress?: (e: UploadProgressEvent) => void,
): Promise<MediaUploadResult> {
  return upload<MediaUploadResult>(
    createUploadConfig(`${envConfig.imageUploadApiBaseUrl}/work/upload/image`, filePath, onProgress),
  ).then(unwrapUploadResult)
}

/**
 * 通过 multipart/form-data 上传视频
 */
export function uploadVideo(
  filePath: string,
  onProgress?: (e: UploadProgressEvent) => void,
): Promise<MediaUploadResult> {
  return upload<MediaUploadResult>(createUploadConfig('/work/upload/video', filePath, onProgress)).then(unwrapUploadResult)
}

/**
 * 删除媒体（仅未绑定的孤儿）
 */
export async function deleteMedia(mediaId: number): Promise<{ msg: string }> {
  const result = await del<{ msg: string }>('/work/upload/media', { id: mediaId })
  return result.data || { msg: result.message || '删除成功' }
}
