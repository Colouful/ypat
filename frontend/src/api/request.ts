import { envConfig } from '@/config/env'
import type { ApiResult } from './types'

/** 后端原始响应结构 */
interface BackendResponse {
  code: number
  message: string
  result: any
}

/** 错误码映射 */
const ERROR_CODE_MAP: Record<string | number, string> = {
  200: '请求成功',
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '没有权限访问',
  404: '请求资源不存在',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务不可用',
  1001: '认证失败，请重新登录',
  1002: '参数错误',
  1003: '验证码错误',
  1004: '验证码已过期',
  1005: '数据不存在',
  1006: '用户不存在',
  1007: '密码错误',
  1008: '识别失败',
  1009: '余额不足',
  1010: '未实名认证',
  1011: '信用分不足',
  1012: '订单不存在',
  1013: '实名失败',
  1014: '识别超限',
  1015: '水印失败',
}

/** 请求配置 */
interface RequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  params?: Record<string, any>
  header?: Record<string, string>
  timeout?: number
  showLoading?: boolean
  showError?: boolean
  withToken?: boolean
}

/** 文件上传配置 */
interface UploadConfig {
  url: string
  filePath: string
  name?: string
  formData?: Record<string, any>
  header?: Record<string, string>
  showLoading?: boolean
  showError?: boolean
  withToken?: boolean
}

/** 请求队列项（用于token刷新时的请求排队） */
interface PendingRequest {
  resolve: (value: any) => void
  reject: (reason: any) => void
  config: RequestConfig
}

/** 进行中的请求记录（防重复请求） */
const pendingRequests = new Map<string, boolean>()

/** token 刷新状态 */
let isRefreshing = false

/** 刷新 token 时排队的请求 */
let requestQueue: PendingRequest[] = []

/** 生成请求唯一标识 */
function generateRequestKey(config: RequestConfig): string {
  const { url, method, data, params } = config
  return `${method || 'GET'}_${url}_${JSON.stringify(data || '')}_${JSON.stringify(params || '')}`
}

/** 获取 Token */
function getToken(): string {
  return uni.getStorageSync('ypat_token') || ''
}

/** 设置 Token */
function setToken(token: string): void {
  uni.setStorageSync('ypat_token', token)
}

/** 清除登录信息 */
function clearAuth(): void {
  uni.removeStorageSync('ypat_token')
  uni.removeStorageSync('userInfo')
}

/** 跳转到登录页 */
function redirectToLogin(): void {
  clearAuth()
  uni.reLaunch({
    url: '/pages/login/index',
  })
}

/** 将后端原始响应映射为 ApiResult */
function mapResponse<T>(raw: BackendResponse): ApiResult<T> {
  return {
    success: raw.code === 200,
    data: raw.result as T,
    code: String(raw.code),
    message: raw.message,
  }
}

/** 处理 token 过期 - 尝试刷新 */
async function handleTokenExpired(config: RequestConfig): Promise<ApiResult> {
  if (isRefreshing) {
    // 正在刷新 token，将请求加入队列
    return new Promise((resolve, reject) => {
      requestQueue.push({ resolve, reject, config })
    })
  }

  isRefreshing = true

  try {
    const tokenValue = getToken()
    if (!tokenValue) {
      redirectToLogin()
      return Promise.reject(new Error('No token'))
    }

    // 调用刷新 token 接口
    const res = await new Promise<UniApp.RequestSuccessCallbackResult>((resolve, reject) => {
      uni.request({
        url: `${envConfig.apiBaseUrl}/user/token`,
        method: 'GET',
        header: {
          Token: tokenValue,
        },
        success: resolve,
        fail: reject,
      })
    })

    const raw = res.data as BackendResponse
    const result = mapResponse(raw)
    if (result.success && result.data?.token) {
      setToken(result.data.token)

      // 重新发送队列中的请求
      requestQueue.forEach(({ resolve, config: pendingConfig }) => {
        resolve(request(pendingConfig))
      })
      requestQueue = []

      // 重新发送当前请求
      return request(config)
    } else {
      // 刷新失败，跳转登录
      requestQueue.forEach(({ reject }) => {
        reject(new Error('Token refresh failed'))
      })
      requestQueue = []
      redirectToLogin()
      return Promise.reject(new Error('Token refresh failed'))
    }
  } catch (error) {
    requestQueue.forEach(({ reject }) => {
      reject(error)
    })
    requestQueue = []
    redirectToLogin()
    return Promise.reject(error)
  } finally {
    isRefreshing = false
  }
}

/** 构建带 query 参数的 URL */
function buildUrl(url: string, params?: Record<string, any>): string {
  if (!params) return url
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&')
  return query ? `${url}?${query}` : url
}

/** 统一请求方法 */
export function request<T = any>(config: RequestConfig): Promise<ApiResult<T>> {
  const {
    url,
    method = 'GET',
    data,
    params,
    header = {},
    timeout = 10000,
    showLoading = false,
    showError = true,
    withToken = true,
  } = config

  // 拼接完整 URL
  const fullUrl = buildUrl(
    url.startsWith('http') ? url : `${envConfig.apiBaseUrl}${url}`,
    params,
  )

  // 防止重复请求
  const requestKey = generateRequestKey(config)
  if (pendingRequests.has(requestKey)) {
    return Promise.reject(new Error('重复请求已被拦截'))
  }
  pendingRequests.set(requestKey, true)

  // 请求头处理
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...header,
  }

  // Token 注入
  if (withToken) {
    const token = getToken()
    if (token) {
      headers['Token'] = token
    }
  }

  // 显示 loading
  if (showLoading) {
    uni.showLoading({ title: '加载中...', mask: true })
  }

  return new Promise<ApiResult<T>>((resolve, reject) => {
    uni.request({
      url: fullUrl,
      method,
      data,
      header: headers,
      timeout,
      success: async (res) => {
        const statusCode = res.statusCode

        // HTTP 状态码非 200
        if (statusCode !== 200) {
          const errorMsg = ERROR_CODE_MAP[statusCode] || `网络错误(${statusCode})`
          if (showError) {
            uni.showToast({ title: errorMsg, icon: 'none' })
          }
          reject(new Error(errorMsg))
          return
        }

        // 将后端原始响应映射为 ApiResult
        const raw = res.data as BackendResponse
        const result = mapResponse<T>(raw)

        // 业务状态码处理
        const code = result.code

        if (code === '200') {
          resolve(result)
          return
        }

        // Token 过期，尝试刷新
        if (code === '401' || code === '1001') {
          try {
            const refreshResult = await handleTokenExpired(config)
            resolve(refreshResult as ApiResult<T>)
          } catch (error) {
            reject(error)
          }
          return
        }

        // 特殊业务码处理
        if (code === '1010') {
          // 未实名认证
          uni.showModal({
            title: '提示',
            content: '请先完成实名认证',
            confirmText: '去认证',
            success: (modalRes) => {
              if (modalRes.confirm) {
                uni.navigateTo({ url: '/pages-sub/user/realname' })
              }
            },
          })
          reject(new Error(result.message || ERROR_CODE_MAP[code]))
          return
        }

        if (code === '1009') {
          // 余额不足
          uni.showModal({
            title: '提示',
            content: '余额不足，请充值',
            confirmText: '去充值',
            success: (modalRes) => {
              if (modalRes.confirm) {
                uni.navigateTo({ url: '/pages-sub/user/recharge' })
              }
            },
          })
          reject(new Error(result.message || ERROR_CODE_MAP[code]))
          return
        }

        if (code === '1011') {
          // 信用分不足
          uni.showToast({ title: '信用分不足，无法操作', icon: 'none' })
          reject(new Error(result.message || ERROR_CODE_MAP[code]))
          return
        }

        // 其他错误码
        const errorMessage = result.message || ERROR_CODE_MAP[code] || '请求失败'
        if (showError) {
          uni.showToast({ title: errorMessage, icon: 'none' })
        }
        reject(new Error(errorMessage))
      },
      fail: (err) => {
        const errorMessage = err.errMsg?.includes('timeout')
          ? '请求超时，请检查网络'
          : '网络异常，请检查网络连接'
        if (showError) {
          uni.showToast({ title: errorMessage, icon: 'none' })
        }
        reject(new Error(errorMessage))
      },
      complete: () => {
        // 移除请求记录
        pendingRequests.delete(requestKey)
        // 隐藏 loading
        if (showLoading) {
          uni.hideLoading()
        }
      },
    })
  })
}

/** 文件上传方法 */
export function upload<T = any>(config: UploadConfig): Promise<ApiResult<T>> {
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

  const fullUrl = url.startsWith('http') ? url : `${envConfig.apiBaseUrl}${url}`

  // 请求头处理
  const headers: Record<string, string> = { ...header }

  // Token 注入
  if (withToken) {
    const token = getToken()
    if (token) {
      headers['Token'] = token
    }
  }

  if (showLoading) {
    uni.showLoading({ title: '上传中...', mask: true })
  }

  return new Promise<ApiResult<T>>((resolve, reject) => {
    uni.uploadFile({
      url: fullUrl,
      filePath,
      name,
      formData,
      header: headers,
      success: (res) => {
        if (res.statusCode !== 200) {
          const errorMsg = ERROR_CODE_MAP[res.statusCode] || `上传失败(${res.statusCode})`
          if (showError) {
            uni.showToast({ title: errorMsg, icon: 'none' })
          }
          reject(new Error(errorMsg))
          return
        }

        try {
          const raw = JSON.parse(res.data) as BackendResponse
          const result = mapResponse<T>(raw)
          if (result.success) {
            resolve(result)
          } else {
            const errorMessage = result.message || '上传失败'
            if (showError) {
              uni.showToast({ title: errorMessage, icon: 'none' })
            }
            reject(new Error(errorMessage))
          }
        } catch (e) {
          reject(new Error('响应数据解析失败'))
        }
      },
      fail: (err) => {
        const errorMessage = '上传失败，请检查网络连接'
        if (showError) {
          uni.showToast({ title: errorMessage, icon: 'none' })
        }
        reject(new Error(err.errMsg || errorMessage))
      },
      complete: () => {
        if (showLoading) {
          uni.hideLoading()
        }
      },
    })
  })
}

/** GET 请求快捷方法 */
export function get<T = any>(url: string, params?: Record<string, any>, options?: Partial<RequestConfig>): Promise<ApiResult<T>> {
  return request<T>({ url, method: 'GET', params, ...options })
}

/** POST 请求快捷方法 */
export function post<T = any>(url: string, data?: any, options?: Partial<RequestConfig>): Promise<ApiResult<T>> {
  return request<T>({ url, method: 'POST', data, ...options })
}

/** PUT 请求快捷方法 */
export function put<T = any>(url: string, data?: any, options?: Partial<RequestConfig>): Promise<ApiResult<T>> {
  return request<T>({ url, method: 'PUT', data, ...options })
}

/** DELETE 请求快捷方法 */
export function del<T = any>(url: string, params?: Record<string, any>, options?: Partial<RequestConfig>): Promise<ApiResult<T>> {
  return request<T>({ url, method: 'DELETE', params, ...options })
}

export default {
  request,
  upload,
  get,
  post,
  put,
  del,
}
