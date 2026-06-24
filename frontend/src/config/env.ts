export interface EnvConfig {
  apiBaseUrl: string
  imageBaseUrl: string
  env: 'development' | 'staging' | 'production'
}

const configs: Record<string, EnvConfig> = {
  development: {
    apiBaseUrl: 'https://www.91qupaier.com',
    imageBaseUrl: 'https://www.91qupaier.com/',
    env: 'development',
  },
  staging: {
    apiBaseUrl: 'https://www.91qupaier.com',
    imageBaseUrl: 'https://www.91qupaier.com/',
    env: 'staging',
  },
  production: {
    apiBaseUrl: 'https://www.91qupaier.com',
    imageBaseUrl: 'https://www.91qupaier.com/',
    env: 'production',
  },
}

const currentEnv = import.meta.env.VITE_APP_ENV || 'development'

export const envConfig: EnvConfig = configs[currentEnv] || configs.development
