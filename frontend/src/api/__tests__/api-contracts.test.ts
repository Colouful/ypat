import { beforeEach, describe, expect, it, vi } from 'vitest'

const requestMocks = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
}))

vi.mock('../request', () => requestMocks)

import * as userApi from '../modules/user'
import * as paymentApi from '../modules/payment'
import * as contentApi from '../modules/content'
import * as ypatApi from '../modules/ypat'

describe('API contracts', () => {
  beforeEach(() => {
    requestMocks.get.mockReset()
    requestMocks.post.mockReset()
    requestMocks.put.mockReset()
    requestMocks.get.mockResolvedValue({ success: true, data: null, code: '200', message: '' })
    requestMocks.post.mockResolvedValue({ success: true, data: null, code: '200', message: '' })
    requestMocks.put.mockResolvedValue({ success: true, data: null, code: '200', message: '' })
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

  // GAP-API-01: 后端 MypatInfoController 这些列表均为 @GetMapping，必须用 GET。
  // 见 docs/migration/frontend-parity/03-api-contract-matrix.md。
  it('my-ypat list endpoints use GET (backend @GetMapping)', async () => {
    const p = { page: 1, size: 10 }
    await ypatApi.getMyPublishList(p)
    await ypatApi.getMyFavoriteList(p)
    await ypatApi.getMyReceivedList(p)
    await ypatApi.getMySentList(p)
    expect(requestMocks.post).not.toHaveBeenCalled()
    expect(requestMocks.get).toHaveBeenCalledWith('/my/ypat/pub/list', { ...p })
    expect(requestMocks.get).toHaveBeenCalledWith('/my/ypat/sc/list', { ...p })
    expect(requestMocks.get).toHaveBeenCalledWith('/my/ypat/rec/list', { ...p })
    expect(requestMocks.get).toHaveBeenCalledWith('/my/ypat/send/list', { ...p })
  })

  it('ypat write actions use the methods the backend accepts', async () => {
    await ypatApi.addReadCount(5)
    await ypatApi.addFavorite(1, 2)
    expect(requestMocks.put).toHaveBeenCalledWith('/ypat/yd/add', { ypatid: 5 })
    expect(requestMocks.put).toHaveBeenCalledWith('/my/ypat/sc/add', { userid: 1, ypatid: 2 })
  })
})
