/**
 * 从作品发起约拍
 */
import { post } from '../request-adapter'
import type { WorkQuickApplyResult } from '../types/work'
import type { QuickApplyParams } from '../types/quick-apply'
import type { ApiResult } from '../types'

export function quickApply(params: QuickApplyParams): Promise<ApiResult<WorkQuickApplyResult>> {
  return post<WorkQuickApplyResult>('/work/quick-apply', params)
}
