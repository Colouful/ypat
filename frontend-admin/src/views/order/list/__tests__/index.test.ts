import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('admin order list source', () => {
  const source = readFileSync(resolve(__dirname, '../index.vue'), 'utf8')

  it('converts the amount from fen to yuan', () => {
    expect(source).toContain('function fenText')
    expect(source).toContain('fenText(row.total_fee)')
    expect(source).not.toContain('prop="total_fee" label="金额"')
  })
})
