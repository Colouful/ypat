import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const source = readFileSync(resolve(__dirname, '../complete-info.vue'), 'utf8')

describe('complete-info form option styles', () => {
  it('uses the same font size for gender and profession option prompts', () => {
    const compactOptionStyle = source.match(/\.option--compact\s*\{([\s\S]*?)\n\}/)?.[1]

    expect(compactOptionStyle).toBeDefined()
    expect(compactOptionStyle).not.toMatch(/font-size\s*:/)
  })
})
