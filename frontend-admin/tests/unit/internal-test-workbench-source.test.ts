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

    const saveResource = page.slice(page.indexOf('async function saveResource'), page.indexOf('async function changeStatus'))
    expect(saveResource).not.toContain('.filter(Boolean)')
  })

  it('生成页提供新增用户、新增作品、新增约拍三类动作和内测用户搜索', () => {
    const page = source('src/views/internal-test/generator/index.vue')

    expect(page).toContain('InternalTestGenerateAction')
    expect(page).toContain('generateInternalUsers')
    expect(page).toContain('generateInternalWorks')
    expect(page).toContain('generateInternalYpats')
    expect(page).toContain('searchInternalUsers')
    expect(page).toContain("import ResourcePickerDialog from './ResourcePickerDialog.vue'")
    expect(page).toContain('selectedWorkGroup')
    expect(page).toContain('selectedYpatResources')
    expect(page).toContain('getYpatPatstyleOptions')
    expect(page).toContain('patTimeRange')
    expect(page).toContain('约拍时间段')
    expect(page).toContain('请选择到区县')
    expect(page).toContain("return '请选择作品组'")
    expect(page).toContain("return '请选择约拍图片'")
    expect(page).toContain("return '约拍图片数据异常，请重新选择'")
    expect(page).toContain('validSelectedYpatResourceIds.value.length !== selectedYpatResources.value.length')
    expect(page).toContain('(isCreateWorks.value || isCreateYpats.value) && form.styleCodes.length === 0')
    expect(page).toContain("publishStatus: '2'")
    expect(page).toContain('v-if="!isCreateUsers" label="发布状态"')
    expect(page).toContain('选择作品组')
    expect(page).toContain('选择约拍图片')
    expect(page).toContain('已选')
    expect(page).toContain('resource-thumbnail')
    expect(page).toContain('微信号')
    expect(page).toContain('联系电话')
    expect(page).not.toContain('v-model="form.groupNos"')
    expect(page).not.toContain('getInternalResourceGroups')
    expect(page).not.toContain('loadWorkGroups')
    expect(page).not.toContain('activeWorkMediaType')
    expect(page).not.toContain('workMediaChanged')
    expect(page).not.toContain('模板类型')

    const styleSelectStart = page.indexOf('<el-select\n            v-model="form.styleCodes"')
    const styleSelectEnd = page.indexOf('</el-select>', styleSelectStart)
    expect(styleSelectStart).toBeGreaterThanOrEqual(0)
    expect(styleSelectEnd).toBeGreaterThan(styleSelectStart)
    const styleSelect = page.slice(styleSelectStart, styleSelectEnd)
    expect(styleSelect).toContain('v-model="form.styleCodes"\n            multiple\n')

    const payload = page.slice(page.indexOf('function buildPayload'), page.indexOf('async function submitGenerate'))
    expect(payload).toContain('groupNos: isCreateWorks.value')
    expect(payload).toContain('[selectedWorkGroup.value.groupNo]')
    expect(payload).toContain('ypatResourceIds: isCreateYpats.value')
    expect(payload).toContain('validSelectedYpatResourceIds.value')
    expect(payload).toContain('validSelectedYpatResourceIds.value.length === selectedYpatResources.value.length')

    const pickerStart = page.indexOf('<ResourcePickerDialog')
    const pickerEnd = page.indexOf('</ResourcePickerDialog>', pickerStart)
    expect(pickerStart).toBeGreaterThanOrEqual(0)
    expect(pickerEnd).toBeGreaterThan(pickerStart)
    const picker = page.slice(pickerStart, pickerEnd)
    expect(picker).toContain('v-model:visible="resourcePickerVisible"')
    expect(picker).toContain(':mode="resourcePickerMode"')
    expect(picker).toContain(':selected-work-group="selectedWorkGroup"')
    expect(picker).toContain(':selected-ypat-resources="selectedYpatResources"')
    expect(picker).toContain('@confirm-work="confirmWorkGroup"')
    expect(picker).toContain('@confirm-ypat="confirmYpatResources"')
    expect(picker).not.toContain('styleCodes')
  })

  it('生成页将批次创建时间格式化后展示', () => {
    const page = source('src/views/internal-test/generator/index.vue')

    expect(page).toContain("import { formatDate } from '@/utils/format'")
    expect(page).toContain("formatDate(row.createdAt ?? null) || '-'")
  })

  it('资源选择弹窗独立查询并按作品与约拍规则维护临时选择', () => {
    const dialog = source('src/views/internal-test/generator/ResourcePickerDialog.vue')

    expect(dialog).toContain('getInternalResourceGroups')
    expect(dialog).toContain('getInternalResources')
    expect(dialog).toContain('<el-tabs')
    expect(dialog).toContain('type="selection"')
    expect(dialog).toContain('@select="handleYpatSelect"')
    expect(dialog).toContain('@select-all="handleYpatSelectAll"')
    expect(dialog).toContain(':select-on-indeterminate="false"')
    expect(dialog).toContain('replaceWorkGroupSelection')
    expect(dialog).toContain('toggleYpatResourceSelection')
    expect(dialog).toContain('row-key="groupNo"')
    expect(dialog).toContain('row-key="id"')
    expect(dialog).toContain('await nextTick()')
    expect(dialog).not.toContain('selection-change')
    expect(dialog).toContain('当前约拍仅支持图片资源')
    expect(dialog).toContain('detailVisible')
    expect(dialog).toContain('<el-pagination')
    expect(dialog).toContain('const query = reactive<InternalTestResourceQuery>')
    expect(dialog).not.toContain('props.styleCodes')

    const selectableBody = dialog.slice(
      dialog.indexOf('function isYpatSelectable'),
      dialog.indexOf('async function synchronizeCurrentPageSelection'),
    )
    expect(selectableBody).toContain('const selected = temporaryYpatResources.value.some')
    expect(selectableBody).toContain('return selected || temporaryYpatResources.value.length < YPAT_RESOURCE_LIMIT')

    const fetchBody = dialog.slice(
      dialog.indexOf('async function fetchResources'),
      dialog.indexOf('function resetPageAndFetch'),
    )
    const workRequest = fetchBody.slice(0, fetchBody.indexOf('} else {'))
    expect(workRequest).not.toContain('province:')
    expect(workRequest).not.toContain('city:')
    expect(workRequest).not.toContain('area:')
    expect(fetchBody).toContain('province: query.province')
    expect(fetchBody).toContain('city: query.city')
    expect(fetchBody).toContain('area: query.area')

    const resetBody = dialog.slice(
      dialog.indexOf('function resetQueryFilters'),
      dialog.indexOf('function clearResourcePage'),
    )
    expect(resetBody).toContain("query.keyword = ''")
    expect(resetBody).toContain("query.styleCode = ''")
    expect(resetBody).toContain('query.usedFlag = InternalTestResourceUsedFlag.UNUSED.value')
    expect(resetBody).toContain('activeMediaType.value = InternalTestMediaType.IMAGE.value')
    expect(resetBody).toContain('return mediaChanged')
    expect(dialog).toContain('watch(activeMediaType')
    expect(dialog).toContain("watch([() => props.visible, () => props.mode]")
    expect(dialog).not.toContain('@tab-change=')
    const visibilityWatch = dialog.slice(
      dialog.indexOf("watch([() => props.visible, () => props.mode]"),
      dialog.indexOf('</script>'),
    )
    expect(visibilityWatch).toContain('requestSequence += 1')
    expect(visibilityWatch).toContain('loading.value = false')
    expect(visibilityWatch).toContain('if (!mediaChanged) void fetchResources()')
    expect(dialog).toContain('width="min(960px, calc(100vw - 32px))"')
    expect(dialog).toContain('width="min(760px, calc(100vw - 32px))"')

    const cancelBody = dialog.slice(
      dialog.indexOf('function cancel()'),
      dialog.indexOf('function confirm()'),
    )
    expect(cancelBody).toContain("emit('update:visible', false)")
    expect(cancelBody).not.toContain("emit('confirmWork'")
    expect(cancelBody).not.toContain("emit('confirmYpat'")
  })

  it('作品组使用资源标题展示并在新增时保存组标题', () => {
    const generator = source('src/views/internal-test/generator/index.vue')
    const resource = source('src/views/internal-test/resource/index.vue')

    expect(generator).toContain('function workGroupLabel(group: InternalTestResourceGroup)')
    expect(generator).toContain('group.resources?.[0]?.title?.trim()')
    expect(generator).toContain('workGroupLabel(selectedWorkGroup)')
    expect(generator).not.toContain('group.groupTitle || group.groupNo')
    expect(resource).toContain('form.usageType === InternalTestUsageType.WORK.value')
    expect(resource).toContain('? form.title?.trim() || undefined')
  })

  it('生成页提供有明确风险提示的一键清除全部内测数据', () => {
    const page = source('src/views/internal-test/generator/index.vue')

    expect(page).toContain('cleanupAllInternalData')
    expect(page).toContain('cleanupAll: true')
    expect(page).toContain('一键清除全部内测数据')
    expect(page).toContain('真实数据不会被处理')
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
