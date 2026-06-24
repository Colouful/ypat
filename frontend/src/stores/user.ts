import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { get, post } from '@/api/request'
import {
  clearAuth,
  getStoredUserInfo,
  getToken,
  registerAuthResetHandler,
  setStoredUserInfo,
  setToken,
} from '@/services/auth-storage'
import type {
  LoginParams,
  LoginResult,
  UserInfo,
  WxSessionResult,
} from '@/api/types'

export interface WechatLoginInput {
  code: string
  encryptedData: string
  iv: string
  channel?: string
  nickname?: string
  avatarurl?: string
  gender?: string
  recmobile?: string
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)
  const token = ref('')
  const unreadCount = ref(0)

  const isLoggedIn = computed(() => Boolean(token.value && userInfo.value?.id))

  function resetMemoryState(): void {
    token.value = ''
    userInfo.value = null
    unreadCount.value = 0
  }

  registerAuthResetHandler(resetMemoryState)

  function persistSession(nextToken: string, nextUser: UserInfo): void {
    token.value = nextToken
    userInfo.value = nextUser
    setToken(nextToken)
    setStoredUserInfo(nextUser)
  }

  function createFallbackUser(loginResult: LoginResult): UserInfo {
    return {
      id: Number(loginResult.id),
      token: loginResult.token,
      mobile: loginResult.mobile,
      nickname: loginResult.nickname,
      gender: loginResult.gender,
      profess: loginResult.profess,
    }
  }

  /**
   * 真实微信登录链路：
   * uni.login code -> /user/code -> encryptedData/sessionKey/iv -> /user/login。
   */
  async function login(input: WechatLoginInput): Promise<UserInfo> {
    if (!input.code || !input.encryptedData || !input.iv) {
      throw new Error('缺少微信手机号授权信息，请重新授权')
    }

    const sessionResult = await get<WxSessionResult>(
      '/user/code',
      { code: input.code },
      { withToken: false, showError: false },
    )

    if (!sessionResult.data?.openid || !sessionResult.data?.session_key) {
      throw new Error(sessionResult.data?.errmsg || '微信登录凭证换取失败')
    }

    const loginParams: LoginParams = {
      openid: sessionResult.data.openid,
      encryptedData: input.encryptedData,
      sessionKey: sessionResult.data.session_key,
      iv: input.iv,
      channel: input.channel || '0',
      nickname: input.nickname,
      avatarurl: input.avatarurl,
      gender: input.gender,
      recmobile: input.recmobile,
    }

    const loginResult = await post<LoginResult>(
      '/user/login',
      loginParams,
      { withToken: false, showError: false },
    )

    if (!loginResult.data?.token || !loginResult.data?.id) {
      throw new Error(loginResult.message || '登录响应缺少用户凭证')
    }

    setToken(loginResult.data.token)
    token.value = loginResult.data.token

    let completeUser = createFallbackUser(loginResult.data)
    try {
      const detailResult = await get<UserInfo>('/user/get', { id: Number(loginResult.data.id) })
      if (detailResult.data?.id) completeUser = detailResult.data
    } catch {
      // 获取完整资料失败时保留登录接口返回的基础资料，不影响本次登录。
    }

    persistSession(loginResult.data.token, completeUser)
    return completeUser
  }

  function restoreSession(): void {
    const storedToken = getToken()
    const storedUser = getStoredUserInfo()
    if (storedToken && storedUser?.id) {
      token.value = storedToken
      userInfo.value = storedUser
    } else {
      clearAuth()
    }
  }

  function logout(): void {
    clearAuth()
    uni.switchTab({ url: '/pages/home/index' })
  }

  async function updateUserInfo(): Promise<UserInfo | null> {
    if (!token.value || !userInfo.value?.id) return null
    const result = await get<UserInfo>('/user/get', { id: userInfo.value.id })
    if (result.data?.id) {
      userInfo.value = result.data
      setStoredUserInfo(result.data)
    }
    return userInfo.value
  }

  async function refreshUnreadCount(): Promise<number> {
    if (!isLoggedIn.value || !userInfo.value?.id) return 0
    try {
      const result = await get<number>('/my/ypat/unread/count', {
        userid: userInfo.value.id,
      })
      unreadCount.value = Number(result.data || 0)
    } catch {
      unreadCount.value = 0
    }
    return unreadCount.value
  }

  return {
    userInfo,
    token,
    unreadCount,
    isLoggedIn,
    login,
    logout,
    restoreSession,
    updateUserInfo,
    refreshUnreadCount,
    resetMemoryState,
  }
})
