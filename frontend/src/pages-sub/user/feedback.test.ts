import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('feedback page', () => {
  it('keeps contact input height stable in mini-program', () => {
    const file = fileURLToPath(new URL('./feedback.vue', import.meta.url))
    const source = readFileSync(file, 'utf8')

    expect(source).toContain('class="contact-input"')
    expect(source).toMatch(/\.contact-input\s*\{[\s\S]*height:\s*88rpx;/)
    expect(source).toMatch(/\.contact-input\s*\{[\s\S]*box-sizing:\s*border-box;/)
    expect(source).toMatch(/\.contact-input\s*\{[\s\S]*line-height:\s*88rpx;/)
  })
})
