/**
 * Axios 请求实例 + 拦截器 + ResponseApiBody 适配器
 */

import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import type { ResponseApiBody, ApiResult } from './types'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器：注入 Token Header
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (isFormData(config.data)) {
      unsetContentType(config.headers)
    }

    const token = getToken()
    if (token) {
      // 后端 JWT Header 名为 "Token"（Const.HEADER_STRING），无 Bearer 前缀
      config.headers['Token'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

function isFormData(value: unknown): value is FormData {
  return typeof FormData !== 'undefined' && value instanceof FormData
}

function unsetContentType(headers: InternalAxiosRequestConfig['headers']): void {
  headers.delete?.('Content-Type')
  headers.delete?.('content-type')
  delete (headers as Record<string, unknown>)['Content-Type']
  delete (headers as Record<string, unknown>)['content-type']
}

// 响应拦截器：解包 ResponseApiBody {code, msg, res} → ApiResult<T>
service.interceptors.response.use(
  (response: AxiosResponse<ResponseApiBody>) => {
    const res = response.data

    // Blob 下载直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 适配后端 ResponseApiBody 格式
    const result: ApiResult = {
      code: res.code,
      msg: res.msg,
      data: res.res,
      success: res.code === 200,
    }

    // 业务成功
    if (result.success) {
      return result as unknown as AxiosResponse
    }

    // 业务错误
    ElMessage.error(result.msg || '请求失败')
    return Promise.reject(new Error(result.msg || '请求失败'))
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          ElMessage.error('登录已失效，请重新登录')
          removeToken()
          // 避免在登录页重复跳转
          if (window.location.pathname !== '/login') {
            window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`
          }
          break
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default: {
          // 尝试解析后端返回的错误信息
          const errorData = error.response.data
          if (errorData && typeof errorData === 'object' && 'msg' in errorData) {
            ElMessage.error(errorData.msg)
          } else {
            ElMessage.error(`请求错误 (${status})`)
          }
        }
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.message?.includes('Network Error')) {
      ElMessage.error('网络异常，请检查网络连接')
    } else {
      ElMessage.error(error.message || '未知错误')
    }
    return Promise.reject(error)
  },
)

/**
 * 通用请求方法
 */
export function request<T = unknown>(config: AxiosRequestConfig): Promise<ApiResult<T>> {
  return service(config) as unknown as Promise<ApiResult<T>>
}

/**
 * GET 请求
 */
export function get<T = unknown>(
  url: string,
  params?: Record<string, unknown>,
  config?: AxiosRequestConfig,
): Promise<ApiResult<T>> {
  return request<T>({ method: 'GET', url, params, ...config })
}

/**
 * POST 请求
 */
export function post<T = unknown>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): Promise<ApiResult<T>> {
  return request<T>({ method: 'POST', url, data, ...config })
}

/**
 * PUT 请求
 */
export function put<T = unknown>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): Promise<ApiResult<T>> {
  return request<T>({ method: 'PUT', url, data, ...config })
}

/**
 * Blob 下载
 */
export function download(
  url: string,
  params?: Record<string, unknown>,
  config?: AxiosRequestConfig,
): Promise<AxiosResponse<Blob>> {
  return service({
    method: 'GET',
    url,
    params,
    responseType: 'blob',
    ...config,
  })
}

export default service
