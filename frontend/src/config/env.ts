export interface EnvConfig {
  apiBaseUrl: string
  imageBaseUrl: string
  env: 'development' | 'staging' | 'production'
}

function getEnvConfig(): EnvConfig {
  const env = import.meta.env.VITE_APP_ENV || 'development'
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'https://www.91qupaier.com'
  const imageBaseUrl = import.meta.env.VITE_IMAGE_BASE_URL || 'https://www.91qupaier.com/'

  return {
    apiBaseUrl,
    imageBaseUrl,
    env: env as EnvConfig['env'],
  }
}

export const envConfig = getEnvConfig()
