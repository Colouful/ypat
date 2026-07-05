import { describe, expect, it, vi } from 'vitest'

vi.mock('../request', () => ({
  get: vi.fn((url, params) => Promise.resolve({ success: true, data: { url, params } })),
  post: vi.fn((url, data) => Promise.resolve({ success: true, data: { url, data } })),
  put: vi.fn((url, data) => Promise.resolve({ success: true, data: { url, data } })),
}))

import {
  cancelMember,
  getMemberLogs,
  getMemberOrders,
  getMemberPlans,
  saveMemberBenefitRule,
  saveMemberPlan,
} from '../modules/member'

describe('admin member api', () => {
  it('queries member plans from admin path', async () => {
    const res = await getMemberPlans({ page: 0, size: 10, status: '1' })

    expect((res.data as any).url).toBe('/admin/member/plans')
    expect((res.data as any).params.status).toBe('1')
  })

  it('creates plan with POST and updates plan with PUT', async () => {
    const created = await saveMemberPlan({ name: '月卡' })
    const updated = await saveMemberPlan({ id: 7, name: '季卡' })

    expect((created.data as any).url).toBe('/admin/member/plans')
    expect((updated.data as any).url).toBe('/admin/member/plans/7')
  })

  it('updates benefit rule by id', async () => {
    const res = await saveMemberBenefitRule({ id: 3, discountPpd: 2 })

    expect((res.data as any).url).toBe('/admin/member/benefit-rules/3')
  })

  it('maps orders, logs, and cancel action paths', async () => {
    const orders = await getMemberOrders({ userId: 12 })
    const logs = await getMemberLogs({ actionType: 'ADMIN_CANCEL' })
    const cancelled = await cancelMember(12, { reason: '误开通' })

    expect((orders.data as any).url).toBe('/admin/member/orders')
    expect((logs.data as any).url).toBe('/admin/member/logs')
    expect((cancelled.data as any).url).toBe('/admin/member/users/12/cancel')
  })
})
