import fs from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'

describe('realname page legacy paid flow source', () => {
  const source = fs.readFileSync(path.resolve(__dirname, '../realname.vue'), 'utf8')

  it('requires three photos and labels hand-held ID upload', () => {
    expect(source).toContain('handPath')
    expect(source).toContain("chooseImage('hand')")
    expect(source).toContain('手持身份证')
    expect(source).toContain('REALNAME_PHOTO_COUNT')
  })

  it('pays before first submission and waits for server confirmation', () => {
    expect(source).toContain('createRealnameOrder')
    expect(source).toContain('waitForRealnamePayment')
    expect(source).toContain('getOrderStatus')
    expect(source).toContain('submitAfterPaymentConfirmed')
    expect(source).not.toContain('pics: [frontPath.value, backPath.value]')
  })
})
