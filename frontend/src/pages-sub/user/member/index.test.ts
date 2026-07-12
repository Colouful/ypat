import fs from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'

describe('会员中心紧凑布局', () => {
  const source = fs.readFileSync(path.resolve(__dirname, 'index.vue'), 'utf8')

  it('在会员状态卡与完整权益之间展示双列套餐', () => {
    const plansIndex = source.indexOf('section--plans')
    const benefitsIndex = source.indexOf('section--benefits')

    expect(plansIndex).toBeGreaterThan(-1)
    expect(benefitsIndex).toBeGreaterThan(-1)
    expect(plansIndex).toBeLessThan(benefitsIndex)
    expect(source).toContain('grid-template-columns: repeat(2, minmax(0, 1fr))')
    expect(source).toContain('.plan-card:last-child:nth-child(odd)')
    expect(source).toContain('benefit-item__desc')
    expect(source).not.toContain('plan-card__benefits')
    expect(source).not.toContain('plan-card__select')
  })
})
