import { beforeEach, describe, expect, it, vi } from 'vitest'

const requestMocks = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
}))

vi.mock('../request', () => requestMocks)

import * as userApi from '../modules/user'
import * as paymentApi from '../modules/payment'
import * as contentApi from '../modules/content'

describe('API contracts', () => {
  beforeEach(() => {
    requestMocks.get.mockReset()
    requestMocks.post.mockReset()
    requestMocks.get.mockResolvedValue({ success: true, data: null, code: '200', message: '' })
    requestMocks.post.mockResolvedValue({ success: true, data: null, code: '200', message: '' })
  })

  it('wxLogin sends code to /user/code', async () => {
    await userApi.wxLogin('wx-code')
    expect(requestMocks.get).toHaveBeenCalledWith('/user/code', { code: 'wx-code' })
  })

  it('sendH5LoginCode posts mobile to /user/sms/code', async () => {
    await userApi.sendH5LoginCode('13800138000')
    expect(requestMocks.post).toHaveBeenCalledWith('/user/sms/code', { mobile: '13800138000' }, { withToken: false })
  })

  it('h5PhoneLogin posts mobile verification payload to /user/login', async () => {
    await userApi.h5PhoneLogin({ mobile: '13800138000', smsCode: '123456' })
    expect(requestMocks.post).toHaveBeenCalledWith('/user/login', {
      mobile: '13800138000',
      smsCode: '123456',
      channel: '2',
    }, { withToken: false })
  })

  it('getLinkWay sends only userid and messid', async () => {
    await userApi.getLinkWay(12, 34)
    expect(requestMocks.get).toHaveBeenCalledWith('/user/linkway/get', { userid: 12, messid: 34 })
  })

  it('getOrderStatus sends merchant order number only', async () => {
    await paymentApi.getOrderStatus('ORDER-1')
    expect(requestMocks.get).toHaveBeenCalledWith('/order/status', { out_trade_no: 'ORDER-1' })
  })

  it('content config endpoints use object responses', async () => {
    await contentApi.getAreaList()
    await contentApi.getParams()
    expect(requestMocks.get).toHaveBeenNthCalledWith(1, '/area/list')
    expect(requestMocks.get).toHaveBeenNthCalledWith(2, '/param/list')
  })
})
