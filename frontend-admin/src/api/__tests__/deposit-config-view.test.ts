import fs from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'

describe('保证金配置页面', () => {
  const source = fs.readFileSync(path.resolve(__dirname, '../../views/deposit/config/index.vue'), 'utf8')

  it('提供实名认证审核费配置', () => {
    expect(source).toContain('realnameAuditFeeFen')
    expect(source).toContain('realnameAuditFeeYuan')
    expect(source).toContain('实名认证审核费')
    expect(source).toContain(':min="0.01"')
  })
})
