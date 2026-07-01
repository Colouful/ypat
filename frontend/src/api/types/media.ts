/**
 * 媒体上传类型
 */
export interface MediaUploadResult {
  id: number
  url: string
  type: 'IMAGE' | 'VIDEO'
  fileSize: number
  width?: number
  height?: number
  duration?: number
}

export interface UploadProgressEvent {
  progress: number
  totalBytesSent: number
  totalBytesExpectedToSend: number
}

export interface MediaItem {
  /** 本地临时路径 */
  localPath: string
  /** 上传后的 mediaId（上传成功后才有） */
  mediaId?: number
  /** 远程 URL */
  url?: string
  /** 媒体类型 */
  type: 'IMAGE' | 'VIDEO'
  /** 文件大小（字节） */
  size: number
  /** 上传状态 */
  uploadStatus: 'pending' | 'uploading' | 'success' | 'failed'
  /** 进度 0-100 */
  progress: number
  /** 错误信息 */
  error?: string
  /** 视频封面（仅视频） */
  thumb?: string
  /** 视频时长（秒） */
  duration?: number
}
