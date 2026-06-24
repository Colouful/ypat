import { filePathToBase64 } from '@/utils/file-base64'
import {
  del,
  get,
  post,
  put,
  request,
  upload as networkUpload,
} from './request'
import type { ApiResult } from './types'

interface UploadConfig {
  url: string
  filePath: string
  name?: string
  formData?: Record<string, string>
  header?: Record<string, string>
  showLoading?: boolean
  showError?: boolean
  withToken?: boolean
}

/**
 * 兼容迁移页历史上的 /upload 调用：真实后端 /ypat/submit 接收 Base64，
 * 因此该路径只做本地转换，不再请求不存在的通用上传接口。
 */
export async function upload<T = unknown>(config: UploadConfig): Promise<ApiResult<T>> {
  if (config.url === '/upload') {
    const base64 = await filePathToBase64(config.filePath)
    return {
      success: true,
      data: { url: base64 } as T,
      code: '200',
      message: '',
    }
  }
  return networkUpload<T>(config)
}

export { request, get, post, put, del }
export default { request, upload, get, post, put, del }
