export interface WebViewUrlResult {
  targetUrl: string
  fallbackUrl: string
  errorMessage: string
}

export function isAllowedWebViewUrl(url: string): boolean {
  return /^https?:\/\//i.test(url)
}

export function decodeWebViewUrl(value: string): string {
  try {
    return decodeURIComponent(value)
  } catch {
    return value
  }
}

export function resolveWebViewUrl(value: unknown): WebViewUrlResult {
  const rawUrl = typeof value === 'string' ? value : ''
  const fallbackUrl = rawUrl ? decodeWebViewUrl(rawUrl).trim() : ''

  if (!fallbackUrl) {
    return {
      targetUrl: '',
      fallbackUrl: '',
      errorMessage: '未提供可打开的链接',
    }
  }

  if (!isAllowedWebViewUrl(fallbackUrl)) {
    return {
      targetUrl: '',
      fallbackUrl,
      errorMessage: '仅支持 http:// 或 https:// 开头的链接',
    }
  }

  return {
    targetUrl: fallbackUrl,
    fallbackUrl,
    errorMessage: '',
  }
}
