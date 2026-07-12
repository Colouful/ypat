/**
 * 作品相关类型定义
 */

export type WorkMediaType = 'IMAGE' | 'VIDEO'

export interface WorkMedia {
  id?: number
  type: WorkMediaType | '1' | '2'
  url: string
  fileSize?: number
  mime?: string
  width?: number
  height?: number
  duration?: number
}

export interface WorkTag {
  id: number
  code: string
  name: string
  sortNo?: number
}

export interface WorkUser {
  id: number
  nickname?: string
  avatar?: string
  avatarurl?: string
  imgpath?: string
  gender?: string
  profession?: string
  profess?: string
  city?: string
  area?: string
  activeTime?: string
}

export interface WorkListItem {
  id: number
  description: string
  coverUrl?: string
  mediaType: '1' | '2'
  isVideo?: '0' | '1'
  userId?: number
  nickname?: string
  avatar?: string
  gender?: string
  profession?: string
  city?: string
  area?: string
  activeTime?: string
  readCount?: number
  likeCount?: number
  favoriteCount?: number
  tags?: string[]
  publishTime?: string
}

export interface WorkDetail {
  id: number
  description: string
  device?: string
  shootLocation?: string
  returnPhotoFlag?: 0 | 1
  mediaType: '1' | '2'
  isNationwide?: 0 | 1
  status?: string
  readCount: number
  likeCount: number
  favoriteCount: number
  publishTime?: string
  medias: WorkMedia[]
  tags: WorkTag[]
  user: WorkUser
  isLiked: boolean
  isFavorited: boolean
  isApplied: boolean
  isOwner: boolean
  likeflag?: string
  liked?: boolean
  colflag?: string
  favoriteflag?: string
  favorited?: boolean
  favoriteFlag?: string
  userid?: number
  userId?: number
}

export interface WorkSubmitParams {
  description: string
  device?: string
  shootLocation?: string
  returnPhotoFlag: '0' | '1'
  mediaType: '1' | '2'
  isNationwide?: '0' | '1'
  mediaIds: string  // 逗号分隔
  tagIds: string    // 逗号分隔
}

export interface WorkListParams {
  page?: number
  size?: number
  category?: '推荐' | '同城' | '模特' | '摄影' | '化妆' | '修图'
  city?: string
  gender?: '1' | '2'
  profession?: string
  tagIds?: string
}

export interface WorkListResult {
  page: number
  size: number
  total: number
  hasMore?: boolean
  items: WorkListItem[]
}

export interface WorkLikeParams {
  workId: number
}

export interface WorkFavoriteParams {
  workId: number
}

export interface WorkComplainParams {
  workId: number
  reason: string
  contact?: string
  content?: string
  pics?: string
  blockFlag?: '0' | '1'
}

export interface WorkQuickApplyParams {
  workId: number
  reason: string
  mobile?: string
  wx?: string
}

export interface WorkQuickApplyResult {
  workId: number
  authorId: number
  authorNickname?: string
  target: string
  targetLabel: string
  profession?: string
}
