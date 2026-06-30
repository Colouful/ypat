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
