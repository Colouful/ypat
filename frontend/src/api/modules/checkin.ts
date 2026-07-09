import { get, post } from '../request'
import type { ApiResult, CheckinResult, CheckinToday } from '../types'

export function getCheckinToday(): Promise<ApiResult<CheckinToday>> {
  return get('/checkin/today')
}

export function doCheckin(): Promise<ApiResult<CheckinResult>> {
  return post('/checkin/do')
}
