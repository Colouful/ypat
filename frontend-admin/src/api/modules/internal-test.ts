/**
 * 内测数据工厂 API
 */

import { get, post } from '../request'
import type { ApiResult, PageQuery, PageResult } from '../types'

export interface InternalTestResource {
  id?: number
  mediaType?: string
  usageType?: string
  styleCode?: string
  url?: string
  title?: string
  description?: string
  profession?: string
  city?: string
  status?: string
  sortNo?: number
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface InternalTestResourceQuery extends PageQuery {
  mediaType?: string
  usageType?: string
  styleCode?: string
  profession?: string
  city?: string
  status?: string
  keyword?: string
}

export interface InternalTestGeneratePayload {
  mode?: string
  userCount?: number
  userIds?: number[]
  nicknamePrefix?: string
  gender?: string
  profess?: string
  province?: string
  city?: string
  area?: string
  styleCode?: string
  contentType?: string
  templateType?: string
  publishStatus?: string
  avatarResourceIds?: number[]
  ypatResourceIds?: number[]
  workResourceIds?: number[]
  batchNo?: string
}

export interface InternalTestUserQuery extends PageQuery {
  batchNo?: string
  city?: string
  area?: string
  profess?: string
  gender?: string
}

export interface InternalTestBatchQuery extends PageQuery {
  batchNo?: string
}

export interface InternalTestBatch {
  batchNo: string
  userCount: number
  ypatCount: number
  workCount: number
  status?: string
  errors?: string[]
  createdAt?: string
}

export interface InternalTestUser {
  id: number
  nickname?: string
  mobile?: string
  gender?: string
  profess?: string
  city?: string
  area?: string
  dataFlag?: string
  internalBatchNo?: string
}

export function getInternalResources(
  params: InternalTestResourceQuery,
): Promise<ApiResult<PageResult<InternalTestResource>>> {
  return get<PageResult<InternalTestResource>>('/admin/internal-test/resources', params as Record<string, unknown>)
}

export function createInternalResource(data: InternalTestResource): Promise<ApiResult<InternalTestResource>> {
  return post<InternalTestResource>('/admin/internal-test/resources', data)
}

export function updateInternalResource(data: InternalTestResource): Promise<ApiResult<InternalTestResource>> {
  return post<InternalTestResource>('/admin/internal-test/resources/update', data)
}

export function updateInternalResourceStatus(id: number, status: string): Promise<ApiResult<unknown>> {
  return post('/admin/internal-test/resources/status', undefined, { params: { id, status } })
}

export function getInternalUsers(
  params: InternalTestUserQuery,
): Promise<ApiResult<PageResult<InternalTestUser>>> {
  return get<PageResult<InternalTestUser>>('/admin/internal-test/users', params as Record<string, unknown>)
}

export function createInternalUsers(data: InternalTestGeneratePayload): Promise<ApiResult<InternalTestBatch>> {
  return post<InternalTestBatch>('/admin/internal-test/users/create', data)
}

export function generateInternalData(data: InternalTestGeneratePayload): Promise<ApiResult<InternalTestBatch>> {
  return post<InternalTestBatch>('/admin/internal-test/generate', data)
}

export function getInternalBatches(
  params?: InternalTestBatchQuery,
): Promise<ApiResult<PageResult<InternalTestBatch>>> {
  return get<PageResult<InternalTestBatch>>('/admin/internal-test/batches', params as Record<string, unknown>)
}

export function cleanupInternalData(data: InternalTestGeneratePayload): Promise<ApiResult<InternalTestBatch>> {
  return post<InternalTestBatch>('/admin/internal-test/cleanup', data)
}
