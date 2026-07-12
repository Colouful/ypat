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
        sceneName: '发布约拍',
        memberActive: true,
        levelCode: 'BASIC',
        levelName: '基础会员',
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

  it('caches benefit quotes independently by scene', async () => {
    vi.mocked(memberApi.getMemberBenefitQuote).mockImplementation(async (scene) => ({
      success: true,
      code: '200',
      message: '',
      data: {
        scene,
        sceneName: scene === 'SUBMIT_YPAT' ? '发布约拍' : '发起约拍申请',
        memberActive: true,
        levelCode: 'BASIC',
        levelName: '基础会员',
        originalPpd: 3,
        discountPpd: scene === 'SUBMIT_YPAT' ? 2 : 1,
        actualPpd: scene === 'SUBMIT_YPAT' ? 1 : 2,
        ruleEffective: true,
      },
    }))
    const store = useMemberStore()

    await store.refreshBenefitQuote('SUBMIT_YPAT')
    await store.refreshBenefitQuote('APPLY_YPAT')

    expect(store.quotes.SUBMIT_YPAT?.actualPpd).toBe(1)
    expect(store.quotes.APPLY_YPAT?.actualPpd).toBe(2)
    expect(memberApi.getMemberBenefitQuote).toHaveBeenNthCalledWith(1, 'SUBMIT_YPAT')
    expect(memberApi.getMemberBenefitQuote).toHaveBeenNthCalledWith(2, 'APPLY_YPAT')
  })
})
