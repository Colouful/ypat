<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createInternalResource,
  getInternalResources,
  updateInternalResource,
  updateInternalResourceStatus,
  type InternalTestResource,
  type InternalTestResourceQuery,
} from '@/api/modules/internal-test'
import {
  InternalTestMediaType,
  InternalTestResourceStatus,
  getInternalTestResourceStatusOptions,
  getInternalTestUsageTypeOptions,
  getProfessOptions,
  getYpatPatstyleOptions,
} from '@/constants/enums'

type ResourceForm = Omit<InternalTestResource, 'id'> & { id?: number }

const mediaTabs = [
  { label: '图片', value: InternalTestMediaType.IMAGE.value },
  { label: '视频', value: InternalTestMediaType.VIDEO.value },
]

const activeMediaType = ref(InternalTestMediaType.IMAGE.value)
const query = reactive<InternalTestResourceQuery>({
  mediaType: activeMediaType.value,
  usageType: '',
  styleCode: '',
  profession: '',
  city: '',
  status: '',
  keyword: '',
  page: 0,
  size: 10,
})
const list = ref<InternalTestResource[]>([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增内测资源')
const saving = ref(false)
const formRef = ref<FormInstance>()
const currentPage = computed(() => (query.page ?? 0) + 1)
let requestSeq = 0

const form = reactive<ResourceForm>(createEmptyForm())

const rules: FormRules<ResourceForm> = {
  usageType: [{ required: true, message: '请选择用途', trigger: 'change' }],
  url: [{ required: true, message: '请输入资源 URL', trigger: 'blur' }],
}

function createEmptyForm(): ResourceForm {
  return {
    id: undefined,
    mediaType: activeMediaType.value,
    usageType: '',
    styleCode: '',
    url: '',
    title: '',
    description: '',
    profession: '',
    city: '',
    status: InternalTestResourceStatus.ENABLED.value,
    sortNo: 0,
    remark: '',
  }
}

function assignForm(data: ResourceForm): void {
  Object.assign(form, data)
}

async function fetchList(): Promise<void> {
  const seq = ++requestSeq
  loading.value = true
  query.mediaType = activeMediaType.value
  try {
    const res = await getInternalResources(query)
    if (seq === requestSeq) {
      list.value = res.data.content || []
      total.value = res.data.totalElements || 0
    }
  } finally {
    if (seq === requestSeq) {
      loading.value = false
    }
  }
}

function handleTabChange(): void {
  query.page = 0
  fetchList()
}

function search(): void {
  query.page = 0
  fetchList()
}

function reset(): void {
  query.usageType = ''
  query.styleCode = ''
  query.profession = ''
  query.city = ''
  query.status = ''
  query.keyword = ''
  query.page = 0
  fetchList()
}

function pageChange(page: number): void {
  query.page = page - 1
  fetchList()
}

function sizeChange(size: number): void {
  query.size = size
  query.page = 0
  fetchList()
}

function openCreate(): void {
  dialogTitle.value = '新增内测资源'
  assignForm(createEmptyForm())
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function openEdit(row: InternalTestResource): void {
  dialogTitle.value = '编辑内测资源'
  assignForm({
    id: row.id,
    mediaType: row.mediaType || activeMediaType.value,
    usageType: row.usageType || '',
    styleCode: row.styleCode || '',
    url: row.url || '',
    title: row.title || '',
    description: row.description || '',
    profession: row.profession || '',
    city: row.city || '',
    status: row.status || InternalTestResourceStatus.ENABLED.value,
    sortNo: row.sortNo ?? 0,
    remark: row.remark || '',
  })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function saveResource(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload: InternalTestResource = {
      ...form,
      mediaType: activeMediaType.value,
      status: form.status || InternalTestResourceStatus.ENABLED.value,
      sortNo: form.sortNo ?? 0,
    }
    if (payload.id) {
      await updateInternalResource(payload)
      ElMessage.success('资源更新成功')
    } else {
      await createInternalResource(payload)
      ElMessage.success('资源创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

async function changeStatus(row: InternalTestResource, status: string): Promise<void> {
  if (!row.id) return
  const text = status === InternalTestResourceStatus.ENABLED.value ? '启用' : '停用'
  await ElMessageBox.confirm(`确定要${text}该内测资源吗？`, '提示', { type: 'warning' })
  await updateInternalResourceStatus(row.id, status)
  ElMessage.success(`${text}成功`)
  fetchList()
}

function getOptionLabel(options: Array<{ label: string; value: string | number }>, value?: string): string {
  return options.find((item) => item.value === value)?.label || value || '-'
}

function mediaText(value?: string): string {
  return getOptionLabel(mediaTabs, value)
}

function usageText(value?: string): string {
  return getOptionLabel(getInternalTestUsageTypeOptions(), value)
}

function statusInfo(value?: string) {
  const statuses = Object.values(InternalTestResourceStatus)
  return statuses.find((item) => item.value === value) || { name: '未知', type: 'info' as const }
}

function styleText(value?: string): string {
  return getOptionLabel(getYpatPatstyleOptions(), value)
}

function professText(value?: string): string {
  return getOptionLabel(getProfessOptions(), value)
}

onMounted(fetchList)
</script>

<template>
  <div class="internal-resource-page">
    <el-tabs v-model="activeMediaType" @tab-change="handleTabChange">
      <el-tab-pane
        v-for="tab in mediaTabs"
        :key="tab.value"
        :label="tab.label"
        :name="tab.value"
      />
    </el-tabs>

    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="用途">
          <el-select v-model="query.usageType" clearable placeholder="全部" style="width: 140px">
            <el-option
              v-for="option in getInternalTestUsageTypeOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="风格">
          <el-select v-model="query.styleCode" clearable placeholder="全部" style="width: 140px">
            <el-option
              v-for="option in getYpatPatstyleOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职业">
          <el-select v-model="query.profession" clearable placeholder="全部" style="width: 140px">
            <el-option
              v-for="option in getProfessOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="query.city" clearable placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
            <el-option
              v-for="option in getInternalTestResourceStatusOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="标题、URL、备注" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="success" @click="openCreate">新增</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="预览" width="110" align="center">
        <template #default="{ row }">
          <el-image
            v-if="activeMediaType === InternalTestMediaType.IMAGE.value && row.url"
            :src="row.url"
            fit="cover"
            class="resource-preview"
            :preview-src-list="[row.url]"
            preview-teleported
          />
          <el-tag v-else-if="row.url" type="info" effect="plain">视频</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
      <el-table-column label="媒体" width="90" align="center">
        <template #default="{ row }">{{ mediaText(row.mediaType) }}</template>
      </el-table-column>
      <el-table-column label="用途" width="100" align="center">
        <template #default="{ row }">{{ usageText(row.usageType) }}</template>
      </el-table-column>
      <el-table-column label="风格" width="100" align="center">
        <template #default="{ row }">{{ styleText(row.styleCode) }}</template>
      </el-table-column>
      <el-table-column label="职业" width="100" align="center">
        <template #default="{ row }">{{ professText(row.profession) }}</template>
      </el-table-column>
      <el-table-column prop="city" label="城市" width="110" show-overflow-tooltip />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusInfo(row.status).type" size="small">
            {{ statusInfo(row.status).name }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortNo" label="排序" width="90" align="center" />
      <el-table-column prop="createdAt" label="创建时间" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openEdit(row as InternalTestResource)">
            编辑
          </el-button>
          <el-button
            v-if="row.status !== InternalTestResourceStatus.ENABLED.value"
            type="success"
            link
            size="small"
            @click="changeStatus(row as InternalTestResource, InternalTestResourceStatus.ENABLED.value)"
          >
            启用
          </el-button>
          <el-button
            v-else
            type="info"
            link
            size="small"
            @click="changeStatus(row as InternalTestResource, InternalTestResourceStatus.DISABLED.value)"
          >
            停用
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        :current-page="currentPage"
        :page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="pageChange"
        @size-change="sizeChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="媒体类型">
          <el-input :model-value="mediaText(activeMediaType)" disabled />
        </el-form-item>
        <el-form-item label="用途" prop="usageType">
          <el-select v-model="form.usageType" placeholder="请选择用途" style="width: 100%">
            <el-option
              v-for="option in getInternalTestUsageTypeOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资源 URL" prop="url">
          <el-input v-model="form.url" placeholder="请输入已上传资源 URL" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="风格">
          <el-select v-model="form.styleCode" clearable placeholder="请选择风格" style="width: 100%">
            <el-option
              v-for="option in getYpatPatstyleOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职业">
          <el-select v-model="form.profession" clearable placeholder="请选择职业" style="width: 100%">
            <el-option
              v-for="option in getProfessOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option
              v-for="option in getInternalTestResourceStatusOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveResource">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.internal-resource-page {
  color: $text-primary;
}

.resource-preview {
  width: 56px;
  height: 56px;
  border-radius: $radius-sm;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
