import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { get, post } from '@/api/request'

export interface UserInfo {
  id: number
  gender: string
  nickname: string
  profess: string
  mobile: string
  ppd: number
  avatarurl: string
  realnameflag: string
  creditflag: string
  pubtimes: number
  rectimes: number
  coltimes: number
  status: string
  province: string
  city: string
  openid: string
  imgpath: string
  channel: string
}

export interface LoginParams {
  openid: string
  encryptedData?: string
  sessionKey?: string
  iv?: string
  nickname?: string
  avatarurl?: string
  gender?: string
  channel?: string
}

export const useUserStore = defineStore(
  'user',
  () => {
    const userInfo = ref<UserInfo | null>(null)
    const token = ref<string>('')
    const isLoggedIn = computed(() => !!token.value)
    const unreadCount = ref<number>(0)

    /**
     * Login via POST /user/login
     * Backend returns UserInfo + token in response body
     */
    async function login(params: LoginParams) {
      const res = await post<{ token: string } & UserInfo>('/user/login', params)
      if (res.data) {
        // Extract token from response
        const responseToken = res.data.token
        if (responseToken) {
          token.value = responseToken
          uni.setStorageSync('ypat_token', responseToken)
        }
        // The rest of the response data is user info
        const { token: _token, ...user } = res.data
        userInfo.value = user as unknown as UserInfo
        uni.setStorageSync('ypat_user_info', userInfo.value)
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
      uni.removeStorageSync('ypat_token')
      uni.removeStorageSync('ypat_user_info')
      uni.reLaunch({ url: '/pages/home/index' })
    }

    /**
     * Restore session from local storage
     */
    function restoreSession() {
      const storedToken = uni.getStorageSync('ypat_token')
      const storedUserInfo = uni.getStorageSync('ypat_user_info')
      if (storedToken) {
        token.value = storedToken
      }
      if (storedUserInfo) {
        try {
          userInfo.value = typeof storedUserInfo === 'string'
            ? JSON.parse(storedUserInfo)
            : storedUserInfo
        } catch {
          userInfo.value = null
        }
      }
    }

    /**
     * Refresh token via GET /user/token with current token in header
     */
    async function refreshToken() {
      if (!token.value) {
        logout()
        return
      }
      try {
        const res = await get<{ token: string }>('/user/token', undefined, {
          header: { Token: token.value },
        })
        if (res.data?.token) {
          token.value = res.data.token
          uni.setStorageSync('ypat_token', res.data.token)
        }
      } catch {
        logout()
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
          uni.setStorageSync('ypat_user_info', res.data)
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
      refreshToken,
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
            const t = uni.getStorageSync('ypat_token')
            const u = uni.getStorageSync('ypat_user_info')
            return JSON.stringify({ token: t || '', userInfo: u || null })
          }
          return null
        },
        setItem(key: string, value: string) {
          if (key === 'ypat_user') {
            try {
              const parsed = JSON.parse(value)
              if (parsed.token) {
                uni.setStorageSync('ypat_token', parsed.token)
              }
              if (parsed.userInfo) {
                uni.setStorageSync('ypat_user_info', parsed.userInfo)
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
