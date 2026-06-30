/**
 * 格式化工具函数
 */

/**
 * 格式化日期
 */
export function formatDate(
  date: Date | string | number | null,
  pattern = 'YYYY-MM-DD HH:mm:ss',
): string {
  if (!date) return ''

  const d = new Date(date)
  if (Number.isNaN(d.getTime())) return ''

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return pattern
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 手机号脱敏
 */
export function maskMobile(mobile: string | null | undefined): string {
  if (!mobile || mobile.length < 7) return '***'
  return mobile.substring(0, 3) + '****' + mobile.substring(mobile.length - 4)
}

/**
 * 证件号脱敏
 */
export function maskCertcode(code: string | null | undefined): string {
  if (!code || code.length < 6) return '***'
  return code.substring(0, 4) + '********' + code.substring(code.length - 4)
}
