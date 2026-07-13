import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('消息详情联系方式报价', () => {
  const file = fileURLToPath(new URL('./message-detail.vue', import.meta.url))
  const source = readFileSync(file, 'utf8')

  it('loads and displays the VIEW_CONTACT quote', () => {
    expect(source).toContain("refreshBenefitQuote('VIEW_CONTACT')")
    expect(source).toContain('memberStore.quotes.VIEW_CONTACT')
    expect(source).toContain('本次实扣')
    expect(source).toContain('原价')
    expect(source).toContain('会员优惠')
    expect(source).not.toContain('VIEW_CONTACT_PPD')
  })

  it('uses actual cost for balance checks and blocks unlock when quote failed', () => {
    expect(source).toContain('currentPpd < viewContactCost.value')
    expect(source).toContain('if (quoteFailed.value)')
    expect(source.indexOf('if (quoteFailed.value)')).toBeLessThan(
      source.indexOf('revealContact()', source.indexOf('function handleViewContact')),
    )
    expect(source).toContain('费用加载失败，点击重试')
  })

  it('restores already unlocked contact details without showing the paid action', () => {
    expect(source).toContain("if (message.value?.linkwayflag === '1')")
    expect(source).toContain('await revealContact()')
  })
})
