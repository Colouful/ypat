import { describe, expect, it } from 'vitest'
import { packageValue, toMiniappPayParams } from '../payment-channel'

describe('payment-channel', () => {
  it('uses packageValue from backend payment params', () => {
    expect(packageValue({ packageValue: 'prepay_id=1' } as never)).toBe('prepay_id=1')
  })

  it('normalizes miniapp payment params', () => {
    expect(toMiniappPayParams({
      outTradeNo: 'D1',
      businessType: 'DEPOSIT',
      channel: 'MINIAPP',
      amountFen: 1,
      payParams: {
        timeStamp: '1',
        nonceStr: 'n',
        packageValue: 'prepay_id=1',
        signType: 'RSA',
        paySign: 's',
      },
    })).toEqual({
      timeStamp: '1',
      nonceStr: 'n',
      package: 'prepay_id=1',
      signType: 'RSA',
      paySign: 's',
    })
  })

  it('throws when miniapp payment params are incomplete', () => {
    expect(() => toMiniappPayParams({ payParams: { timeStamp: '1' } as never })).toThrow('支付参数不完整')
  })
})
