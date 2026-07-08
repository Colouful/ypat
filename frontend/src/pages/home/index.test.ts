import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('home default filters', () => {
  it('matches the visible 推荐 + 全部 state without hidden chargeway or style filters', () => {
    const file = fileURLToPath(new URL('./index.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain("const activeTab = ref<TabKey>('recommend')")
    expect(source).toContain("const activeChip = ref('all')")
    expect(source).toContain("filterValue = ref<FilterValue>({\n  target: ['all'],\n  chargeway: [],\n  style: [],\n})")
  })
})
