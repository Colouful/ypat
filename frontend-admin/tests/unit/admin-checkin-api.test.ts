import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

describe('admin checkin api source contract', () => {
  const source = readFileSync(resolve(__dirname, '../../src/api/modules/checkin.ts'), 'utf-8')
  const types = readFileSync(resolve(__dirname, '../../src/api/types.ts'), 'utf-8')

  it('defines checkin admin endpoints', () => {
    expect(source).toContain('/admin/checkin/rule')
    expect(source).toContain('/admin/checkin/records')
    expect(source).toContain('getCheckinRule')
    expect(source).toContain('saveCheckinRule')
    expect(source).toContain('getCheckinRecords')
  })

  it('defines checkin rule and record types', () => {
    expect(types).toContain('interface CheckinRule')
    expect(types).toContain('interface CheckinRecord')
    expect(types).toContain('rewardPpd')
    expect(types).toContain('checkinDate')
  })
})
