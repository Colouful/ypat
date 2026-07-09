import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

describe('product edit dialog source contract', () => {
  const source = readFileSync(resolve(__dirname, '../ProductEditDialog.vue'), 'utf-8')

  it('makes ppd amount and payment amount explicit for recharge configuration', () => {
    expect(source).toContain('充值数量')
    expect(source).toContain('支付金额')
    expect(source).toContain('拍豆')
    expect(source).toContain('元')
    expect(source).toContain(':min="1"')
  })
})
