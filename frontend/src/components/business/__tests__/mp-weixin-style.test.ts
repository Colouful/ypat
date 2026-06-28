import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('mp-weixin component styles', () => {
  it('does not use tag selectors in KeepTabBar component wxss', () => {
    const file = fileURLToPath(new URL('../KeepTabBar.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).not.toMatch(/\.keep-tabbar__[^{]+\s+(text|view|image|button|navigator)\b/)
  })
})
