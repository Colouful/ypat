import type { UserInfo } from '@/api/types'

const TOKEN_KEY = 'ypat_token'
const USER_KEY = 'ypat_user_info'

export function getToken(): string {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  uni.setStorageSync(TOKEN_KEY, token)
}

export function removeToken(): void {
  uni.removeStorageSync(TOKEN_KEY)
}

export function getStoredUserInfo(): UserInfo | null {
  const raw = uni.getStorageSync(USER_KEY)
  if (!raw) return null
  try {
    return typeof raw === 'string' ? JSON.parse(raw) : raw
  } catch {
    return null
  }
}

export function setStoredUserInfo(info: UserInfo): void {
  uni.setStorageSync(USER_KEY, JSON.stringify(info))
}

export function clearAuth(): void {
  removeToken()
  uni.removeStorageSync(USER_KEY)
}
