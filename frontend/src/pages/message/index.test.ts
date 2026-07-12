import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('message ypat list contract', () => {
  const file = fileURLToPath(new URL('./index.vue', import.meta.url))
  const source = readFileSync(file, 'utf8')

  it('keeps received messages and loads submitted applications for the sent tab', () => {
    expect(source).toContain('ypatApi.getMyReceivedList')
    expect(source).toContain('ypatApi.getMyApplicationList')
    expect(source).not.toContain('ypatApi.getMySentList')
    expect(source).toContain('const receivedItems = ref<MessInfo[]>([])')
    expect(source).toContain('const applicationItems = ref<YpatInfo[]>([])')
  })

  it('shows application totals instead of viewed-feedback unread counts', () => {
    expect(source).toContain('申请总数')
    expect(source).toContain('applicationCount')
    expect(source).not.toContain('getSendUnreadCount')
  })

  it('opens submitted applications by ypat id', () => {
    expect(source).toContain('function openApplicationDetail(item: YpatInfo)')
    expect(source).toContain('`/pages-sub/ypat/detail?id=${item.id}`')
  })
})
