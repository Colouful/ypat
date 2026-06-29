export interface EnvConfig {
  apiBaseUrl: string
  imageBaseUrl: string
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

function assertSecureRemoteUrl(
  env: 'staging' | 'production',
  name: string,
  value: string,
): void {
  if (!value.startsWith('https://')) {
    throw new Error(`${name} must use HTTPS in ${env} environment, got: ${value}`)
  }
  let hostname: string
  try {
    hostname = new URL(value).hostname
  } catch {
    throw new Error(`${name} is not a valid URL: ${value}`)
  }
  if (hostname === 'localhost' || /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/.test(hostname)) {
    throw new Error(`${name} cannot use localhost or IP address in ${env} environment, got: ${hostname}`)
  }
  // 禁止显式非标准端口
  const port = new URL(value).port
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

  const apiBaseUrl = normalizeBaseUrl(rawApiBaseUrl || 'http://localhost:8088')
  const imageBaseUrl = normalizeBaseUrl(import.meta.env.VITE_IMAGE_BASE_URL || apiBaseUrl)

  if (env === 'staging') {
    assertSecureRemoteUrl('staging', '图片地址', imageBaseUrl)
  }

  if (env === 'production') {
    assertSecureRemoteUrl('production', '图片地址', imageBaseUrl)
  }

  return { apiBaseUrl, imageBaseUrl, env }
}

export const envConfig = getEnvConfig()