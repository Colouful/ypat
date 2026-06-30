/**
 * 格式化工具测试
 */

import { describe, it, expect } from 'vitest'
import { formatDate, maskMobile, maskCertcode } from '@/utils/format'

describe('格式化工具', () => {
  describe('formatDate', () => {
    it('应正确格式化日期字符串', () => {
      const result = formatDate('2026-06-30T08:00:00Z', 'YYYY-MM-DD')
      expect(result).toMatch(/^\d{4}-\d{2}-\d{2}$/)
    })

    it('应正确格式化时间戳', () => {
      const timestamp = new Date('2026-01-15T10:30:00Z').getTime()
      const result = formatDate(timestamp, 'YYYY-MM-DD')
      expect(result).toMatch(/^\d{4}-\d{2}-\d{2}$/)
    })

    it('空值应返回空字符串', () => {
      expect(formatDate(null)).toBe('')
      expect(formatDate('')).toBe('')
    })

    it('无效日期应返回空字符串', () => {
      expect(formatDate('invalid-date')).toBe('')
    })
  })

  describe('maskMobile', () => {
    it('应正确脱敏手机号', () => {
      expect(maskMobile('13812345678')).toBe('138****5678')
    })

    it('短号码应返回 ***', () => {
      expect(maskMobile('12345')).toBe('***')
    })

    it('空值应返回 ***', () => {
      expect(maskMobile(null)).toBe('***')
      expect(maskMobile('')).toBe('***')
    })
  })

  describe('maskCertcode', () => {
    it('应正确脱敏证件号', () => {
      const result = maskCertcode('110101199001011234')
      expect(result).toContain('1101')
      expect(result).toContain('1234')
      expect(result).toContain('********')
    })

    it('短号码应返回 ***', () => {
      expect(maskCertcode('12345')).toBe('***')
    })

    it('空值应返回 ***', () => {
      expect(maskCertcode(null)).toBe('***')
    })
  })
})
