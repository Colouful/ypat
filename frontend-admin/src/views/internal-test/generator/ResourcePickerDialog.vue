<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getInternalResourceGroups,
  getInternalResources,
  type InternalTestResource,
  type InternalTestResourceGroup,
  type InternalTestResourceQuery,
} from '@/api/modules/internal-test'
import {
  InternalTestMediaType,
  InternalTestResourceStatus,
  InternalTestResourceUsedFlag,
  InternalTestUsageType,
  getInternalTestResourceUsedFlagOptions,
  getWorkTagStyleOptions,
  getYpatPatstyleOptions,
  resolveWorkTagStyleName,
  getYpatPatstyleText,
} from '@/constants/enums'
import { regionCascaderOptions, toRegionFields, type RegionPath } from '@/utils/region'
import {
  replaceWorkGroupSelection,
  toggleYpatResourceSelection,
  YPAT_RESOURCE_LIMIT,
} from './resource-picker-selection'

type PickerMode = 'work' | 'ypat'
type SelectionTable = {
  clearSelection: () => void
  toggleRowSelection: (row: InternalTestResource | InternalTestResourceGroup, selected?: boolean) => void
}

const props = defineProps<{
  visible: boolean
  mode: PickerMode
  selectedWorkGroup?: InternalTestResourceGroup
  selectedYpatResources: InternalTestResource[]
}>()

const emit = defineEmits<{
  (event: 'update:visible', visible: boolean): void
  (event: 'confirmWork', group: InternalTestResourceGroup | undefined): void
  (event: 'confirmYpat', resources: InternalTestResource[]): void
}>()

const mediaTabs = [
  { label: '图片', value: InternalTestMediaType.IMAGE.value },
  { label: '视频', value: InternalTestMediaType.VIDEO.value },
]
const regionCascaderProps = {
  value: 'label',
  label: 'label',
  children: 'children',
  emitPath: true,
  checkStrictly: true,
} as const

const localVisible = computed({
  get: () => props.visible,
  set: (visible: boolean) => emit('update:visible', visible),
})
const activeMediaType = ref(InternalTestMediaType.IMAGE.value)
const query = reactive<InternalTestResourceQuery>({
  keyword: '',
  styleCode: '',
  usedFlag: InternalTestResourceUsedFlag.UNUSED.value,
  province: '',
  city: '',
  area: '',
  page: 0,
  size: 10,
})
const workGroups = ref<InternalTestResourceGroup[]>([])
const ypatResources = ref<InternalTestResource[]>([])
const total = ref(0)
const loading = ref(false)
const workTableRef = ref<SelectionTable>()
const ypatTableRef = ref<SelectionTable>()
const temporaryWorkGroup = ref<InternalTestResourceGroup>()
const temporaryYpatResources = ref<InternalTestResource[]>([])
const detailVisible = ref(false)
const detailGroup = ref<InternalTestResourceGroup>()
let requestSequence = 0

const currentPage = computed(() => (query.page ?? 0) + 1)
const selectedCount = computed(() => (
  props.mode === 'work' ? (temporaryWorkGroup.value ? 1 : 0) : temporaryYpatResources.value.length
))
const styleOptions = computed(() => (
  props.mode === 'work' ? getWorkTagStyleOptions() : getYpatPatstyleOptions()
))
const queryRegionPath = computed<RegionPath>({
  get: () => [query.province, query.city, query.area].filter(Boolean) as RegionPath,
  set: (path) => Object.assign(query, toRegionFields(path)),
})

function sameResource(first: InternalTestResource, second: InternalTestResource): boolean {
  return first.id === undefined ? first === second : first.id === second.id
}

function workGroupLabel(group: InternalTestResourceGroup): string {
  return group.groupTitle?.trim() || group.resources?.[0]?.title?.trim() || '未命名作品组'
}

function previewResource(group: InternalTestResourceGroup): InternalTestResource | undefined {
  return group.resources?.[0]
}

function styleText(value?: string): string {
  return props.mode === 'work' ? resolveWorkTagStyleName(value) : getYpatPatstyleText(value)
}

function isWorkSelectable(group: InternalTestResourceGroup): boolean {
  return group.usedFlag !== InternalTestResourceUsedFlag.USED.value
    && group.resources.length > 0
    && group.resources.every((resource) => resource.status === InternalTestResourceStatus.ENABLED.value)
}

function isYpatSelectable(resource: InternalTestResource): boolean {
  if (
    activeMediaType.value !== InternalTestMediaType.IMAGE.value
    || resource.usedFlag === InternalTestResourceUsedFlag.USED.value
    || resource.status !== InternalTestResourceStatus.ENABLED.value
  ) return false

  const selected = temporaryYpatResources.value.some((item) => sameResource(item, resource))
  return selected || temporaryYpatResources.value.length < YPAT_RESOURCE_LIMIT
}

async function synchronizeCurrentPageSelection(): Promise<void> {
  await nextTick()
  if (props.mode === 'work') {
    workTableRef.value?.clearSelection()
    const selected = temporaryWorkGroup.value
    const current = selected && workGroups.value.find((group) => group.groupNo === selected.groupNo)
    if (current) workTableRef.value?.toggleRowSelection(current, true)
    return
  }

  ypatTableRef.value?.clearSelection()
  for (const row of ypatResources.value) {
    if (temporaryYpatResources.value.some((selected) => sameResource(selected, row))) {
      ypatTableRef.value?.toggleRowSelection(row, true)
    }
  }
}

async function fetchResources(): Promise<void> {
  const sequence = ++requestSequence
  loading.value = true
  try {
    const commonQuery: InternalTestResourceQuery = {
      keyword: query.keyword,
      styleCode: query.styleCode,
      usedFlag: query.usedFlag,
      page: query.page,
      size: query.size,
      mediaType: activeMediaType.value,
      status: InternalTestResourceStatus.ENABLED.value,
    }
    if (props.mode === 'work') {
      const response = await getInternalResourceGroups({
        ...commonQuery,
        usageType: InternalTestUsageType.WORK.value,
      })
      if (sequence !== requestSequence) return
      workGroups.value = response.data.content || []
      ypatResources.value = []
      total.value = response.data.totalElements || 0
    } else {
      const response = await getInternalResources({
        ...commonQuery,
        usageType: InternalTestUsageType.YPAT.value,
        province: query.province,
        city: query.city,
        area: query.area,
      })
      if (sequence !== requestSequence) return
      ypatResources.value = response.data.content || []
      workGroups.value = []
      total.value = response.data.totalElements || 0
    }
    await synchronizeCurrentPageSelection()
  } finally {
    if (sequence === requestSequence) loading.value = false
  }
}

function resetQueryFilters(): boolean {
  const mediaChanged = activeMediaType.value !== InternalTestMediaType.IMAGE.value
  query.keyword = ''
  query.styleCode = ''
  query.usedFlag = InternalTestResourceUsedFlag.UNUSED.value
  query.province = ''
  query.city = ''
  query.area = ''
  query.page = 0
  activeMediaType.value = InternalTestMediaType.IMAGE.value
  return mediaChanged
}

function clearResourcePage(): void {
  workGroups.value = []
  ypatResources.value = []
  total.value = 0
}

function resetPageAndFetch(): void {
  query.page = 0
  clearResourcePage()
  void fetchResources()
}

function search(): void {
  resetPageAndFetch()
}

function reset(): void {
  const mediaChanged = resetQueryFilters()
  clearResourcePage()
  if (!mediaChanged) void fetchResources()
}

function handlePageChange(page: number): void {
  query.page = page - 1
  void fetchResources()
}

function handleSizeChange(size: number): void {
  query.size = size
  resetPageAndFetch()
}

function handleWorkSelect(selection: InternalTestResourceGroup[], row: InternalTestResourceGroup): void {
  const checked = selection.some((group) => group.groupNo === row.groupNo)
  temporaryWorkGroup.value = replaceWorkGroupSelection(row, checked)
  void synchronizeCurrentPageSelection()
}

function handleYpatSelect(selection: InternalTestResource[], row: InternalTestResource): void {
  const checked = selection.some((resource) => sameResource(resource, row))
  const result = toggleYpatResourceSelection(temporaryYpatResources.value, row, checked)
  temporaryYpatResources.value = result.selected
  if (result.limitReached) ElMessage.warning(`约拍图片最多选择 ${YPAT_RESOURCE_LIMIT} 张`)
  void synchronizeCurrentPageSelection()
}

function handleYpatSelectAll(selection: InternalTestResource[]): void {
  const selectableRows = ypatResources.value.filter(isYpatSelectable)
  const checking = selectableRows.some((row) => selection.some((selected) => sameResource(selected, row)))
  let selected = temporaryYpatResources.value
  let limitReached = false

  for (const row of selectableRows) {
    const result = toggleYpatResourceSelection(selected, row, checking)
    selected = result.selected
    limitReached ||= result.limitReached
  }
  temporaryYpatResources.value = selected
  if (limitReached) ElMessage.warning(`最多选择 ${YPAT_RESOURCE_LIMIT} 张，已按当前页顺序选取`)
  void synchronizeCurrentPageSelection()
}

function openDetail(group: InternalTestResourceGroup): void {
  detailGroup.value = group
  detailVisible.value = true
}

function cancel(): void {
  emit('update:visible', false)
}

function confirm(): void {
  if (props.mode === 'work') {
    emit('confirmWork', temporaryWorkGroup.value)
  } else {
    emit('confirmYpat', [...temporaryYpatResources.value])
  }
  emit('update:visible', false)
}

watch(activeMediaType, () => {
  if (!props.visible) return
  resetPageAndFetch()
})

watch([() => props.visible, () => props.mode], ([visible], previousValues) => {
  if (!visible) {
    requestSequence += 1
    loading.value = false
    return
  }
  const wasVisible = previousValues?.[0] ?? false
  if (!wasVisible) {
    temporaryWorkGroup.value = props.selectedWorkGroup
    temporaryYpatResources.value = [...props.selectedYpatResources]
  }
  const mediaChanged = resetQueryFilters()
  clearResourcePage()
  if (!mediaChanged) void fetchResources()
}, { immediate: true })
</script>

<template>
  <el-dialog
    v-model="localVisible"
    title="选择内测资源"
    width="min(960px, calc(100vw - 32px))"
    class="resource-picker-dialog"
  >
    <el-tabs v-model="activeMediaType">
      <el-tab-pane v-for="tab in mediaTabs" :key="tab.value" :label="tab.label" :name="tab.value" />
    </el-tabs>

    <el-form :model="query" inline class="picker-filters">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="标题、URL、备注" />
      </el-form-item>
      <el-form-item label="风格">
        <el-select v-model="query.styleCode" clearable placeholder="全部" style="width: 140px">
          <el-option
            v-for="option in styleOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="占用状态">
        <el-select v-model="query.usedFlag" clearable placeholder="全部" style="width: 120px">
          <el-option
            v-for="option in getInternalTestResourceUsedFlagOptions()"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="mode === 'ypat'" label="地区">
        <el-cascader
          v-model="queryRegionPath"
          :options="regionCascaderOptions"
          :props="regionCascaderProps"
          clearable
          filterable
          placeholder="省 / 市 / 区"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      v-if="mode === 'ypat' && activeMediaType === InternalTestMediaType.VIDEO.value"
      title="当前约拍仅支持图片资源"
      type="info"
      :closable="false"
      show-icon
      class="video-tip"
    />

    <el-table
      v-if="mode === 'work'"
      ref="workTableRef"
      v-loading="loading"
      :data="workGroups"
      row-key="groupNo"
      border
      stripe
      class="work-table"
      empty-text="暂无可用作品组"
      @select="handleWorkSelect"
    >
      <el-table-column type="selection" label="" width="48" :selectable="isWorkSelectable" />
      <el-table-column label="预览" width="100" align="center">
        <template #default="{ row }">
          <el-image
            v-if="previewResource(row)?.mediaType === InternalTestMediaType.IMAGE.value && previewResource(row)?.url"
            :src="previewResource(row)?.url"
            fit="cover"
            class="resource-preview"
          />
          <video
            v-else-if="previewResource(row)?.url"
            :src="previewResource(row)?.url"
            class="resource-preview"
            preload="metadata"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="作品组" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ workGroupLabel(row) }}</template>
      </el-table-column>
      <el-table-column prop="groupNo" label="组编号" min-width="160" show-overflow-tooltip />
      <el-table-column label="资源数" width="90" align="center">
        <template #default="{ row }">{{ row.resources?.length || 0 }}</template>
      </el-table-column>
      <el-table-column label="占用状态" width="100" align="center">
        <template #default="{ row }">{{ row.usedFlag === 1 ? '已占用' : '未占用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-table
      v-else
      ref="ypatTableRef"
      v-loading="loading"
      :data="ypatResources"
      row-key="id"
      :select-on-indeterminate="false"
      border
      stripe
      empty-text="暂无可用约拍资源"
      @select="handleYpatSelect"
      @select-all="handleYpatSelectAll"
    >
      <el-table-column type="selection" width="48" :selectable="isYpatSelectable" />
      <el-table-column label="预览" width="100" align="center">
        <template #default="{ row }">
          <el-image v-if="row.mediaType === 'image' && row.url" :src="row.url" fit="cover" class="resource-preview" />
          <video v-else-if="row.url" :src="row.url" class="resource-preview" preload="metadata" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="风格" width="110" show-overflow-tooltip>
        <template #default="{ row }">{{ styleText(row.styleCode) }}</template>
      </el-table-column>
      <el-table-column label="地区" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ [row.province, row.city, row.area].filter(Boolean).join(' / ') || '-' }}</template>
      </el-table-column>
      <el-table-column label="占用状态" width="100" align="center">
        <template #default="{ row }">{{ row.usedFlag === 1 ? '已占用' : '未占用' }}</template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <el-pagination
        :current-page="currentPage"
        :page-size="query.size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <template #footer>
      <div class="picker-footer">
        <span>已选 {{ selectedCount }} 项</span>
        <div class="footer-actions">
          <el-button @click="cancel">取消</el-button>
          <el-button type="primary" @click="confirm">确认</el-button>
        </div>
      </div>
    </template>
  </el-dialog>

  <el-dialog
    v-model="detailVisible"
    title="作品组详情"
    width="min(760px, calc(100vw - 32px))"
    append-to-body
  >
    <div class="detail-grid">
      <div v-for="resource in detailGroup?.resources || []" :key="resource.id || resource.url" class="detail-item">
        <el-image
          v-if="resource.mediaType === InternalTestMediaType.IMAGE.value && resource.url"
          :src="resource.url"
          fit="cover"
          class="detail-media"
          :preview-src-list="[resource.url]"
          preview-teleported
        />
        <video v-else-if="resource.url" :src="resource.url" class="detail-media" controls preload="metadata" />
        <div class="detail-title" :title="resource.title || resource.url">{{ resource.title || resource.url || '-' }}</div>
      </div>
    </div>
    <el-empty v-if="!detailGroup?.resources?.length" description="暂无资源" />
  </el-dialog>
</template>

<style scoped>
.picker-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 0 8px;
}

.video-tip {
  margin-bottom: 12px;
}

.resource-preview {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  object-fit: cover;
  vertical-align: middle;
}

.work-table :deep(.el-table__header .el-checkbox) {
  visibility: hidden;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  overflow-x: auto;
}

.picker-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.detail-item {
  min-width: 0;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
}

.detail-media {
  display: block;
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}

.detail-title {
  padding: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
