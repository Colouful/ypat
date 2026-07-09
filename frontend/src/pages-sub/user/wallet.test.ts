import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

describe('wallet page source contract', () => {
  const source = readFileSync(resolve(__dirname, 'wallet.vue'), 'utf-8')

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
})
