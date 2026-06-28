import { envConfig } from '@/config/env'
import {
  clearAuth,
  getToken,
  setToken,
} from '@/services/auth-storage'
import type { ApiResult } from './types'

interface BackendResponse<T = unknown> {
  code: number | string
  msg?: string
  res?: T
  message?: string
  result?: T
}

export interface RequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: unknown
  params?: Record<string, unknown>
  header?: Record<string, string>
  timeout?: number
  showLoading?: boolean
  showError?: boolean
  withToken?: boolean
  contentType?: 'form' | 'json'
  skipDuplicateCheck?: boolean
}

interface UploadConfig {
  url: string
  filePath: string
  name?: string
  formData?: Record<string, string>
  header?: Record<string, string>
  showLoading?: boolean
  showError?: boolean
  withToken?: boolean
}

type RequestBody = string | ArrayBuffer | Record<string, unknown> | undefined

const ERROR_CODE_MAP: Record<string, string> = {
  '400': '请求参数错误',
  '401': '登录已过期，请重新登录',
  '403': '没有权限访问',
  '404': '请求资源不存在',
  '500': '服务器内部错误',
  '502': '网关错误',
  '503': '服务不可用',
  '1001': '鉴权失败，请重新登录',
  '1002': '参数错误',
  '1003': 'JSON 格式错误',
  '1004': '请求中包含非法字符',
  '1005': '数据不存在',
  '1006': '数据已存在',
  '1008': '身份证识别失败',
  '1009': '拍拍豆余额不足',
  '1010': '请先完成实名认证',
  '1011': '该约拍要求信用保证金',
  '1012': '密码错误',
  '1013': '实名认证失败',
  '1014': '身份证识别次数已达上限',
  '1015': '图片水印处理失败',
  '2001': '微信服务通信失败',
  '2002': '微信下单失败',
  '2003': '微信支付失败',
}

const pendingRequests = new Set<string>()
let refreshPromise: Promise<string> | null = null
let redirectingToLogin = false

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function isArrayBuffer(value: unknown): value is ArrayBuffer {
  return typeof ArrayBuffer !== 'undefined' && value instanceof ArrayBuffer
}

function parseResponsePayload(raw: unknown): unknown {
  if (typeof raw !== 'string') return raw
  const text = raw.trim()
  if (!text) return null
  try {
    return JSON.parse(text) as unknown
  } catch {
    return raw
  }
}

// 提取 res/result 字段值：仅对"非空字符串"尝试二次 JSON 解析（兼容后端把
// JSON 再次序列化进 res 的情况，如 /user/code）。其余原值保留，确保 falsy
// 值（0 / false / '' / null）和对象不被强制转 null。
function extractDataField(value: unknown): unknown {
  if (typeof value === 'string' && value.trim()) {
    try {
      return JSON.parse(value) as unknown
    } catch {
      return value
    }
  }
  return value
}

export function mapBackendResponse<T>(raw: unknown): ApiResult<T> {
  const payload = parseResponsePayload(raw)
  if (isRecord(payload) && Object.prototype.hasOwnProperty.call(payload, 'code')) {
    const response = payload as unknown as BackendResponse<T>
    const code = String(response.code ?? '-1')
    const hasRes = Object.prototype.hasOwnProperty.call(response, 'res')
    const hasResult = Object.prototype.hasOwnProperty.call(response, 'result')
    // 后端 /user/code 接口会把 wxPayClient.code2Session 返回的 JSON 字符串再次
    // 序列化进 res 字段（参考后端 LoginController.code() + WXPayClient.code2Session）。
    // 所以前端的 res 可能拿到 string 而不是 object。这里兼容：
    // 如果是 string，再 JSON.parse 一次；如果已经是 object，直接用。
    let data: unknown = null
    if (hasRes) {
      data = extractDataField(response.res)
    } else if (hasResult) {
      data = extractDataField(response.result)
    }
    const message = response.msg ?? response.message ?? ERROR_CODE_MAP[code] ?? ''
    return {
      success: code === '200',
      data: data as T,
      code,
      message,
    }
  }
  return { success: true, data: payload as T, code: '200', message: '' }
}

function flattenFormValue(
  output: Record<string, string | number | boolean>,
  key: string,
  value: unknown,
): void {
  if (value === undefined || value === null || value === '') return
  if (Array.isArray(value)) {
    value.forEach((item, index) => flattenFormValue(output, `${key}[${index}]`, item))
    return
  }
  if (isRecord(value)) {
    Object.entries(value).forEach(([childKey, childValue]) => {
      flattenFormValue(output, key ? `${key}.${childKey}` : childKey, childValue)
    })
    return
  }
  if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
    output[key] = value
  }
}

function toFormData(data: unknown): Record<string, string | number | boolean> {
  if (!isRecord(data)) return {}
  const output: Record<string, string | number | boolean> = {}
  Object.entries(data).forEach(([key, value]) => flattenFormValue(output, key, value))
  return output
}

function normalizeJsonData(data: unknown): RequestBody {
  if (data === undefined) return undefined
  if (typeof data === 'string' || isArrayBuffer(data)) return data
  if (isRecord(data)) return data
  return {}
}

function buildUrl(url: string, params?: Record<string, unknown>): string {
  if (!params) return url
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&')
  return query ? `${url}${url.includes('?') ? '&' : '?'}${query}` : url
}

function createRequestKey(config: RequestConfig): string {
  return `${config.method || 'GET'}:${config.url}:${JSON.stringify(config.params || {})}:${JSON.stringify(config.data || {})}`
}

function redirectToLogin(): void {
  clearAuth()
  if (redirectingToLogin) return
  redirectingToLogin = true
  uni.reLaunch({
    url: '/pages/login/index',
    complete: () => setTimeout(() => { redirectingToLogin = false }, 300),
  })
}

function refreshToken(): Promise<string> {
  if (refreshPromise) return refreshPromise
  refreshPromise = new Promise<string>((resolve, reject) => {
    const currentToken = getToken()
    if (!currentToken) {
      reject(new Error('登录状态已失效'))
      return
    }
    uni.request({
      url: buildUrl(`${envConfig.apiBaseUrl}/user/token`),
      method: 'GET',
      header: { Token: currentToken },
      success: (res) => {
        if (res.statusCode !== 200) {
          reject(new Error('刷新登录状态失败'))
          return
        }
        const result = mapBackendResponse<{ token?: string } | string>(res.data)
        const nextToken = typeof result.data === 'string' ? result.data : result.data?.token
        if (!result.success || !nextToken) {
          reject(new Error(result.message || '刷新登录状态失败'))
          return
        }
        setToken(nextToken)
        resolve(nextToken)
      },
      fail: () => reject(new Error('刷新登录状态失败')),
    })
  })
    .catch((error) => {
      redirectToLogin()
      throw error
    })
    .finally(() => { refreshPromise = null })
  return refreshPromise
}

function showBusinessGuide(code: string): void {
  if (code === '1010') {
    uni.showModal({
      title: '需要实名认证',
      content: '完成实名认证后才能继续当前操作。',
      confirmText: '去认证',
      success: ({ confirm }) => confirm && uni.navigateTo({ url: '/pages-sub/user/realname' }),
    })
  } else if (code === '1009') {
    uni.showModal({
      title: '拍拍豆不足',
      content: '当前拍拍豆余额不足，请先充值。',
      confirmText: '去充值',
      success: ({ confirm }) => confirm && uni.navigateTo({ url: '/pages-sub/user/recharge' }),
    })
  } else if (code === '1011') {
    uni.showModal({
      title: '该约拍要求信用保证金',
      content: '当前版本暂未开放保证金服务，暂时无法报名该约拍。',
      confirmText: '知道了',
      showCancel: false,
    })
  }
}

export function request<T = unknown>(config: RequestConfig): Promise<ApiResult<T>> {
  const {
    url,
    method = 'GET',
    data,
    params,
    header = {},
    timeout = 15000,
    showLoading = false,
    showError = true,
    withToken = true,
    contentType = 'form',
    skipDuplicateCheck = false,
  } = config

  const fullUrl = buildUrl(url.startsWith('http') ? url : `${envConfig.apiBaseUrl}${url}`, params)
  const requestKey = createRequestKey(config)
  if (!skipDuplicateCheck && pendingRequests.has(requestKey)) {
    return Promise.reject(new Error('请求正在处理中，请勿重复操作'))
  }
  pendingRequests.add(requestKey)

  const headers: Record<string, string> = {
    'Content-Type': contentType === 'json' ? 'application/json' : 'application/x-www-form-urlencoded',
    ...header,
  }
  if (withToken) {
    const token = getToken()
    if (token) headers.Token = token
  }

  const requestData: RequestBody = method === 'GET'
    ? undefined
    : contentType === 'json'
      ? normalizeJsonData(data)
      : toFormData(data)

  if (showLoading) uni.showLoading({ title: '加载中...', mask: true })

  return new Promise<ApiResult<T>>((resolve, reject) => {
    uni.request({
      url: fullUrl,
      method,
      data: requestData,
      header: headers,
      timeout,
      success: async (res) => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          const message = ERROR_CODE_MAP[String(res.statusCode)] || `网络错误（${res.statusCode}）`
          if (showError) uni.showToast({ title: message, icon: 'none' })
          reject(new Error(message))
          return
        }

        const result = mapBackendResponse<T>(res.data)
        if (result.success) {
          resolve(result)
          return
        }

        if (withToken && url !== '/user/token' && (result.code === '401' || result.code === '1001')) {
          try {
            pendingRequests.delete(requestKey)
            await refreshToken()
            resolve(await request<T>({ ...config, skipDuplicateCheck: true }))
          } catch (error) {
            reject(error)
          }
          return
        }

        showBusinessGuide(result.code)
        const message = result.message || ERROR_CODE_MAP[result.code] || '请求失败'
        if (showError && !['1009', '1010', '1011'].includes(result.code)) {
          uni.showToast({ title: message, icon: 'none' })
        }
        reject(new Error(message))
      },
      fail: (error) => {
        const message = error.errMsg?.includes('timeout') ? '请求超时，请稍后重试' : '网络异常，请检查网络连接'
        if (showError) uni.showToast({ title: message, icon: 'none' })
        reject(new Error(message))
      },
      complete: () => {
        pendingRequests.delete(requestKey)
        if (showLoading) uni.hideLoading()
      },
    })
  })
}

export function upload<T = unknown>(config: UploadConfig): Promise<ApiResult<T>> {
  const {
    url,
    filePath,
    name = 'file',
    formData = {},
    header = {},
    showLoading = true,
    showError = true,
    withToken = true,
  } = config

  const headers: Record<string, string> = { ...header }
  if (withToken) {
    const token = getToken()
    if (token) headers.Token = token
  }
  if (showLoading) uni.showLoading({ title: '上传中...', mask: true })

  return new Promise<ApiResult<T>>((resolve, reject) => {
    uni.uploadFile({
      url: url.startsWith('http') ? url : `${envConfig.apiBaseUrl}${url}`,
      filePath,
      name,
      formData,
      header: headers,
      success: (res) => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(new Error(`上传失败（${res.statusCode}）`))
          return
        }
        const result = mapBackendResponse<T>(res.data)
        if (result.success) resolve(result)
        else reject(new Error(result.message || '上传失败'))
      },
      fail: (error) => {
        const message = error.errMsg || '上传失败，请检查网络连接'
        if (showError) uni.showToast({ title: message, icon: 'none' })
        reject(new Error(message))
      },
      complete: () => { if (showLoading) uni.hideLoading() },
    })
  })
}

export function get<T = unknown>(url: string, params?: Record<string, unknown>, options?: Partial<RequestConfig>): Promise<ApiResult<T>> {
  return request<T>({ url, method: 'GET', params, ...options })
}

export function post<T = unknown>(url: string, data?: unknown, options?: Partial<RequestConfig>): Promise<ApiResult<T>> {
  return request<T>({ url, method: 'POST', data, ...options })
}

export function put<T = unknown>(url: string, data?: unknown, options?: Partial<RequestConfig>): Promise<ApiResult<T>> {
  return request<T>({ url, method: 'PUT', data, ...options })
}

export function del<T = unknown>(url: string, params?: Record<string, unknown>, options?: Partial<RequestConfig>): Promise<ApiResult<T>> {
  return request<T>({ url, method: 'DELETE', params, ...options })
}

export default { request, upload, get, post, put, del }
