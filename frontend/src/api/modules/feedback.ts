import { post, upload } from '../request'
import type { ApiResult, FeedbackAddParams } from '../types'
import type { MediaUploadResult, UploadProgressEvent } from '../types/media'

export function addFeedback(data: FeedbackAddParams): Promise<ApiResult<null>> {
  return post('/feedback/add', data)
}

export function uploadFeedbackImage(
  filePath: string,
  onProgress?: (e: UploadProgressEvent) => void,
): Promise<ApiResult<MediaUploadResult>> {
  return upload<MediaUploadResult>({
    url: '/feedback/upload/image',
    filePath,
    name: 'file',
    showLoading: false,
    onProgress,
  })
}
