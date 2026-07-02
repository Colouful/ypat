import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('work page mini-program category tabs', () => {
  it('uses horizontal scroll-view for category tabs', () => {
    const file = fileURLToPath(new URL('./index.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain('<scroll-view class="work-tab__categories" scroll-x')
    expect(source).toContain('<view class="work-tab__categories-row">')
  })
})
