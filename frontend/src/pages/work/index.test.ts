import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('work page mini-program category tabs', () => {
  it('uses horizontal scroll-view for category tabs', () => {
    const file = fileURLToPath(new URL('./index.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain('class="work-tab__filter-bar"')
    expect(source).toContain('<scroll-view class="work-tab__categories" scroll-x')
    expect(source).toContain('<view class="work-tab__categories-row">')
    expect(source).toContain('class="work-tab__filter-btn"')
    expect(source.indexOf('class="work-tab__filter-btn"')).toBeGreaterThan(source.indexOf('</scroll-view>'))
    expect(source).not.toContain('work-tab__cat--filter')
  })

  it('uses native region picker in the work filter panel', () => {
    const file = fileURLToPath(new URL('../../components/business/WorkFilterPanel.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain('<picker mode="region"')
    expect(source).toContain('@change="changeRegion"')
    expect(source).toContain('local.region = city')
  })

  it('does not enable native pull-down refresh without a refresh handler', () => {
    const file = fileURLToPath(new URL('../../pages.json', import.meta.url))
    const pagesJson = JSON.parse(readFileSync(file, 'utf8'))
    const workPage = pagesJson.pages.find((page: { path: string }) => page.path === 'pages/work/index')

    expect(workPage?.style?.enablePullDownRefresh).toBe(false)
  })
})
