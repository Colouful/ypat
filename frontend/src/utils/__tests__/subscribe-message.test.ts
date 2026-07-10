import { describe, expect, it } from 'vitest'
import { pickTemplateIds } from '../subscribe-message'
import type { TemplateIdItem } from '@/api/types'

const templates: TemplateIdItem[] = [
  { id: 0, name: '拍摄模板', value: 'tmpl-send' },
  { id: 1, name: '实名认证审核模板', value: 'tmpl-oauth' },
  { id: 2, name: '发布信息审核模板', value: 'tmpl-audit' },
  { id: 3, name: '新订单通知模板', value: 'tmpl-order' },
]

describe('subscribe-message', () => {
  it('申请约拍场景应选择拍摄模板', () => {
    expect(pickTemplateIds('apply', templates)).toEqual(['tmpl-send'])
  })

  it('发布约拍场景应选择审核结果模板', () => {
    expect(pickTemplateIds('publish', templates)).toEqual(['tmpl-audit'])
  })

  it('消息页入口应一次最多请求三个模板', () => {
    expect(pickTemplateIds('message', templates)).toEqual(['tmpl-send', 'tmpl-oauth', 'tmpl-audit'])
  })
})
