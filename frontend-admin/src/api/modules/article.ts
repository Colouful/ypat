import { get, post } from '../request'
import type { PageResult, PageQuery } from '../types'

export interface Article { id: number; title: string; describ: string; content: string; imgpath: string; credate: string; status: string; statusTxt: string; flag: string; readtimes: number }
export interface ArticleListQuery extends PageQuery { name?: string; status?: string }
export const getArticleList = (params: ArticleListQuery) => get<PageResult<Article>>('/admin/article/list', params as Record<string, unknown>)
export const getArticleDetail = (id: number) => get<Article>('/admin/article/detail', { id })
export const saveArticle = (data: Partial<Article>) => post('/admin/article/save', data)
export const upDownArticle = (id: number, status: string) => post('/admin/article/upDown', undefined, { params: { id, status } })
