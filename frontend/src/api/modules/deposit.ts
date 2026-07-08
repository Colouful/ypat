import { get, post } from '../request'
import type {
  ApiResult,
  DepositConfig,
  DepositOrder,
  PaymentChannel,
  PaymentCreateResult,
} from '../types'

export function getDepositConfig(): Promise<ApiResult<DepositConfig>> {
  return get('/deposit/config')
}

export function createDepositOrder(channel: PaymentChannel): Promise<ApiResult<PaymentCreateResult>> {
  return post('/deposit/order/create', { channel })
}

export function getDepositOrderStatus(outTradeNo: string): Promise<ApiResult<DepositOrder>> {
  return get('/deposit/order/status', { out_trade_no: outTradeNo })
}
