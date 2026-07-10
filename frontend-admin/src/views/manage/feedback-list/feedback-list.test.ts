import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('feedback admin page source', () => {
  it('registers filters, detail preview and handle actions', () => {
    const file = fileURLToPath(new URL('./index.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain('getFeedbackList')
    expect(source).toContain('typeOptions')
    expect(source).toContain('意见反馈')
    expect(source).toContain('反馈图片')
    expect(source).toContain('handleFeedback')
    expect(source).toContain('el-image')
  })
})
