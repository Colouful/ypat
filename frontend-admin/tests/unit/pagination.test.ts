/**
 * 分页转换测试
 *
 * Element Plus pagination 使用 1-based currentPage
 * 后端使用 0-based page
 */

import { describe, it, expect } from 'vitest'

/**
 * 前端页码转后端页码（1-based → 0-based）
 */
function toBackendPage(frontendPage: number): number {
  return Math.max(0, frontendPage - 1)
}

/**
 * 后端页码转前端页码（0-based → 1-based）
 */
function toFrontendPage(backendPage: number): number {
  return backendPage + 1
}

describe('分页转换', () => {
  it('前端第1页应转换为后端 page=0', () => {
    expect(toBackendPage(1)).toBe(0)
  })

  it('前端第2页应转换为后端 page=1', () => {
    expect(toBackendPage(2)).toBe(1)
  })

  it('前端第5页应转换为后端 page=4', () => {
    expect(toBackendPage(5)).toBe(4)
  })

  it('后端 page=0 应转换为前端第1页', () => {
    expect(toFrontendPage(0)).toBe(1)
  })

  it('后端 page=3 应转换为前端第4页', () => {
    expect(toFrontendPage(3)).toBe(4)
  })

  it('前端页码不应为负数', () => {
    expect(toBackendPage(0)).toBe(0)
    expect(toBackendPage(-1)).toBe(0)
  })
})
