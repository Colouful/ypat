import { describe, expect, it } from 'vitest'
import { mapBackendResponse } from '../request'

describe('mapBackendResponse', () => {
  it('maps backend code/msg/res object', () => {
    expect(mapBackendResponse<{ id: number }>({ code: 200, msg: 'ok', res: { id: 1 } })).toEqual({
      success: true,
      data: { id: 1 },
      code: '200',
      message: 'ok',
    })
  })

  it('supports string success code', () => {
    const result = mapBackendResponse<number[]>({ code: '200', msg: 'ok', res: [1, 2] })
    expect(result.success).toBe(true)
    expect(result.data).toEqual([1, 2])
  })

  it.each([
    ['zero', 0],
    ['false', false],
    ['empty string', ''],
    ['null', null],
  ])('preserves %s response value', (_name, value) => {
    expect(mapBackendResponse({ code: 200, msg: 'ok', res: value }).data).toBe(value)
  })

  it('maps business error message', () => {
    const result = mapBackendResponse({ code: 1002, msg: '参数错误', res: null })
    expect(result.success).toBe(false)
    expect(result.code).toBe('1002')
    expect(result.message).toBe('参数错误')
  })

  it('falls back to error code message', () => {
    expect(mapBackendResponse({ code: 1009, res: null }).message).toBe('拍拍豆余额不足')
  })

  it('supports legacy message/result wrapper', () => {
    const result = mapBackendResponse({ code: 200, message: 'legacy', result: { ok: true } })
    expect(result.data).toEqual({ ok: true })
    expect(result.message).toBe('legacy')
  })

  it('prefers res over result when both exist', () => {
    const result = mapBackendResponse({ code: 200, res: 'new', result: 'old' })
    expect(result.data).toBe('new')
  })

  it('parses JSON string payload', () => {
    const result = mapBackendResponse<{ id: number }>('{"code":200,"msg":"ok","res":{"id":2}}')
    expect(result.data.id).toBe(2)
  })

  it('returns non JSON string as raw success payload', () => {
    const result = mapBackendResponse<string>('not-json')
    expect(result.success).toBe(true)
    expect(result.data).toBe('not-json')
  })

  it('returns null for empty response', () => {
    expect(mapBackendResponse<null>('').data).toBeNull()
  })

  it('returns naked data as successful payload', () => {
    const result = mapBackendResponse({ raw: true })
    expect(result.success).toBe(true)
    expect(result.data).toEqual({ raw: true })
  })
})
