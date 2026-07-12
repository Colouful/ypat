import fs from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'

describe('realname intro page source', () => {
  const source = fs.readFileSync(path.resolve(__dirname, '../realname-intro.vue'), 'utf8')

  it('redirects an auditing user to the realname status page before showing the intro', () => {
    expect(source).toContain('v-if="!checkingStatus"')
    expect(source).toContain('await userStore.updateUserInfo()')
    expect(source).toContain("latestUser?.status === '1'")
    expect(source).toContain('uni.redirectTo({ url: realnameUrl.value })')
  })
})
