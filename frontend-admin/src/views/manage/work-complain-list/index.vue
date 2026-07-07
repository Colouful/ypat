<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getWorkComplainDetail,
  getWorkComplainList,
  handleWorkComplain,
  type WorkComplainInfo,
  type WorkComplainListQuery,
} from '@/api/modules/work-complain'

type HandleStatus = '1' | '2'
type DialogMode = 'detail' | 'handle'

const statusOptions = [
  { label: '待处理', value: '0' },
  { label: '已处理', value: '1' },
  { label: '已驳回', value: '2' },
]

const handleStatusOptions = [
  { label: '处理并通过', value: '1' },
  { label: '驳回投诉', value: '2' },
]

const query = reactive<WorkComplainListQuery>({
  page: 0,
  size: 10,
  status: '',
  workId: '',
  userId: '',
})

const list = ref<WorkComplainInfo[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)
let listRequestSeq = 0

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const submitLoading = ref(false)
const currentComplaint = ref<WorkComplainInfo | null>(null)
const dialogMode = ref<DialogMode>('detail')
const handleForm = reactive({
  status: '1' as HandleStatus,
  reason: '',
  offlineWork: true,
})
const dialogTitle = computed(() => {
  if (dialogMode.value === 'detail') return '投诉详情'
  return handleForm.status === '1' ? '处理投诉' : '驳回投诉'
})

function normalizeStatus(status?: number | string): string {
  if (status === 0 || status === '0' || status === 'pending') return '0'
  if (status === 1 || status === '1' || status === 'handled') return '1'
  if (status === 2 || status === '2' || status === 'rejected') return '2'
  return ''
}

function getStatusLabel(row: WorkComplainInfo): string {
  if (row.statusText) return row.statusText
  return statusOptions.find((item) => item.value === normalizeStatus(row.status))?.label || '未知状态'
}

function getStatusTagType(status?: number | string): 'warning' | 'success' | 'info' {
  const normalized = normalizeStatus(status)
  if (normalized === '0') return 'warning'
  if (normalized === '1') return 'success'
  return 'info'
}

function isPending(row: WorkComplainInfo): boolean {
  return normalizeStatus(row.status) === '0'
}

function getDisplayText(...values: Array<string | number | undefined | null>): string {
  for (const value of values) {
    if (value == null) continue
    const text = String(value).trim()
    if (text) return text
  }
  return '-'
}

function getComplaintReason(row: WorkComplainInfo): string {
  return getDisplayText(row.reason, row.content)
}

function getEvidenceList(row: WorkComplainInfo): string[] {
  if (Array.isArray(row.pics)) {
    return row.pics.filter((item): item is string => typeof item === 'string' && Boolean(item.trim()))
  }
  if (typeof row.pics === 'string' && row.pics.trim()) {
    return row.pics
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
  }
  return []
}

function getUserText(userId?: number | string, nickname?: string): string {
  const idText = getDisplayText(userId)
  const nicknameText = getDisplayText(nickname)
  return nicknameText === '-' ? idText : `${nicknameText} / ${idText}`
}

function resetHandleForm(status: HandleStatus): void {
  handleForm.status = status
  handleForm.reason = ''
  handleForm.offlineWork = status === '1'
}

async function fetchList(): Promise<void> {
  const requestSeq = ++listRequestSeq
  loading.value = true
  try {
    const res = await getWorkComplainList(query)
    if (requestSeq === listRequestSeq) {
      list.value = res.data.content || []
      total.value = res.data.totalElements || 0
    }
  } finally {
    if (requestSeq === listRequestSeq) {
      loading.value = false
    }
  }
}

function search(): void {
  query.page = 0
  fetchList()
}

function reset(): void {
  query.status = ''
  query.workId = ''
  query.userId = ''
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

function handleStatusChange(value: HandleStatus): void {
  handleForm.status = value
  handleForm.offlineWork = value === '1'
}

async function openHandleDialog(row: WorkComplainInfo, status: HandleStatus): Promise<void> {
  dialogMode.value = 'handle'
  resetHandleForm(status)
  await openComplaintDialog(row)
}

async function openDetailDialog(row: WorkComplainInfo): Promise<void> {
  dialogMode.value = 'detail'
  await openComplaintDialog(row)
}

async function openComplaintDialog(row: WorkComplainInfo): Promise<void> {
  currentComplaint.value = row
  dialogVisible.value = true
  dialogLoading.value = true
  try {
    const res = await getWorkComplainDetail(row.id)
    currentComplaint.value = { ...row, ...res.data }
  } finally {
    dialogLoading.value = false
  }
}

async function submitHandle(): Promise<void> {
  if (!currentComplaint.value || submitLoading.value) return

  submitLoading.value = true
  try {
    await handleWorkComplain(
      currentComplaint.value.id,
      handleForm.status,
      handleForm.reason.trim() || undefined,
      handleForm.offlineWork,
    )
    ElMessage.success(handleForm.status === '1' ? '投诉处理成功' : '投诉驳回成功')
    dialogVisible.value = false
    await fetchList()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="work-complain-page">
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="状态">
          <el-select
            v-model="query.status"
            clearable
            placeholder="全部"
            style="width: 160px"
          >
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作品 ID">
          <el-input v-model="query.workId" clearable placeholder="请输入作品 ID" />
        </el-form-item>
        <el-form-item label="投诉人 ID">
          <el-input v-model="query.userId" clearable placeholder="请输入投诉人 ID" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="投诉 ID" width="96" align="center" />
      <el-table-column prop="workId" label="作品 ID" width="96" align="center" />
      <el-table-column label="投诉人" min-width="170" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getUserText((row as WorkComplainInfo).userId, (row as WorkComplainInfo).userNickname) }}
        </template>
      </el-table-column>
      <el-table-column label="被投诉用户" min-width="170" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getUserText((row as WorkComplainInfo).targetUserId, (row as WorkComplainInfo).targetNickname) }}
        </template>
      </el-table-column>
      <el-table-column label="原因" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getComplaintReason(row as WorkComplainInfo) }}
        </template>
      </el-table-column>
      <el-table-column label="联系方式" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getDisplayText((row as WorkComplainInfo).contact) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType((row as WorkComplainInfo).status)" size="small">
            {{ getStatusLabel(row as WorkComplainInfo) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="投诉时间" min-width="168" />
      <el-table-column label="作品描述" min-width="240" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getDisplayText((row as WorkComplainInfo).workDescription) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            size="small"
            @click="openDetailDialog(row as WorkComplainInfo)"
          >
            详情
          </el-button>
          <template v-if="isPending(row as WorkComplainInfo)">
            <el-button
              type="success"
              link
              size="small"
              @click="openHandleDialog(row as WorkComplainInfo, '1')"
            >
              处理
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="openHandleDialog(row as WorkComplainInfo, '2')"
            >
              驳回
            </el-button>
          </template>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="640px"
      destroy-on-close
    >
      <div v-loading="dialogLoading" class="dialog-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">投诉 ID</span>
            <span>{{ getDisplayText(currentComplaint?.id) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">作品 ID</span>
            <span>{{ getDisplayText(currentComplaint?.workId) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">投诉人</span>
            <span>{{ getUserText(currentComplaint?.userId, currentComplaint?.userNickname) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">被投诉用户</span>
            <span>{{ getUserText(currentComplaint?.targetUserId, currentComplaint?.targetNickname) }}</span>
          </div>
          <div class="detail-item detail-item--full">
            <span class="detail-label">投诉原因</span>
            <span>{{ getComplaintReason(currentComplaint || { id: 0 }) }}</span>
          </div>
          <div class="detail-item detail-item--full">
            <span class="detail-label">联系方式</span>
            <span>{{ getDisplayText(currentComplaint?.contact) }}</span>
          </div>
          <div class="detail-item detail-item--full">
            <span class="detail-label">作品描述</span>
            <span>{{ getDisplayText(currentComplaint?.workDescription) }}</span>
          </div>
          <div v-if="getEvidenceList(currentComplaint || { id: 0 }).length" class="detail-item detail-item--full">
            <span class="detail-label">投诉凭证</span>
            <div class="evidence-list">
              <el-image
                v-for="url in getEvidenceList(currentComplaint || { id: 0 })"
                :key="url"
                :src="url"
                :preview-src-list="getEvidenceList(currentComplaint || { id: 0 })"
                preview-teleported
                fit="cover"
                class="evidence"
              />
            </div>
          </div>
        </div>

        <el-form v-if="dialogMode === 'handle'" label-width="88px" class="handle-form">
          <el-form-item label="处理结果">
            <el-select
              v-model="handleForm.status"
              style="width: 220px"
              @change="handleStatusChange"
            >
              <el-option
                v-for="option in handleStatusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="处理备注">
            <el-input
              v-model="handleForm.reason"
              type="textarea"
              :rows="4"
              maxlength="200"
              show-word-limit
              placeholder="请输入处理备注，可留空"
            />
          </el-form-item>
          <el-form-item label="下架作品">
            <el-switch
              v-model="handleForm.offlineWork"
              inline-prompt
              active-text="下架"
              inactive-text="保留"
            />
          </el-form-item>
          <el-form-item
            v-if="currentComplaint?.handleReason"
            label="历史备注"
          >
            <div class="history-reason">{{ currentComplaint.handleReason }}</div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">
          {{ dialogMode === 'detail' ? '关闭' : '取消' }}
        </el-button>
        <el-button
          v-if="dialogMode === 'handle'"
          type="primary"
          :loading="submitLoading"
          @click="submitHandle"
        >
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}

.dialog-content {
  min-height: 120px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  color: #374151;
  word-break: break-all;
}

.detail-item--full {
  grid-column: 1 / -1;
}

.detail-label {
  color: #6b7280;
  font-size: 12px;
}

.evidence-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.evidence {
  width: 56px;
  height: 56px;
  border-radius: 6px;
}

.handle-form {
  margin-top: 8px;
}

.history-reason {
  line-height: 1.6;
  color: #475569;
  white-space: pre-wrap;
}
</style>
