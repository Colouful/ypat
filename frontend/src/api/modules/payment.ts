import { get, post } from '../request'
import type {
  ApiResult,
  PageResult,
  Product,
  ProductListParams,
  CreateOrderParams,
  CreateOrderResult,
  Bill,
  BillListParams,
  RecordInfo,
  RecordListParams,
} from '../types'

type CreateResult = Omit<CreateOrderResult, 'out_trade_no'> & { out_trade_no?: string }

export function getProductList(params: ProductListParams): Promise<ApiResult<PageResult<Product>>> {
  return get('/product/list', { ...params })
}

export function createOrder(data: CreateOrderParams): Promise<ApiResult<CreateResult>> {
  return post('/order/create', data)
}

export function getBillList(params: BillListParams): Promise<ApiResult<PageResult<Bill>>> {
  return post('/bill/findPage', params)
}

export function getRecordList(params: RecordListParams): Promise<ApiResult<PageResult<RecordInfo>>> {
  return get('/record/findPage', { ...params })
}
