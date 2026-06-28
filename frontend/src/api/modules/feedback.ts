import { post } from '../request'
import type { ApiResult, FeedbackAddParams } from '../types'

export function addFeedback(data: FeedbackAddParams): Promise<ApiResult<null>> {
  return post('/feedback/add', data)
}
