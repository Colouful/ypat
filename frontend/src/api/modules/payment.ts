import { get, post } from '../request'
import type {
  ApiResult,
  PageResult,
  Product,
  ProductListParams,
  CreateOrderParams,
  CreateOrderResult,
  OrderInfo,
  PaymentChannel,
  PaymentCreateResult,
  Bill,
  BillListParams,
  RecordInfo,
  RecordListParams,
} from '../types'

export function getProductList(params: ProductListParams): Promise<ApiResult<PageResult<Product>>> {
  return get('/product/list', { ...params })
}

export function createOrder(data: CreateOrderParams): Promise<ApiResult<CreateOrderResult>> {
  return post('/order/create', data)
}

export function createPpdOrder(productId: number, channel: PaymentChannel): Promise<ApiResult<PaymentCreateResult>> {
  return post('/ppd/order/create', { productId, channel })
}

export function getPpdOrderStatus(outTradeNo: string): Promise<ApiResult<PageResult<OrderInfo>>> {
  return get('/ppd/order/status', { out_trade_no: outTradeNo })
}

export function createRealnameOrder(channel: PaymentChannel): Promise<ApiResult<PaymentCreateResult>> {
  return post('/realname/order/create', { channel })
}

export function getRealnameOrderStatus(outTradeNo: string): Promise<ApiResult<PageResult<OrderInfo>>> {
  return get('/realname/order/status', { out_trade_no: outTradeNo })
}

export function getOrderStatus(outTradeNo: string): Promise<ApiResult<PageResult<OrderInfo>>> {
  return get('/order/status', { out_trade_no: outTradeNo })
}

export function getBillList(params: BillListParams): Promise<ApiResult<PageResult<Bill>>> {
  return post('/bill/findPage', params)
}

export function getRecordList(params: RecordListParams): Promise<ApiResult<PageResult<RecordInfo>>> {
  return get('/record/findPage', { ...params })
}
