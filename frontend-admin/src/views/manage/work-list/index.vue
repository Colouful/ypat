<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import WorkAuditDialog from './WorkAuditDialog.vue'
import WorkDetailDrawer from './WorkDetailDrawer.vue'
import { getWorkList, offlineWork, type WorkAdminInfo, type WorkListQuery } from '@/api/modules/work'
import { WorkStatus, getWorkStatusOptions } from '@/constants/enums'

const mediaTypeOptions = [
  { label: '图片', value: '1' },
  { label: '视频', value: '2' },
]

const queryParams = reactive<WorkListQuery>({
  status: '',
  nickname: '',
  mobile: '',
  city: '',
  mediaType: '',
  page: 0,
  size: 10,
})

const tableData = ref<WorkAdminInfo[]>([])
const total = ref(0)
const loading = ref(false)
let listRequestSeq = 0

const detailVisible = ref(false)
const detailId = ref<number>()

const auditVisible = ref(false)
const currentWork = ref<WorkAdminInfo | null>(null)
const offlineIds = ref<Set<number>>(new Set())

const currentPage = computed(() => (queryParams.page ?? 0) + 1)

function getWorkStatusInfo(status?: string) {
  const statuses = Object.values(WorkStatus)
  return statuses.find((item) => item.value === status) || { name: '未知', type: 'info' as const }
}

function getMediaTypeLabel(value?: string): string {
  if (value === '1') return '图片'
  if (value === '2') return '视频'
  return value || '-'
}

function getRecordString(record: Record<string, unknown> | undefined, key: string): string {
  const value = record?.[key]
  return typeof value === 'string' && value.trim() ? value : ''
}

function getDisplayText(...values: Array<string | undefined>): string {
  for (const value of values) {
    if (typeof value === 'string' && value.trim()) {
      return value
    }
  }
  return '-'
}

function getAuthorName(row: WorkAdminInfo): string {
  return getDisplayText(row.nickname, getRecordString(row.user, 'nickname'))
}

function getMobile(row: WorkAdminInfo): string {
  return getDisplayText(row.mobile, getRecordString(row.user, 'mobile'))
}

function getCity(row: WorkAdminInfo): string {
  return getDisplayText(row.city, getRecordString(row.user, 'city'))
}

async function fetchList(): Promise<void> {
  const requestSeq = ++listRequestSeq
  loading.value = true
  try {
    const res = await getWorkList(queryParams)
    if (requestSeq === listRequestSeq) {
      tableData.value = res.data.content || []
      total.value = res.data.totalElements || 0
    }
  } finally {
    if (requestSeq === listRequestSeq) {
      loading.value = false
    }
  }
}

function handleSearch(): void {
  queryParams.page = 0
  fetchList()
}

function handleReset(): void {
  queryParams.status = ''
  queryParams.nickname = ''
  queryParams.mobile = ''
  queryParams.city = ''
  queryParams.mediaType = ''
  queryParams.page = 0
  fetchList()
}

function handlePageChange(page: number): void {
  queryParams.page = page - 1
  fetchList()
}

function handleSizeChange(size: number): void {
  queryParams.size = size
  queryParams.page = 0
  fetchList()
}

function openDetail(row: WorkAdminInfo): void {
  detailId.value = row.id
  detailVisible.value = true
}

function openAudit(row: WorkAdminInfo): void {
  currentWork.value = row
  auditVisible.value = true
}

async function handleOffline(row: WorkAdminInfo): Promise<void> {
  if (offlineIds.value.has(row.id)) return

  offlineIds.value = new Set(offlineIds.value).add(row.id)
  try {
    await ElMessageBox.confirm(`确定要下架作品 #${row.id} 吗？`, '下架确认', {
      type: 'warning',
      confirmButtonText: '确认下架',
      cancelButtonText: '取消',
    })
    await offlineWork(row.id, '后台下架')
    ElMessage.success('作品下架成功')
    fetchList()
  } finally {
    const nextOfflineIds = new Set(offlineIds.value)
    nextOfflineIds.delete(row.id)
    offlineIds.value = nextOfflineIds
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="work-list-page">
    <div class="search-bar">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="option in getWorkStatusOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="queryParams.nickname" placeholder="请输入作者昵称" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.mobile" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="queryParams.city" placeholder="请输入城市" clearable />
        </el-form-item>
        <el-form-item label="媒体">
          <el-select
            v-model="queryParams.mediaType"
            placeholder="全部"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="option in mediaTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="封面" width="100" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.coverUrl"
            :src="row.coverUrl"
            fit="cover"
            style="width: 56px; height: 56px; border-radius: 4px"
            :preview-src-list="[row.coverUrl]"
            preview-teleported
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column label="作者" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getAuthorName(row as WorkAdminInfo) }}
        </template>
      </el-table-column>
      <el-table-column label="手机号" min-width="140">
        <template #default="{ row }">
          {{ getMobile(row as WorkAdminInfo) }}
        </template>
      </el-table-column>
      <el-table-column label="城市" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getCity(row as WorkAdminInfo) }}
        </template>
      </el-table-column>
      <el-table-column label="媒体" width="100" align="center">
        <template #default="{ row }">
          {{ getMediaTypeLabel((row as WorkAdminInfo).mediaType) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getWorkStatusInfo((row as WorkAdminInfo).status).type" size="small">
            {{ getWorkStatusInfo((row as WorkAdminInfo).status).name }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="170" align="center" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDetail(row as WorkAdminInfo)">
            详情
          </el-button>
          <el-button type="success" link size="small" @click="openAudit(row as WorkAdminInfo)">
            审核
          </el-button>
          <el-button
            v-if="(row as WorkAdminInfo).status === WorkStatus.APPROVED.value"
            type="danger"
            link
            size="small"
            :loading="offlineIds.has((row as WorkAdminInfo).id)"
            :disabled="offlineIds.has((row as WorkAdminInfo).id)"
            @click="handleOffline(row as WorkAdminInfo)"
          >
            下架
          </el-button>
        </template>
      </el-table-column>

      <template #empty>
        <el-empty description="暂无数据" />
      </template>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        :current-page="currentPage"
        :page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <WorkDetailDrawer v-model:visible="detailVisible" :id="detailId" />
    <WorkAuditDialog v-model:visible="auditVisible" :data="currentWork" @success="fetchList" />
  </div>
</template>

<style scoped lang="scss">
.work-list-page {
  padding: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
