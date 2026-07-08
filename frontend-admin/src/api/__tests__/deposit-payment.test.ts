import { describe, expect, it, vi } from 'vitest'

vi.mock('../request', () => ({
  get: vi.fn((url, params) => Promise.resolve({ success: true, data: { url, params } })),
  put: vi.fn((url, data) => Promise.resolve({ success: true, data: { url, data } })),
}))

import { getDepositConfig, getDepositOrders, saveDepositConfig } from '../modules/deposit'
import { getPaymentOrders } from '../modules/payment'

describe('deposit and payment admin api', () => {
  it('maps deposit config and order paths', async () => {
    const config = await getDepositConfig()
    const saved = await saveDepositConfig({ amountFen: 19900, testAmountFen: 1 })
    const orders = await getDepositOrders({ status: 'PAID' })

    expect((config.data as any).url).toBe('/admin/deposit/config')
    expect((saved.data as any).url).toBe('/admin/deposit/config')
    expect((orders.data as any).url).toBe('/admin/deposit/orders')
  })

  it('maps payment order path', async () => {
    const orders = await getPaymentOrders({ businessType: 'DEPOSIT' })

    expect((orders.data as any).url).toBe('/admin/payment/orders')
    expect((orders.data as any).params.businessType).toBe('DEPOSIT')
  })
})
