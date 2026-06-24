/** 统一接口响应结构 */
export interface ApiResult<T = any> {
  success: boolean
  data: T
  code: string
  message: string
}

/** 分页结果（对应 Spring Data Page） */
export interface PageResult<T = any> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

/** 分页请求参数 */
export interface PageParams {
  page: number
  size: number
}

// ===== User 相关类型 =====

export interface UserInfo {
  id: number
  gender: string
  nickname: string
  profess: string
  mobile: string
  wx: string
  qq: string
  wb: string
  name: string
  certcode: string
  ppd: number
  avatarurl: string
  realnameflag: string
  creditflag: string
  pubtimes: number
  rectimes: number
  coltimes: number
  recmobile: string
  status: string
  province: string
  city: string
  area: string
  openid: string
  birthday: string
  imgpath: string
  channel: string
}

export interface LoginParams {
  openid?: string
  encryptedData?: string
  sessionKey?: string
  iv?: string
  nickname?: string
  avatarurl?: string
  gender?: string
  channel?: string
  recmobile?: string
  mobile?: string
}

export interface LoginResult {
  token: string
  userInfo: UserInfo
}

export interface LinkWay {
  nickname: string
  profess: string
  mobile: string
  wx: string
  qq: string
  wb: string
  name: string
}

// ===== YpatInfo 相关类型 =====

export interface YpatInfo {
  id: number
  describ: string
  target: string
  patdate: string
  patarea: string
  patslice: string
  chargeway: string
  chargeamt: number
  province: string
  city: string
  area: string
  creditflag: string
  realnameflag: string
  patstyle: string
  status: string
  longitude: number
  latitude: number
  pubdate: string
  readtimes: number
  pattimes: number
  coltimes: number
  userQo: UserInfo | null
  userid: number
  timeStr: string
  pics: string[]
  colflag: string
  recomflag: string
  reason: string
  chargewayTxt: string
  targetTxt: string
  patstyleTxt: string
  statusTxt: string
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
  userid: number
  describ: string
  target: string
  patdate: string
  province?: string
  city: string
  area?: string
  chargeway: string
  chargeamt?: number
  patstyle?: string
  creditflag?: string
  realnameflag?: string
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

// ===== MessInfo 消息相关类型 =====

export interface MessInfo {
  id: number
  type: string
  content: string
  status: string
  sendperid: number
  recperid: number
  messviewflag: string
  linkwayflag: string
  credate: string
  nickname: string
  imgpath: string
  ypatid: number
  timeStr: string
  city: string
}

export interface MessListParams extends PageParams {
  sendperid?: number
  recperid?: number
  type?: string
}

// ===== Product 商品相关类型 =====

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

// ===== Order 订单相关类型 =====

export interface CreateOrderParams {
  type: string
  userid: number
  productid: number
  total_fee: number
}

export interface CreateOrderResult {
  prepay_id: string
  out_trade_no: string
  [key: string]: any
}

// ===== Bill 账单相关类型 =====

export interface Bill {
  id: number
  type: string
  credate: string
  openid: string
  total_fee: number
  out_trade_no: string
  return_code: string
  result_code: string
}

export interface BillListParams extends PageParams {
  userid?: number
  type?: string
}

// ===== Record 记录相关类型 =====

export interface RecordInfo {
  id: number
  type: string
  credate: string
  ppd: number
  userid: number
  typeTxt: string
}

export interface RecordListParams extends PageParams {
  userid?: number
  type?: string
}

// ===== Banner 轮播相关类型 =====

export interface Banner {
  id: number
  title: string
  imgpath: string
  credate: string
  userid: number
  status: string
}

export interface BannerListParams extends PageParams {
  status?: string
}

// ===== Article 文章相关类型 =====

export interface Article {
  id: number
  title: string
  describ: string
  content: string
  credate: string
  status: string
  flag: string
  plat: string
  readtimes: number
  imgpath: string
  timeStr: string
}

export interface ArticleListParams extends PageParams {
  flag?: string
  status?: string
}

// ===== Oauth 认证相关类型 =====

export interface OcrResult {
  name: string
  certcode: string
}

export interface OauthSubmitParams {
  userid: number
  name: string
  certcode: string
  pics: string[]
}

export interface OauthInfo {
  userid: number
  name: string
  certcode: string
  pics: string[]
  status: string
  statusTxt: string
}
