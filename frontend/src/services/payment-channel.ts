import type { PaymentChannel, PaymentCreateResult, PaymentPayParams } from '@/api/types'

export interface MiniappPayParams {
  timeStamp: string
  nonceStr: string
  package: string
  signType: string
  paySign: string
}

export function getPaymentChannel(): PaymentChannel {
  let channel: PaymentChannel = 'H5'
  // #ifdef MP-WEIXIN
  channel = 'MINIAPP'
  // #endif
  // #ifdef H5
  channel = 'H5'
  // #endif
  return channel
}

export function packageValue(params?: PaymentPayParams): string {
  return params?.packageValue || params?.package || ''
}

export function toMiniappPayParams(result: PaymentCreateResult | { payParams?: PaymentPayParams }): MiniappPayParams {
  const params = result.payParams
  const packageText = packageValue(params)
  if (!params?.timeStamp || !params.nonceStr || !packageText || !params.signType || !params.paySign) {
    throw new Error('支付参数不完整')
  }
  return {
    timeStamp: params.timeStamp,
    nonceStr: params.nonceStr,
    package: packageText,
    signType: params.signType,
    paySign: params.paySign,
  }
}

export function redirectToH5Pay(h5Url?: string): void {
  if (!h5Url) throw new Error('H5 支付链接缺失')
  // #ifdef H5
  window.location.href = h5Url
  // #endif
}
