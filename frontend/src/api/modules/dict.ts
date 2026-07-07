/**
 * 字典 API
 */
import { get } from '../request-adapter'
import type { WorkTag } from '../types/work'
import type { ApiResult } from '../types'

/** 作品主题标签字典（请求失败静默处理，不打扰用户） */
export function getWorkTags(): Promise<ApiResult<WorkTag[]>> {
  return get<WorkTag[]>('/dict/work-tag', undefined, { showError: false })
}
