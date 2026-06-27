import { describe, expect, it } from 'vitest'
import { isProfileComplete, isPublishProfileReady } from '../profile'
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

const publishReady: UserInfo = {
  id: 1,
  gender: '2',
  wx: 'my-wechat',
  mobile: '13800138000',
  nickname: '小明',
  imgpath: 'https://x/y.png',
}

describe('isPublishProfileReady (GAP-D-03 / GAP-A-EDIT-01)', () => {
  it('true when gender/wx/mobile/nickname/avatar present', () => {
    expect(isPublishProfileReady(publishReady)).toBe(true)
    expect(isPublishProfileReady({ ...publishReady, imgpath: '', avatarurl: 'https://a/b.png' })).toBe(true)
  })

  it.each(['wx', 'mobile', 'nickname'] as const)('false when %s missing', (field) => {
    expect(isPublishProfileReady({ ...publishReady, [field]: '' })).toBe(false)
  })

  it('false when no avatar at all and unknown gender', () => {
    expect(isPublishProfileReady({ ...publishReady, imgpath: '', avatarurl: '' })).toBe(false)
    expect(isPublishProfileReady({ ...publishReady, gender: '0' })).toBe(false)
    expect(isPublishProfileReady(null)).toBe(false)
  })
})
