import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('work detail action feedback contract', () => {
  const file = fileURLToPath(new URL('./detail.vue', import.meta.url))
  const source = readFileSync(file, 'utf8')

  it('shows friendly feedback after action state changes', () => {
    expect(source).toContain("uni.showToast({ title: '点赞成功', icon: 'success' })")
    expect(source).toContain("uni.showToast({ title: '已取消点赞', icon: 'none' })")
    expect(source).toContain("uni.showToast({ title: '收藏成功', icon: 'success' })")
    expect(source).toContain("uni.showToast({ title: '已取消收藏', icon: 'none' })")
  })

  it('keeps share action as a visible user hint', () => {
    expect(source).toContain("uni.showToast({ title: '点击右上角分享给好友', icon: 'none' })")
  })
})
