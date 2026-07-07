import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const source = readFileSync(resolve(__dirname, 'profile.vue'), 'utf8')

describe('user profile refresh', () => {
  it('refreshes profile data when returning from edit info page', () => {
    expect(source).toMatch(/import\s*\{\s*onLoad,\s*onShow\s*\}\s*from\s*'@dcloudio\/uni-app'/)
    expect(source).toMatch(/onShow\(\(\)\s*=>\s*\{[\s\S]*loadProfile\(\)[\s\S]*\}\)/)
  })
})
