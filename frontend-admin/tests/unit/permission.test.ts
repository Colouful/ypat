/**
 * 权限判断测试
 *
 * v-permission 指令逻辑测试
 * 旧后台无细粒度权限，当前所有登录用户均有权限
 */

import { describe, it, expect } from 'vitest'

/**
 * 模拟权限判断逻辑
 */
function checkPermission(
  required: string | string[] | undefined,
  userPermissions: string[],
): boolean {
  if (!required) return true

  if (Array.isArray(required)) {
    if (required.length === 0) return true
    return required.some((p) => userPermissions.includes(p))
  }

  return userPermissions.includes(required)
}

describe('权限判断', () => {
  it('无权限要求时应放行', () => {
    expect(checkPermission(undefined, [])).toBe(true)
  })

  it('单个权限且用户拥有时应放行', () => {
    expect(checkPermission('user:add', ['user:add', 'user:edit'])).toBe(true)
  })

  it('单个权限但用户没有时应拒绝', () => {
    expect(checkPermission('user:delete', ['user:add', 'user:edit'])).toBe(false)
  })

  it('权限数组，用户拥有其中之一时应放行', () => {
    expect(
      checkPermission(['user:add', 'user:edit'], ['user:edit', 'user:audit']),
    ).toBe(true)
  })

  it('权限数组，用户全都没有时应拒绝', () => {
    expect(
      checkPermission(['user:add', 'user:delete'], ['user:edit', 'user:audit']),
    ).toBe(false)
  })

  it('空权限数组时应放行', () => {
    expect(checkPermission([], [])).toBe(true)
  })
})
