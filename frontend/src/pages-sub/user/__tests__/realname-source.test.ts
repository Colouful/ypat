import fs from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'

describe('realname page paid flow source', () => {
  const source = fs.readFileSync(path.resolve(__dirname, '../realname.vue'), 'utf8')

  it('requires three photos and labels hand-held ID upload', () => {
    expect(source).toContain('handPath')
    expect(source).toContain("chooseImage('hand')")
    expect(source).toContain('手持身份证')
    expect(source).toContain('REALNAME_PHOTO_COUNT')
  })

  it('pays before first submission and waits for server confirmation', () => {
    expect(source).toContain('createRealnamePaymentOrder')
    expect(source).toContain('waitForRealnamePayment')
    expect(source).toContain('getRealnameOrderStatus')
    expect(source).toContain('submitAfterPaymentConfirmed')
    expect(source).toContain('resumePendingRealnamePayment')
    expect(source).toContain('pendingOutTradeNo')
    expect(source).toContain('getPaymentChannel')
    expect(source).toContain('toMiniappPayParams')
    expect(source).toContain('redirectToH5Pay')
    expect(source).toContain('order.outTradeNo')
    expect(source).not.toContain('paymentApi.createOrder({')
    expect(source).not.toContain('pics: [frontPath.value, backPath.value]')
    expect(source).not.toContain("order.result_code === 'SUCCESS'")
  })

  it('rechecks a pending payment order before creating another realname order', () => {
    const submitBody = source.slice(source.indexOf('async function submit()'), source.indexOf('async function confirmAndPay()'))
    expect(submitBody).toMatch(
      /if \(needsPayment\.value\)[\s\S]*const resumed = await resumePendingRealnamePayment\(\)[\s\S]*if \(resumed\) return[\s\S]*await confirmAndPay\(\)/,
    )
  })

  it('loads the configured audit fee and blocks payment when the fee is unavailable', () => {
    expect(source).toContain('getDepositConfig')
    expect(source).toContain('realnameAuditFeeFen')
    expect(source).toContain('realnameAuditFeeText')
    expect(source).toContain('认证费用加载失败，请稍后重试')
    expect(source).not.toContain('实名认证审核费 29 元')
    expect(source).not.toContain('REALNAME_AUDIT_FEE_YUAN')
  })

  it('refreshes realname state on pending payment timeout without clearing the pending order first', () => {
    const confirmBody = source.slice(source.indexOf('async function confirmAndPay()'), source.indexOf('function confirmRealnamePayment()'))
    const pendingBranchStart = confirmBody.indexOf('if (!paid)')
    const pendingBranchEnd = confirmBody.indexOf("uni.showToast({ title: '支付确认中，请稍后重试'")
    const pendingBranch = confirmBody.slice(pendingBranchStart, pendingBranchEnd)
    const submitWhenStateRefreshedStart = pendingBranch.indexOf('if (canSubmitWithoutPay.value)')
    const stillPendingPath = pendingBranch.slice(pendingBranch.lastIndexOf('}') + 1)

    expect(pendingBranchStart).toBeGreaterThan(-1)
    expect(pendingBranchEnd).toBeGreaterThan(pendingBranchStart)
    expect(submitWhenStateRefreshedStart).toBeGreaterThan(-1)
    expect(pendingBranch).toContain('await refreshRealnameState()')
    expect(pendingBranch).toContain('if (canSubmitWithoutPay.value)')
    expect(pendingBranch.slice(submitWhenStateRefreshedStart)).toContain("pendingOutTradeNo = ''")
    expect(stillPendingPath).not.toContain("pendingOutTradeNo = ''")
  })
})
