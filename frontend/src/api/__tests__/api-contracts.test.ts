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
