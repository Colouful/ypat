import { get } from '../request'
import type {
  ApiResult,
  PageResult,
  Banner,
  BannerListParams,
  Article,
  ArticleListParams,
} from '../types'
import type { AreaInfo, ParamInfo } from '../types/area-types'

export function getBannerList(params: BannerListParams): Promise<ApiResult<PageResult<Banner>>> {
  return get('/banner/list', { ...params })
}

export function getArticleList(params: ArticleListParams): Promise<ApiResult<PageResult<Article>>> {
  return get('/article/list', { ...params })
}

export function getArticleDetail(id: number): Promise<ApiResult<Article>> {
  return get('/article/get', { id })
}

export function getAreaList(): Promise<ApiResult<AreaInfo>> {
  return get('/area/list')
}

export function getTemplateIds(): Promise<ApiResult<string[]>> {
  return get('/tmplid/list')
}

export function getParams(): Promise<ApiResult<ParamInfo>> {
  return get('/param/list')
}
