import { get } from '../request'
import type { ApiResult, PageQuery, PageResult, PaymentOrder } from '../types'

export interface PaymentOrderQuery extends PageQuery {
  userId?: number
  businessType?: string
  businessOrderNo?: string
  outTradeNo?: string
  channel?: string
  status?: string
}

export function getPaymentOrders(params: PaymentOrderQuery): Promise<ApiResult<PageResult<PaymentOrder>>> {
  return get<PageResult<PaymentOrder>>('/admin/payment/orders', params as Record<string, unknown>)
}
