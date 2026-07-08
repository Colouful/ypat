import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const source = readFileSync(
  fileURLToPath(new URL('../AppointmentPublishForm.vue', import.meta.url)),
  'utf8',
)

describe('AppointmentPublishForm mini-program controls', () => {
  it('uses supported date and region pickers for appointment time and target area', () => {
    expect(source).toContain('picker mode="date"')
    expect(source).toContain('mode="region"')
    expect(source).not.toContain('mode="datetime"')
    expect(source).not.toContain('onPickRegion')
    expect(source).not.toContain('北京市 / 北京市 / 东城区')
  })

  it('uses a static mini-program-safe image converter import before publishing', () => {
    expect(source).toContain("import { filePathToDataUrl } from '@/utils/file-base64'")
    expect(source).not.toContain("await import('@/utils/file-base64')")
    expect(source).not.toContain('const { filePathToDataUrl }')
  })
})
