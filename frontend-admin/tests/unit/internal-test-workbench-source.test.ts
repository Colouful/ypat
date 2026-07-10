import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

function source(path: string): string {
  return readFileSync(resolve(process.cwd(), path), 'utf-8')
}

describe('internal test workbench source contracts', () => {
  it('资源管理页支持批量 URL、作品组、占用筛选和三级城市', () => {
    const page = source('src/views/internal-test/resource/index.vue')

    expect(page).toContain('batchCreateInternalResources')
    expect(page).toContain('urlsText')
    expect(page).toContain('groupSize')
    expect(page).toContain('usedFlag')
    expect(page).toContain('regionCascaderOptions')
    expect(page).toContain('一行一个 URL')
    expect(page).toContain('作品组')
    expect(page).toContain('占用状态')
  })

  it('生成页提供新增用户、新增作品、新增约拍三类动作和内测用户搜索', () => {
    const page = source('src/views/internal-test/generator/index.vue')

    expect(page).toContain('InternalTestGenerateAction')
    expect(page).toContain('generateInternalUsers')
    expect(page).toContain('generateInternalWorks')
    expect(page).toContain('generateInternalYpats')
    expect(page).toContain('searchInternalUsers')
    expect(page).toContain('getInternalResourceGroups')
    expect(page).toContain('微信号')
    expect(page).toContain('联系电话')
    expect(page).not.toContain('模板类型')
  })

  it('用户作品约拍列表展示内测数据列和筛选，用户列表展示内测专属按钮', () => {
    const user = source('src/views/query/user-list/index.vue')
    const ypat = source('src/views/query/ypat-list/index.vue')
    const work = source('src/views/manage/work-list/index.vue')

    for (const page of [user, ypat, work]) {
      expect(page).toContain('dataFlag')
      expect(page).toContain('内测数据')
      expect(page).toContain('InternalTestDataFlag')
    }
    expect(user).toContain('grantInternalUserMember')
    expect(user).toContain('verifyInternalUser')
    expect(user).toContain('markInternalUserDepositPaid')
    expect(user).toContain('row.dataFlag === InternalTestDataFlag.INTERNAL_TEST.value')
  })
})
