import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('admin realname user list source', () => {
  const source = readFileSync(resolve(__dirname, '../index.vue'), 'utf8')

  it('defaults to today and exposes the realname submission date filter', () => {
    expect(source).toContain('function getTodayText()')
    expect(source).toContain('realnameSubmitDate: getTodayText()')
    expect(source).toContain('label="提交日期"')
    expect(source).toContain('value-format="YYYY-MM-DD"')
    expect(source).toContain('prop="realnameSubmitAt"')
  })

  it('always exposes details and only exposes audit for pending users', () => {
    expect(source).toContain('详情')
    expect(source).toContain('v-if="row.status === \'1\'"')
    expect(source).toContain('openUserDialog')
  })
})
