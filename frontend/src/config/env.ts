export interface EnvConfig {
  apiBaseUrl: string
  imageBaseUrl: string
  env: 'development' | 'staging' | 'production'
}

function getEnvConfig(): EnvConfig {
  const env = (import.meta.env.VITE_APP_ENV || 'development') as EnvConfig['env']
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL

  if (!apiBaseUrl && env === 'production') {
    throw new Error('VITE_API_BASE_URL must be set in production environment')
  }

  const resolvedApiBaseUrl = apiBaseUrl || 'http://localhost:8081'
  const imageBaseUrl = import.meta.env.VITE_IMAGE_BASE_URL || `${resolvedApiBaseUrl}/`

  return {
    apiBaseUrl: resolvedApiBaseUrl,
    imageBaseUrl,
    env,
  }
}

export const envConfig = getEnvConfig()
