export interface EnvConfig {
  apiBaseUrl: string
  imageBaseUrl: string
  env: 'development' | 'staging' | 'production'
}

const VALID_ENVS: EnvConfig['env'][] = ['development', 'staging', 'production']

function normalizeBaseUrl(value: string): string {
  return value.trim().replace(/\/+$/, '')
}

function getEnvConfig(): EnvConfig {
  const rawEnv = import.meta.env.VITE_APP_ENV || 'development'
  if (!VALID_ENVS.includes(rawEnv as EnvConfig['env'])) {
    throw new Error(`不支持的运行环境：${rawEnv}`)
  }

  const env = rawEnv as EnvConfig['env']
  const rawApiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''
  if (env === 'production' && !rawApiBaseUrl) {
    throw new Error('生产环境必须配置 VITE_API_BASE_URL')
  }

  const apiBaseUrl = normalizeBaseUrl(rawApiBaseUrl || 'http://localhost:8081')
  const imageBaseUrl = normalizeBaseUrl(import.meta.env.VITE_IMAGE_BASE_URL || apiBaseUrl)

  return { apiBaseUrl, imageBaseUrl, env }
}

export const envConfig = getEnvConfig()
