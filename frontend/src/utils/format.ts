// ============================================================
// YPAT Formatting Utilities
// ============================================================

/**
 * 将分转换为元并格式化显示
 * @param fen 金额（单位：分）
 * @returns 格式化后的金额字符串（如 "12.50"）
 */
export function formatPrice(fen: number): string {
  if (!fen && fen !== 0) return '0.00';
  return (fen / 100).toFixed(2);
}

/**
 * 格式化PPD积分，带正负号
 * @param ppd PPD数量
 * @returns 带符号的PPD字符串（如 "+10" 或 "-5"）
 */
export function formatPPD(ppd: number): string {
  if (!ppd && ppd !== 0) return '0';
  if (ppd > 0) return `+${ppd}`;
  return String(ppd);
}

/**
 * 手机号脱敏
 * @param phone 手机号
 * @returns 脱敏后的手机号（如 "137****1234"）
 */
export function formatPhone(phone: string): string {
  if (!phone || phone.length < 11) return phone || '';
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`;
}

/**
 * 身份证号脱敏
 * @param card 身份证号
 * @returns 脱敏后的身份证号（如 "110***********1234"）
 */
export function formatIdCard(card: string): string {
  if (!card || card.length < 15) return card || '';
  return `${card.slice(0, 3)}${'*'.repeat(card.length - 7)}${card.slice(-4)}`;
}

/**
 * 格式化为相对时间
 * @param dateStr 日期字符串或时间戳
 * @returns 相对时间描述（如 "3分钟前"、"2小时前"、"昨天"）
 */
export function formatRelativeTime(dateStr: string | number): string {
  if (!dateStr) return '';

  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  if (diff < 0) return '刚刚';

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  const months = Math.floor(days / 30);
  const years = Math.floor(days / 365);

  if (seconds < 60) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days === 1) return '昨天';
  if (days < 7) return `${days}天前`;
  if (days < 30) return `${Math.floor(days / 7)}周前`;
  if (months < 12) return `${months}个月前`;
  return `${years}年前`;
}

/**
 * 日期格式化
 * @param date 日期对象、字符串或时间戳
 * @param format 格式模板（默认 "YYYY-MM-DD HH:mm:ss"）
 * @returns 格式化后的日期字符串
 */
export function formatDate(
  date: Date | string | number,
  format: string = 'YYYY-MM-DD HH:mm:ss'
): string {
  if (!date) return '';

  const d = date instanceof Date ? date : new Date(date);

  if (isNaN(d.getTime())) return '';

  const year = d.getFullYear();
  const month = d.getMonth() + 1;
  const day = d.getDate();
  const hours = d.getHours();
  const minutes = d.getMinutes();
  const seconds = d.getSeconds();

  const pad = (n: number): string => (n < 10 ? `0${n}` : String(n));

  return format
    .replace('YYYY', String(year))
    .replace('MM', pad(month))
    .replace('DD', pad(day))
    .replace('HH', pad(hours))
    .replace('mm', pad(minutes))
    .replace('ss', pad(seconds));
}
