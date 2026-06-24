import { post } from '../request'
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

/**
 * 获取商品列表
 */
export function getProductList(params: ProductListParams): Promise<ApiResult<PageResult<Product>>> {
  return post('/product/findPage', params)
}

/**
 * 创建订单
 */
export function createOrder(data: CreateOrderParams): Promise<ApiResult<CreateOrderResult>> {
  return post('/order/create', data)
}

/**
 * 获取账单列表
 */
export function getBillList(params: BillListParams): Promise<ApiResult<PageResult<Bill>>> {
  return post('/bill/findPage', params)
}

/**
 * 获取消费记录列表
 */
export function getRecordList(params: RecordListParams): Promise<ApiResult<PageResult<RecordInfo>>> {
  return post('/record/findPage', params)
}
