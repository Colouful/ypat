import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('会员权益配置页面', () => {
  it('按场景展示中文配置且不直接输出内部编码', () => {
    const source = readFileSync(resolve(__dirname, '../index.vue'), 'utf8')

    for (const text of [
      '发布约拍',
      '发起约拍申请',
      '查看联系方式',
      '基础消耗拍豆',
      '基础会员',
      '高级会员',
      '专业会员',
      '拍豆减免',
    ]) {
      expect(source).toContain(text)
    }
    expect(source).not.toContain('{{ row.levelCode }}')
    expect(source).not.toContain('{{ row.scene }}')
    expect(source).not.toContain('{{ row.benefitType }}')
    expect(source).not.toContain('<el-pagination')
  })
})
