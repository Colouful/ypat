import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { get, post } from '@/api/request'
import { sendH5LoginCode } from '@/api/modules/user'
import {
  clearAuth,
  getStoredUserInfo,
  getToken,
  registerAuthResetHandler,
  setStoredUserInfo,
  setToken,
} from '@/services/auth-storage'
import type { LoginResult, UserInfo, WxSessionResult } from '@/api/types'

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

export interface H5PhoneLoginInput {
  mobile: string
  smsCode: string
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

  function createFallbackUser(result: LoginResult): UserInfo {
    return {
      id: Number(result.id),
      token: result.token,
      mobile: result.mobile,
      nickname: result.nickname,
      gender: result.gender,
      profess: result.profess,
    }
  }

  /**
   * 小程序端"微信一键登录"按钮的统一入口。
   * 旧版后端 WxUtils.getUserInfo 用 AES 解密 encryptedData 拿手机号，
   * 新版微信 (基础库 2.20+) 的 encryptedData 不再包含明文手机号，
   * 强制走解密会失败。开发阶段统一走 mock 短信登录:
   * 手机号 18888888888 + 验证码 888888 (后端 H5_TEST_MOBILE 分支)
   */
  async function login(input: WechatLoginInput): Promise<UserInfo> {
    return loginByPhoneInternal('18888888888', '888888')
  }

  async function loginByPhone(input: H5PhoneLoginInput): Promise<UserInfo> {
    return loginByPhoneInternal(input.mobile.trim(), input.smsCode.trim())
  }

  async function loginByPhoneInternal(mobile: string, smsCode: string): Promise<UserInfo> {
    const result = await post<LoginResult>(
      '/user/login',
      { mobile, smsCode, channel: '2' },
      { withToken: false, showError: false },
    )
    if (!result.data?.token || !result.data?.id) {
      throw new Error(result.message || '登录响应缺少用户凭证')
    }
    setToken(result.data.token)
    token.value = result.data.token

    let completeUser = createFallbackUser(result.data)
    try {
      const detail = await get<UserInfo>('/user/get', { id: Number(result.data.id) })
      if (detail.data?.id) completeUser = detail.data
    } catch {
      // 完整资料可在进入个人中心时再次刷新。
    }
    persistSession(result.data.token, completeUser)
    return completeUser
  }

  async function requestH5LoginCode(mobile: string): Promise<string | undefined> {
    const result = await sendH5LoginCode(mobile)
    if (!result.success) {
      throw new Error(result.message || '验证码发送失败')
    }
    return result.data?.debugCode
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

  async function updateUserInfo(localPatch?: Partial<UserInfo>): Promise<UserInfo | null> {
    if (!token.value || !userInfo.value?.id) return null

    if (localPatch) {
      userInfo.value = { ...userInfo.value, ...localPatch }
      setStoredUserInfo(userInfo.value)
    }

    try {
      const result = await get<UserInfo>('/user/get', { id: userInfo.value.id })
      if (result.data?.id) {
        userInfo.value = result.data
        setStoredUserInfo(result.data)
      }
    } catch {
      // 服务端刷新失败时保留已确认保存成功的本地补丁。
    }
    return userInfo.value
  }

  async function refreshUnreadCount(): Promise<number> {
    if (!isLoggedIn.value || !userInfo.value?.id) return 0
    try {
      const params = { type: '0', userid: userInfo.value.id }
      const [received, sent] = await Promise.all([
        get<number>('/my/ypat/rec/unread/count', params),
        get<number>('/my/ypat/send/unread/count', params),
      ])
      unreadCount.value = Number(received.data || 0) + Number(sent.data || 0)
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
    loginByPhone,
    requestH5LoginCode,
    logout,
    restoreSession,
    updateUserInfo,
    refreshUnreadCount,
    resetMemoryState,
  }
})