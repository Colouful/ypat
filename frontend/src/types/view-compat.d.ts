import '@/api/types'

declare module '@/api/types' {
  interface YpatInfo {
    title?: string
  }

  interface OauthInfo {
    credate?: string
    reason?: string
  }

  interface Article {
    author?: string
  }

  interface Bill {
    description?: string
    amount?: number
    balance?: number
  }
}
