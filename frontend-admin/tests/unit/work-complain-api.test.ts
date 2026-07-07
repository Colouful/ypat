import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { resolve } from 'node:path'
import { computed, defineComponent, h, inject, provide, type ComputedRef } from 'vue'

beforeAll(() => {
  vi.stubGlobal('ResizeObserver', class {
    observe() {}
    unobserve() {}
    disconnect() {}
  })
})

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
})

describe('后台作品投诉页面行为', () => {
  const tableDataKey = Symbol('table-data')
  const ElButtonStub = defineComponent({
    name: 'ElButton',
    emits: ['click'],
    setup(_, { slots, emit }) {
      return () => h('button', { onClick: (event: MouseEvent) => emit('click', event) }, slots.default?.())
    },
  })
  const ElTableStub = defineComponent({
    name: 'ElTable',
    props: {
      data: {
        type: Array,
        default: () => [],
      },
    },
    setup(props, { slots }) {
      provide(tableDataKey, computed(() => props.data as Record<string, unknown>[]))
      return () => h('div', { class: 'el-table-stub' }, slots.default?.())
    },
  })
  const ElTableColumnStub = defineComponent({
    name: 'ElTableColumn',
    props: {
      prop: {
        type: String,
        default: '',
      },
    },
    setup(props, { slots }) {
      const rows = inject<ComputedRef<Record<string, unknown>[]>>(tableDataKey, computed(() => []))
      return () => h(
        'div',
        { class: 'el-table-column-stub' },
        rows.value.map((row, index) => {
          const content = slots.default?.({ row })
            ?? [h('span', String(props.prop ? (row[props.prop] ?? '') : ''))]
          return h('div', { key: `${String(props.prop)}-${index}` }, content)
        }),
      )
    },
  })
  const SimpleWrapperStub = defineComponent({
    setup(_, { slots }) {
      return () => h('div', slots.default?.())
    },
  })
  const ElDialogStub = defineComponent({
    name: 'ElDialog',
    props: {
      modelValue: {
        type: Boolean,
        default: false,
      },
      title: {
        type: String,
        default: '',
      },
    },
    emits: ['update:modelValue'],
    setup(props, { slots }) {
      return () => props.modelValue
        ? h('div', { class: 'el-dialog-stub' }, [
            h('div', { class: 'el-dialog-title' }, props.title),
            h('div', { class: 'el-dialog-body' }, slots.default?.()),
            h('div', { class: 'el-dialog-footer' }, slots.footer?.()),
          ])
        : null
    },
  })
  const ElTagStub = defineComponent({
    name: 'ElTag',
    setup(_, { slots }) {
      return () => h('span', { class: 'el-tag-stub' }, slots.default?.())
    },
  })

  afterEach(() => {
    document.body.innerHTML = ''
    vi.doUnmock('@/api/modules/work-complain')
    vi.resetModules()
  })

  async function mountPage(options: {
    listData: Array<Record<string, unknown>>
    detailData: Record<string, unknown>
  }) {
    const getWorkComplainList = vi.fn().mockResolvedValue({
      data: {
        content: options.listData,
        totalElements: options.listData.length,
      },
    })
    const getWorkComplainDetail = vi.fn().mockResolvedValue({
      data: options.detailData,
    })
    const handleWorkComplain = vi.fn().mockResolvedValue({
      code: 200,
      data: null,
      msg: 'ok',
      success: true,
    })

    vi.doMock('@/api/modules/work-complain', () => ({
      getWorkComplainList,
      getWorkComplainDetail,
      handleWorkComplain,
    }))

    const viewPath = resolve(process.cwd(), 'src/views/manage/work-complain-list/index.vue')
    const WorkComplainList = (await import(viewPath)).default
    const wrapper = mount(WorkComplainList, {
      attachTo: document.body,
      global: {
        components: {
          ElButton: ElButtonStub,
          ElDialog: ElDialogStub,
          ElForm: SimpleWrapperStub,
          ElFormItem: SimpleWrapperStub,
          ElImage: SimpleWrapperStub,
          ElInput: SimpleWrapperStub,
          ElOption: SimpleWrapperStub,
          ElPagination: SimpleWrapperStub,
          ElSelect: SimpleWrapperStub,
          ElSwitch: SimpleWrapperStub,
          ElTable: ElTableStub,
          ElTableColumn: ElTableColumnStub,
          ElTag: ElTagStub,
        },
        directives: {
          loading: () => undefined,
        },
      },
    })

    await flushPromises()

    return { wrapper, getWorkComplainList, getWorkComplainDetail, handleWorkComplain }
  }

  it('点击详情时应展示只读详情、处理状态和处理备注', async () => {
    const { wrapper, getWorkComplainDetail } = await mountPage({
      listData: [
        {
          id: 23,
          workId: 1001,
          userId: 2001,
          userNickname: '投诉人',
          targetUserId: 3001,
          targetNickname: '被投诉人',
          reason: '骚扰',
          status: '1',
          statusText: '已处理',
          createdAt: '2026-07-08 10:00:00',
          workDescription: '作品描述',
        },
      ],
      detailData: {
        id: 23,
        workId: 1001,
        userId: 2001,
        userNickname: '投诉人',
        targetUserId: 3001,
        targetNickname: '被投诉人',
        reason: '骚扰',
        contact: 'wechat-001',
        status: '1',
        statusText: '已处理',
        createdAt: '2026-07-08 10:00:00',
        workDescription: '作品描述',
        handleReason: '证据属实',
      },
    })

    const detailButton = wrapper
      .findAll('button')
      .find((button) => button.text().includes('详情'))

    expect(detailButton, '列表中应存在详情入口').toBeTruthy()

    await detailButton!.trigger('click')
    await flushPromises()

    expect(getWorkComplainDetail).toHaveBeenCalledWith(23)
    expect(document.body.textContent).toContain('投诉详情')
    expect(document.body.textContent).toContain('已处理')
    expect(document.body.textContent).toContain('证据属实')
    expect(document.body.textContent).not.toContain('确认提交')

    wrapper.unmount()
  })

  it('待处理记录应同时渲染详情、处理、驳回操作', async () => {
    const { wrapper } = await mountPage({
      listData: [
        {
          id: 24,
          workId: 1002,
          userId: 2002,
          userNickname: '投诉人二',
          targetUserId: 3002,
          targetNickname: '被投诉人二',
          reason: '侵权',
          status: '0',
          statusText: '待处理',
          createdAt: '2026-07-08 11:00:00',
          workDescription: '待处理作品',
        },
      ],
      detailData: {
        id: 24,
      },
    })

    const buttons = wrapper.findAll('button').map((button) => button.text())

    expect(buttons).toContain('详情')
    expect(buttons).toContain('处理')
    expect(buttons).toContain('驳回')

    wrapper.unmount()
  })
})
