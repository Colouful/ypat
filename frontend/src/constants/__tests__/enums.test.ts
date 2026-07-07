import { describe, expect, it } from 'vitest'
import {
  PHOTO_STYLES,
  PROFESS_LABELS,
  PUBLIC_PROFESS_OPTIONS,
  UserProfess,
  getProfessLabel,
} from '../enums'

describe('miniapp enum consistency', () => {
  it('exposes six public professions including videographer', () => {
    expect(UserProfess.VIDEOGRAPHER).toBe('9')
    expect(PUBLIC_PROFESS_OPTIONS).toEqual([
      { label: '商家', value: '6' },
      { label: '摄影师', value: '0' },
      { label: '化妆师', value: '2' },
      { label: '摄像师', value: '9' },
      { label: '修图师', value: '3' },
      { label: '模特', value: '1' },
    ])
    expect(PROFESS_LABELS['8']).toBe('素人模特')
    expect(getProfessLabel('9')).toBe('摄像师')
  })

  it('uses 29 work tag names for photo style keywords', () => {
    expect(PHOTO_STYLES).toHaveLength(29)
    expect(PHOTO_STYLES).toContain('cosplay')
    expect(PHOTO_STYLES).toContain('黑白')
    expect(PHOTO_STYLES).toContain('纪实')
    expect(PHOTO_STYLES).toContain('日系')
    expect(PHOTO_STYLES).toContain('复古')
  })
})
