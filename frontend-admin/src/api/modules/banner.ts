import { get, post } from '../request'
import type { PageResult, PageQuery } from '../types'

export type BannerJumpType = 'miniapp' | 'web'

export interface Banner {
  id: number
  title: string
  imgpath: string
  credate: string
  status: string
  statusTxt: string
  jumpflag?: string
  jumptype?: BannerJumpType | (string & {})
  jumpurl?: string
}

export function isBannerJumpType(value: string | undefined): value is BannerJumpType {
  return value === 'miniapp' || value === 'web'
}

export function isValidBannerJumpTarget(type: BannerJumpType, target: string): boolean {
  const trimmedTarget = target.trim()
  if (type === 'miniapp') return trimmedTarget.startsWith('/pages/') || trimmedTarget.startsWith('/pages-sub/')
  const normalizedTarget = trimmedTarget.toLowerCase()
  return normalizedTarget.startsWith('http://') || normalizedTarget.startsWith('https://')
}

export interface BannerListQuery extends PageQuery { name?: string; status?: string }
export const getBannerList = (params: BannerListQuery) => get<PageResult<Banner>>('/admin/banner/list', params as Record<string, unknown>)
export const getBannerDetail = (id: number) => get<Banner>('/admin/banner/detail', { id })
export const saveBanner = (data: Partial<Banner>) => post('/admin/banner/save', data)
export const upDownBanner = (id: number, status: string) => post('/admin/banner/upDown', undefined, { params: { id, status } })
