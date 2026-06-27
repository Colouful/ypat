import { UserGender } from '@/constants/enums'
import type { UserInfo } from '@/api/types'

/**
 * 登录完善资料门禁 — 对齐旧版 getNextUrl.js 的判定:
 * profess && gender∈{1,2} && birthday && province && city。
 * 任一缺失即视为资料不完整,登录后需引导到 complete-info。
 * (发布/报名另有更严格的前置: gender/wx/mobile/nickname/imgpath, 见 Module D)
 */
export function isProfileComplete(user: UserInfo | null | undefined): boolean {
  if (!user) return false
  const genderValid = user.gender === UserGender.MALE || user.gender === UserGender.FEMALE
  return Boolean(
    user.profess &&
      genderValid &&
      user.birthday &&
      user.province &&
      user.city,
  )
}
