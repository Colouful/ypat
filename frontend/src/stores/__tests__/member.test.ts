import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import * as memberApi from '@/api/modules/member'
import { useMemberStore } from '../member'

vi.mock('@/api/modules/member', () => ({
  getMemberBenefitQuote: vi.fn(),
  getMemberOrderStatus: vi.fn(),
  getMemberStatus: vi.fn(),
}))

describe('member store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(memberApi.getMemberBenefitQuote).mockReset()
  })

  it('caches submit ypat quote', async () => {
    vi.mocked(memberApi.getMemberBenefitQuote).mockResolvedValue({
      success: true,
      code: '200',
      message: '',
      data: {
        scene: 'SUBMIT_YPAT',
        memberActive: true,
        levelCode: 'BASIC',
        originalPpd: 5,
        discountPpd: 2,
        actualPpd: 3,
        ruleEffective: true,
      },
    })
    const store = useMemberStore()

    const quote = await store.refreshSubmitYpatQuote()

    expect(quote?.actualPpd).toBe(3)
    expect(store.submitYpatQuote?.discountPpd).toBe(2)
    expect(memberApi.getMemberBenefitQuote).toHaveBeenCalledWith('SUBMIT_YPAT')
  })
})
