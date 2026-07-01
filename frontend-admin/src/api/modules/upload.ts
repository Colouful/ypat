import service from '../request'
import type { ApiResult } from '../types'

export function uploadFiles(files: File[], withWatermark = false): Promise<ApiResult<{ urls: string[] }>> {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return service({
    method: 'POST',
    url: withWatermark ? '/admin/ypat/upload' : '/admin/upload',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  }) as unknown as Promise<ApiResult<{ urls: string[] }>>
}
