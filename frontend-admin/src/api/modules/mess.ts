import { get } from '../request'
import type { PageResult, PageQuery } from '../types'

export interface MessInfo {
  id: number
  ypatid: number
  sendperid: number
  recperid: number
  nickname: string
  imgpath: string
  content: string
  type?: string
  messviewflag?: string
  credate: string
}
export interface MessListQuery extends PageQuery { ypatid?: number; sendperid?: number; recperid?: number }
export const getMessList = (params: MessListQuery) => get<PageResult<MessInfo>>('/admin/mess/list', params as Record<string, unknown>)
