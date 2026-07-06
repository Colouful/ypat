import { describe, expect, it } from 'vitest'
import { normalizeUploadProgress } from '../media-uploader'

describe('normalizeUploadProgress', () => {
  it('keeps mini-program upload progress in the 0-100 range', () => {
    expect(normalizeUploadProgress(50)).toBe(50)
    expect(normalizeUploadProgress(100)).toBe(100)
  })

  it('also accepts ratio progress values', () => {
    expect(normalizeUploadProgress(0.5)).toBe(50)
    expect(normalizeUploadProgress(1)).toBe(1)
  })

  it('clamps invalid progress values', () => {
    expect(normalizeUploadProgress(-10)).toBe(0)
    expect(normalizeUploadProgress(120)).toBe(100)
    expect(normalizeUploadProgress(Number.NaN)).toBe(0)
  })
})
