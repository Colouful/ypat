import { get, put } from '../request'
import type { ApiResult, CheckinRecord, CheckinRule, PageQuery, PageResult } from '../types'

export interface CheckinRecordQuery extends PageQuery {
  userid?: number
  mobile?: string
  dateFrom?: string
  dateTo?: string
}

export function getCheckinRule(): Promise<ApiResult<CheckinRule>> {
  return get<CheckinRule>('/admin/checkin/rule')
}

export function saveCheckinRule(data: CheckinRule): Promise<ApiResult<CheckinRule>> {
  return put<CheckinRule>('/admin/checkin/rule', data)
}

export function getCheckinRecords(
  params: CheckinRecordQuery,
): Promise<ApiResult<PageResult<CheckinRecord>>> {
  return get<PageResult<CheckinRecord>>('/admin/checkin/records', params as Record<string, unknown>)
}
