import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('mp-weixin component styles', () => {
  it('does not use tag selectors in KeepTabBar component wxss', () => {
    const file = fileURLToPath(new URL('../KeepTabBar.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).not.toMatch(/\.keep-tabbar__[^{]+\s+(text|view|image|button|navigator)\b/)
  })

  it('uses the Keep-style translucent tabbar with active color changes', () => {
    const file = fileURLToPath(new URL('../KeepTabBar.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).not.toContain('keep-tabbar__bg')
    expect(source).not.toContain('tabbarBgSvg')
    expect(source).not.toContain('data:image/svg+xml')
    expect(source).toContain("label: '首页'")
    expect(source).toContain("label: '发布'")
    expect(source).toContain("label: '我的'")
    expect(source).not.toContain("label: '发现'")
    expect(source).not.toContain("label: '消息'")
    expect(source).toContain('activeColor')
    expect(source).toContain('inactiveColor')
    expect(source).toContain(':color="active === item.key ? activeColor : inactiveColor"')
    expect(source).toContain('keep-tabbar__item--publish')
    expect(source).toContain('bottom: 0')
    expect(source).toContain('height: calc(148rpx + env(safe-area-inset-bottom))')
    expect(source).toContain('background: rgba(255, 255, 255, 0.98)')
    expect(source).toContain('backdrop-filter: blur(24rpx)')
    expect(source).toContain('border-top: 1rpx solid $color-border')
    expect(source).toContain('color: $color-text-helper')
    expect(source).toContain('color: $color-text-primary')
    expect(source).toContain('font-size: 22rpx')
  })

  it('maps mini-program image icons to existing asset names and color tokens', () => {
    const file = fileURLToPath(new URL('../KeepIcon.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain('class="keep-icon__svg"')
    expect(source).toContain('normalizeHexColor')
    expect(source).toContain('svgDataUrl')
  })

  it('keeps publish submit button themed in disabled state', () => {
    const file = fileURLToPath(new URL('../YpatPublishForm.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toMatch(/\.publish-submit__button--disabled\s*\{[^}]*background-color:\s*\$color-primary/s)
    expect(source).not.toMatch(/\.publish-submit__button--disabled\s*\{[^}]*opacity:/s)
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
