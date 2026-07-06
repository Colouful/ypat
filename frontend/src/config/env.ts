export interface EnvConfig {
  apiBaseUrl: string
  imageBaseUrl: string
  imageUploadApiBaseUrl: string
  env: 'development' | 'staging' | 'production'
}

const VALID_ENVS: EnvConfig['env'][] = ['development', 'staging', 'production']

function normalizeBaseUrl(value: string): string {
  return value.trim().replace(/\/+$/, '')
}

/**
 * 拒绝作为 production 地址的列表（任务要求：production 不得使用任何 staging 串用地址）
 */
const FORBIDDEN_PRODUCTION_HOSTS = [
  'panghu.work',
  'www.panghu.work',
  'localhost',
  '127.0.0.1',
]

const DEFAULT_DEVELOPMENT_API_BASE_URL = 'http://localhost:8080/api'
const DEFAULT_DEVELOPMENT_IMAGE_BASE_URL = 'http://localhost:8080/files'

/**
 * 解析 https URL 的 host 与 port。
 * 不使用 WHATWG `new URL()`：微信小程序运行时没有全局 URL 构造函数，
 * 直接 new URL() 会抛错。此处用字符串解析，H5 与小程序均可用。
 */
function parseHttpsHostPort(value: string): { hostname: string; port: string } | null {
  const match = /^https:\/\/([^/?#]+)/.exec(value)
  if (!match) {
    return null
  }
  let authority = match[1]
  const atIndex = authority.lastIndexOf('@')
  if (atIndex !== -1) {
    authority = authority.slice(atIndex + 1)
  }
  const colonIndex = authority.lastIndexOf(':')
  if (colonIndex !== -1) {
    return { hostname: authority.slice(0, colonIndex), port: authority.slice(colonIndex + 1) }
  }
  return { hostname: authority, port: '' }
}

function assertSecureRemoteUrl(
  env: 'staging' | 'production',
  name: string,
  value: string,
): void {
  if (!value.startsWith('https://')) {
    throw new Error(`${name} must use HTTPS in ${env} environment, got: ${value}`)
  }
  const parsed = parseHttpsHostPort(value)
  if (!parsed) {
    throw new Error(`${name} is not a valid URL: ${value}`)
  }
  const { hostname, port } = parsed
  if (hostname === 'localhost' || /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/.test(hostname)) {
    throw new Error(`${name} cannot use localhost or IP address in ${env} environment, got: ${hostname}`)
  }
  // 禁止显式非标准端口
  if (port && port !== '443') {
    throw new Error(`${name} cannot use non-standard port in ${env} environment, got: ${port}`)
  }
  // production 专属：禁止 staging 域名串用
  if (env === 'production' && FORBIDDEN_PRODUCTION_HOSTS.includes(hostname)) {
    throw new Error(
      `${name} uses forbidden hostname '${hostname}' in production environment. ` +
        `Production must have an independent domain — not a staging alias.`,
    )
  }
}

function getEnvConfig(): EnvConfig {
  const rawEnv = import.meta.env.VITE_APP_ENV || 'development'
  if (!VALID_ENVS.includes(rawEnv as EnvConfig['env'])) {
    throw new Error(`不支持的运行环境：${rawEnv}`)
  }

  const env = rawEnv as EnvConfig['env']
  const rawApiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''

  if (env === 'staging') {
    if (!rawApiBaseUrl) {
      throw new Error('预发环境必须配置 VITE_API_BASE_URL')
    }
    assertSecureRemoteUrl('staging', 'API 地址', rawApiBaseUrl)
  }

  if (env === 'production') {
    if (!rawApiBaseUrl) {
      throw new Error('生产环境必须配置 VITE_API_BASE_URL，未配置 production 构建失败')
    }
    assertSecureRemoteUrl('production', 'API 地址', rawApiBaseUrl)
  }

  const apiBaseUrl = normalizeBaseUrl(rawApiBaseUrl || DEFAULT_DEVELOPMENT_API_BASE_URL)
  const imageBaseUrl = normalizeBaseUrl(
    import.meta.env.VITE_IMAGE_BASE_URL
      || (rawApiBaseUrl ? apiBaseUrl : DEFAULT_DEVELOPMENT_IMAGE_BASE_URL),
  )
  const imageUploadApiBaseUrl = normalizeBaseUrl(import.meta.env.VITE_IMAGE_UPLOAD_API_BASE_URL || apiBaseUrl)

  if (env === 'staging') {
    assertSecureRemoteUrl('staging', '图片地址', imageBaseUrl)
    assertSecureRemoteUrl('staging', '图片上传地址', imageUploadApiBaseUrl)
  }

  if (env === 'production') {
    assertSecureRemoteUrl('production', '图片地址', imageBaseUrl)
    assertSecureRemoteUrl('production', '图片上传地址', imageUploadApiBaseUrl)
  }

  return { apiBaseUrl, imageBaseUrl, imageUploadApiBaseUrl, env }
}

export const envConfig = getEnvConfig()
