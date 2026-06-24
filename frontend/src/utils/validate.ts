// ============================================================
// YPAT Validation Utilities
// ============================================================

/**
 * 验证中国大陆手机号
 * 支持 13x/14x/15x/16x/17x/18x/19x 号段
 * @param value 手机号字符串
 */
export function isPhone(value: string): boolean {
  if (!value) return false;
  return /^1[3-9]\d{9}$/.test(value.trim());
}

/**
 * 验证中国大陆身份证号（18位）
 * 校验规则：前17位为数字，最后一位为数字或X
 * @param value 身份证号字符串
 */
export function isIdCard(value: string): boolean {
  if (!value) return false;
  const trimmed = value.trim();

  // 基础格式验证（18位）
  if (!/^\d{17}[\dXx]$/.test(trimmed)) return false;

  // 校验码验证
  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
  const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];

  let sum = 0;
  for (let i = 0; i < 17; i++) {
    sum += parseInt(trimmed[i], 10) * weights[i];
  }

  const checkCode = checkCodes[sum % 11];
  return trimmed[17].toUpperCase() === checkCode;
}

/**
 * 判断值是否为空
 * 空值包括：null、undefined、空字符串、空数组、空对象
 * @param value 待检查的值
 */
export function isEmpty(value: unknown): boolean {
  if (value === null || value === undefined) return true;
  if (typeof value === 'string') return value.trim().length === 0;
  if (Array.isArray(value)) return value.length === 0;
  if (typeof value === 'object') return Object.keys(value as object).length === 0;
  return false;
}

/**
 * 判断文件名是否为图片文件
 * 支持格式：jpg, jpeg, png, gif, bmp, webp, svg
 * @param filename 文件名
 */
export function isImageFile(filename: string): boolean {
  if (!filename) return false;
  const ext = filename.split('.').pop()?.toLowerCase() || '';
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext);
}
