/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_ENV?: 'development' | 'staging' | 'production'
  readonly VITE_API_BASE_URL?: string
  readonly VITE_IMAGE_BASE_URL?: string
  readonly VITE_WX_APPID?: string
  readonly VITE_BD_APPID?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}