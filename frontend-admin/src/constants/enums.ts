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
export function getYpatPatstyleText(value?: string): string {
  if (!value) return '-'

  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => {
      const style = Object.values(YpatPatstyle).find((option) => option.value === item || option.name === item)
      return style?.name || item
    })
    .join('、') || '-'
}

/** 作品主题标签风格（对应后端 WorkDictController 默认标签） */
export const WorkTagStyle = [
  { code: 'qinglv', name: '情侣' },
  { code: 'shangwu', name: '商务' },
  { code: 'minguo', name: '民国' },
  { code: 'hanfu', name: '汉服' },
  { code: 'yunzhao', name: '孕照' },
  { code: 'ertong', name: '儿童摄影' },
  { code: 'anhei', name: '暗黑' },
  { code: 'qingxu', name: '情绪' },
  { code: 'yejing', name: '夜景' },
  { code: 'xiaoyuan', name: '校园' },
  { code: 'zhuangrong', name: '妆容' },
  { code: 'gufeng', name: '古风' },
  { code: 'taobao', name: '淘宝' },
  { code: 'shishang', name: '时尚' },
  { code: 'hefu', name: '和服' },
  { code: 'qipao', name: '旗袍' },
  { code: 'hanxi', name: '韩系' },
  { code: 'oumei', name: '欧美' },
  { code: 'senxi', name: '森系' },
  { code: 'shaonv', name: '少女' },
  { code: 'baolilai', name: '宝丽来' },
  { code: 'qingxin', name: '清新' },
  { code: 'hunli', name: '婚礼' },
  { code: 'cosplay', name: 'cosplay' },
  { code: 'jiaopian', name: '胶片' },
  { code: 'heibai', name: '黑白' },
  { code: 'jishi', name: '纪实' },
  { code: 'rixi', name: '日系' },
  { code: 'fugu', name: '复古' },
] as const
export const WORK_TAG_STYLE_CODES = WorkTagStyle.map((item) => item.code)
export const getWorkTagStyleOptions = () => WorkTagStyle.map((o) => ({ label: o.name, value: o.code }))
export function resolveWorkTagStyleName(value?: string): string {
  if (!value) return '-'

  const workTag = WorkTagStyle.find((item) => item.code === value || item.name === value)
  if (workTag) return workTag.name

  const legacyStyle = Object.values(YpatPatstyle).find((item) => item.value === value || item.name === value)
  return legacyStyle?.name || value
}

/** 收费方式 */
export const YpatChargeWay = {
  FREE: { value: '0', name: '希望互勉' },
  CHARGE: { value: '1', name: '我要收费' },
  CAN_PAY: { value: '2', name: '可付费' },
  NEGOTIATE: { value: '3', name: '费用协商' },
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
  { label: WorkStatus.PENDING.name, value: WorkStatus.PENDING.value },
  { label: WorkStatus.APPROVED.name, value: WorkStatus.APPROVED.value },
  { label: WorkStatus.REJECTED.name, value: WorkStatus.REJECTED.value },
  { label: WorkStatus.OFFLINE.name, value: WorkStatus.OFFLINE.value },
]

/** 性别 */
export const Gender = {
  MALE: { value: '1', name: '男' },
  FEMALE: { value: '2', name: '女' },
}
export const getGenderOptions = () => Object.values(Gender).map((o) => ({ label: o.name, value: o.value }))

/** 职业 */
export const UserProfess = {
  BUSINESS: { value: '6', name: '商家' },
  PHOTOGRAPHER: { value: '0', name: '摄影师' },
  MAKEUP: { value: '2', name: '化妆师' },
  VIDEOGRAPHER: { value: '9', name: '摄像师' },
  RETOUCHER: { value: '3', name: '修图师' },
  MODEL: { value: '1', name: '模特' },
} as const
export const getProfessOptions = () => Object.values(UserProfess).map((o) => ({ label: o.name, value: o.value }))
const LegacyProfessDisplayName: Record<string, string> = {
  '4': '个人',
  '5': '演员',
  '7': '其他',
  '8': '素人模特',
}
export function getProfessDisplayName(value?: string): string {
  if (!value) return '-'

  const profess = Object.values(UserProfess).find((item) => item.value === value || item.name === value)
  return profess?.name || LegacyProfessDisplayName[value] || value
}

/** 内测数据标识 */
export const InternalTestDataFlag = {
  REAL: { value: 'real', name: '真实数据' },
  INTERNAL_TEST: { value: 'internal_test', name: '内测数据' },
} as const
export const getInternalTestDataFlagOptions = () =>
  Object.values(InternalTestDataFlag).map((o) => ({ label: o.name, value: o.value }))

/** 内测资源媒体类型 */
export const InternalTestMediaType = {
  IMAGE: { value: 'image', name: '图片' },
  VIDEO: { value: 'video', name: '视频' },
} as const
export const getInternalTestMediaTypeOptions = () =>
  Object.values(InternalTestMediaType).map((o) => ({ label: o.name, value: o.value }))

/** 内测资源用途 */
export const InternalTestUsageType = {
  AVATAR: { value: 'avatar', name: '头像' },
  YPAT: { value: 'ypat', name: '约拍' },
  WORK: { value: 'work', name: '作品' },
} as const
export const getInternalTestUsageTypeOptions = () =>
  Object.values(InternalTestUsageType).map((o) => ({ label: o.name, value: o.value }))

/** 内测资源状态 */
export const InternalTestResourceStatus = {
  ENABLED: { value: 'enabled', name: '启用', type: 'success' as const },
  DISABLED: { value: 'disabled', name: '停用', type: 'info' as const },
} as const
export const getInternalTestResourceStatusOptions = () =>
  Object.values(InternalTestResourceStatus).map((o) => ({ label: o.name, value: o.value }))

/** 内测数据生成模式 */
export const InternalTestGenerateMode = {
  CREATE_AND_GENERATE: { value: 'create_and_generate', name: '新建用户并生成' },
  APPEND_TO_USERS: { value: 'append_to_users', name: '给已有内测用户追加' },
} as const
export const getInternalTestGenerateModeOptions = () =>
  Object.values(InternalTestGenerateMode).map((o) => ({ label: o.name, value: o.value }))
