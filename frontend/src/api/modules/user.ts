import { get, post } from '../request'
import type {
  ApiResult,
  LoginParams,
  LoginResult,
  UserInfo,
  UpdateUserParams,
  LinkWay,
} from '../types'

/**
 * 微信登录 - 通过 code 换取 openId
 */
export function wxLogin(code: string): Promise<ApiResult<{ openId: string; unionId: string; sessionKey: string }>> {
  return get('/user/code', { code })
}

/**
 * 用户登录
 */
export function login(data: LoginParams): Promise<ApiResult<LoginResult>> {
  return post('/user/login', data)
}

/**
 * 刷新 Token
 */
export function refreshToken(): Promise<ApiResult<{ token: string; refreshToken: string }>> {
  return get('/user/token')
}

/**
 * 获取用户信息
 */
export function getUserInfo(id: number): Promise<ApiResult<UserInfo>> {
  return get('/user/get', { id })
}

/**
 * 更新用户信息
 */
export function updateUser(data: UpdateUserParams): Promise<ApiResult<null>> {
  return post('/user/upd', data)
}

/**
 * 获取联系方式
 */
export function getLinkWay(id: number, userid: number, messid: number): Promise<ApiResult<LinkWay>> {
  return get('/user/linkway/get', { id, userid, messid })
}

/**
 * 按城市和职业查找用户
 */
export function findByCityAndProfess(userid: number, city: string): Promise<ApiResult<UserInfo[]>> {
  return get('/user/findByCityAndProfess', { userid, city })
}
