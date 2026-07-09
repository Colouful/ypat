import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

describe('wallet page source contract', () => {
  const source = readFileSync(resolve(__dirname, 'wallet.vue'), 'utf-8')
  const devSeed = readFileSync(resolve(__dirname, '../../../../docker/mysql/dev-seed.sql'), 'utf-8')

  it('renders the three bean tabs from the reference flow', () => {
    expect(source).toContain('获得拍豆')
    expect(source).toContain('拍豆用途')
    expect(source).toContain('拍豆记录')
    expect(source).toContain('activeTab')
  })

  it('keeps recharge on the wallet page with a package popup', () => {
    expect(source).toContain('showRechargeModal')
    expect(source).toContain('loadProducts')
    expect(source).toContain('currval')
    expect(source).not.toContain("uni.navigateTo({ url: '/pages-sub/user/recharge' })")
  })

  it('renders recharge products as non-scrolling cards with amount, ppd and recommendation badge', () => {
    expect(source).toContain('recommendedProducts')
    expect(source).toContain('hasRechargeProducts')
    expect(source).toContain('isRecommendedProduct')
    expect(source).toContain('product-card__badge')
    expect(source).toContain('优先推荐')
    expect(source).toContain('充值金额')
    expect(source).toContain('获得拍豆数')
    expect(source).not.toContain('<scroll-view')
  })

  it('loads wallet data once on first entry to avoid duplicate request blocking', () => {
    expect(source).toContain('refreshWalletData')
    expect(source).toContain('walletLoaded')
    expect(source).toContain('productLoadPromise')
    expect(source).not.toMatch(/onLoad\(\(\) => \{[\s\S]*fetchRecentRecords\(\)[\s\S]*\}\)/)
    expect(source).not.toMatch(/onShow\(\(\) => \{[\s\S]*fetchRecentRecords\(\)[\s\S]*\}\)/)
  })

  it('uses the app green theme for page and recharge popup', () => {
    expect(source).toContain('$color-primary')
    expect(source).toContain('$color-primary-dark')
    expect(source).toContain('$color-primary-light')
    expect(source).not.toContain('#f85a6d')
    expect(source).not.toContain('#ff4e63')
    expect(source).not.toContain('#ff6f8a')
  })

  it('uses a taller recharge popup', () => {
    expect(source).toContain('max-height: 88vh')
    expect(source).toContain('min-height: 640rpx')
    expect(source).toContain('env(safe-area-inset-bottom)')
    expect(source).toContain('recharge-empty__retry')
  })

  it('initializes ppd recharge products for local development', () => {
    expect(devSeed).toContain('10拍豆')
    expect(devSeed).toContain('30拍豆')
    expect(devSeed).toContain('60拍豆')
    expect(devSeed).toContain('recommended')
    expect(devSeed).toContain("'1'")
    expect(devSeed).toContain("'0'")
    expect(devSeed).toContain('ON DUPLICATE KEY UPDATE')
  })
})
