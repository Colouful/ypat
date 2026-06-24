import type { UserInfo } from '@/api/types'

const TOKEN_KEY = 'ypat_token'
const USER_KEY = 'ypat_user_info'

let resetHandler: (() => void) | null = null

export function getToken(): string {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  if (token) uni.setStorageSync(TOKEN_KEY, token)
  else uni.removeStorageSync(TOKEN_KEY)
}

export function removeToken(): void {
  uni.removeStorageSync(TOKEN_KEY)
}

export function getStoredUserInfo(): UserInfo | null {
  const raw = uni.getStorageSync(USER_KEY)
  if (!raw) return null
  try {
    return typeof raw === 'string' ? (JSON.parse(raw) as UserInfo) : (raw as UserInfo)
  } catch {
    uni.removeStorageSync(USER_KEY)
    return null
  }
}

export function setStoredUserInfo(info: UserInfo): void {
  uni.setStorageSync(USER_KEY, info)
}

export function registerAuthResetHandler(handler: (() => void) | null): () => void {
  resetHandler = handler
  return () => {
    if (resetHandler === handler) resetHandler = null
  }
}

export function clearAuth(): void {
  removeToken()
  uni.removeStorageSync(USER_KEY)
  resetHandler?.()
}
