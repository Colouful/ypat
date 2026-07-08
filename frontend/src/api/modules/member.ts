import { get, post } from '../request'
import type {
  ApiResult,
  MemberBenefitQuote,
  MemberOrder,
  MemberOrderCreateResult,
  MemberPlan,
  MemberStatus,
  PageResult,
  PaymentChannel,
  PaymentCreateResult,
} from '../types'

/** 套餐列表（公开接口，未登录也能调用）。 */
export function getMemberPlans(): Promise<ApiResult<MemberPlan[]>> {
  return get('/member/plans', undefined, { withToken: false })
}

/** 当前用户的会员状态。 */
export function getMemberStatus(): Promise<ApiResult<MemberStatus>> {
  return get('/member/status')
}

/** 当前用户在指定场景下的会员权益报价。 */
export function getMemberBenefitQuote(scene: 'SUBMIT_YPAT'): Promise<ApiResult<MemberBenefitQuote>> {
  return get('/member/benefit/quote', { scene })
}

/** 创建会员订单 + 调微信统一下单，返回支付参数。 */
export function createMemberOrder(planId: number, channel: PaymentChannel): Promise<ApiResult<PaymentCreateResult & MemberOrderCreateResult>> {
  return post('/member/order/create', { planId, channel })
}

/** 轮询订单状态：支付成功后前端每 1.5s 调用，最多 20 次。 */
export function getMemberOrderStatus(outTradeNo: string): Promise<ApiResult<MemberOrder>> {
  return get('/member/order/status', { out_trade_no: outTradeNo })
}

/** 当前用户的会员订单分页。 */
export function getMemberOrders(params: { page?: number; size?: number }): Promise<ApiResult<PageResult<MemberOrder>>> {
  return get('/member/orders', params)
}

/** 取消未支付订单。 */
export function cancelMemberOrder(outTradeNo: string): Promise<ApiResult<boolean>> {
  return post('/member/order/cancel', { out_trade_no: outTradeNo })
}
