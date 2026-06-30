/**
 * 与后端 `system-wap/src/main/java/com/ypat/comm/Const.SYS_ADMIN` 同步的管理员 openid。
 * 后端无 role/isAdmin 字段，前端通过 openid 比对兜底判定管理员入口可见性；
 * 切片 2/3 推荐由后端在 `/user/get` 响应增加 `roles` 字段后移除该硬编码。
 */
export const ADMIN_OPENIDS: readonly string[] = ['o5ZmB4kyCVPskEOaO0PK1He0Kl7w']

export function isAdminOpenid(openid: string | undefined | null): boolean {
  if (!openid) return false
  return ADMIN_OPENIDS.includes(openid)
}
