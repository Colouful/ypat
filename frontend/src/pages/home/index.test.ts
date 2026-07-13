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

  it('cardItems 使用共享 trust helper 解析认证与担保金状态，而不是直接用 OR 逻辑', () => {
    const file = fileURLToPath(new URL('./index.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain("import { resolveYpatCreditFlag, resolveYpatRealnameFlag } from '@/utils/ypat-trust'")
    expect(source).toContain('realname: resolveYpatRealnameFlag(item.userQo?.realnameflag, item.realnameflag)')
    expect(source).toContain('credit: resolveYpatCreditFlag(item.creditflag, item.userQo?.creditflag)')
    expect(source).not.toContain("item.realnameflag === '1' || item.userQo?.realnameflag === '1'")
    expect(source).not.toContain("item.creditflag === '1' || item.userQo?.creditflag === '1'")
  })

  it('把约拍主题标签解析后传给首页卡片', () => {
    const file = fileURLToPath(new URL('./index.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain('resolveYpatTopicTags(item.patstyle, item.patstyleTxt, topicTagOptions.value)')
    expect(source).toContain('tags:')
  })
})
