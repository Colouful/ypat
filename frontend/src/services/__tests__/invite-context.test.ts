import { beforeEach, describe, expect, it } from 'vitest'
import {
  captureInviteFromQuery,
  clearInviteContext,
  consumeInviteContext,
  getInviteContext,
} from '../invite-context'

describe('invite-context', () => {
  beforeEach(() => {
    uni.clearStorageSync()
  })

  it('captures recmobile only when it passes phone validation', () => {
    const ctx = captureInviteFromQuery({ recmobile: '13800138000' })
    expect(ctx?.recmobile).toBe('13800138000')
  })

  it('rejects malformed recmobile silently', () => {
    const ctx = captureInviteFromQuery({ recmobile: '00000' })
    expect(ctx).toBeNull()
    expect(getInviteContext()).toBeNull()
  })

  it('captures inviteCode independently of recmobile', () => {
    const ctx = captureInviteFromQuery({ inviteCode: 'IV3F' })
    expect(ctx?.inviteCode).toBe('IV3F')
  })

  it('captures source when provided', () => {
    const ctx = captureInviteFromQuery({ inviteCode: 'IV3F', source: 'share' })
    expect(ctx?.source).toBe('share')
  })

  it('preserves inviteCode alongside recmobile', () => {
    const ctx = captureInviteFromQuery({ inviteCode: 'IV3F', recmobile: '13800138000' })
    expect(ctx?.inviteCode).toBe('IV3F')
    expect(ctx?.recmobile).toBe('13800138000')
  })

  it('expires after 24 hours', () => {
    captureInviteFromQuery({ recmobile: '13800138000' }, 1_000_000_000)
    expect(getInviteContext(1_000_000_000 + 23 * 3600 * 1000)).not.toBeNull()
    expect(getInviteContext(1_000_000_000 + 25 * 3600 * 1000)).toBeNull()
  })

  it('refuses self-invite when consumed', () => {
    captureInviteFromQuery({ recmobile: '13800138000' })
    expect(consumeInviteContext('13800138000')).toBeNull()
  })

  it('returns context once on consume and clears storage', () => {
    captureInviteFromQuery({ recmobile: '13800138000' })
    expect(consumeInviteContext('13900139000')?.recmobile).toBe('13800138000')
    expect(getInviteContext()).toBeNull()
  })

  it('clears storage on demand', () => {
    captureInviteFromQuery({ inviteCode: 'X' })
    clearInviteContext()
    expect(getInviteContext()).toBeNull()
  })
})
