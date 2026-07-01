/**
 * 产品管理 API
 */

import { get, post } from '../request'
import type { ApiResult, PageResult, PageQuery } from '../types'

/** 产品信息 */
export interface Product {
  id: number
  name: string
  currval: number
  oldval: number
  status: string
}

/** 产品列表查询参数 */
export interface ProductListQuery extends PageQuery {
  name?: string
  status?: string
}

/**
 * 产品列表
 */
export function getProductList(params: ProductListQuery): Promise<ApiResult<PageResult<Product>>> {
  return get<PageResult<Product>>('/admin/product/list', params as Record<string, unknown>)
}

/**
 * 产品详情
 */
export function getProductDetail(id: number): Promise<ApiResult<Product>> {
  return get<Product>('/admin/product/detail', { id })
}

/**
 * 保存产品
 */
export function saveProduct(data: Partial<Product>): Promise<ApiResult<unknown>> {
  return post('/admin/product/save', data)
}

/**
 * 上架 / 下架
 */
export function upDownProduct(id: number, status: string): Promise<ApiResult<unknown>> {
  return post('/admin/product/upDown', undefined, { params: { id, status } })
}
