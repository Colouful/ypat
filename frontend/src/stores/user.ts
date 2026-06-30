import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { get, post } from '@/api/request'
import { envConfig } from '@/config/env'
import { sendH5LoginCode } from '@/api/modules/user'
import { goRootTab } from '@/utils/tab-navigation'
import {
  clearAuth,
  getStoredUserInfo,
  getToken,
  registerAuthResetHandler,
  setStoredUserInfo,
  setToken,
} from '@/services/auth-storage'
import type { ApiResult, LoginResult, UserInfo, WxSessionResult } from '@/api/types'

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
  /** 邀请人手机号，仅在首次注册时由后端绑定并发放 +3 拍拍豆。 */
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
   * 真实流程: 将登录页通过 getPhoneNumber + uni.login 收集到的
   * code / encryptedData / iv 透传给后端 /user/login(UserQo 接受这些字段),
   * 由后端解密手机号并签发 token。**生产环境绝不使用测试账号。**
   *
   * 开发环境保留测试账号便于无微信开发者工具联调
   * (手机号 18888888888 + 验证码 888888, 后端 H5_TEST_MOBILE 分支)。
   * 注意: 新版微信(基础库 2.21+)的 getPhoneNumber 已改为返回 code 换取,
   * 若后端仍依赖 encryptedData 解密失败,登录页已有明确提示(见 login/index.vue)。
   */
  async function login(input: WechatLoginInput): Promise<UserInfo> {
    if (envConfig.env === 'development') {
      return loginByPhoneInternal('18888888888', '888888')
    }
    const result = await post<LoginResult>(
      '/user/login',
      {
        code: input.code,
        encryptedData: input.encryptedData,
        iv: input.iv,
        channel: input.channel ?? '0',
        nickname: input.nickname,
        avatarurl: input.avatarurl,
        gender: input.gender,
        recmobile: input.recmobile,
      },
      { withToken: false, showError: false },
    )
    return applyLoginResult(result)
  }

  async function loginByPhone(input: H5PhoneLoginInput): Promise<UserInfo> {
    return loginByPhoneInternal(input.mobile.trim(), input.smsCode.trim(), input.recmobile?.trim())
  }

  async function loginByPhoneInternal(mobile: string, smsCode: string, recmobile?: string): Promise<UserInfo> {
    const payload: Record<string, string> = { mobile, smsCode, channel: '2' }
    if (recmobile && recmobile !== mobile) payload.recmobile = recmobile
    const result = await post<LoginResult>(
      '/user/login',
      payload,
      { withToken: false, showError: false },
    )
    return applyLoginResult(result)
  }

  // 统一处理登录响应: 校验凭证、保存 token、拉取完整资料、落地会话。
  async function applyLoginResult(result: ApiResult<LoginResult>): Promise<UserInfo> {
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
    goRootTab('/pages/home/index')
  }

  async function updateUserInfo(localPatch?: Partial<UserInfo>): Promise<UserInfo | null> {
    if (!token.value || !userInfo.value?.id) return null

    const confirmedPatch = omitUndefined(localPatch)
    if (localPatch) {
      userInfo.value = { ...userInfo.value, ...confirmedPatch }
      setStoredUserInfo(userInfo.value)
    }

    try {
      const result = await get<UserInfo>('/user/get', { id: userInfo.value.id })
      if (result.data?.id) {
        userInfo.value = { ...result.data, ...confirmedPatch }
        setStoredUserInfo(userInfo.value)
      }
    } catch {
      // 服务端刷新失败时保留已确认保存成功的本地补丁。
    }
    return userInfo.value
  }

  function omitUndefined<T extends Record<string, unknown>>(value?: T): Partial<T> {
    if (!value) return {}
    return Object.fromEntries(Object.entries(value).filter(([, item]) => item !== undefined)) as Partial<T>
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
