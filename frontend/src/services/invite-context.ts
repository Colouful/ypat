import { isPhone } from '@/utils/validate'

const STORAGE_KEY = 'ypat_invite_context'
const TTL_MS = 24 * 60 * 60 * 1000

export interface InviteContext {
  inviteCode?: string
  recmobile?: string
  source?: string
  createdAt: number
}

interface InviteParamsInput {
  inviteCode?: string | null
  recmobile?: string | null
  source?: string | null
}

function readRaw(): InviteContext | null {
  const raw = uni.getStorageSync(STORAGE_KEY)
  if (!raw) return null
  try {
    const parsed = typeof raw === 'string' ? (JSON.parse(raw) as InviteContext) : (raw as InviteContext)
    if (!parsed || typeof parsed.createdAt !== 'number') return null
    return parsed
  } catch {
    uni.removeStorageSync(STORAGE_KEY)
    return null
  }
}

export function getInviteContext(now: number = Date.now()): InviteContext | null {
  const ctx = readRaw()
  if (!ctx) return null
  if (now - ctx.createdAt > TTL_MS) {
    uni.removeStorageSync(STORAGE_KEY)
    return null
  }
  return ctx
}

/**
 * 从登录页 query 写入邀请上下文。recmobile 必须通过手机号格式校验，
 * 避免旧版分享 URL（recmobile=138xxxx）被随意伪造。inviteCode 留给切片 2 上线后使用。
 */
export function captureInviteFromQuery(input: InviteParamsInput, now: number = Date.now()): InviteContext | null {
  const ctx: InviteContext = { createdAt: now }
  const code = (input.inviteCode || '').toString().trim()
  if (code) ctx.inviteCode = code

  const mobile = (input.recmobile || '').toString().trim()
  if (mobile && isPhone(mobile)) ctx.recmobile = mobile

  const source = (input.source || '').toString().trim()
  if (source) ctx.source = source

  if (!ctx.inviteCode && !ctx.recmobile) return null
  uni.setStorageSync(STORAGE_KEY, ctx)
  return ctx
}

/**
 * 登录成功且完成 recmobile 绑定后调用。不接受自我邀请（recmobile === 当前用户手机号）。
 */
export function consumeInviteContext(currentUserMobile?: string): InviteContext | null {
  const ctx = getInviteContext()
  uni.removeStorageSync(STORAGE_KEY)
  if (!ctx) return null
  if (ctx.recmobile && currentUserMobile && ctx.recmobile === currentUserMobile) return null
  return ctx
}

export function clearInviteContext(): void {
  uni.removeStorageSync(STORAGE_KEY)
}
