import { get, post, put } from '../request'
import type {
  ApiResult,
  MemberBenefitRule,
  MemberOperationLog,
  MemberOrder,
  MemberPlan,
  MemberUser,
  PageQuery,
  PageResult,
} from '../types'

export interface MemberPlanQuery extends PageQuery {
  name?: string
  status?: string
}

export interface MemberBenefitRuleQuery extends PageQuery {
  levelCode?: string
  scene?: string
  status?: string
}

export interface MemberUserQuery extends PageQuery {
  mobile?: string
  nickname?: string
  memberStatus?: string
}

export interface MemberOrderQuery extends PageQuery {
  userId?: number
  status?: string
  outTradeNo?: string
}

export interface MemberLogQuery extends PageQuery {
  userId?: number
  operatorId?: number
  actionType?: string
}

export interface MemberActionPayload {
  days?: number
  reason: string
}

export function getMemberPlans(
  params: MemberPlanQuery,
): Promise<ApiResult<PageResult<MemberPlan>>> {
  return get<PageResult<MemberPlan>>('/admin/member/plans', params as Record<string, unknown>)
}

export function saveMemberPlan(data: Partial<MemberPlan>): Promise<ApiResult<MemberPlan>> {
  if (data.id) return put<MemberPlan>(`/admin/member/plans/${data.id}`, data)
  return post<MemberPlan>('/admin/member/plans', data)
}

export function getMemberBenefitRules(
  params: MemberBenefitRuleQuery,
): Promise<ApiResult<PageResult<MemberBenefitRule>>> {
  return get<PageResult<MemberBenefitRule>>(
    '/admin/member/benefit-rules',
    params as Record<string, unknown>,
  )
}

export function saveMemberBenefitRule(
  data: Partial<MemberBenefitRule>,
): Promise<ApiResult<MemberBenefitRule>> {
  return put<MemberBenefitRule>(`/admin/member/benefit-rules/${data.id}`, data)
}

export function getMemberUsers(
  params: MemberUserQuery,
): Promise<ApiResult<PageResult<MemberUser>>> {
  return get<PageResult<MemberUser>>('/admin/member/users', params as Record<string, unknown>)
}

export function grantMember(
  userId: number,
  data: MemberActionPayload,
): Promise<ApiResult<boolean>> {
  return post<boolean>(`/admin/member/users/${userId}/grant`, data)
}

export function extendMember(
  userId: number,
  data: MemberActionPayload,
): Promise<ApiResult<boolean>> {
  return post<boolean>(`/admin/member/users/${userId}/extend`, data)
}

export function cancelMember(
  userId: number,
  data: Pick<MemberActionPayload, 'reason'>,
): Promise<ApiResult<boolean>> {
  return post<boolean>(`/admin/member/users/${userId}/cancel`, data)
}

export function getMemberOrders(
  params: MemberOrderQuery,
): Promise<ApiResult<PageResult<MemberOrder>>> {
  return get<PageResult<MemberOrder>>('/admin/member/orders', params as Record<string, unknown>)
}

export function getMemberLogs(
  params: MemberLogQuery,
): Promise<ApiResult<PageResult<MemberOperationLog>>> {
  return get<PageResult<MemberOperationLog>>('/admin/member/logs', params as Record<string, unknown>)
}
