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

  it('详情视图展示主题标签区域', () => {
    const detailViewFile = fileURLToPath(new URL('../../components/business/YpatDetailView.vue', import.meta.url))
    const detailViewSource = readFileSync(detailViewFile, 'utf8')

    expect(detailViewSource).toContain('v-if="topicTags.length"')
    expect(detailViewSource).toContain('class="topic-tags__title"')
    expect(detailViewSource).toContain('主题标签')
    expect(detailViewSource).toContain('v-for="tag in topicTags"')
  })
})
