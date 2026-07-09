import { beforeEach, describe, expect, it, vi } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const getMock = vi.fn((url: string, params?: Record<string, unknown>) => Promise.resolve({ url, params }))
const putMock = vi.fn((url: string, data?: unknown) => Promise.resolve({ url, data }))

vi.mock('@/api/request', () => ({
  get: getMock,
  put: putMock,
}))

describe('admin checkin api source contract', () => {
  const source = readFileSync(resolve(__dirname, '../../src/api/modules/checkin.ts'), 'utf-8')
  const types = readFileSync(resolve(__dirname, '../../src/api/types.ts'), 'utf-8')

  beforeEach(() => {
    getMock.mockClear()
    putMock.mockClear()
  })

  it('calls checkin admin endpoints with expected request contract', async () => {
    const api = await import('@/api/modules/checkin')
    const payload = {
      enabled: '1',
      rewardPpd: 20,
      confirmTitle: '签到成功',
      confirmContent: '今日已获得奖励',
    }
    const params = {
      page: 0,
      size: 10,
      userid: 3,
      mobile: '13800000000',
      dateFrom: '2026-07-01',
      dateTo: '2026-07-09',
    }

    await api.getCheckinRule()
    expect(getMock).toHaveBeenNthCalledWith(1, '/admin/checkin/rule')

    await api.saveCheckinRule(payload)
    expect(putMock).toHaveBeenCalledWith('/admin/checkin/rule', payload)

    await api.getCheckinRecords(params)
    expect(getMock).toHaveBeenNthCalledWith(2, '/admin/checkin/records', params)
  })

  it('defines checkin admin endpoints and payload type', () => {
    expect(source).toContain('/admin/checkin/rule')
    expect(source).toContain('/admin/checkin/records')
    expect(source).toContain('getCheckinRule')
    expect(source).toContain('saveCheckinRule')
    expect(source).toContain('getCheckinRecords')
    expect(source).toContain("export type CheckinRulePayload = Pick<CheckinRule, 'enabled' | 'rewardPpd' | 'confirmTitle' | 'confirmContent'>")
  })

  it('defines checkin rule and record types', () => {
    expect(types).toContain('interface CheckinRule')
    expect(types).toContain('interface CheckinRecord')
    expect(types).toContain('rewardPpd')
    expect(types).toContain('checkinDate')
  })
})
