import { describe, expect, it } from 'vitest'
import { isProfileComplete } from '../profile'
import type { UserInfo } from '@/api/types'

const complete: UserInfo = {
  id: 1,
  gender: '1',
  profess: '2',
  birthday: '1995-01-01',
  province: '广东省',
  city: '深圳市',
}

describe('isProfileComplete (GAP-AUTH-03)', () => {
  it('true when all login-gate fields present and gender is 1/2', () => {
    expect(isProfileComplete(complete)).toBe(true)
    expect(isProfileComplete({ ...complete, gender: '2' })).toBe(true)
  })

  it('false for null / unknown gender', () => {
    expect(isProfileComplete(null)).toBe(false)
    expect(isProfileComplete({ ...complete, gender: '0' })).toBe(false)
    expect(isProfileComplete({ ...complete, gender: undefined })).toBe(false)
  })

  it.each(['profess', 'birthday', 'province', 'city'] as const)('false when %s missing', (field) => {
    expect(isProfileComplete({ ...complete, [field]: '' })).toBe(false)
  })
})
