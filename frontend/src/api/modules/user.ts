import { get, post } from '../request'
import type {
  ApiResult,
  LoginParams,
  LoginResult,
  WxSessionResult,
  UserInfo,
  UpdateUserParams,
  LinkWay,
  H5PhoneLoginInput,
  H5LoginCodeResult,
} from '../types'

export function wxLogin(code: string): Promise<ApiResult<WxSessionResult>> {
  return get('/user/code', { code })
}

export function login(data: LoginParams): Promise<ApiResult<LoginResult>> {
  return post('/user/login', data)
}

export function sendH5LoginCode(mobile: string): Promise<ApiResult<H5LoginCodeResult>> {
  return post('/user/sms/code', { mobile }, { withToken: false })
}

export function h5PhoneLogin(data: H5PhoneLoginInput): Promise<ApiResult<LoginResult>> {
  return post('/user/login', {
    mobile: data.mobile,
    smsCode: data.smsCode,
    channel: '2',
  }, { withToken: false })
}

export function refreshToken(): Promise<ApiResult<{ token: string; refreshToken?: string }>> {
  return get('/user/token')
}

export function getUserInfo(id: number): Promise<ApiResult<UserInfo>> {
  return get('/user/get', { id })
}

export function updateUser(data: UpdateUserParams): Promise<ApiResult<null>> {
  return post('/user/upd', data)
}

export function getLinkWay(userid: number, messid: number): Promise<ApiResult<LinkWay>> {
  return get('/user/linkway/get', { userid, messid })
}

export function findByCityAndProfess(userid: number, city: string): Promise<ApiResult<UserInfo[]>> {
  return get('/user/findByCityAndProfess', { userid, city })
}
