/**
 * 下载文件名解析测试
 */

import { describe, it, expect } from 'vitest'
import { parseFileName } from '@/utils/download'

describe('下载文件名解析', () => {
  it('应解析 filename*=UTF-8 格式', () => {
    const header = "attachment; filename*=UTF-8''%E7%94%A8%E6%88%B7%E5%88%97%E8%A1%A8.xlsx"
    expect(parseFileName(header)).toBe('用户列表.xlsx')
  })

  it('应解析 filename="xxx" 格式', () => {
    const header = 'attachment; filename="export.xlsx"'
    expect(parseFileName(header)).toBe('export.xlsx')
  })

  it('应解析 filename=xxx 格式（无引号）', () => {
    const header = 'attachment; filename=export.xlsx'
    expect(parseFileName(header)).toBe('export.xlsx')
  })

  it('无 header 时应返回默认文件名', () => {
    expect(parseFileName(undefined)).toBe('download')
  })

  it('空 header 时应返回默认文件名', () => {
    expect(parseFileName('')).toBe('download')
  })

  it('无 filename 字段时应返回默认文件名', () => {
    expect(parseFileName('attachment')).toBe('download')
  })
})
