/**
 * API 通用类型定义
 */

/** 后端 ResponseApiBody 真实格式 */
export interface ResponseApiBody<T = unknown> {
  code: number
  msg: string
  res: T
}

/** 前端内部统一响应格式（适配后端 ResponseApiBody） */
export interface ApiResult<T = unknown> {
  code: number
  msg: string
  data: T
  success: boolean
}

/** 分页响应格式（Spring Data Page） */
export interface PageResult<T = unknown> {
  content: T[]
  totalElements: number
  totalPages?: number
  size?: number
  number?: number
}

/** 分页请求参数 */
export interface PageQuery {
  page?: number
  size?: number
}

/** 通用选项 */
export interface SelectOption {
  label: string
  value: string | number
}
