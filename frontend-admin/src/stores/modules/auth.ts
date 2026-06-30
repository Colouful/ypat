/**
 * Auth Store - Token + 管理员信息管理
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import * as authApi from '@/api/modules/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const adminInfo = ref<authApi.AdminInfo | null>(null)

  /**
   * 登录
   */
  async function login(params: authApi.LoginParams): Promise<void> {
    const res = await authApi.login(params)
    token.value = res.data.token
    setToken(res.data.token)
    adminInfo.value = {
      id: res.data.id,
      mobile: res.data.mobile,
      name: res.data.name,
      nickname: res.data.nickname,
    }
  }

  /**
   * 获取管理员信息
   */
  async function fetchAdminInfo(): Promise<void> {
    const res = await authApi.getAdminInfo()
    adminInfo.value = res.data
  }

  /**
   * 退出登录
   */
  async function logout(): Promise<void> {
    try {
      await authApi.logout()
    } catch {
      // 即使后端退出失败，前端也清除本地状态
    }
    resetState()
  }

  /**
   * 重置状态
   */
  function resetState(): void {
    token.value = null
    adminInfo.value = null
    removeToken()
  }

  return {
    token,
    adminInfo,
    login,
    fetchAdminInfo,
    logout,
    resetState,
  }
})
