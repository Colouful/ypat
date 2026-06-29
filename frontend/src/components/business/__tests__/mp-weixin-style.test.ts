import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('mp-weixin component styles', () => {
  it('does not use tag selectors in KeepTabBar component wxss', () => {
    const file = fileURLToPath(new URL('../KeepTabBar.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).not.toMatch(/\.keep-tabbar__[^{]+\s+(text|view|image|button|navigator)\b/)
  })

  it('uses the reference floating publish tabbar shell with project theme color', () => {
    const file = fileURLToPath(new URL('../KeepTabBar.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain('keep-tabbar__shell')
    expect(source).toContain('keep-tabbar__notch')
    expect(source).toContain('keep-tabbar__item--publish')
    expect(source).toContain('background: $color-primary')
    expect(source).not.toContain('#ff2d3d')
    expect(source).toContain('font-size: 0')
  })

  it('keeps publish submit button themed in disabled state', () => {
    const file = fileURLToPath(new URL('../YpatPublishForm.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toMatch(/\.publish-submit__button\[disabled\]\s*\{[^}]*background-color:\s*\$color-primary/s)
    expect(source).not.toMatch(/\.publish-submit__button\[disabled\]\s*\{[^}]*opacity:/s)
  })

  it('does not reference browser-only DOM constructors in publish form events', () => {
    const file = fileURLToPath(new URL('../YpatPublishForm.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).not.toContain('HTMLInputElement')
  })

  it('limits publish description to 200 characters and shows a counter', () => {
    const file = fileURLToPath(new URL('../YpatPublishForm.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain('maxlength="200"')
    expect(source).toContain("{{ model.describ.length }}/200")
  })

  it('refreshes user info before publish profile readiness check', () => {
    const file = fileURLToPath(new URL('../YpatPublishForm.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toMatch(/const cachedUser = userStore\.userInfo[\s\S]*const latestUser = await userStore\.updateUserInfo\(\)[\s\S]*isPublishProfileReady\(mergeNonEmptyUser\(latestUser, cachedUser\)\)/)
  })

  it('has clear controls on edit profile fields', () => {
    const file = fileURLToPath(new URL('../../../pages-sub/user/edit-info.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain('class="field-clear"')
    expect(source).toContain('clearRegion')
  })
})
