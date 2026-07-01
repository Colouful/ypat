import { get } from '../request'
import type { PageResult, PageQuery } from '../types'

export interface Order { id: number; credate: string; userid: number; type: string; typeTxt: string; total_fee: number; status: string }
export interface OrderListQuery extends PageQuery { status?: string; type?: string }
export const getOrderList = (params: OrderListQuery) => get<PageResult<Order>>('/admin/order/list', params as Record<string, unknown>)
