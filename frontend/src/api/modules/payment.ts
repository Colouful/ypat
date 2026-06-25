import { get, post } from '../request'
import type {
  ApiResult,
  PageResult,
  Product,
  ProductListParams,
  CreateOrderParams,
  CreateOrderResult,
  OrderInfo,
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

export function getOrderStatus(outTradeNo: string): Promise<ApiResult<PageResult<OrderInfo>>> {
  return get('/order/status', { out_trade_no: outTradeNo })
}

export function getBillList(params: BillListParams): Promise<ApiResult<PageResult<Bill>>> {
  return post('/bill/findPage', params)
}

export function getRecordList(params: RecordListParams): Promise<ApiResult<PageResult<RecordInfo>>> {
  return get('/record/findPage', { ...params })
}
