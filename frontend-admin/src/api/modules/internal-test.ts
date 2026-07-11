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
  province?: string
  title?: string
  description?: string
  profession?: string
  city?: string
  area?: string
  status?: string
  sortNo?: number
  groupNo?: string
  groupTitle?: string
  groupSortNo?: number
  usedFlag?: number
  usedBatchNo?: string
  usedTargetType?: string
  usedTargetId?: number
  usedAt?: string
  urls?: string[]
  groupSize?: number
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface InternalTestResourceQuery extends PageQuery {
  mediaType?: string
  usageType?: string
  styleCode?: string
  profession?: string
  province?: string
  city?: string
  area?: string
  status?: string
  usedFlag?: number
  groupNo?: string
  keyword?: string
}

export interface InternalTestGeneratePayload {
  actionType?: string
  mode?: string
  userCount?: number
  userId?: number
  userIds?: number[]
  groupNos?: string[]
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
  wx?: string
  mobile?: string
  styleCodes?: string[]
  patdate?: string
  patslice?: string
  describ?: string
  target?: string
  avatarResourceIds?: number[]
  ypatResourceIds?: number[]
  workResourceIds?: number[]
  batchNo?: string
  cleanupAll?: boolean
}

export interface InternalTestResourceGroup {
  groupNo: string
  groupTitle?: string
  mediaType?: string
  usedFlag?: number
  resources: InternalTestResource[]
}

export interface InternalTestUserActionPayload {
  days?: number
  reason?: string
}

export interface InternalTestUserQuery extends PageQuery {
  batchNo?: string
  keyword?: string
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
  ignoredRealCount?: number
  releasedResourceCount?: number
  status?: string
  errors?: string[]
  createdAt?: string | number
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

export function batchCreateInternalResources(data: InternalTestResource): Promise<ApiResult<unknown>> {
  return post('/admin/internal-test/resources/batch', data)
}

export function getInternalResourceGroups(
  params: InternalTestResourceQuery,
): Promise<ApiResult<PageResult<InternalTestResourceGroup>>> {
  return get<PageResult<InternalTestResourceGroup>>('/admin/internal-test/resource-groups', params as Record<string, unknown>)
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

export function generateInternalUsers(data: InternalTestGeneratePayload): Promise<ApiResult<InternalTestBatch>> {
  return post<InternalTestBatch>('/admin/internal-test/generate/users', data)
}

export function generateInternalWorks(data: InternalTestGeneratePayload): Promise<ApiResult<InternalTestBatch>> {
  return post<InternalTestBatch>('/admin/internal-test/generate/works', data)
}

export function generateInternalYpats(data: InternalTestGeneratePayload): Promise<ApiResult<InternalTestBatch>> {
  return post<InternalTestBatch>('/admin/internal-test/generate/ypats', data)
}

export function searchInternalUsers(params: InternalTestUserQuery): Promise<ApiResult<PageResult<InternalTestUser>>> {
  return get<PageResult<InternalTestUser>>('/admin/internal-test/users/search', params as Record<string, unknown>)
}

export function grantInternalUserMember(userId: number, data: InternalTestUserActionPayload): Promise<ApiResult<boolean>> {
  return post<boolean>(`/admin/internal-test/users/${userId}/grant-member`, data)
}

export function verifyInternalUser(userId: number, data: InternalTestUserActionPayload): Promise<ApiResult<boolean>> {
  return post<boolean>(`/admin/internal-test/users/${userId}/verify`, data)
}

export function markInternalUserDepositPaid(userId: number, data: InternalTestUserActionPayload): Promise<ApiResult<boolean>> {
  return post<boolean>(`/admin/internal-test/users/${userId}/deposit-paid`, data)
}

export function getInternalBatches(
  params?: InternalTestBatchQuery,
): Promise<ApiResult<PageResult<InternalTestBatch>>> {
  const query = params
    ? { ...params, batchNo: params.batchNo?.trim() || undefined }
    : undefined
  return get<PageResult<InternalTestBatch>>('/admin/internal-test/batches', query as Record<string, unknown>)
}

export function cleanupInternalData(data: InternalTestGeneratePayload): Promise<ApiResult<InternalTestBatch>> {
  return post<InternalTestBatch>('/admin/internal-test/cleanup', data)
}
