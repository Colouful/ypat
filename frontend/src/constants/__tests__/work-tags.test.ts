import { describe, expect, it } from 'vitest'
import { resolveWorkTagOptions, WORK_TAGS_FALLBACK } from '../work-tags'

describe('resolveWorkTagOptions', () => {
  it('uses screenshot fallback labels when backend returns an empty list', () => {
    const tags = resolveWorkTagOptions([])

    expect(tags.map((tag) => tag.name)).toEqual(WORK_TAGS_FALLBACK)
    expect(tags[0]).toEqual({ id: 1, code: 'fallback_1', name: '情侣' })
  })

  it('keeps non-empty backend tags', () => {
    const backendTags = [{ id: 9, code: 'x', name: '胶片' }]

    expect(resolveWorkTagOptions(backendTags)).toBe(backendTags)
  })
})
