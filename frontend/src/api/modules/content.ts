import { get, post } from '../request'
import type {
  ApiResult,
  PageResult,
  Banner,
  BannerListParams,
  Article,
  ArticleListParams,
  AreaInfo,
  ParamInfo,
} from '../types'

/**
 * 获取轮播图列表
 */
export function getBannerList(params: BannerListParams): Promise<ApiResult<PageResult<Banner>>> {
  return post('/banner/findPage', params)
}

/**
 * 获取文章列表
 */
export function getArticleList(params: ArticleListParams): Promise<ApiResult<PageResult<Article>>> {
  return post('/article/findPage', params)
}

/**
 * 获取文章详情
 */
export function getArticleDetail(id: number): Promise<ApiResult<Article>> {
  return get('/article/get', { id })
}

/**
 * 获取地区列表
 */
export function getAreaList(): Promise<ApiResult<AreaInfo[]>> {
  return get('/area/list')
}

/**
 * 获取订阅消息模板 ID 列表
 */
export function getTemplateIds(): Promise<ApiResult<string[]>> {
  return get('/tmplid/list')
}

/**
 * 获取系统参数列表
 */
export function getParams(): Promise<ApiResult<ParamInfo[]>> {
  return get('/param/list')
}
