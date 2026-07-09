import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('admin checkin page source contract', () => {
  const page = readFileSync(resolve(__dirname, '../../src/views/checkin/index.vue'), 'utf-8')
  const menu = readFileSync(resolve(__dirname, '../../src/constants/menu.ts'), 'utf-8')
  const permission = readFileSync(resolve(__dirname, '../../src/stores/modules/permission.ts'), 'utf-8')

  it('contains rule form and record table', () => {
    expect(page).toContain('签到规则')
    expect(page).toContain('签到记录')
    expect(page).toContain('rewardPpd')
    expect(page).toContain('confirmTitle')
    expect(page).toContain('getCheckinRecords')
  })

  it('registers checkin menu and route component', () => {
    expect(menu).toContain('签到管理')
    expect(menu).toContain('checkin/index')
    expect(permission).toContain('CheckinIndex')
    expect(permission).toContain("'checkin/index': CheckinIndex")
  })
})
