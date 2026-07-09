import { get, put } from '../request'
import type { ApiResult, PageQuery, PageResult } from '../types'

export interface InviteConfig {
  id?: number
  enabled: string
  rewardPpd: number
  rewardUnit?: string
  ruleText: string
  shareTitle: string
  landingTitle: string
  createdAt?: string
  updatedAt?: string
}

export interface InviteRecord {
  id: number
  inviterUserid: number
  inviteeUserid: number
  inviteCode?: string
  source?: string
  rewardPpd?: number
  credate?: string
  inviteeNickname?: string
  inviteeMobileMask?: string
}

export interface InviteRecordQuery extends PageQuery {
  inviterUserid?: number
  inviteeUserid?: number
  inviteCode?: string
  source?: string
}

export function getInviteConfig(): Promise<ApiResult<InviteConfig>> {
  return get<InviteConfig>('/admin/invite/config')
}

export function saveInviteConfig(data: InviteConfig): Promise<ApiResult<InviteConfig>> {
  return put<InviteConfig>('/admin/invite/config', data)
}

export function getInviteRecords(
  params: InviteRecordQuery,
): Promise<ApiResult<PageResult<InviteRecord>>> {
  return get<PageResult<InviteRecord>>('/admin/invite/records', params as Record<string, unknown>)
}
