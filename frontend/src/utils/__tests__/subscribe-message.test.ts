import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getTemplateIds } from '@/api/modules/content'
import {
  getSubscribeMessageToastTitle,
  pickTemplateIds,
} from '../subscribe-message'
import type { TemplateIdItem } from '@/api/types'
import type { SubscribeMessageResult } from '../subscribe-message'

vi.mock('@/api/modules/content', () => ({
  getTemplateIds: vi.fn(),
}))

const templates: TemplateIdItem[] = [
  { id: 0, name: '拍摄模板', value: 'tmpl-send' },
  { id: 1, name: '实名认证审核模板', value: 'tmpl-oauth' },
  { id: 2, name: '发布信息审核模板', value: 'tmpl-audit' },
  { id: 3, name: '新订单通知模板', value: 'tmpl-order' },
]

const messageTestTemplateId = '7nzQyG3qOBV5Vb6nYCkZHh7hhKJoHp_kHC4mX6vo6lg'

const baseResult: SubscribeMessageResult = {
  supported: true,
  requested: true,
  accepted: [],
  rejected: [],
}

beforeEach(() => {
  vi.mocked(getTemplateIds).mockReset()
  Reflect.deleteProperty(uni, 'requestSubscribeMessage')
})

describe('subscribe-message', () => {
  it('申请约拍场景应选择拍摄模板', () => {
    expect(pickTemplateIds('apply', templates)).toEqual(['tmpl-send'])
  })

  it('发布约拍场景应选择审核结果模板', () => {
    expect(pickTemplateIds('publish', templates)).toEqual(['tmpl-audit'])
  })

  it('消息页入口应使用微信后台已添加的测试模板', () => {
    expect(pickTemplateIds('message', templates)).toEqual([messageTestTemplateId])
  })

  it('消息页授权应只请求测试模板', async () => {
    vi.resetModules()
    const contentApi = await import('@/api/modules/content')
    const requestSubscribeMessage = vi.fn((options: {
      tmplIds: string[]
      success?: (res: Record<string, string>) => void
    }) => {
      options.success?.({
        errMsg: 'requestSubscribeMessage:ok',
        [messageTestTemplateId]: 'accept',
      })
    })
    Reflect.set(uni, 'requestSubscribeMessage', requestSubscribeMessage)

    const { requestMessageSubscribe } = await import('../subscribe-message')
    const result = await requestMessageSubscribe('message')

    expect(contentApi.getTemplateIds).not.toHaveBeenCalled()
    expect(requestSubscribeMessage).toHaveBeenCalledWith(expect.objectContaining({ tmplIds: [messageTestTemplateId] }))
    expect(result.accepted).toEqual([messageTestTemplateId])
  })

  it('预加载失败后点击授权应重新获取模板', async () => {
    vi.resetModules()
    const contentApi = await import('@/api/modules/content')
    vi.mocked(contentApi.getTemplateIds)
      .mockRejectedValueOnce(new Error('网络异常'))
      .mockResolvedValueOnce({ success: true, data: templates, code: '200', message: '' })

    const requestSubscribeMessage = vi.fn((options: {
      tmplIds: string[]
      success?: (res: Record<string, string>) => void
    }) => {
      options.success?.({
        errMsg: 'requestSubscribeMessage:ok',
        'tmpl-send': 'accept',
      })
    })
    Reflect.set(uni, 'requestSubscribeMessage', requestSubscribeMessage)

    const { preloadMessageSubscribeTemplates, requestMessageSubscribe } = await import('../subscribe-message')
    await preloadMessageSubscribeTemplates()
    const result = await requestMessageSubscribe('apply')

    expect(contentApi.getTemplateIds).toHaveBeenCalledTimes(2)
    expect(requestSubscribeMessage).toHaveBeenCalledWith(expect.objectContaining({ tmplIds: ['tmpl-send'] }))
    expect(result.accepted).toEqual(['tmpl-send'])
  })

  it('点击取模板失败后不应缓存空模板', async () => {
    vi.resetModules()
    const contentApi = await import('@/api/modules/content')
    vi.mocked(contentApi.getTemplateIds)
      .mockRejectedValueOnce(new Error('网络异常'))
      .mockResolvedValueOnce({ success: true, data: templates, code: '200', message: '' })

    const requestSubscribeMessage = vi.fn((options: {
      tmplIds: string[]
      success?: (res: Record<string, string>) => void
    }) => {
      options.success?.({
        errMsg: 'requestSubscribeMessage:ok',
        'tmpl-send': 'accept',
      })
    })
    Reflect.set(uni, 'requestSubscribeMessage', requestSubscribeMessage)

    const { requestMessageSubscribe } = await import('../subscribe-message')
    const failed = await requestMessageSubscribe('apply')
    const retried = await requestMessageSubscribe('apply')

    expect(contentApi.getTemplateIds).toHaveBeenCalledTimes(2)
    expect(failed).toEqual({
      supported: true,
      requested: false,
      accepted: [],
      rejected: [],
      error: '未配置订阅模板',
    })
    expect(retried.accepted).toEqual(['tmpl-send'])
  })

  it('订阅提示应展示具体失败原因', () => {
    expect(getSubscribeMessageToastTitle({ ...baseResult, accepted: ['tmpl-send'] })).toBe('已开启提醒')
    expect(getSubscribeMessageToastTitle({
      supported: false,
      requested: false,
      accepted: [],
      rejected: [],
      error: '当前环境不支持订阅消息',
    })).toBe('当前环境不支持订阅消息')
    expect(getSubscribeMessageToastTitle({
      ...baseResult,
      requested: false,
      error: '未配置订阅模板',
    })).toBe('未配置订阅模板')
    expect(getSubscribeMessageToastTitle({
      ...baseResult,
      rejected: ['tmpl-send'],
      error: 'requestSubscribeMessage:fail ban',
    })).toBe('请在微信设置中开启订阅消息')
    expect(getSubscribeMessageToastTitle({
      ...baseResult,
      rejected: ['tmpl-send'],
      error: 'requestSubscribeMessage:fail No template data return, verify the template id exist',
    })).toBe('订阅模板未在微信后台配置')
    expect(getSubscribeMessageToastTitle({
      ...baseResult,
      rejected: ['tmpl-send'],
    })).toBe('请在弹窗中勾选消息模板')
  })
})
