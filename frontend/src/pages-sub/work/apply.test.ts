import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('work apply page bean cost contract', () => {
  const file = fileURLToPath(new URL('./apply.vue', import.meta.url))
  const source = readFileSync(file, 'utf8')

  it('shows current balance and apply cost in the fixed bottom bar', () => {
    expect(source).toContain('class="bottom-bar__cost"')
    expect(source).toContain('剩余拍豆')
    expect(source).toContain('本次消耗')
    expect(source).toContain('{{ currentPpd }}')
    expect(source).toContain('{{ applyCost }}')
  })

  it('uses the shared apply bean cost constant instead of hardcoded copy', () => {
    expect(source).toContain('APPLY_PPD')
    expect(source).toContain('const applyCost = APPLY_PPD')
    expect(source).not.toContain('1 拍拍豆')
  })

  it('refreshes balance before submit and guides low-balance users to recharge', () => {
    expect(source).toContain('await refreshPpdBalance()')
    expect(source).toContain('showRechargeGuide')
    expect(source).toContain('拍豆余额不足')
    expect(source).toContain("uni.navigateTo({ url: '/pages-sub/user/recharge' })")
    expect(source.indexOf('const latestPpd = await refreshPpdBalance()')).toBeLessThan(source.indexOf("await requestMessageSubscribe('apply')"))
  })

  it('locks submission before refreshing the balance', () => {
    const submitStart = source.indexOf('async function submitApply()')
    const submitEnd = source.indexOf('function buildYpatApplyContent()', submitStart)
    const submitSource = source.slice(submitStart, submitEnd)

    expect(submitSource.indexOf('submitting.value = true')).toBeLessThan(
      submitSource.indexOf('await refreshPpdBalance()'),
    )
  })

  it('blocks direct links after either ypat or work has already been applied to', () => {
    expect(source).toContain("const isAlreadyApplied = computed(() => ypatId.value ? ypat.value?.msgflag === '1' : work.value?.isApplied === true)")
    expect(source).toContain("isAlreadyApplied ? '已约拍' : '确认提交'")
    expect(source).toContain("uni.showToast({ title: '你已提交过该约拍', icon: 'none' })")
  })

  it('refreshes target state before balance and lets the server resolve identities', () => {
    expect(source).toContain('const latestTarget = ypatId.value ? await loadYpat() : await loadWork()')
    expect(source.indexOf('const latestTarget =')).toBeLessThan(source.indexOf('await refreshPpdBalance()'))
    expect(source).not.toContain('sendperid: currentUserId')
    expect(source).not.toContain('recperid: publisherId')
  })
})
