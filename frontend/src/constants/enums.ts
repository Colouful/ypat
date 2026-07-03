// ============================================================
// YPAT Business Enums & Label Maps
// ============================================================

/** 用户职业类型 */
export const UserProfess = {
  PHOTOGRAPHER: '0',
  MODEL: '1',
  MAKEUP: '2',
  RETOUCHER: '3',
  PERSONAL: '4',
  ACTOR: '5',
  BUSINESS: '6',
  OTHER: '7',
  /** 素人模特（2026-07 新增） */
  AMATEUR_MODEL: '8',
} as const;

export type UserProfessType = (typeof UserProfess)[keyof typeof UserProfess];

export const PROFESS_LABELS: Record<string, string> = {
  [UserProfess.PHOTOGRAPHER]: '摄影师',
  [UserProfess.MODEL]: '模特',
  [UserProfess.MAKEUP]: '化妆师',
  [UserProfess.RETOUCHER]: '修图师',
  [UserProfess.PERSONAL]: '个人',
  [UserProfess.ACTOR]: '艺人',
  [UserProfess.BUSINESS]: '商家',
  [UserProfess.OTHER]: '其他',
  [UserProfess.AMATEUR_MODEL]: '素人模特',
};

/** 用户性别 */
export const UserGender = {
  UNKNOWN: '0',
  MALE: '1',
  FEMALE: '2',
} as const;

export type UserGenderType = (typeof UserGender)[keyof typeof UserGender];

export const GENDER_LABELS: Record<string, string> = {
  [UserGender.UNKNOWN]: '未知',
  [UserGender.MALE]: '男',
  [UserGender.FEMALE]: '女',
};

/** 用户状态 */
export const UserStatus = {
  NORMAL: '0',
  PENDING: '1',
  VERIFIED: '2',
  REJECTED: '3',
  CREDIT: '9',
} as const;

export type UserStatusType = (typeof UserStatus)[keyof typeof UserStatus];

export const STATUS_LABELS: Record<string, string> = {
  [UserStatus.NORMAL]: '正常',
  [UserStatus.PENDING]: '待审核',
  [UserStatus.VERIFIED]: '已认证',
  [UserStatus.REJECTED]: '已拒绝',
  [UserStatus.CREDIT]: '信用异常',
};

/** 约拍目标对象（6 类） */
export const YpatTarget = {
  PHOTOGRAPHER: '0',
  MODEL: '1',
  VIDEOGRAPHER: '2', // 约摄像师
  MERCHANT: '3',     // 约商家
  MAKEUP: '4',       // 约化妆师
  RETOUCHER: '5',    // 约修图师
} as const;

export type YpatTargetType = (typeof YpatTarget)[keyof typeof YpatTarget];

export const TARGET_LABELS: Record<string, string> = {
  [YpatTarget.PHOTOGRAPHER]: '找摄影师',
  [YpatTarget.MODEL]: '找模特',
  [YpatTarget.VIDEOGRAPHER]: '找摄像师',
  [YpatTarget.MERCHANT]: '找商家',
  [YpatTarget.MAKEUP]: '找化妆师',
  [YpatTarget.RETOUCHER]: '找修图师',
};

/** 6 类约拍角色配置（用于 AppointmentPublishForm 配置驱动） */
export interface YpatRoleConfig {
  value: YpatTargetType
  label: string
  showTime: boolean         // 是否显示"时间"字段
  showLocation: boolean     // 是否显示"地点"字段
  showDeliverable: boolean  // 是否显示"成片"字段
  allowNationwide: boolean  // 是否支持"全国"选项
  uploadHint: string        // 上传提示文字
  descriptionPlaceholder: string // 约拍要求占位文字
  riskText: string          // 红色风险提示（6 类共用）
}

export const YPAT_ROLE_CONFIGS: Record<YpatTargetType, YpatRoleConfig> = {
  [YpatTarget.PHOTOGRAPHER]: {
    value: YpatTarget.PHOTOGRAPHER,
    label: '约摄影师',
    showTime: true, showLocation: true, showDeliverable: true, allowNationwide: false,
    uploadHint: '本人作品/本人照片/期待风格',
    descriptionPlaceholder: '请详细描述拍摄需求，如风格、场景、用途等（至少 5 个字）',
    riskText: '约拍必须是本人，严禁为他人发布约拍！',
  },
  [YpatTarget.VIDEOGRAPHER]: {
    value: YpatTarget.VIDEOGRAPHER,
    label: '约摄像师',
    showTime: true, showLocation: true, showDeliverable: true, allowNationwide: false,
    uploadHint: '本人作品/本人照片/期待风格',
    descriptionPlaceholder: '请详细描述拍摄需求（至少 5 个字）',
    riskText: '约拍必须是本人，严禁为他人发布约拍！',
  },
  [YpatTarget.MERCHANT]: {
    value: YpatTarget.MERCHANT,
    label: '约商家',
    showTime: false, showLocation: false, showDeliverable: false, allowNationwide: true,
    uploadHint: '本人作品/本人照片/期待风格',
    descriptionPlaceholder: '请详细描述商家服务需求（至少 5 个字）',
    riskText: '约拍必须是本人，严禁为他人发布约拍！',
  },
  [YpatTarget.MAKEUP]: {
    value: YpatTarget.MAKEUP,
    label: '约化妆师',
    showTime: true, showLocation: true, showDeliverable: true, allowNationwide: false,
    uploadHint: '本人作品/本人照片/期待风格',
    descriptionPlaceholder: '请详细描述化妆需求（至少 5 个字）',
    riskText: '约拍必须是本人，严禁为他人发布约拍！',
  },
  [YpatTarget.RETOUCHER]: {
    value: YpatTarget.RETOUCHER,
    label: '约修图师',
    showTime: false, showLocation: false, showDeliverable: false, allowNationwide: true,
    uploadHint: '本人作品/本人照片/期待风格',
    descriptionPlaceholder: '请详细描述修图需求（至少 5 个字）',
    riskText: '约拍必须是本人，严禁为他人发布约拍！',
  },
  [YpatTarget.MODEL]: {
    value: YpatTarget.MODEL,
    label: '约模特',
    showTime: true, showLocation: true, showDeliverable: true, allowNationwide: false,
    uploadHint: '本人作品/本人照片/期待风格',
    descriptionPlaceholder: '请详细描述模特需求（至少 5 个字）',
    riskText: '约拍必须是本人，严禁为他人发布约拍！',
  },
};

/** 约拍收费方式 */
export const YpatChargeWay = {
  FREE: '0',
  CHARGE: '1',
  CAN_PAY: '2',
  NEGOTIATE: '3',
} as const;

export type YpatChargeWayType = (typeof YpatChargeWay)[keyof typeof YpatChargeWay];

export const CHARGE_WAY_LABELS: Record<string, string> = {
  [YpatChargeWay.FREE]: '希望互勉',
  [YpatChargeWay.CHARGE]: '我要收费',
  [YpatChargeWay.CAN_PAY]: '可付费',
  [YpatChargeWay.NEGOTIATE]: '费用协商',
};

/** 约拍帖子状态 */
export const YpatStatus = {
  DRAFT: 'zc',
  SUBMITTED: 'ytj',
  APPROVED: 'shtg',
  REJECTED: 'shbtg',
} as const;

export type YpatStatusType = (typeof YpatStatus)[keyof typeof YpatStatus];

export const YPAT_STATUS_LABELS: Record<string, string> = {
  [YpatStatus.DRAFT]: '草稿',
  [YpatStatus.SUBMITTED]: '已提交',
  [YpatStatus.APPROVED]: '审核通过',
  [YpatStatus.REJECTED]: '审核不通过',
};

/** 作品状态（2026-07 新增，5 态） */
export const WorkStatus = {
  DRAFT: '0',
  PENDING: '1',
  APPROVED: '2',
  REJECTED: '3',
  OFFLINE: '4',
} as const;

export type WorkStatusType = (typeof WorkStatus)[keyof typeof WorkStatus];

export const WORK_STATUS_LABELS: Record<string, string> = {
  [WorkStatus.DRAFT]: '暂存',
  [WorkStatus.PENDING]: '待审核',
  [WorkStatus.APPROVED]: '审核通过',
  [WorkStatus.REJECTED]: '审核未通过',
  [WorkStatus.OFFLINE]: '已下架',
};

/** 作品媒体类型 */
export const WorkMediaType = {
  IMAGE: '1',
  VIDEO: '2',
} as const;

export type WorkMediaTypeValue = (typeof WorkMediaType)[keyof typeof WorkMediaType];

/**
 * 拍拍豆消耗(对齐后端 system-domain Constant):
 * PUB_NEED_PPD / APPLY_NEED_PPD / VIEW_NEED_PPD 均为 3。
 * 仅用于前端预检与文案展示;实际扣费以服务端为准。
 */
export const VIEW_CONTACT_PPD = 3
export const PUBLISH_PPD = 3
export const APPLY_PPD = 3

/** 积分/PPD记录类型 */
export const RecordType = {
  TOPUP: '0',
  INVITE: '1',
  SYSTEM: '2',
  PUBLISH: '3',
  APPLY: '4',
  VIEW_CONTACT: '5',
} as const;

export type RecordTypeType = (typeof RecordType)[keyof typeof RecordType];

export const RECORD_TYPE_LABELS: Record<string, string> = {
  [RecordType.TOPUP]: '充值',
  [RecordType.INVITE]: '邀请奖励',
  [RecordType.SYSTEM]: '系统赠送',
  [RecordType.PUBLISH]: '发布约拍',
  [RecordType.APPLY]: '报名约拍',
  [RecordType.VIEW_CONTACT]: '查看联系方式',
};

/** 订单类型 */
export const OrderType = {
  PPD: '0',
  REALNAME: '1',
  CREDIT: '2',
} as const;

export type OrderTypeType = (typeof OrderType)[keyof typeof OrderType];

export const ORDER_TYPE_LABELS: Record<string, string> = {
  [OrderType.PPD]: 'PPD充值',
  [OrderType.REALNAME]: '实名认证',
  [OrderType.CREDIT]: '信用分充值',
};

/** 订单状态 */
export const OrderStatus = {
  PAID: '0',
  UNPAID: '1',
} as const;

export type OrderStatusType = (typeof OrderStatus)[keyof typeof OrderStatus];

export const ORDER_STATUS_LABELS: Record<string, string> = {
  [OrderStatus.PAID]: '已支付',
  [OrderStatus.UNPAID]: '未支付',
};

/** 渠道 */
export const Channel = {
  WECHAT: '0',
  BAIDU: '1',
  PC: '2',
} as const;

export type ChannelType = (typeof Channel)[keyof typeof Channel];

export const CHANNEL_LABELS: Record<string, string> = {
  [Channel.WECHAT]: '微信小程序',
  [Channel.BAIDU]: '百度小程序',
  [Channel.PC]: 'PC端',
};

/** 摄影风格列表 */
export const PHOTO_STYLES: string[] = [
  '复古',
  'INS',
  '胶片',
  '少女',
  '暗黑',
  '情绪',
  '夜景',
  '欧美',
  '商务',
  '韩系',
  '日系',
  '情侣',
  '样片',
];
