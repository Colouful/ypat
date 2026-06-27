import { describe, expect, it } from 'vitest'
import { ensureImageDataUrl } from '../file-base64'

// GAP-IMG-01: 后端以 data:...;base64,xxx 形式接收图片(见 backend MockTest)。
describe('ensureImageDataUrl', () => {
  it('keeps an existing data URL unchanged', async () => {
    const value = 'data:image/png;base64,AAAA'
    expect(await ensureImageDataUrl(value)).toBe(value)
  })

  it('prefixes a bare base64 string', async () => {
    const raw = 'A'.repeat(300)
    const result = await ensureImageDataUrl(raw)
    expect(result.startsWith('data:image/jpeg;base64,')).toBe(true)
    expect(result.endsWith(raw)).toBe(true)
  })

  it('returns empty string untouched', async () => {
    expect(await ensureImageDataUrl('')).toBe('')
  })
})
