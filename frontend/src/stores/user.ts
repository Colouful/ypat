import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  id: string
  nickname: string
  gender: number
  profess: string
  mobile: string
  ppd: string
  avatarurl: string
  realnameflag: number
  creditflag: number
  pubtimes: number
  rectimes: number
  coltimes: number
  status: number
  province: string
  city: string
  openid: string
}

export const useUserStore = defineStore(
  'user',
  () => {
    const userInfo = ref<UserInfo | null>(null)
    const token = ref<string>('')
    const isLoggedIn = computed(() => !!token.value)
    const unreadCount = ref<number>(0)

    async function login(loginData: { code?: string; mobile?: string; password?: string }) {
      const res = await uni.$http.post<{ token: string; userInfo: UserInfo }>('/auth/login', loginData)
      if (res.data) {
        token.value = res.data.token
        userInfo.value = res.data.userInfo
      }
      return res
    }

    function logout() {
      token.value = ''
      userInfo.value = null
      unreadCount.value = 0
      uni.reLaunch({ url: '/pages/index/index' })
    }

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

    async function refreshToken() {
      try {
        const res = await uni.$http.post<{ token: string }>('/auth/refresh')
        if (res.data?.token) {
          token.value = res.data.token
        }
      } catch {
        logout()
      }
    }

    async function refreshUnreadCount() {
      if (!isLoggedIn.value || !userInfo.value) return
      try {
        const res = await uni.$http.get<{ count: number }>('/message/unread-count', {
          userid: userInfo.value.id,
        })
        if (res.data) {
          unreadCount.value = res.data.count
        }
      } catch {
        // silently fail
      }
    }

    function updateUserInfo(data: Partial<UserInfo>) {
      if (userInfo.value) {
        userInfo.value = { ...userInfo.value, ...data }
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
            const token = uni.getStorageSync('ypat_token')
            const userInfo = uni.getStorageSync('ypat_user_info')
            return JSON.stringify({ token, userInfo })
          }
          return null
        },
        setItem(key: string, value: string) {
          if (key === 'ypat_user') {
            try {
              const parsed = JSON.parse(value)
              uni.setStorageSync('ypat_token', parsed.token || '')
              uni.setStorageSync('ypat_user_info', parsed.userInfo || null)
            } catch {
              // ignore
            }
          }
        },
      },
    },
  }
)
