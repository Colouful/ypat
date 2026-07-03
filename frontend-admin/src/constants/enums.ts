/**
 * 枚举定义（对应后端 enums 包）
 */

/** 用户状态（对应后端 UserStatus 枚举） */
export const UserStatus = {
  ZC: { value: '0', name: '暂存', type: 'info' as const },
  YTJ: { value: '1', name: '待审核', type: 'warning' as const },
  SHTG: { value: '2', name: '审核通过', type: 'success' as const },
  SHBTG: { value: '3', name: '审核未通过', type: 'danger' as const },
  ZFCG: { value: '4', name: '支付成功', type: 'primary' as const },
} as const

/** 审核标志 */
export const AuditFlag = {
  PASS: '2',
  REJECT: '3',
} as const

/** 获取用户状态选项（用于下拉筛选） */
export function getUserStatusOptions() {
  return [
    { label: UserStatus.YTJ.name, value: UserStatus.YTJ.value },
    { label: UserStatus.SHTG.name, value: UserStatus.SHTG.value },
    { label: UserStatus.SHBTG.name, value: UserStatus.SHBTG.value },
  ]
}

/** 根据 status 值获取状态信息 */
export function getUserStatusInfo(value: string) {
  const statuses = Object.values(UserStatus)
  return statuses.find((s) => s.value === value) || { value, name: '未知', type: 'info' as const }
}

/** 约拍/作品状态 */
export const YpatStatus = {
  ZC: { value: '0', name: '暂存', type: 'info' as const },
  YTJ: { value: '1', name: '待审核', type: 'warning' as const },
  SHTG: { value: '2', name: '审核通过', type: 'success' as const },
  SHBTG: { value: '3', name: '审核未通过', type: 'danger' as const },
}
export const getYpatStatusOptions = () => [
  { label: YpatStatus.YTJ.name, value: YpatStatus.YTJ.value },
  { label: YpatStatus.SHTG.name, value: YpatStatus.SHTG.value },
  { label: YpatStatus.SHBTG.name, value: YpatStatus.SHBTG.value },
]

/** 是否推荐 */
export const RecomFlag = {
  YES: { value: '1', name: '已推荐', type: 'success' as const },
  NO: { value: '0', name: '未推荐', type: 'info' as const },
}
export const getRecomOptions = () => [
  { label: RecomFlag.YES.name, value: RecomFlag.YES.value },
  { label: RecomFlag.NO.name, value: RecomFlag.NO.value },
]

/** 产品上下架状态 */
export const ProductStatus = {
  UP: { value: '0', name: '上架', type: 'success' as const },
  DOWN: { value: '1', name: '下架', type: 'info' as const },
}
export const getProductStatusOptions = () => [
  { label: ProductStatus.UP.name, value: ProductStatus.UP.value },
  { label: ProductStatus.DOWN.name, value: ProductStatus.DOWN.value },
]

/** 文章状态 */
export const ArticleStatus = {
  ZC: { value: '0', name: '暂存', type: 'info' as const },
  YFB: { value: '1', name: '已发布', type: 'success' as const },
  YCH: { value: '2', name: '已撤回', type: 'warning' as const },
}
export const getArticleStatusOptions = () => [
  { label: ArticleStatus.YFB.name, value: ArticleStatus.YFB.value },
  { label: ArticleStatus.YCH.name, value: ArticleStatus.YCH.value },
]

/** 订单支付状态 */
export const OrderStatus = {
  PAID: { value: '0', name: '已支付', type: 'success' as const },
  UNPAID: { value: '1', name: '未支付', type: 'warning' as const },
}
export const getOrderStatusOptions = () => [
  { label: OrderStatus.PAID.name, value: OrderStatus.PAID.value },
  { label: OrderStatus.UNPAID.name, value: OrderStatus.UNPAID.value },
]

/** 订单类型 */
export const OrderType = {
  PP: { value: '0', name: '拍拍充值' },
  REAL: { value: '1', name: '实名认证充值' },
  DEPOSIT: { value: '2', name: '保证金充值' },
}
export const getOrderTypeOptions = () => Object.values(OrderType).map((o) => ({ label: o.name, value: o.value }))

/** 约拍对象 */
export const YpatTarget = {
  PHOTOGRAPHER: { value: '0', name: '约摄影师' },
  MODEL: { value: '1', name: '约模特' },
  VIDEOGRAPHER: { value: '2', name: '约摄像师' },
  MERCHANT: { value: '3', name: '约商家' },
  MAKEUP: { value: '4', name: '约化妆师' },
  RETOUCHER: { value: '5', name: '约修图师' },
} as const
export const getYpatTargetOptions = () => Object.values(YpatTarget).map((o) => ({ label: o.name, value: o.value }))

/** 约拍风格（多选，逗号拼接） */
export const YpatPatstyle = {
  RETRO: { value: '0', name: '复古' },
  INS: { value: '1', name: 'INS' },
  FILM: { value: '2', name: '胶片' },
  GIRLISH: { value: '3', name: '少女' },
  DARK: { value: '4', name: '暗黑' },
  MOOD: { value: '5', name: '情绪' },
  NIGHT: { value: '6', name: '夜景' },
  EUROPEAN: { value: '7', name: '欧美' },
  BUSINESS: { value: '8', name: '商务' },
  KOREAN: { value: '9', name: '韩系' },
  JAPANESE: { value: '10', name: '日系' },
  COUPLE: { value: '11', name: '情侣' },
  SAMPLE: { value: '12', name: '样片' },
} as const
export const getYpatPatstyleOptions = () => Object.values(YpatPatstyle).map((o) => ({ label: o.name, value: o.value }))

/** 收费方式 */
export const YpatChargeWay = {
  FREE: { value: '0', name: '免费互拍' },
  CHARGE: { value: '1', name: '收费拍摄' },
  CAN_PAY: { value: '2', name: '可付费' },
  NEGOTIATE: { value: '3', name: '费用面议' },
} as const
export const getYpatChargeWayOptions = () => Object.values(YpatChargeWay).map((o) => ({ label: o.name, value: o.value }))

/** 作品状态 */
export const WorkStatus = {
  DRAFT: { value: '0', name: '暂存', type: 'info' as const },
  PENDING: { value: '1', name: '待审核', type: 'warning' as const },
  APPROVED: { value: '2', name: '审核通过', type: 'success' as const },
  REJECTED: { value: '3', name: '审核未通过', type: 'danger' as const },
  OFFLINE: { value: '4', name: '已下架', type: 'info' as const },
} as const
export const getWorkStatusOptions = () => [
  WorkStatus.PENDING,
  WorkStatus.APPROVED,
  WorkStatus.REJECTED,
  WorkStatus.OFFLINE,
]

/** 性别 */
export const Gender = {
  MALE: { value: '1', name: '男' },
  FEMALE: { value: '2', name: '女' },
}
export const getGenderOptions = () => Object.values(Gender).map((o) => ({ label: o.name, value: o.value }))

/** 职业 */
export const UserProfess = {
  MODEL: { value: '1', name: '模特' },
  PHOTOGRAPHER: { value: '2', name: '摄影师' },
  MAKEUP: { value: '3', name: '化妆师' },
  STUDIO: { value: '4', name: '影楼' },
  OTHER: { value: '5', name: '其他' },
}
export const getProfessOptions = () => Object.values(UserProfess).map((o) => ({ label: o.name, value: o.value }))
