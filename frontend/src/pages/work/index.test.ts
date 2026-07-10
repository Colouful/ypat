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

  it('enables native pull-down refresh and reloads the list through the page handler', () => {
    const pagesFile = fileURLToPath(new URL('../../pages.json', import.meta.url))
    const workFile = fileURLToPath(new URL('./index.vue', import.meta.url))
    const pagesJson = JSON.parse(readFileSync(pagesFile, 'utf8'))
    const workPage = pagesJson.pages.find((page: { path: string }) => page.path === 'pages/work/index')
    const source = readFileSync(workFile, 'utf8')

    expect(workPage?.style?.enablePullDownRefresh).toBe(true)
    expect(source).toContain("import { onPullDownRefresh } from '@dcloudio/uni-app'")
    expect(source).toMatch(/onPullDownRefresh\(\(\) => \{\s*load\(true\)\.finally\(\(\) => \{\s*uni\.stopPullDownRefresh\(\)\s*\}\)\s*\}\)/)
  })
})
