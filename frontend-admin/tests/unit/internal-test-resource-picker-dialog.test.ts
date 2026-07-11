import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import { computed, defineComponent, h, inject, nextTick, provide, type ComputedRef } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { InternalTestResource } from '@/api/modules/internal-test'
import ResourcePickerDialog from '@/views/internal-test/generator/ResourcePickerDialog.vue'

const { getInternalResourceGroupsMock, getInternalResourcesMock, warningMock } = vi.hoisted(() => ({
  getInternalResourceGroupsMock: vi.fn(),
  getInternalResourcesMock: vi.fn(),
  warningMock: vi.fn(),
}))

vi.mock('@/api/modules/internal-test', () => ({
  getInternalResourceGroups: getInternalResourceGroupsMock,
  getInternalResources: getInternalResourcesMock,
}))

vi.mock('element-plus', () => ({
  ElMessage: { warning: warningMock },
}))

const tableDataKey = Symbol('resource-picker-table-data')

const ElTableStub = defineComponent({
  name: 'ElTable',
  props: {
    data: { type: Array, default: () => [] },
  },
  emits: ['select', 'select-all'],
  setup(props, { expose, slots }) {
    provide(tableDataKey, computed(() => props.data as InternalTestResource[]))
    expose({
      clearSelection: vi.fn(),
      toggleRowSelection: vi.fn(),
    })
    return () => h('div', { class: 'table-stub' }, [
      h('div', { class: 'table-data' }, JSON.stringify(props.data)),
      slots.default?.(),
    ])
  },
})

const ElTableColumnStub = defineComponent({
  name: 'ElTableColumn',
  props: {
    type: { type: String, default: '' },
    prop: { type: String, default: '' },
    selectable: { type: Function, default: undefined },
  },
  setup(props, { slots }) {
    const rows = inject<ComputedRef<InternalTestResource[]>>(tableDataKey, computed(() => []))
    return () => h('div', { class: 'table-column-stub', 'data-type': props.type }, rows.value.map((row) => (
      h('div', { key: `${props.prop}-${row.id ?? row.groupNo}` }, slots.default?.({ row }))
    )))
  },
})

const ElDialogStub = defineComponent({
  name: 'ElDialog',
  props: {
    modelValue: { type: Boolean, default: false },
    title: { type: String, default: '' },
  },
  emits: ['update:modelValue'],
  setup(props, { slots }) {
    return () => h('section', {
      class: 'dialog-stub',
      'data-title': props.title,
      'data-visible': String(props.modelValue),
    }, [
      slots.default?.(),
      h('footer', slots.footer?.()),
    ])
  },
})

const ElTabsStub = defineComponent({
  name: 'ElTabs',
  props: { modelValue: { type: String, default: '' } },
  emits: ['update:modelValue'],
  setup(_, { slots }) {
    return () => h('div', { class: 'tabs-stub' }, slots.default?.())
  },
})

const ElPaginationStub = defineComponent({
  name: 'ElPagination',
  props: {
    currentPage: { type: Number, default: 1 },
    pageSize: { type: Number, default: 10 },
  },
  emits: ['current-change', 'size-change'],
  setup() {
    return () => h('div', { class: 'pagination-stub' })
  },
})

const ElButtonStub = defineComponent({
  name: 'ElButton',
  emits: ['click'],
  setup(_, { emit, slots }) {
    return () => h('button', { onClick: () => emit('click') }, slots.default?.())
  },
})

function modelStub(name: string) {
  return defineComponent({
    name,
    props: { modelValue: { default: undefined } },
    emits: ['update:modelValue'],
    setup(_, { slots }) {
      return () => h('div', { class: `${name}-stub` }, slots.default?.())
    },
  })
}

const simpleStub = defineComponent({
  setup(_, { slots }) {
    return () => h('div', slots.default?.())
  },
})

const globalOptions = {
  components: {
    ElAlert: simpleStub,
    ElButton: ElButtonStub,
    ElCascader: modelStub('ElCascader'),
    ElDialog: ElDialogStub,
    ElEmpty: simpleStub,
    ElForm: simpleStub,
    ElFormItem: simpleStub,
    ElImage: simpleStub,
    ElInput: modelStub('ElInput'),
    ElOption: simpleStub,
    ElPagination: ElPaginationStub,
    ElSelect: modelStub('ElSelect'),
    ElTable: ElTableStub,
    ElTableColumn: ElTableColumnStub,
    ElTabPane: simpleStub,
    ElTabs: ElTabsStub,
  },
  directives: {
    loading: () => undefined,
  },
}

function resource(id: number, overrides: Partial<InternalTestResource> = {}): InternalTestResource {
  return {
    id,
    mediaType: 'image',
    usageType: 'ypat',
    title: `resource-${id}`,
    usedFlag: 0,
    ...overrides,
  }
}

function page(rows: InternalTestResource[]) {
  return {
    data: {
      content: rows,
      totalElements: rows.length,
    },
  }
}

function deferred<T>() {
  let resolve!: (value: T) => void
  const promise = new Promise<T>((resolvePromise) => {
    resolve = resolvePromise
  })
  return { promise, resolve }
}

function mountDialog(selectedYpatResources: InternalTestResource[] = []) {
  return mount(ResourcePickerDialog, {
    props: {
      visible: true,
      mode: 'ypat',
      selectedYpatResources,
    },
    global: globalOptions,
  })
}

function currentTable(wrapper: VueWrapper) {
  return wrapper.findComponent({ name: 'ElTable' })
}

function clickButton(wrapper: VueWrapper, text: string): Promise<void> {
  const button = wrapper.findAll('button').find((item) => item.text() === text)
  if (!button) throw new Error(`未找到按钮：${text}`)
  return button.trigger('click')
}

function lastConfirmedYpatResources(wrapper: VueWrapper): InternalTestResource[] {
  const events = wrapper.emitted('confirmYpat') || []
  return (events[events.length - 1]?.[0] || []) as InternalTestResource[]
}

beforeEach(() => {
  vi.clearAllMocks()
  getInternalResourcesMock.mockResolvedValue(page([]))
  getInternalResourceGroupsMock.mockResolvedValue(page([]))
})

describe('ResourcePickerDialog', () => {
  it('旧请求晚返回时不覆盖新标签请求的数据', async () => {
    const imageRequest = deferred<ReturnType<typeof page>>()
    const videoRequest = deferred<ReturnType<typeof page>>()
    getInternalResourcesMock
      .mockReturnValueOnce(imageRequest.promise)
      .mockReturnValueOnce(videoRequest.promise)
    const wrapper = mountDialog()

    wrapper.findComponent({ name: 'ElTabs' }).vm.$emit('update:modelValue', 'video')
    await nextTick()
    expect(getInternalResourcesMock).toHaveBeenCalledTimes(2)

    videoRequest.resolve(page([resource(2, { mediaType: 'video' })]))
    await flushPromises()
    expect(currentTable(wrapper).props('data')).toEqual([resource(2, { mediaType: 'video' })])

    imageRequest.resolve(page([resource(1)]))
    await flushPromises()
    expect(currentTable(wrapper).props('data')).toEqual([resource(2, { mediaType: 'video' })])
  })

  it('关闭弹窗后在途请求返回时不写入隐藏列表', async () => {
    const pendingRequest = deferred<ReturnType<typeof page>>()
    getInternalResourcesMock.mockReturnValueOnce(pendingRequest.promise)
    const wrapper = mountDialog()

    await wrapper.setProps({ visible: false })
    pendingRequest.resolve(page([resource(1)]))
    await flushPromises()

    expect(currentTable(wrapper).props('data')).toEqual([])
  })

  it('跨页取消当前页全选时保留其他页选择', async () => {
    getInternalResourcesMock.mockImplementation((query: { page?: number }) => Promise.resolve(
      page(query.page === 1 ? [resource(3), resource(4)] : [resource(1), resource(2)]),
    ))
    const wrapper = mountDialog()
    await flushPromises()

    currentTable(wrapper).vm.$emit('select-all', [resource(1), resource(2)])
    await nextTick()
    wrapper.findComponent({ name: 'ElPagination' }).vm.$emit('current-change', 2)
    await flushPromises()
    currentTable(wrapper).vm.$emit('select-all', [resource(3), resource(4)])
    await nextTick()
    currentTable(wrapper).vm.$emit('select-all', [])
    await nextTick()
    await clickButton(wrapper, '确认')

    expect(lastConfirmedYpatResources(wrapper)).toEqual([resource(1), resource(2)])
  })

  it('已有八张时当前页全选只按顺序加入第九张', async () => {
    const initial = Array.from({ length: 8 }, (_, index) => resource(index + 1))
    getInternalResourcesMock.mockResolvedValue(page([resource(9), resource(10)]))
    const wrapper = mountDialog(initial)
    await flushPromises()

    currentTable(wrapper).vm.$emit('select-all', [resource(9), resource(10)])
    await nextTick()
    await clickButton(wrapper, '确认')

    const confirmed = lastConfirmedYpatResources(wrapper)
    expect(confirmed.map((item) => item.id)).toEqual([1, 2, 3, 4, 5, 6, 7, 8, 9])
    expect(confirmed).toHaveLength(9)
    expect(warningMock).toHaveBeenCalledOnce()
  })

  it('已占用行与视频标签行不可选择', async () => {
    const used = resource(1, { usedFlag: 1 })
    const unused = resource(2)
    getInternalResourcesMock.mockResolvedValueOnce(page([used, unused])).mockResolvedValueOnce(page([unused]))
    const wrapper = mountDialog()
    await flushPromises()

    const selectionColumn = wrapper.findAllComponents({ name: 'ElTableColumn' })
      .find((column) => column.props('type') === 'selection')
    const selectable = selectionColumn?.props('selectable') as (row: InternalTestResource) => boolean
    expect(selectable(used)).toBe(false)
    expect(selectable(unused)).toBe(true)

    wrapper.findComponent({ name: 'ElTabs' }).vm.$emit('update:modelValue', 'video')
    await flushPromises()
    expect(selectable(unused)).toBe(false)
  })

  it('取消和关闭不确认，只有确认按钮发出确认事件', async () => {
    const wrapper = mountDialog()
    await flushPromises()

    await clickButton(wrapper, '取消')
    wrapper.findAllComponents({ name: 'ElDialog' })[0].vm.$emit('update:modelValue', false)
    await nextTick()
    expect(wrapper.emitted('confirmYpat')).toBeUndefined()
    expect(wrapper.emitted('confirmWork')).toBeUndefined()

    await clickButton(wrapper, '确认')
    expect(wrapper.emitted('confirmYpat')).toHaveLength(1)
  })

  it('切换为作品模式时清除旧约拍地区和风格且只请求一次', async () => {
    const wrapper = mountDialog()
    await flushPromises()

    wrapper.findAllComponents({ name: 'ElSelect' })[0].vm.$emit('update:modelValue', '12')
    wrapper.findComponent({ name: 'ElCascader' }).vm.$emit('update:modelValue', ['浙江省', '杭州市', '西湖区'])
    await nextTick()
    getInternalResourceGroupsMock.mockClear()

    await wrapper.setProps({ mode: 'work' })
    await flushPromises()

    expect(getInternalResourceGroupsMock).toHaveBeenCalledOnce()
    expect(getInternalResourceGroupsMock).toHaveBeenCalledWith({
      keyword: '',
      styleCode: '',
      usedFlag: 0,
      page: 0,
      size: 10,
      mediaType: 'image',
      usageType: 'work',
    })
  })
})
