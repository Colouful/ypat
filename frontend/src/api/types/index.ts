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

export interface WxLoginParams {
  code: string
}

export interface LoginParams {
  phone: string
  code: string
  openId?: string
  unionId?: string
  nickName?: string
  avatarUrl?: string
}

export interface LoginResult {
  token: string
  refreshToken: string
  userId: number
  isNew: boolean
}

export interface UserInfo {
  id: number
  phone: string
  nickName: string
  avatarUrl: string
  gender: number
  city: string
  province: string
  country: string
  openId: string
  unionId: string
  status: number
  role: number
  balance: number
  credit: number
  verified: boolean
  createTime: string
  updateTime: string
}

export interface UpdateUserParams {
  id: number
  nickName?: string
  avatarUrl?: string
  gender?: number
  city?: string
  province?: string
  phone?: string
}

export interface LinkWay {
  id: number
  userId: number
  messId: number
  type: number
  value: string
  label: string
}

// ===== YpatInfo 相关类型 =====

export interface YpatInfo {
  id: number
  userId: number
  title: string
  content: string
  images: string
  city: string
  area: string
  address: string
  profess: string
  price: number
  priceType: number
  contactWay: number
  status: number
  readCount: number
  favoriteCount: number
  applyCount: number
  createTime: string
  updateTime: string
  nickName?: string
  avatarUrl?: string
  verified?: boolean
}

export interface YpatListParams extends PageParams {
  city?: string
  profess?: string
  userId?: number
}

export interface YpatSubmitParams {
  id?: number
  userId: number
  title: string
  content: string
  images: string
  city: string
  area: string
  address: string
  profess: string
  price: number
  priceType: number
  contactWay: number
}

export interface YpatApplyParams {
  userId: number
  ypatId: number
  messId?: number
  content?: string
}

export interface YpatMyListParams extends PageParams {
  userId: number
  status?: number
}

export interface UnreadCountResult {
  total: number
  rec: number
  send: number
}

// ===== MessInfo 消息相关类型 =====

export interface MessInfo {
  id: number
  fromUserId: number
  toUserId: number
  ypatId: number
  content: string
  type: number
  status: number
  read: boolean
  createTime: string
  fromNickName?: string
  fromAvatarUrl?: string
  ypatTitle?: string
}

// ===== Product 商品相关类型 =====

export interface Product {
  id: number
  name: string
  description: string
  price: number
  originalPrice: number
  type: number
  credit: number
  status: number
  sort: number
  createTime: string
}

export interface ProductListParams extends PageParams {
  type?: number
  status?: number
}

// ===== Order 订单相关类型 =====

export interface Order {
  id: number
  orderNo: string
  userId: number
  productId: number
  productName: string
  amount: number
  payAmount: number
  payType: number
  status: number
  payTime: string
  createTime: string
}

export interface CreateOrderParams {
  userId: number
  productId: number
  payType: number
}

export interface CreateOrderResult {
  orderNo: string
  payParams: Record<string, string>
}

// ===== Bill 账单相关类型 =====

export interface Bill {
  id: number
  userId: number
  type: number
  amount: number
  balance: number
  description: string
  orderNo: string
  createTime: string
}

export interface BillListParams extends PageParams {
  userId: number
  type?: number
}

// ===== Record 记录相关类型 =====

export interface RecordInfo {
  id: number
  userId: number
  type: number
  targetId: number
  targetType: number
  content: string
  createTime: string
}

export interface RecordListParams extends PageParams {
  userId: number
  type?: number
}

// ===== Banner 轮播相关类型 =====

export interface Banner {
  id: number
  title: string
  imageUrl: string
  linkUrl: string
  type: number
  sort: number
  status: number
  createTime: string
}

export interface BannerListParams extends PageParams {
  type?: number
  status?: number
}

// ===== Article 文章相关类型 =====

export interface Article {
  id: number
  title: string
  content: string
  coverUrl: string
  summary: string
  category: number
  author: string
  readCount: number
  status: number
  createTime: string
  updateTime: string
}

export interface ArticleListParams extends PageParams {
  category?: number
  status?: number
}

// ===== Oauth 认证相关类型 =====

export interface OcrResult {
  name: string
  idCard: string
  address: string
  gender: string
  nation: string
  birth: string
}

export interface OauthSubmitParams {
  userId: number
  realName: string
  idCard: string
  idCardFront: string
  idCardBack: string
  holdIdCard: string
}

export interface OauthInfo {
  id: number
  userId: number
  realName: string
  idCard: string
  idCardFront: string
  idCardBack: string
  holdIdCard: string
  status: number
  reason: string
  createTime: string
  updateTime: string
}

// ===== Area 地区相关类型 =====

export interface AreaInfo {
  id: number
  name: string
  parentId: number
  level: number
  children?: AreaInfo[]
}

// ===== Param 参数相关类型 =====

export interface ParamInfo {
  id: number
  key: string
  value: string
  description: string
}
