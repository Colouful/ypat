import { get, put } from '../request'
import type { ApiResult, DepositConfig, DepositOrder, PageQuery, PageResult } from '../types'

export interface DepositOrderQuery extends PageQuery {
  userId?: number
  status?: string
  channel?: string
  outTradeNo?: string
}

export function getDepositConfig(): Promise<ApiResult<DepositConfig>> {
  return get<DepositConfig>('/admin/deposit/config')
}

export function saveDepositConfig(data: Partial<DepositConfig>): Promise<ApiResult<DepositConfig>> {
  return put<DepositConfig>('/admin/deposit/config', data)
}

export function getDepositOrders(params: DepositOrderQuery): Promise<ApiResult<PageResult<DepositOrder>>> {
  return get<PageResult<DepositOrder>>('/admin/deposit/orders', params as Record<string, unknown>)
}
