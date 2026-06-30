import { get } from '../request'
import type {
  ApiResult,
  InviteRecord,
  InviteRecordListParams,
  InviteRule,
  InviteSummary,
  PageResult,
} from '../types'

/** 当前用户的邀请概览（邀请码 + 已邀请人数 + 累计奖励）。 */
export function getMyInviteInfo(): Promise<ApiResult<InviteSummary>> {
  return get('/invite/my-info')
}

/** 邀请规则（奖励数 / 文案），未登录可调用。 */
export function getInviteRule(): Promise<ApiResult<InviteRule>> {
  return get('/invite/rule', undefined, { withToken: false })
}

/** 当前用户的邀请记录分页，inviterUserid 由后端从 SecurityContext 注入，前端不能伪造。 */
export function getInviteRecords(params: InviteRecordListParams): Promise<ApiResult<PageResult<InviteRecord>>> {
  return get('/invite/records', params)
}
