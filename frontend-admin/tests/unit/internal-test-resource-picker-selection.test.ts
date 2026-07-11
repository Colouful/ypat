import { describe, expect, it } from 'vitest'
import type { InternalTestResource, InternalTestResourceGroup } from '@/api/modules/internal-test'
import {
  YPAT_RESOURCE_LIMIT,
  replaceWorkGroupSelection,
  toggleYpatResourceSelection,
} from '@/views/internal-test/generator/resource-picker-selection'

function resource(id: number): InternalTestResource {
  return { id, title: `resource-${id}` }
}

function group(groupNo: string): InternalTestResourceGroup {
  return { groupNo, resources: [resource(Number(groupNo.slice(1)))] }
}

describe('内测资源选择状态', () => {
  it('作品组选择由 G1 替换为 G2，并可取消', () => {
    const group1 = group('G1')
    const group2 = group('G2')

    let selected = replaceWorkGroupSelection(group1, true)
    expect(selected).toBe(group1)

    selected = replaceWorkGroupSelection(group2, true)
    expect(selected).toBe(group2)
    expect(replaceWorkGroupSelection(group2, false)).toBeUndefined()
  })

  it('约拍资源保留选择顺序且重复勾选不重复', () => {
    const third = resource(3)
    const first = resource(1)
    const second = resource(2)
    const resources = [third, first, second]
    let selected: InternalTestResource[] = []

    for (const item of resources) {
      selected = toggleYpatResourceSelection(selected, item, true).selected
    }

    const duplicateFirst = resource(1)
    expect(duplicateFirst).not.toBe(first)

    const repeated = toggleYpatResourceSelection(selected, duplicateFirst, true)
    expect(repeated.selected.map((item) => item.id)).toEqual([3, 1, 2])
    expect(repeated.selected.filter((item) => item.id === 1)).toHaveLength(1)
    expect(repeated.selected[1]).toBe(first)
    expect(repeated.limitReached).toBe(false)
  })

  it('取消资源时只删除对应 id 且不改变其他顺序', () => {
    const selected = [resource(1), resource(2), resource(3)]

    const result = toggleYpatResourceSelection(selected, resource(2), false)

    expect(result.selected.map((item) => item.id)).toEqual([1, 3])
    expect(result.limitReached).toBe(false)
  })

  it('选择 1 到 9 后拒绝第 10 个资源', () => {
    let selected: InternalTestResource[] = []

    for (let id = 1; id <= YPAT_RESOURCE_LIMIT; id += 1) {
      const result = toggleYpatResourceSelection(selected, resource(id), true)
      expect(result.limitReached).toBe(false)
      selected = result.selected
    }

    const beforeLimitAttempt = [...selected]
    const rejected = toggleYpatResourceSelection(selected, resource(10), true)
    expect(YPAT_RESOURCE_LIMIT).toBe(9)
    expect(rejected.limitReached).toBe(true)
    expect(rejected.selected).toEqual(beforeLimitAttempt)
  })

  it('达到上限后取消一个资源即可再选', () => {
    const selected = Array.from({ length: YPAT_RESOURCE_LIMIT }, (_, index) => resource(index + 1))
    const afterRemoval = toggleYpatResourceSelection(selected, resource(5), false)
    const afterAddition = toggleYpatResourceSelection(afterRemoval.selected, resource(10), true)

    expect(afterAddition.limitReached).toBe(false)
    expect(afterAddition.selected.map((item) => item.id)).toEqual([1, 2, 3, 4, 6, 7, 8, 9, 10])
  })

  it('勾选、取消和达到上限都不修改输入数组', () => {
    const selected = [resource(1), resource(2)]
    const snapshot = [...selected]

    toggleYpatResourceSelection(selected, resource(3), true)
    toggleYpatResourceSelection(selected, resource(1), false)

    expect(selected).toEqual(snapshot)

    const full = Array.from({ length: YPAT_RESOURCE_LIMIT }, (_, index) => resource(index + 1))
    const fullSnapshot = [...full]
    toggleYpatResourceSelection(full, resource(10), true)
    expect(full).toEqual(fullSnapshot)
  })

  it('无 id 资源按对象引用区分，避免相互误判为重复项', () => {
    const first: InternalTestResource = { title: 'draft-1' }
    const second: InternalTestResource = { title: 'draft-2' }
    const selected = toggleYpatResourceSelection([], first, true).selected
    const withSecond = toggleYpatResourceSelection(selected, second, true).selected
    const repeated = toggleYpatResourceSelection(withSecond, first, true).selected

    expect(withSecond).toEqual([first, second])
    expect(repeated).toEqual([first, second])
  })
})
