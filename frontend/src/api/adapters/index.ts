import { envConfig } from '@/config/env'
import type { PageResult } from '../types'

/**
 * 标准化图片 URL
 * 如果是相对路径则拼接基础地址，绝对路径直接返回
 */
export function normalizeImageUrl(path: string | null | undefined): string {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  // 去除开头多余的斜杠
  const cleanPath = path.replace(/^\/+/, '')
  return `${envConfig.imageBaseUrl}/${cleanPath}`
}

/**
 * 标准化 Spring Data 分页结果为统一格式
 * 兼容后端返回的各种分页格式
 */
export function normalizePageResult<T>(data: any): PageResult<T> {
  if (!data) {
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: 0,
      size: 10,
    }
  }

  // 标准 Spring Page 格式
  if (data.content !== undefined) {
    return {
      content: data.content || [],
      totalElements: data.totalElements || 0,
      totalPages: data.totalPages || 0,
      number: data.number || 0,
      size: data.size || 10,
    }
  }

  // 兼容 records/list + total 格式
  if (data.records !== undefined || data.list !== undefined) {
    const list = data.records || data.list || []
    const total = data.total || data.totalElements || 0
    const size = data.size || data.pageSize || 10
    const current = data.current || data.pageNum || data.number || 0
    const pages = data.pages || data.totalPages || Math.ceil(total / size)

    return {
      content: list,
      totalElements: total,
      totalPages: pages,
      number: current,
      size,
    }
  }

  // 如果直接是数组
  if (Array.isArray(data)) {
    return {
      content: data,
      totalElements: data.length,
      totalPages: 1,
      number: 0,
      size: data.length,
    }
  }

  return {
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
  }
}

/**
 * 格式化相对时间
 * 将日期字符串转换为相对时间显示（如：刚刚、5分钟前、3小时前、昨天、3天前）
 */
export function formatRelativeTime(dateStr: string | null | undefined): string {
  if (!dateStr) return ''

  const date = new Date(dateStr.replace(/-/g, '/'))
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 0) return '刚刚'

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(days / 365)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  if (months < 12) return `${months}个月前`
  if (years >= 1) return `${years}年前`

  // 超过一年显示具体日期
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 手机号脱敏
 * 隐藏中间4位：138****8888
 */
export function maskPhone(phone: string | null | undefined): string {
  if (!phone) return ''
  const str = String(phone).trim()
  if (str.length < 7) return str
  return str.replace(/^(\d{3})\d{4}(\d+)$/, '$1****$2')
}

/**
 * 身份证号脱敏
 * 隐藏中间位数：110***********1234
 */
export function maskIdCard(idcard: string | null | undefined): string {
  if (!idcard) return ''
  const str = String(idcard).trim()
  if (str.length <= 6) return str
  if (str.length <= 15) {
    // 15位身份证：显示前3后3
    return str.replace(/^(.{3}).*(.{3})$/, '$1*********$2')
  }
  // 18位身份证：显示前3后4
  return str.replace(/^(.{3}).*(.{4})$/, '$1***********$2')
}
