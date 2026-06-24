import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { get, post } from '@/api/request'
import { getToken, setToken, getStoredUserInfo, setStoredUserInfo, clearAuth } from '@/services/auth-storage'
import type { UserInfo } from '@/api/types'

export interface LoginParams {
  code?: string
  openid?: string
  encryptedData?: string
  sessionKey?: string
  iv?: string
  nickname?: string
  avatarurl?: string
  gender?: string
  channel?: string
  recmobile?: string
  mobile?: string
}

export const useUserStore = defineStore(
  'user',
  () => {
    const userInfo = ref<UserInfo | null>(null)
    const token = ref<string>('')
    const isLoggedIn = computed(() => !!token.value)
    const unreadCount = ref<number>(0)

    /**
     * Login flow:
     * Step 1: If params.code exists, call GET /user/code to get openid + session_key
     * Step 2: Call POST /user/login with params + openid to get token + userInfo
     * Step 3: Persist token and userInfo, update state
     */
    async function login(params: LoginParams) {
      let openid = params.openid || ''
      let sessionKey = params.sessionKey || ''

      // Step 1: Exchange code for openid + session_key
      if (params.code) {
        const codeRes = await get<{ openid: string; session_key: string }>('/user/code', { code: params.code })
        if (codeRes.data) {
          openid = codeRes.data.openid
          sessionKey = codeRes.data.session_key
        }
      }

      // Step 2: Call login endpoint
      const loginData = { ...params, openid, sessionKey }
      delete loginData.code
      const res = await post<{ token: string; userInfo: UserInfo }>('/user/login', loginData)

      // Step 3: Persist and update state
      if (res.data) {
        const responseToken = res.data.token
        if (responseToken) {
          token.value = responseToken
          setToken(responseToken)
        }
        if (res.data.userInfo) {
          userInfo.value = res.data.userInfo
          setStoredUserInfo(res.data.userInfo)
        }
      }
      return res
    }

    /**
     * Logout: clear all stored data and redirect to home tab
     */
    function logout() {
      token.value = ''
      userInfo.value = null
      unreadCount.value = 0
      clearAuth()
      uni.switchTab({ url: '/pages/home/index' })
    }

    /**
     * Restore session from local storage
     */
    function restoreSession() {
      const storedToken = getToken()
      const storedUser = getStoredUserInfo()
      if (storedToken) {
        token.value = storedToken
      }
      if (storedUser) {
        userInfo.value = storedUser
      }
    }

    /**
     * Refresh unread message count via GET /my/ypat/unread/count?userid=xxx
     */
    async function refreshUnreadCount() {
      if (!isLoggedIn.value || !userInfo.value) return
      try {
        const res = await get<number>('/my/ypat/unread/count', {
          userid: userInfo.value.id,
        })
        if (res.data !== undefined && res.data !== null) {
          unreadCount.value = typeof res.data === 'number' ? res.data : Number(res.data)
        }
      } catch {
        // silently fail
      }
    }

    /**
     * Update user info from server via GET /user/get?id=xxx
     */
    async function updateUserInfo() {
      if (!isLoggedIn.value || !userInfo.value) return
      try {
        const res = await get<UserInfo>('/user/get', {
          id: userInfo.value.id,
        })
        if (res.data) {
          userInfo.value = res.data
          setStoredUserInfo(res.data)
        }
      } catch {
        // silently fail
      }
    }

    return {
      userInfo,
      token,
      isLoggedIn,
      unreadCount,
      login,
      logout,
      restoreSession,
      refreshUnreadCount,
      updateUserInfo,
    }
  },
  {
    persist: {
      key: 'ypat_user',
      paths: ['token', 'userInfo'],
      storage: {
        getItem(key: string) {
          if (key === 'ypat_user') {
            const t = getToken()
            const u = getStoredUserInfo()
            return JSON.stringify({ token: t || '', userInfo: u || null })
          }
          return null
        },
        setItem(key: string, value: string) {
          if (key === 'ypat_user') {
            try {
              const parsed = JSON.parse(value)
              if (parsed.token) {
                setToken(parsed.token)
              }
              if (parsed.userInfo) {
                setStoredUserInfo(parsed.userInfo)
              }
            } catch {
              // ignore parse errors
            }
          }
        },
      },
    },
  },
)
