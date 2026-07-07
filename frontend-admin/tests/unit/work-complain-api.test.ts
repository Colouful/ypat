import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const getMock = vi.fn<(url: string, params?: Record<string, unknown>) => Promise<unknown>>(
  (url: string, params?: Record<string, unknown>) => Promise.resolve({ url, params }),
)
const postMock = vi.fn<(url: string, data?: unknown, config?: unknown) => Promise<unknown>>(
  (url: string, data?: unknown, config?: unknown) => Promise.resolve({ url, data, config }),
)

vi.mock('@/api/request', () => ({
  get: getMock,
  post: postMock,
}))

describe('后台作品投诉 API', () => {
  beforeEach(() => {
    getMock.mockClear()
    postMock.mockClear()
  })

  it('应请求投诉列表与详情', async () => {
    const api = await import('@/api/modules/work-complain')

    await expect(
      api.getWorkComplainList({ page: 0, size: 10, status: '0', workId: '88', userId: '66' }) as unknown as Promise<unknown>,
    ).resolves.toMatchObject({
      url: '/admin/work/complain/list',
      params: { page: 0, size: 10, status: '0', workId: '88', userId: '66' },
    })
    expect(getMock).toHaveBeenNthCalledWith(1, '/admin/work/complain/list', {
      page: 0,
      size: 10,
      status: '0',
      workId: '88',
      userId: '66',
    })

    await expect(api.getWorkComplainDetail(12) as unknown as Promise<unknown>).resolves.toMatchObject({
      url: '/admin/work/complain/detail',
      params: { id: 12 },
    })
    expect(getMock).toHaveBeenNthCalledWith(2, '/admin/work/complain/detail', { id: 12 })
  })

  it('应提交投诉处理参数', async () => {
    const api = await import('@/api/modules/work-complain')

    await expect(api.handleWorkComplain(12, '1', '证据属实', true) as unknown as Promise<unknown>).resolves.toMatchObject({
      url: '/admin/work/complain/handle',
      data: undefined,
      config: { params: { id: 12, status: '1', reason: '证据属实', offlineWork: true } },
    })
    expect(postMock).toHaveBeenNthCalledWith(1, '/admin/work/complain/handle', undefined, {
      params: { id: 12, status: '1', reason: '证据属实', offlineWork: true },
    })
  })

  it('菜单标题应为作品投诉', async () => {
    const { menuConfig } = await import('@/constants/menu')
    const item = menuConfig
      .flatMap((group) => group.children)
      .find((entry) => entry.path === '/manage/work-complain/index')

    expect(item?.title).toBe('作品投诉')
  })

  it('详情弹窗应支持只读详情态', () => {
    const source = readFileSync(
      resolve(process.cwd(), 'src/views/manage/work-complain-list/index.vue'),
      'utf8',
    )

    expect(source).toContain("type DialogMode = 'detail' | 'handle'")
    expect(source).toContain("if (dialogMode.value === 'detail') return '投诉详情'")
    expect(source).toContain('async function openDetailDialog(row: WorkComplainInfo)')
    expect(source).toContain('@click="openDetailDialog(row as WorkComplainInfo)"')
    expect(source).toContain('<el-form v-if="dialogMode === \'handle\'"')
    expect(source).toContain('<el-button\n          v-if="dialogMode === \'handle\'"')
  })

  it('待处理记录应同时保留详情、处理、驳回操作', () => {
    const source = readFileSync(
      resolve(process.cwd(), 'src/views/manage/work-complain-list/index.vue'),
      'utf8',
    )

    expect(source).toMatch(/>\s*详情\s*</)
    expect(source).toContain('v-if="isPending(row as WorkComplainInfo)"')
    expect(source).toMatch(/>\s*处理\s*</)
    expect(source).toMatch(/>\s*驳回\s*</)
  })
})
