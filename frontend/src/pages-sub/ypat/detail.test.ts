import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('ypat detail refresh contract', () => {
  const file = fileURLToPath(new URL('./detail.vue', import.meta.url))
  const source = readFileSync(file, 'utf8')

  it('refreshes the detail component when the page becomes visible again', () => {
    expect(source).toContain('<YpatDetailView ref="detailView"')
    expect(source).toContain('onShow(() => {')
    expect(source).toContain('detailView.value?.load()')
  })
})
