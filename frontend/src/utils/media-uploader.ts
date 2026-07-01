/**
 * 媒体上传工具
 * 封装 chooseImage/chooseVideo、上传、进度、重试
 */
import { uploadImage, uploadVideo, deleteMedia } from '@/api/modules/media'
import type { MediaItem, UploadProgressEvent } from '@/api/types/media'

export const MAX_IMAGE_COUNT = 9
export const MAX_VIDEO_COUNT = 1
export const MAX_IMAGE_TOTAL_SIZE = 100 * 1024 * 1024 // 100MB
export const MAX_VIDEO_SIZE = 200 * 1024 * 1024       // 200MB

export const RETRY_MAX = 3

export interface ChooseOptions {
  count: number
  sourceType?: ('album' | 'camera')[]
  sizeType?: ('original' | 'compressed')[]
}

export function chooseImages(opts: ChooseOptions): Promise<UniApp.ChooseImageSuccessCallbackResultFile[]> {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: opts.count,
      sizeType: opts.sizeType || ['compressed'],
      sourceType: opts.sourceType || ['album', 'camera'],
      success: (res) => resolve(res.tempFiles as any),
      fail: reject,
    })
  })
}

export function chooseVideo(): Promise<UniApp.ChooseVideoSuccess> {
  return new Promise((resolve, reject) => {
    uni.chooseVideo({
      sourceType: ['album', 'camera'],
      maxDuration: 60,
      success: resolve,
      fail: reject,
    } as any)
  })
}

/**
 * 上传图片（带重试）
 */
export async function uploadImageWithRetry(
  item: MediaItem,
  onProgress?: (e: UploadProgressEvent) => void,
): Promise<MediaItem> {
  let attempt = 0
  let lastErr: any
  while (attempt < RETRY_MAX) {
    try {
      const res = await uploadImage(item.localPath, onProgress)
      return { ...item, mediaId: res.id, url: res.url, uploadStatus: 'success', progress: 100 }
    } catch (e) {
      lastErr = e
      attempt++
      if (attempt >= RETRY_MAX) break
      // 退避 500ms
      await new Promise(r => setTimeout(r, 500 * attempt))
    }
  }
  return { ...item, uploadStatus: 'failed', error: (lastErr && lastErr.message) || '上传失败' }
}

/**
 * 上传视频（带重试）
 */
export async function uploadVideoWithRetry(
  item: MediaItem,
  onProgress?: (e: UploadProgressEvent) => void,
): Promise<MediaItem> {
  let attempt = 0
  let lastErr: any
  while (attempt < RETRY_MAX) {
    try {
      const res = await uploadVideo(item.localPath, onProgress)
      return { ...item, mediaId: res.id, url: res.url, uploadStatus: 'success', progress: 100 }
    } catch (e) {
      lastErr = e
      attempt++
      if (attempt >= RETRY_MAX) break
      await new Promise(r => setTimeout(r, 500 * attempt))
    }
  }
  return { ...item, uploadStatus: 'failed', error: (lastErr && lastErr.message) || '上传失败' }
}

/**
 * 计算图片总大小
 */
export function totalImageSize(items: MediaItem[]): number {
  return items.filter(i => i.type === 'IMAGE').reduce((sum, i) => sum + (i.size || 0), 0)
}

/**
 * 验证图片大小总和
 */
export function checkImageTotalSize(items: MediaItem[]): { ok: boolean; totalSize: number; message?: string } {
  const total = totalImageSize(items)
  if (total > MAX_IMAGE_TOTAL_SIZE) {
    return { ok: false, totalSize: total, message: '图片总大小不能超过 100MB' }
  }
  return { ok: true, totalSize: total }
}

export { deleteMedia }
