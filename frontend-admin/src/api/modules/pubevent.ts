import { get } from '../request'
import type { PageResult, PageQuery } from '../types'

export interface PubEvent { id: number; dateStr: string; eventKey: string; eventKeyTxt: string; msgTimes: number }
export interface PubEventListQuery extends PageQuery { dateStrStart?: string; dateStrEnd?: string; eventKey?: string }
export const getPubEventList = (params: PubEventListQuery) => get<PageResult<PubEvent>>('/admin/pubevent/list', params as Record<string, unknown>)
