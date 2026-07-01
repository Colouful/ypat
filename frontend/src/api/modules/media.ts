/**
 * 媒体上传 API
 * 走 multipart/form-data，不走 Base64
 */
import type { MediaUploadResult, UploadProgressEvent } from '../types/media'

/**
 * 通过 uni.uploadFile 上传图片
 * @param filePath 本地路径
 * @param onProgress 进度回调
 */
export function uploadImage(
  filePath: string,
  onProgress?: (e: UploadProgressEvent) => void,
): Promise<MediaUploadResult> {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('ypat_token')
    uni.uploadFile({
      url: buildUrl('/work/upload/image'),
      filePath,
      name: 'file',
      header: token ? { Token: token } : {},
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data && data.code === 200) {
            resolve((data.res || data.data) as MediaUploadResult)
          } else {
            reject(new Error((data && (data.msg || data.message)) || '上传失败'))
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: (err) => reject(err),
    } as UniApp.UploadFileOption)
  })
}

/**
 * 上传视频
 */
export function uploadVideo(
  filePath: string,
  onProgress?: (e: UploadProgressEvent) => void,
): Promise<MediaUploadResult> {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('ypat_token')
    uni.uploadFile({
      url: buildUrl('/work/upload/video'),
      filePath,
      name: 'file',
      header: token ? { Token: token } : {},
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data && data.code === 200) {
            resolve((data.res || data.data) as MediaUploadResult)
          } else {
            reject(new Error((data && (data.msg || data.message)) || '上传失败'))
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: (err) => reject(err),
    } as UniApp.UploadFileOption)
  })
}

/**
 * 删除媒体（仅未绑定的孤儿）
 */
export function deleteMedia(mediaId: number): Promise<{ msg: string }> {
  return new Promise((resolve, reject) => {
    uni.request({
      url: buildUrl('/work/upload/media'),
      method: 'DELETE',
      data: { id: mediaId },
      header: { 'content-type': 'application/x-www-form-urlencoded' },
      success: (res) => {
        try {
          const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          if (data && data.code === 200) {
            resolve(data)
          } else {
            reject(new Error((data && (data.msg || data.message)) || '删除失败'))
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: (err) => reject(err),
    })
  })
}

function buildUrl(path: string): string {
  // 与 request-adapter 保持一致
  const devFlag = (globalThis as any).__DEV__
  const base = devFlag
    ? 'http://localhost:8081'
    : ''
  return base + path
}
