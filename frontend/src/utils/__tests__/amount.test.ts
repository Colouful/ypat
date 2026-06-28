import { describe, expect, it } from 'vitest'
import { normalizePositiveIntegerInput, toPositiveIntegerAmount } from '../amount'

describe('amount input helpers', () => {
  it('keeps the amount empty by default and rejects zero', () => {
    expect(normalizePositiveIntegerInput('')).toBe('')
    expect(normalizePositiveIntegerInput('0')).toBe('')
    expect(toPositiveIntegerAmount('')).toBeUndefined()
    expect(toPositiveIntegerAmount('0')).toBeUndefined()
  })

  it('allows positive integer amounts only', () => {
    expect(normalizePositiveIntegerInput('0012')).toBe('12')
    expect(normalizePositiveIntegerInput('12.34')).toBe('12')
    expect(normalizePositiveIntegerInput('12。34')).toBe('12')
    expect(normalizePositiveIntegerInput('-12')).toBe('12')
    expect(normalizePositiveIntegerInput('¥1a2')).toBe('12')
    expect(toPositiveIntegerAmount('12')).toBe(12)
  })
})
