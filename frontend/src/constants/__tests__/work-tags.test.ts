import { describe, expect, it } from 'vitest'
import { resolveWorkTagOptions } from '../work-tags'

describe('resolveWorkTagOptions', () => {
  it('does not create fake tag ids when backend returns an empty list', () => {
    const tags = resolveWorkTagOptions([])

    expect(tags).toEqual([])
  })

  it('keeps non-empty backend tags', () => {
    const backendTags = [{ id: 9, code: 'x', name: '胶片' }]

    expect(resolveWorkTagOptions(backendTags)).toBe(backendTags)
  })
})
