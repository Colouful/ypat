<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cleanupInternalData,
  generateInternalData,
  getInternalBatches,
  getInternalResources,
  type InternalTestBatch,
  type InternalTestGeneratePayload,
  type InternalTestResource,
} from '@/api/modules/internal-test'
import {
  Gender,
  InternalTestGenerateMode,
  InternalTestMediaType,
  InternalTestResourceStatus,
  InternalTestUsageType,
  getGenderOptions,
  getInternalTestGenerateModeOptions,
  getProfessOptions,
  getYpatPatstyleOptions,
} from '@/constants/enums'

const contentTypeOptions = [
  { label: '约拍', value: 'ypat' },
  { label: '作品', value: 'work' },
  { label: '约拍和作品', value: 'both' },
]
const templateTypeOptions = [
  { label: '发布约拍', value: 'publish_ypat' },
  { label: '约摄影师', value: 'appointment_photographer' },
  { label: '约模特', value: 'appointment_model' },
]
const publishStatusOptions = [
  { label: '待审核', value: '1' },
  { label: '审核通过', value: '2' },
]
const workMediaTabs = [
  { label: '图片', value: InternalTestMediaType.IMAGE.value },
  { label: '视频', value: InternalTestMediaType.VIDEO.value },
]

const form = reactive({
  mode: InternalTestGenerateMode.CREATE_AND_GENERATE.value,
  userCount: 5,
  userIdsText: '',
  nicknamePrefix: '内测用户',
  gender: Gender.FEMALE.value,
  profess: '',
  province: '',
  city: '',
  area: '',
  styleCode: '',
  contentType: 'both',
  templateType: 'publish_ypat',
  publishStatus: '1',
})

const avatarResources = ref<InternalTestResource[]>([])
const ypatResources = ref<InternalTestResource[]>([])
const workResources = ref<InternalTestResource[]>([])
const selectedAvatarResources = ref<InternalTestResource[]>([])
const selectedYpatResources = ref<InternalTestResource[]>([])
const selectedWorkResources = ref<InternalTestResource[]>([])
const batches = ref<InternalTestBatch[]>([])
const activeWorkMediaType = ref(InternalTestMediaType.IMAGE.value)
const resourceLoading = ref(false)
const batchLoading = ref(false)
const submitting = ref(false)
const cleaningBatchNo = ref('')
const batchQuery = reactive({ batchNo: '', page: 0, size: 20 })
let resourceRequestSeq = 0

const isCreateMode = computed(() => form.mode === InternalTestGenerateMode.CREATE_AND_GENERATE.value)
const needsYpat = computed(() => form.contentType === 'ypat' || form.contentType === 'both')
const needsWork = computed(() => form.contentType === 'work' || form.contentType === 'both')

function selectedIds(resources: InternalTestResource[]): number[] {
  return resources.map((item) => item.id).filter((id): id is number => typeof id === 'number')
}

function parseUserIds(): number[] {
  return parseUserIdInput().ids
}

function parseUserIdInput(): { ids: number[]; invalidTokens: string[] } {
  const ids: number[] = []
  const invalidTokens: string[] = []
  for (const raw of form.userIdsText.split(',')) {
    const token = raw.trim()
    if (!token) continue
    const value = Number(token)
    if (Number.isInteger(value) && value > 0) {
      ids.push(value)
    } else {
      invalidTokens.push(token)
    }
  }
  return { ids, invalidTokens }
}

function normalizeTemplateType(value: string): string {
  if (value === 'appointment_photographer') return 'photographer'
  if (value === 'appointment_model') return 'model'
  return value
}

type ResourceLoadScope = 'all' | 'style' | 'work'

async function loadResources(scope: ResourceLoadScope = 'all'): Promise<void> {
  const seq = ++resourceRequestSeq
  resourceLoading.value = true
  try {
    const requests: Array<Promise<void>> = []
    if (scope === 'all') {
      requests.push(
        getInternalResources({
          mediaType: InternalTestMediaType.IMAGE.value,
          usageType: InternalTestUsageType.AVATAR.value,
          status: InternalTestResourceStatus.ENABLED.value,
          page: 0,
          size: 50,
        }).then((res) => {
          if (seq !== resourceRequestSeq) return
          avatarResources.value = res.data.content || []
          selectedAvatarResources.value = []
        }),
      )
    }
    if (scope === 'all' || scope === 'style') {
      requests.push(
        getInternalResources({
          mediaType: InternalTestMediaType.IMAGE.value,
          usageType: InternalTestUsageType.YPAT.value,
          styleCode: form.styleCode,
          status: InternalTestResourceStatus.ENABLED.value,
          page: 0,
          size: 50,
        }).then((res) => {
          if (seq !== resourceRequestSeq) return
          ypatResources.value = res.data.content || []
          selectedYpatResources.value = []
        }),
      )
    }
    requests.push(
      getInternalResources({
        mediaType: activeWorkMediaType.value,
        usageType: InternalTestUsageType.WORK.value,
        styleCode: form.styleCode,
        status: InternalTestResourceStatus.ENABLED.value,
        page: 0,
        size: 50,
      }).then((res) => {
        if (seq !== resourceRequestSeq) return
        workResources.value = res.data.content || []
        selectedWorkResources.value = []
      }),
    )
    await Promise.all(requests)
  } finally {
    if (seq === resourceRequestSeq) {
      resourceLoading.value = false
    }
  }
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
  if (isCreateMode.value && (!form.userCount || form.userCount < 1 || form.userCount > 50)) {
    return '新建用户数量必须在 1 到 50 之间'
  }
  if (!isCreateMode.value) {
    const parsed = parseUserIdInput()
    if (parsed.invalidTokens.length > 0) {
      return `用户 ID 格式错误：${parsed.invalidTokens.join('、')}`
    }
    if (parsed.ids.length === 0) {
      return '请输入至少一个已有内测用户 ID'
    }
  }
  if (!form.city) return '请填写城市'
  if (!form.profess) return '请选择职业'
  if (!form.styleCode) return '请选择风格'
  if (!form.contentType) return '请选择内容类型'
  if (!form.templateType) return '请选择模板类型'
  if (!form.publishStatus) return '请选择发布状态'
  if (selectedAvatarResources.value.length === 0) return '请选择头像资源'
  if (needsYpat.value && selectedYpatResources.value.length === 0) return '请选择约拍资源'
  if (needsWork.value && selectedWorkResources.value.length === 0) return '请选择作品资源'
  return null
}

function buildPayload(): InternalTestGeneratePayload {
  return {
    mode: form.mode,
    userCount: isCreateMode.value ? form.userCount : undefined,
    userIds: isCreateMode.value ? undefined : parseUserIds(),
    nicknamePrefix: form.nicknamePrefix,
    gender: form.gender,
    profess: form.profess,
    province: form.province,
    city: form.city,
    area: form.area,
    styleCode: form.styleCode,
    contentType: form.contentType,
    templateType: normalizeTemplateType(form.templateType),
    publishStatus: form.publishStatus,
    avatarResourceIds: selectedIds(selectedAvatarResources.value),
    ypatResourceIds: selectedIds(selectedYpatResources.value),
    workResourceIds: selectedIds(selectedWorkResources.value),
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
    const res = await generateInternalData(buildPayload())
    ElMessage.success(`生成成功，批次号：${res.data.batchNo}`)
    batchQuery.batchNo = res.data.batchNo
    loadBatches()
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
    loadBatches()
  } finally {
    cleaningBatchNo.value = ''
  }
}

function resourceTitle(row: InternalTestResource): string {
  return row.title || row.url || '-'
}

function styleChanged(): void {
  ypatResources.value = []
  workResources.value = []
  selectedYpatResources.value = []
  selectedWorkResources.value = []
  loadResources('style')
}

function workMediaChanged(): void {
  workResources.value = []
  selectedWorkResources.value = []
  loadResources('work')
}

onMounted(() => {
  loadResources()
  loadBatches()
})
</script>

<template>
  <div class="internal-generator-page">
    <section class="section-block">
      <div class="section-title">生成配置</div>
      <el-form :model="form" label-width="110px" class="config-form">
        <el-form-item label="生成模式">
          <el-select v-model="form.mode" style="width: 240px">
            <el-option
              v-for="option in getInternalTestGenerateModeOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isCreateMode" label="用户数量">
          <el-input-number v-model="form.userCount" :min="1" :max="50" controls-position="right" />
        </el-form-item>
        <el-form-item v-else label="内测用户 ID">
          <el-input v-model="form.userIdsText" placeholder="多个 ID 用英文逗号分隔" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" placeholder="可选" />
        </el-form-item>
        <el-form-item label="地区">
          <el-input v-model="form.area" placeholder="可选" />
        </el-form-item>
        <el-form-item label="职业">
          <el-select v-model="form.profess" placeholder="请选择职业" style="width: 240px">
            <el-option
              v-for="option in getProfessOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" clearable placeholder="请选择性别" style="width: 240px">
            <el-option
              v-for="option in getGenderOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="昵称前缀">
          <el-input v-model="form.nicknamePrefix" placeholder="请输入昵称前缀" />
        </el-form-item>
        <el-form-item label="风格">
          <el-select v-model="form.styleCode" placeholder="请选择风格" style="width: 240px" @change="styleChanged">
            <el-option
              v-for="option in getYpatPatstyleOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容类型">
          <el-select v-model="form.contentType" style="width: 240px">
            <el-option
              v-for="option in contentTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模板类型">
          <el-select v-model="form.templateType" style="width: 240px">
            <el-option
              v-for="option in templateTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发布状态">
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
          <el-button :loading="resourceLoading" @click="() => loadResources()">刷新资源</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="section-block">
      <div class="section-title">资源选择</div>
      <div class="resource-grid">
        <div class="resource-panel">
          <div class="panel-title">头像资源</div>
          <el-table
            v-loading="resourceLoading"
            :data="avatarResources"
            border
            height="260"
            @selection-change="selectedAvatarResources = $event"
          >
            <el-table-column type="selection" width="44" />
            <el-table-column label="标题" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ resourceTitle(row as InternalTestResource) }}</template>
            </el-table-column>
            <el-table-column prop="city" label="城市" width="90" show-overflow-tooltip />
          </el-table>
        </div>

        <div class="resource-panel">
          <div class="panel-title">约拍资源</div>
          <el-table
            v-loading="resourceLoading"
            :data="ypatResources"
            border
            height="260"
            @selection-change="selectedYpatResources = $event"
          >
            <el-table-column type="selection" width="44" />
            <el-table-column label="标题" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ resourceTitle(row as InternalTestResource) }}</template>
            </el-table-column>
            <el-table-column prop="city" label="城市" width="90" show-overflow-tooltip />
          </el-table>
        </div>

        <div class="resource-panel">
          <div class="panel-title work-title">
            <span>作品资源</span>
            <el-radio-group v-model="activeWorkMediaType" size="small" @change="workMediaChanged">
              <el-radio-button
                v-for="tab in workMediaTabs"
                :key="tab.value"
                :label="tab.value"
              >
                {{ tab.label }}
              </el-radio-button>
            </el-radio-group>
          </div>
          <el-table
            v-loading="resourceLoading"
            :data="workResources"
            border
            height="260"
            @selection-change="selectedWorkResources = $event"
          >
            <el-table-column type="selection" width="44" />
            <el-table-column label="标题" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ resourceTitle(row as InternalTestResource) }}</template>
            </el-table-column>
            <el-table-column prop="city" label="城市" width="90" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </section>

    <section class="section-block">
      <div class="batch-header">
        <div class="section-title">批次结果</div>
        <div class="batch-filter">
          <el-input v-model="batchQuery.batchNo" clearable placeholder="批次号" />
          <el-button :loading="batchLoading" @click="loadBatches">查询</el-button>
        </div>
      </div>
      <el-table v-loading="batchLoading" :data="batches" border stripe>
        <el-table-column prop="batchNo" label="批次号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userCount" label="用户数" width="100" align="center" />
        <el-table-column prop="ypatCount" label="约拍数" width="100" align="center" />
        <el-table-column prop="workCount" label="作品数" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="110" align="center" />
        <el-table-column prop="createdAt" label="创建时间" min-width="160" show-overflow-tooltip />
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

.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: $spacing-base;
}

.resource-panel {
  min-width: 0;
}

.panel-title,
.batch-header,
.work-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-base;
  flex-wrap: wrap;
}

.panel-title {
  min-height: 32px;
  margin-bottom: $spacing-sm;
  font-weight: 600;
}

.batch-filter {
  display: flex;
  gap: $spacing-sm;
  width: 320px;
  max-width: 100%;
}
</style>
