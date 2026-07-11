<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cleanupInternalData,
  generateInternalUsers,
  generateInternalWorks,
  generateInternalYpats,
  getInternalBatches,
  searchInternalUsers,
  type InternalTestBatch,
  type InternalTestGeneratePayload,
  type InternalTestResource,
  type InternalTestResourceGroup,
  type InternalTestUser,
} from '@/api/modules/internal-test'
import {
  Gender,
  InternalTestGenerateAction,
  YpatTarget,
  getGenderOptions,
  getInternalTestGenerateActionOptions,
  getProfessOptions,
  getWorkTagStyleOptions,
  getYpatPatstyleOptions,
  getYpatTargetOptions,
} from '@/constants/enums'
import { formatDate } from '@/utils/format'
import { regionCascaderOptions, toRegionFields, type RegionPath } from '@/utils/region'
import ResourcePickerDialog from './ResourcePickerDialog.vue'

const publishStatusOptions = [
  { label: '待审核', value: '1' },
  { label: '审核通过', value: '2' },
]
const regionCascaderProps = {
  value: 'label',
  label: 'label',
  children: 'children',
  emitPath: true,
  checkStrictly: true,
} as const

const form = reactive({
  actionType: InternalTestGenerateAction.CREATE_USERS.value as string,
  userCount: 5,
  userId: undefined as number | undefined,
  nicknamePrefix: '',
  gender: Gender.FEMALE.value,
  profess: '',
  province: '',
  city: '',
  area: '',
  styleCodes: [] as string[],
  publishStatus: '2',
  patdate: '',
  patslice: '',
  describ: '',
  target: YpatTarget.PHOTOGRAPHER.value,
  wx: '',
  mobile: '',
})

const batches = ref<InternalTestBatch[]>([])
const userOptions = ref<InternalTestUser[]>([])
const resourcePickerVisible = ref(false)
const resourcePickerMode = ref<'work' | 'ypat'>('work')
const selectedWorkGroup = ref<InternalTestResourceGroup>()
const selectedYpatResources = ref<InternalTestResource[]>([])
const patTimeRange = ref<string[]>([])
const userSearching = ref(false)
const batchLoading = ref(false)
const submitting = ref(false)
const cleaningBatchNo = ref('')
const cleanupAllLoading = ref(false)
const batchQuery = reactive({ batchNo: '', page: 0, size: 20 })

const isCreateUsers = computed(() => form.actionType === InternalTestGenerateAction.CREATE_USERS.value)
const isCreateWorks = computed(() => form.actionType === InternalTestGenerateAction.CREATE_WORKS.value)
const isCreateYpats = computed(() => form.actionType === InternalTestGenerateAction.CREATE_YPATS.value)
const styleOptions = computed(() => (isCreateYpats.value ? getYpatPatstyleOptions() : getWorkTagStyleOptions()))
const selectedYpatPreviewResources = computed(() => selectedYpatResources.value
  .filter((resource): resource is InternalTestResource & { url: string } => (
    typeof resource.url === 'string' && resource.url.trim().length > 0
  ))
  .slice(0, 3))
const hiddenSelectedYpatResourceCount = computed(() => Math.max(
  0,
  selectedYpatResources.value.length - selectedYpatPreviewResources.value.length,
))
const validSelectedYpatResourceIds = computed(() => selectedYpatResources.value
  .map((resource) => resource.id)
  .filter((id): id is number => typeof id === 'number' && Number.isInteger(id) && id > 0))
const regionPath = computed<RegionPath>({
  get: () => [form.province, form.city, form.area].filter(Boolean) as RegionPath,
  set: (path) => {
    const fields = toRegionFields(path)
    form.province = fields.province
    form.city = fields.city
    form.area = fields.area
  },
})

async function remoteSearchUsers(keyword: string): Promise<void> {
  userSearching.value = true
  try {
    const res = await searchInternalUsers({ keyword, page: 0, size: 20 })
    userOptions.value = res.data.content || []
  } finally {
    userSearching.value = false
  }
}

function workGroupLabel(group: InternalTestResourceGroup): string {
  return group.groupTitle?.trim()
    || group.resources?.[0]?.title?.trim()
    || '未命名作品组'
}

function openResourcePicker(mode: 'work' | 'ypat'): void {
  resourcePickerMode.value = mode
  resourcePickerVisible.value = true
}

function confirmWorkGroup(group: InternalTestResourceGroup | undefined): void {
  selectedWorkGroup.value = group
}

function confirmYpatResources(resources: InternalTestResource[]): void {
  selectedYpatResources.value = resources
}

async function loadBatches(): Promise<void> {
  batchLoading.value = true
  try {
    const res = await getInternalBatches(batchQuery)
    batches.value = res.data.content || []
  } finally {
    batchLoading.value = false
  }
}

function validateBeforeSubmit(): string | null {
  if (isCreateUsers.value && (!form.userCount || form.userCount < 1 || form.userCount > 50)) {
    return '新增用户数量必须在 1 到 50 之间'
  }
  if (isCreateWorks.value && !form.userId) return '请选择内测用户'
  if (isCreateWorks.value && !selectedWorkGroup.value) return '请选择作品组'
  if (isCreateYpats.value && !form.userId) return '请选择内测用户'
  if (isCreateYpats.value && selectedYpatResources.value.length === 0) return '请选择约拍图片'
  if (
    isCreateYpats.value
    && validSelectedYpatResourceIds.value.length !== selectedYpatResources.value.length
  ) return '约拍图片数据异常，请重新选择'
  if (isCreateYpats.value && !form.patdate) return '请选择约拍日期'
  if (isCreateYpats.value && patTimeRange.value.length !== 2) return '请选择约拍时间段'
  if (isCreateYpats.value && (!form.province || !form.city || !form.area)) return '约拍地点请选择到区县'
  if (isCreateYpats.value && !form.describ.trim()) return '请输入约拍要求'
  if (isCreateYpats.value && !form.wx.trim()) return '请输入微信号'
  if (isCreateYpats.value && !form.mobile.trim()) return '请输入联系电话'
  if (isCreateYpats.value && !/^1\d{10}$/.test(form.mobile.trim())) return '请输入正确的联系电话'
  if ((isCreateWorks.value || isCreateYpats.value) && form.styleCodes.length === 0) return '请选择风格'
  return null
}

function buildPayload(): InternalTestGeneratePayload {
  return {
    actionType: form.actionType,
    userCount: isCreateUsers.value ? form.userCount : undefined,
    userId: form.userId,
    nicknamePrefix: form.nicknamePrefix || undefined,
    gender: form.gender,
    profess: form.profess,
    province: form.province,
    city: form.city,
    area: form.area,
    styleCodes: form.styleCodes,
    styleCode: form.styleCodes[0],
    publishStatus: form.publishStatus,
    groupNos: isCreateWorks.value && selectedWorkGroup.value ? [selectedWorkGroup.value.groupNo] : undefined,
    ypatResourceIds: isCreateYpats.value
      && validSelectedYpatResourceIds.value.length === selectedYpatResources.value.length
      ? validSelectedYpatResourceIds.value
      : undefined,
    patdate: form.patdate,
    patslice: isCreateYpats.value ? patTimeRange.value.join('-') : undefined,
    describ: form.describ,
    target: form.target,
    wx: form.wx,
    mobile: form.mobile,
  }
}

async function submitGenerate(): Promise<void> {
  const message = validateBeforeSubmit()
  if (message) {
    ElMessage.warning(message)
    return
  }
  await ElMessageBox.confirm('确认提交后仅生成内测数据，不会标记为真实用户数据。', '生成确认', {
    type: 'warning',
    confirmButtonText: '确认生成',
    cancelButtonText: '取消',
  })
  submitting.value = true
  try {
    const payload = buildPayload()
    const res = isCreateUsers.value
      ? await generateInternalUsers(payload)
      : isCreateWorks.value
        ? await generateInternalWorks(payload)
        : await generateInternalYpats(payload)
    ElMessage.success(`生成成功，批次号：${res.data.batchNo}`)
    batchQuery.batchNo = res.data.batchNo
    await loadBatches()
  } finally {
    submitting.value = false
  }
}

async function cleanupBatch(batchNo: string): Promise<void> {
  if (!batchNo) return
  try {
    await ElMessageBox.confirm(`确认软清理批次 ${batchNo} 吗？本操作仅影响内测数据。`, '清理确认', {
      type: 'warning',
      confirmButtonText: '确认清理',
      cancelButtonText: '取消',
    })
    cleaningBatchNo.value = batchNo
    await cleanupInternalData({ batchNo })
    ElMessage.success('清理已提交')
    await loadBatches()
  } finally {
    cleaningBatchNo.value = ''
  }
}

async function cleanupAllInternalData(): Promise<void> {
  await ElMessageBox.confirm(
    '确认一键清除全部内测数据吗？用户将禁用，作品和约拍将下架，资源占用会释放；真实数据不会被处理。',
    '高风险操作确认',
    {
      type: 'warning',
      confirmButtonText: '确认清除全部',
      cancelButtonText: '取消',
    },
  )
  cleanupAllLoading.value = true
  try {
    const res = await cleanupInternalData({ cleanupAll: true })
    ElMessage.success(
      `清理完成：用户 ${res.data.userCount}，作品 ${res.data.workCount}，约拍 ${res.data.ypatCount}，释放资源 ${res.data.releasedResourceCount || 0}`,
    )
    batchQuery.batchNo = ''
    await loadBatches()
  } finally {
    cleanupAllLoading.value = false
  }
}

function actionChanged(): void {
  form.styleCodes = []
}

onMounted(() => {
  loadBatches()
})
</script>

<template>
  <div class="internal-generator-page">
    <section class="section-block">
      <div class="section-title">生成配置</div>
      <el-form :model="form" label-width="110px" class="config-form">
        <el-form-item label="生成动作">
          <el-select v-model="form.actionType" style="width: 240px" @change="actionChanged">
            <el-option
              v-for="option in getInternalTestGenerateActionOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isCreateUsers" label="用户数量">
          <el-input-number v-model="form.userCount" :min="1" :max="50" controls-position="right" />
        </el-form-item>
        <el-form-item v-if="!isCreateUsers" label="内测用户">
          <el-select
            v-model="form.userId"
            filterable
            remote
            :remote-method="remoteSearchUsers"
            :loading="userSearching"
            placeholder="搜索用户ID、昵称或手机号"
            style="width: 260px"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.nickname || '-'} / ${user.mobile || '-'} / ${user.id}`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isCreateUsers" label="城市">
          <el-cascader
            v-model="regionPath"
            :options="regionCascaderOptions"
            :props="regionCascaderProps"
            clearable
            filterable
            placeholder="请选择省 / 市 / 区"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item v-if="isCreateYpats" label="地点">
          <el-cascader
            v-model="regionPath"
            :options="regionCascaderOptions"
            :props="regionCascaderProps"
            clearable
            filterable
            placeholder="请选择省 / 市 / 区"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item v-if="isCreateUsers" label="职业">
          <el-select v-model="form.profess" clearable placeholder="请选择职业" style="width: 240px">
            <el-option
              v-for="option in getProfessOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isCreateUsers" label="性别">
          <el-select v-model="form.gender" clearable placeholder="请选择性别" style="width: 240px">
            <el-option
              v-for="option in getGenderOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isCreateUsers" label="昵称前缀">
          <el-input v-model="form.nicknamePrefix" placeholder="可为空" />
        </el-form-item>
        <el-form-item v-if="isCreateWorks || isCreateYpats" label="风格">
          <el-select
            v-model="form.styleCodes"
            multiple
            :multiple-limit="5"
            clearable
            placeholder="请选择风格"
            style="width: 260px"
          >
            <el-option
              v-for="option in styleOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isCreateWorks" label="作品组">
          <div class="resource-selection-control">
            <el-button type="primary" plain @click="openResourcePicker('work')">选择作品组</el-button>
            <div v-if="selectedWorkGroup" class="selected-resource-summary">
              <span class="selection-title">{{ workGroupLabel(selectedWorkGroup) }}</span>
              <span>{{ selectedWorkGroup.resources?.length || 0 }} 个资源</span>
              <el-button type="danger" link @click="selectedWorkGroup = undefined">清空</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="isCreateYpats" label="约拍图片">
          <div class="resource-selection-control">
            <el-button type="primary" plain @click="openResourcePicker('ypat')">选择约拍图片</el-button>
            <div v-if="selectedYpatResources.length" class="selected-resource-summary">
              <span>已选 {{ selectedYpatResources.length }} 张</span>
              <div
                v-if="selectedYpatPreviewResources.length || hiddenSelectedYpatResourceCount > 0"
                class="resource-thumbnails"
              >
                <el-image
                  v-for="resource in selectedYpatPreviewResources"
                  :key="resource.id || resource.url"
                  :src="resource.url"
                  fit="cover"
                  class="resource-thumbnail"
                />
                <span v-if="hiddenSelectedYpatResourceCount > 0">+{{ hiddenSelectedYpatResourceCount }}</span>
              </div>
              <el-button type="danger" link @click="selectedYpatResources = []">清空</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="isCreateYpats" label="日期">
          <el-date-picker v-model="form.patdate" type="date" value-format="YYYY-MM-DD" placeholder="请选择约拍日期" />
        </el-form-item>
        <el-form-item v-if="isCreateYpats" label="约拍时间段">
          <el-time-picker
            v-model="patTimeRange"
            is-range
            value-format="HH:mm"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item v-if="isCreateYpats" label="要求">
          <el-input v-model="form.describ" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item v-if="isCreateYpats" label="微信号">
          <el-input v-model="form.wx" maxlength="40" />
        </el-form-item>
        <el-form-item v-if="isCreateYpats" label="联系电话">
          <el-input v-model="form.mobile" maxlength="11" />
        </el-form-item>
        <el-form-item v-if="isCreateYpats" label="约拍对象">
          <el-select v-model="form.target" style="width: 240px">
            <el-option
              v-for="option in getYpatTargetOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isCreateUsers" label="发布状态">
          <el-select v-model="form.publishStatus" style="width: 240px">
            <el-option
              v-for="option in publishStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitGenerate">生成内测数据</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="section-block">
      <div class="batch-header">
        <div class="section-title">批次结果</div>
        <div class="batch-actions">
          <div class="batch-filter">
            <el-input v-model="batchQuery.batchNo" clearable placeholder="批次号" />
            <el-button :loading="batchLoading" @click="loadBatches">查询</el-button>
          </div>
          <el-button type="danger" plain :loading="cleanupAllLoading" @click="cleanupAllInternalData">
            一键清除全部内测数据
          </el-button>
        </div>
      </div>
      <el-table v-loading="batchLoading" :data="batches" border stripe>
        <el-table-column prop="batchNo" label="批次号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userCount" label="用户数" width="100" align="center" />
        <el-table-column prop="ypatCount" label="约拍数" width="100" align="center" />
        <el-table-column prop="workCount" label="作品数" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="110" align="center" />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatDate(row.createdAt ?? null) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              link
              size="small"
              :loading="cleaningBatchNo === row.batchNo"
              @click="cleanupBatch(row.batchNo)"
            >
              清理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <ResourcePickerDialog
      v-model:visible="resourcePickerVisible"
      :mode="resourcePickerMode"
      :selected-work-group="selectedWorkGroup"
      :selected-ypat-resources="selectedYpatResources"
      @confirm-work="confirmWorkGroup"
      @confirm-ypat="confirmYpatResources"
    ></ResourcePickerDialog>
  </div>
</template>

<style scoped lang="scss">
.internal-generator-page {
  color: $text-primary;
}

.section-block {
  background: $bg-card;
  border-radius: $radius-base;
  box-shadow: $shadow-light;
  padding: $spacing-lg;
  margin-bottom: $spacing-base;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  margin-bottom: $spacing-base;
}

.config-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  column-gap: $spacing-lg;
}

.batch-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-base;
  flex-wrap: wrap;
}

.resource-selection-control,
.selected-resource-summary,
.resource-thumbnails {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.resource-selection-control {
  min-height: 48px;
  width: 100%;
}

.selected-resource-summary {
  min-width: 0;
  color: $text-secondary;
}

.selection-title {
  max-width: 220px;
  overflow: hidden;
  color: $text-primary;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-thumbnail {
  width: 48px;
  height: 48px;
  border-radius: 4px;
}

.batch-filter {
  display: flex;
  gap: $spacing-sm;
  width: 320px;
  max-width: 100%;
}

.batch-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: $spacing-sm;
  flex-wrap: wrap;
}
</style>
