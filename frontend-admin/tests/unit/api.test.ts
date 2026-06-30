/**
 * ResponseApiBody 适配器测试
 *
 * 测试后端 { code, msg, res } 格式到前端 ApiResult { code, msg, data, success } 的适配逻辑
 */

import { describe, it, expect } from 'vitest'
import type { ResponseApiBody, ApiResult } from '@/api/types'

/**
 * 模拟响应拦截器中的适配逻辑
 */
function adaptResponse<T>(res: ResponseApiBody<T>): ApiResult<T> {
  return {
    code: res.code,
    msg: res.msg,
    data: res.res,
    success: res.code === 200,
  }
}

describe('ResponseApiBody 适配器', () => {
  it('成功响应应正确适配', () => {
    const backend: ResponseApiBody<{ name: string }> = {
      code: 200,
      msg: '成功',
      res: { name: '张三' },
    }

    const result = adaptResponse(backend)

    expect(result.code).toBe(200)
    expect(result.msg).toBe('成功')
    expect(result.data).toEqual({ name: '张三' })
    expect(result.success).toBe(true)
  })

  it('业务错误响应应标记 success=false', () => {
    const backend: ResponseApiBody = {
      code: 1012,
      msg: '密码错误',
      res: null,
    }

    const result = adaptResponse(backend)

    expect(result.code).toBe(1012)
    expect(result.msg).toBe('密码错误')
    expect(result.data).toBeNull()
    expect(result.success).toBe(false)
  })

  it('Token 无效（401）应标记 success=false', () => {
    const backend: ResponseApiBody = {
      code: 401,
      msg: 'token无效',
      res: null,
    }

    const result = adaptResponse(backend)

    expect(result.success).toBe(false)
    expect(result.code).toBe(401)
  })

  it('无权限（403）应标记 success=false', () => {
    const backend: ResponseApiBody = {
      code: 403,
      msg: '无权限',
      res: null,
    }

    const result = adaptResponse(backend)

    expect(result.success).toBe(false)
    expect(result.code).toBe(403)
  })

  it('分页数据应正确保留在 data 字段', () => {
    const backend: ResponseApiBody<{ content: unknown[]; totalElements: number }> = {
      code: 200,
      msg: '成功',
      res: {
        content: [{ id: 1 }, { id: 2 }],
        totalElements: 2,
      },
    }

    const result = adaptResponse(backend)

    expect(result.data.content).toHaveLength(2)
    expect(result.data.totalElements).toBe(2)
  })
})
