/**
 * API 通用类型定义
 */

/** 后端 ResponseApiBody 真实格式 */
export interface ResponseApiBody<T = unknown> {
  code: number
  msg: string
  res: T
}

/** 前端内部统一响应格式（适配后端 ResponseApiBody） */
export interface ApiResult<T = unknown> {
  code: number
  msg: string
  data: T
  success: boolean
}

/** 分页响应格式（Spring Data Page） */
export interface PageResult<T = unknown> {
  content: T[]
  totalElements: number
  totalPages?: number
  size?: number
  number?: number
}

/** 分页请求参数 */
export interface PageQuery {
  page?: number
  size?: number
}

/** 通用选项 */
export interface SelectOption {
  label: string
  value: string | number
}

export interface MemberPlan {
  id: number
  code: string
  name: string
  durationDays: number
  priceFen: number
  originPriceFen?: number
  giftPpd: number
  levelCode: string
  recommended: string
  benefits?: string
  status: string
  sortNo?: number
  credate?: string
  updatedAt?: string
}

export interface MemberBenefitRule {
  id: number
  levelCode: string
  scene: string
  benefitType: string
  discountPpd: number
  minActualPpd: number
  effective: string
  status: string
  description?: string
}

export interface MemberUser {
  userId: number
  mobile?: string
  nickname?: string
  levelCode: string
  expireAt?: string
  expireStart?: string
  expireEnd?: string
  memberStatus: string
  days?: number
  reason?: string
}

export interface MemberOrder {
  id: number
  outTradeNo: string
  userId: number
  planId: number
  planCode?: string
  planNameSnapshot?: string
  levelCodeSnapshot?: string
  priceFen: number
  originPriceFen?: number
  giftPpd?: number
  durationDays: number
  status: string
  wxTransactionId?: string
  paidAt?: string
  credate?: string
  updatedAt?: string
}

export interface MemberOperationLog {
  id: number
  userId: number
  operatorId?: number
  actionType: string
  reason?: string
  beforeValue?: string
  afterValue?: string
  sourceOrderNo?: string
  createdAt?: string
}

export interface DepositConfig {
  id?: number
  enabled: string
  amountFen: number
  testEnabled: string
  testAmountFen: number
  displayAmountFen?: number
  refundWaitDays?: number
  earlyRefundFeeRate?: number
  agreementSummary?: string
  updatedAt?: string
}

export interface DepositOrder {
  id: number
  outTradeNo: string
  userId: number
  amountFen: number
  channel: string
  status: string
  prepayId?: string
  transactionId?: string
  paidAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface PaymentOrder {
  id: number
  paymentNo: string
  businessType: string
  businessOrderNo: string
  outTradeNo: string
  userId: number
  channel: string
  amountFen: number
  status: string
  prepayId?: string
  h5Url?: string
  transactionId?: string
  wechatTradeState?: string
  notifyEventId?: string
  notifyDigest?: string
  paidAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface MessagePushLog {
  id: number
  eventType: string
  businessType?: string
  messageId?: number
  ypatid?: number
  sendperid?: number
  recperid?: number
  touserOpenid?: string
  templateId?: string
  pageUrl?: string
  success?: string
  wechatErrcode?: string
  wechatErrmsg?: string
  responseBody?: string
  remark?: string
  createdAt?: string
}

export interface MessagePushLogStats {
  total?: number
  successCount?: number
  failedCount?: number
  wechatTotal?: number
  inAppTotal?: number
  failedRate?: string
}
