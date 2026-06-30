/**
 * 文件下载工具
 */

/**
 * 从 Content-Disposition 头解析文件名
 */
export function parseFileName(contentDisposition: string | undefined): string {
  if (!contentDisposition) return 'download'

  // 优先匹配 filename*=UTF-8''xxx
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''(.+)/i)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }

  // 其次匹配 filename="xxx"
  const match = contentDisposition.match(/filename="?([^";]+)"?/i)
  if (match?.[1]) {
    return match[1]
  }

  return 'download'
}

/**
 * 下载 Blob
 */
export function downloadBlob(blob: Blob, filename: string): void {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

/**
 * 从响应中下载文件
 */
export function downloadFromResponse(
  data: Blob,
  contentDisposition?: string,
): void {
  const filename = parseFileName(contentDisposition)
  downloadBlob(data, filename)
}
