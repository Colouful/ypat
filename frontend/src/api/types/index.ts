/** 前端统一接口响应结构 */
export interface ApiResult<T = unknown> {
  success: boolean
  data: T
  code: string
  message: string
}

/** Spring Data 分页响应 */
export interface PageResult<T = unknown> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface PageParams {
  page: number
  size: number
}

// ===== 用户 =====

export interface UserInfo {
  id: number
  token?: string
  gender?: string
  nickname?: string
  profess?: string
  mobile?: string
  wx?: string
  qq?: string
  wb?: string
  name?: string
  certcode?: string
  ppd?: number
  avatarurl?: string
  realnameflag?: string
  creditflag?: string
  pubtimes?: number
  rectimes?: number
  coltimes?: number
  recmobile?: string
  status?: string
  province?: string
  city?: string
  area?: string
  openid?: string
  birthday?: string
  imgpath?: string
  channel?: string
}

export interface WxSessionResult {
  openid: string
  session_key: string
  unionid?: string
  errcode?: number
  errmsg?: string
}

export interface LoginParams {
  openid?: string
  encryptedData?: string
  sessionKey?: string
  iv?: string
  nickname?: string
  avatarurl?: string
  gender?: string
  channel: string
  recmobile?: string
  /** base36 编码的邀请人 user.id，安全替代 recmobile（避免泄露手机号）。 */
  inviteCode?: string
  /** 邀请入口来源：share / qr / manual / recmobile，供运营回溯。 */
  inviteSource?: string
  mobile?: string
  smsCode?: string
}

export interface H5PhoneLoginInput {
  mobile: string
  smsCode: string
}

export interface H5LoginCodeResult {
  mobile: string
  expiresIn: string | number
  debugCode?: string
}

/** LoginController 返回顶层 Map，而不是 { userInfo } 嵌套结构。 */
export interface LoginResult {
  token: string
  id: string | number
  mobile?: string
  nickname?: string
  gender?: string
  profess?: string
}

export interface UpdateUserParams extends Partial<UserInfo> {
  id?: number
  pics?: string
}

export interface LinkWay {
  nickname?: string
  profess?: string
  mobile?: string
  wx?: string
  qq?: string
  wb?: string
  name?: string
}

// ===== 约拍 =====

export interface YpatInfo {
  id: number
  describ: string
  target: string
  patdate: string
  patarea?: string
  patslice?: string
  chargeway: string
  chargeamt?: number
  province?: string
  city: string
  area?: string
  creditflag?: string
  realnameflag?: string
  patstyle?: string
  status?: string
  longitude?: number
  latitude?: number
  pubdate?: string
  readtimes?: number
  pattimes?: number
  coltimes?: number
  userQo?: UserInfo | null
  userid: number
  timeStr?: string
  pics: string[]
  colflag?: string
  recomflag?: string
  reason?: string
  chargewayTxt?: string
  targetTxt?: string
  patstyleTxt?: string
  statusTxt?: string
}

export interface YpatListParams extends PageParams {
  status?: string
  city?: string
  target?: string
  chargeway?: string
  patstyle?: string
  recomflag?: string
  userid?: number
}

export interface YpatSubmitParams {
  userid?: number
  describ: string
  target: string
  patdate: string
  patarea?: string
  patslice?: string
  province?: string
  city: string
  area?: string
  chargeway: string
  chargeamt?: number
  patstyle?: string
  creditflag?: string
  realnameflag?: string
  longitude?: number
  latitude?: number
  /** 后端 /ypat/submit 要求原始 Base64 数组，不是 URL。 */
  pics: string[]
}

export interface YpatApplyParams {
  sendperid: number
  recperid: number
  ypatid: number
  content?: string
}

export interface YpatMyListParams extends PageParams {
  userid: number
  status?: string
}

export type UnreadCountResult = number

// ===== 消息 =====

export interface MessInfo {
  id: number
  type?: string
  content?: string
  status?: string
  sendperid: number
  recperid: number
  messviewflag?: string
  linkwayflag?: string
  credate?: string
  nickname?: string
  imgpath?: string
  ypatid?: number
  timeStr?: string
  city?: string
}

export interface MessListParams extends PageParams {
  sendperid?: number
  recperid?: number
  type?: string
}

// ===== 意见反馈 =====

export interface FeedbackAddParams {
  content: string
  contact?: string
}

// ===== 商品与支付 =====

export interface Product {
  id: number
  name: string
  currval: number
  oldval: number
  status: string
}

export interface ProductListParams extends PageParams {
  type?: string
  status?: string
}

export interface CreateOrderParams {
  type: string
  productid?: number
  total_fee: number
}

export interface CreateOrderResult {
  appId: string
  timeStamp: string
  nonceStr: string
  package: string
  signType: string
  paySign: string
  /** 本次修复在后端创建订单响应中补充，用于安全轮询。 */
  out_trade_no: string
}

export type PaymentChannel = 'MINIAPP' | 'H5' | 'APP'

export interface PaymentPayParams {
  timeStamp: string
  nonceStr: string
  packageValue?: string
  package?: string
  signType: string
  paySign: string
}

export interface PaymentCreateResult {
  outTradeNo: string
  businessType: 'DEPOSIT' | 'MEMBER' | string
  channel: PaymentChannel
  amountFen: number
  payParams?: PaymentPayParams
  h5Url?: string
}

export interface DepositConfig {
  id?: number
  enabled: string
  amountFen: number
  testEnabled?: string
  testAmountFen?: number
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
  channel: PaymentChannel
  status: 'PENDING' | 'PAID' | 'CANCELLED' | 'CLOSED' | 'REFUNDED' | string
  prepayId?: string
  transactionId?: string
  paidAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface OrderInfo {
  id: number
  type: string
  credate: string
  userid: number
  productid?: number
  status?: string
  total_fee: number
  out_trade_no: string
  return_code?: string
  return_msg?: string
  result_code?: string
  err_code?: string
  err_code_des?: string
  prepay_id?: string
  typeTxt?: string
}

export interface OrderListParams extends PageParams {
  type?: string
  status?: string
  out_trade_no?: string
}

export interface Bill extends OrderInfo {}

export interface BillListParams extends PageParams {
  userid?: number
  type?: string
  status?: string
  out_trade_no?: string
}

export interface RecordInfo {
  id: number
  type: string
  credate: string
  ppd: number
  userid: number
  typeTxt?: string
}

export interface RecordListParams extends PageParams {
  userid?: number
  type?: string
}

// ===== 内容 =====

export interface Banner {
  id: number
  title?: string
  imgpath: string
  credate?: string
  userid?: number
  status?: string
}

export interface BannerListParams extends PageParams {
  status?: string
}

export interface Article {
  id: number
  title: string
  describ?: string
  content: string
  credate?: string
  status?: string
  flag?: string
  plat?: string
  readtimes?: number
  imgpath?: string
  timeStr?: string
}

export interface ArticleListParams extends PageParams {
  flag?: string
  status?: string
}

// ===== 会员 =====

export interface MemberPlan {
  id: number
  code: string
  name: string
  durationDays: number
  priceFen: number
  originPriceFen?: number
  giftPpd?: number
  levelCode?: string
  recommended?: string
  benefits?: string
  status?: string
  sortNo?: number
  credate?: string
  updatedAt?: string
}

export interface MemberStatus {
  level: string
  expireAt?: string
  active: boolean
  sourceOrderNo?: string
}

export interface MemberOrderCreateResult {
  outTradeNo: string
  businessType?: string
  channel?: PaymentChannel
  amountFen?: number
  appId?: string
  timeStamp?: string
  nonceStr?: string
  packageValue?: string
  signType?: string
  paySign?: string
  payParams?: PaymentPayParams
  h5Url?: string
}

export interface MemberOrder {
  id: number
  outTradeNo: string
  userId: number
  planId: number
  planCode: string
  planNameSnapshot?: string
  levelCodeSnapshot?: string
  priceFen: number
  originPriceFen?: number
  giftPpd?: number
  durationDays: number
  status: string
  statusTxt?: string
  wxTransactionId?: string
  paidAt?: string
  credate?: string
  updatedAt?: string
}

export interface MemberBenefitQuote {
  scene: string
  memberActive: boolean
  levelCode?: string
  originalPpd: number
  discountPpd: number
  actualPpd: number
  ruleEffective: boolean
}

// ===== 邀请 =====

export interface InviteSummary {
  inviteCode: string
  totalInvited: number
  totalReward: number
  rewardPpd: number
}

export interface InviteRule {
  rewardPpd: number
  rewardUnit: string
  ruleText: string
}

export interface InviteRecord {
  id: number
  inviterUserid: number
  inviteeUserid: number
  inviteCode?: string
  source?: string
  rewardPpd?: number
  credate?: string
  inviteeNickname?: string
  inviteeImgpath?: string
  inviteeMobileMask?: string
}

export interface InviteRecordListParams extends PageParams {
  [key: string]: number | undefined
}

// ===== 实名认证 =====

export interface OcrResult {
  userid?: number
  name: string
  certcode: string
}

export interface OauthSubmitParams {
  name: string
  certcode: string
  /** 后端 /oauth/add 要求原始 Base64 数组。 */
  pics: string[]
}

export interface OauthInfo {
  userid: number
  name: string
  certcode: string
  pics: string[]
  status: '0' | '1' | '2' | '3' | string
  statusTxt?: string
}

// 作品模块 re-export
export * from './work'
export * from './media'
export * from './dict'
export * from './quick-apply'
