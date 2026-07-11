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

  it('registers the credit guarantee page and uses configurable deposit payment', () => {
    const pagesJson = readSource('pages.json')
    const creditPage = readSource('pages-sub/user/credit.vue')

    expect(pagesJson).toContain('"path": "credit"')
    expect(creditPage).toContain('保证金金额')
    expect(creditPage).toContain('depositAmountYuan')
    expect(creditPage).toContain('createDepositOrder')
    expect(creditPage).not.toContain('DEPOSIT_FEE_FEN')
    expect(creditPage).not.toContain("type: '2'")
    expect(creditPage).toContain('uni.requestPayment')
    expect(creditPage).toContain('保证金协议')
  })

  it('hides deposit agreement and pay action after credit guarantee is active', () => {
    const creditPage = readSource('pages-sub/user/credit.vue')

    expect(creditPage).toContain('v-if="!isGuaranteed" class="agreement"')
    expect(creditPage).toContain('v-if="!isGuaranteed"')
    expect(creditPage).toContain('申请退还')
    expect(creditPage).toContain('handleRefundRequest')
  })

  it('renders checkin entry beside the menu icon', () => {
    const source = readSource('pages/mine/index.vue')

    expect(source).toContain('mine-top__left')
    expect(source).toContain('mine-top__checkin')
    expect(source).toContain('calendar-check')
    expect(source).toContain('checkinToday.value?.enabled === true')
  })

  it('gives both top action icon hosts explicit centered dimensions', () => {
    const source = readSource('pages/mine/index.vue')

    expect(source).toContain('class="mine-top__action-icon mine-top__action-icon--menu"')
    expect(source).toContain('class="mine-top__action-icon mine-top__action-icon--checkin"')
    expect(source).toContain('.mine-top__action-icon {')
    expect(source).toContain('display: flex;')
    expect(source).toContain('align-items: center;')
    expect(source).toContain('justify-content: center;')
    expect(source).toContain('.mine-top__action-icon--menu {')
    expect(source).toContain('width: 42rpx;')
    expect(source).toContain('.mine-top__action-icon--checkin {')
    expect(source).toContain('width: 34rpx;')
  })

  it('loads checkin status and executes checkin through confirm modal', () => {
    const source = readSource('pages/mine/index.vue')

    expect(source).toContain('getCheckinToday')
    expect(source).toContain('doCheckin')
    expect(source).toContain('uni.showModal')
    expect(source).toContain('checkinSubmitting')
    expect(source).toContain('const data = result.data')
    expect(source).toContain('!data?.checkedIn')
    expect(source).toMatch(/if \(!data\?\.checkedIn\) \{[\s\S]*await loadCheckinToday\(\)[\s\S]*return/)
    expect(source).toContain('签到成功，获得')
  })
})
