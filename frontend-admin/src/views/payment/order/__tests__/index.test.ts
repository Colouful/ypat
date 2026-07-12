import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('admin payment order source', () => {
  const source = readFileSync(resolve(__dirname, '../index.vue'), 'utf8')

  it('shows the Chinese business name followed by the original code', () => {
    expect(source).toContain("DEPOSIT: '保证金'")
    expect(source).toContain("MEMBER: '会员'")
    expect(source).toContain("PPD: '拍拍豆'")
    expect(source).toContain("REALNAME: '实名认证'")
    expect(source).toContain('businessText(row.businessType)')
  })
})
