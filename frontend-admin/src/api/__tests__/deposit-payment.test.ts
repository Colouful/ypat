import { describe, expect, it, vi } from 'vitest'

vi.mock('../request', () => ({
  get: vi.fn((url, params) => Promise.resolve({ success: true, data: { url, params } })),
  put: vi.fn((url, data) => Promise.resolve({ success: true, data: { url, data } })),
}))

import { getDepositConfig, getDepositOrders, saveDepositConfig } from '../modules/deposit'
import { getPaymentOrders } from '../modules/payment'

interface MockResponseData {
  url: string
  data?: { realnameAuditFeeFen?: number }
  params?: { businessType?: string }
}

describe('deposit and payment admin api', () => {
  it('maps deposit config and order paths', async () => {
    const config = await getDepositConfig()
    const saved = await saveDepositConfig({ amountFen: 19900, testAmountFen: 1, realnameAuditFeeFen: 1 })
    const orders = await getDepositOrders({ status: 'PAID' })

    expect((config.data as unknown as MockResponseData).url).toBe('/admin/deposit/config')
    expect((saved.data as unknown as MockResponseData).url).toBe('/admin/deposit/config')
    expect((saved.data as unknown as MockResponseData).data?.realnameAuditFeeFen).toBe(1)
    expect((orders.data as unknown as MockResponseData).url).toBe('/admin/deposit/orders')
  })

  it('maps payment order path', async () => {
    const orders = await getPaymentOrders({ businessType: 'DEPOSIT' })

    expect((orders.data as unknown as MockResponseData).url).toBe('/admin/payment/orders')
    expect((orders.data as unknown as MockResponseData).params?.businessType).toBe('DEPOSIT')
  })
})
