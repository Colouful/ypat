import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('my ypat application page contract', () => {
  const file = fileURLToPath(new URL('./my-apply.vue', import.meta.url))
  const source = readFileSync(file, 'utf8')

  it('loads submitted applications instead of viewed feedback messages', () => {
    expect(source).toContain('ypatApi.getMyApplicationList')
    expect(source).not.toContain('ypatApi.getMySentList')
    expect(source).toContain('const items = ref<YpatInfo[]>([])')
  })

  it('renders ypat details and navigates with the ypat id', () => {
    expect(source).toContain("item.pics?.[0] || '/static/default-cover.png'")
    expect(source).toContain("item.userQo?.nickname || '匿名用户'")
    expect(source).toContain('item.describ')
    expect(source).toContain('`/pages-sub/ypat/detail?id=${item.id}`')
  })
})
