import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const srcRoot = resolve(__dirname, '../..')

function readSource(path: string): string {
  return readFileSync(resolve(srcRoot, path), 'utf8')
}

describe('mine navigation', () => {
  it('shows profile, invite, credit and paidou entries in the mine quick grid', () => {
    const source = readSource('pages/mine/index.vue')

    expect(source).toContain('我的拍豆')
    expect(source).not.toContain('我的钱包')
    expect(source).toContain('我的主页')
    expect(source).toContain('好友邀请')
    expect(source).toContain('信用担保')
    expect(source).toContain('/pages-sub/user/credit')
  })

  it('removes message, member, records, profile and invite entries from user center', () => {
    const source = readSource('pages-sub/user/center.vue')

    expect(source).not.toContain('我的消息')
    expect(source).not.toContain('会员中心')
    expect(source).not.toContain('收支记录')
    expect(source).not.toContain('我的主页')
    expect(source).not.toContain('好友邀请')
  })

  it('keeps help, feedback, about and settings in one system group on user center', () => {
    const source = readSource('pages-sub/user/center.vue')

    expect(source).not.toContain("title: '帮助与反馈'")
    expect(source).toContain("title: '系统'")
    expect(source).toContain("title: '帮助中心'")
    expect(source).toContain("title: '意见反馈'")
    expect(source).toContain("title: '关于我们'")
    expect(source).toContain("title: '设置'")
  })

  it('registers the credit guarantee page and keeps the legacy deposit contract', () => {
    const pagesJson = readSource('pages.json')
    const creditPage = readSource('pages-sub/user/credit.vue')

    expect(pagesJson).toContain('"path": "credit"')
    expect(creditPage).toContain('保证金金额')
    expect(creditPage).toContain('199')
    expect(creditPage).toContain("type: '2'")
    expect(creditPage).toContain('uni.requestPayment')
    expect(creditPage).toContain('保证金协议')
  })
})
