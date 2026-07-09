import { describe, expect, it } from 'vitest'
import {
  decodeWebViewUrl,
  isAllowedWebViewUrl,
  resolveWebViewUrl,
} from '../banner-web-view'

describe('banner web-view helpers', () => {
  it('decodes encoded http urls for web-view', () => {
    const url = 'HTTPS://example.com/a?x=1&y=二'

    expect(resolveWebViewUrl(encodeURIComponent(url))).toEqual({
      targetUrl: url,
      fallbackUrl: url,
      errorMessage: '',
    })
  })

  it('keeps malformed encoded values as fallback text', () => {
    expect(decodeWebViewUrl('%E0%A4%A')).toBe('%E0%A4%A')
  })

  it('rejects missing urls without fallback copy text', () => {
    expect(resolveWebViewUrl(undefined)).toEqual({
      targetUrl: '',
      fallbackUrl: '',
      errorMessage: '未提供可打开的链接',
    })
  })

  it('rejects unsupported protocols but keeps url copy fallback', () => {
    expect(resolveWebViewUrl(encodeURIComponent('ftp://example.com/a'))).toEqual({
      targetUrl: '',
      fallbackUrl: 'ftp://example.com/a',
      errorMessage: '仅支持 http:// 或 https:// 开头的链接',
    })
  })

  it('allows http and https protocols only', () => {
    expect(isAllowedWebViewUrl('http://example.com')).toBe(true)
    expect(isAllowedWebViewUrl('HTTPS://example.com')).toBe(true)
    expect(isAllowedWebViewUrl('javascript:alert(1)')).toBe(false)
  })
})
