import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const pageSource = readFileSync(resolve(__dirname, 'index.vue'), 'utf8')

describe('login agreement authorization flow', () => {
  it('checks the agreement before showing the WeChat phone authorization popup', () => {
    expect(pageSource).toContain('v-if="agreed"')
    expect(pageSource).toMatch(/v-if="agreed"[\s\S]*class="wx-button"[\s\S]*open-type="getPhoneNumber"/)
    expect(pageSource).toMatch(/v-else[\s\S]*class="wx-button"[\s\S]*@tap="showAgreementConfirm"/)
    expect(pageSource).toContain('agreementConfirmVisible')
    expect(pageSource).toMatch(/class="agreement-modal__confirm"[\s\S]*open-type="getPhoneNumber"/)
    expect(pageSource).toContain('@tap="acceptAgreementBeforeAuthorization"')
    expect(pageSource).toContain('@getphonenumber="handleWechatPhoneAuthorization"')
  })

  it('keeps agreement links clickable inside the pre-authorization confirmation', () => {
    expect(pageSource).toMatch(/class="agreement-modal__link"[\s\S]*@tap\.stop="goAgreement"/)
    expect(pageSource).toMatch(/class="agreement-modal__link"[\s\S]*@tap\.stop="goPrivacy"/)
  })
})
