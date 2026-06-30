/**
 * 认证相关 API
 */

import { get, post } from '../request'
import type { ApiResult } from '../types'

/** 登录请求参数 */
export interface LoginParams {
  mobile: string
  password: string
  captchaId: string
  captchaCode: string
}

/** 登录响应数据 */
export interface LoginResult {
  token: string
  id: number
  mobile: string
  name: string
  nickname: string
}

/** 管理员信息 */
export interface AdminInfo {
  id: number
  mobile: string
  name: string
  nickname: string
}

/** 验证码响应 */
export interface CaptchaResult {
  captchaId: string
  img: string
}

/** 获取验证码 */
export function getCaptcha(): Promise<ApiResult<CaptchaResult>> {
  return get<CaptchaResult>('/admin/captcha')
}

/** 管理端登录 */
export function login(params: LoginParams): Promise<ApiResult<LoginResult>> {
  return post<LoginResult>('/admin/login', params)
}

/** 获取当前管理员信息 */
export function getAdminInfo(): Promise<ApiResult<AdminInfo>> {
  return get<AdminInfo>('/admin/user/info')
}

/** 退出登录 */
export function logout(): Promise<ApiResult<{ success: boolean }>> {
  return post<{ success: boolean }>('/admin/logout')
}
