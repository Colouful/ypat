<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getFeedbackDetail,
  getFeedbackList,
  handleFeedback,
  type FeedbackInfo,
  type FeedbackListQuery,
} from '@/api/modules/feedback'

type HandleStatus = '1' | '2'
type DialogMode = 'detail' | 'handle'

const statusOptions = [
  { label: '待处理', value: '0' },
  { label: '已处理', value: '1' },
  { label: '已忽略', value: '2' },
]

const handleStatusOptions = [
  { label: '已处理', value: '1' },
  { label: '已忽略', value: '2' },
]

const typeOptions = [
  { label: '功能异常', value: 'function' },
  { label: '体验建议', value: 'experience' },
  { label: '账号/资料', value: 'account' },
  { label: '支付/订单', value: 'payment' },
  { label: '内容/用户举报', value: 'content' },
  { label: '其他', value: 'other' },
]

const query = reactive<FeedbackListQuery>({
  page: 0,
  size: 10,
  status: '',
  type: '',
  userId: '',
})

const list = ref<FeedbackInfo[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)
let listRequestSeq = 0

const dialogVisible = ref(false)
const dialogLoading = ref(false)
const submitLoading = ref(false)
const currentFeedback = ref<FeedbackInfo | null>(null)
const dialogMode = ref<DialogMode>('detail')
const handleForm = reactive({
  status: '1' as HandleStatus,
  reason: '',
})
const dialogTitle = computed(() => {
  if (dialogMode.value === 'detail') return '意见反馈详情'
  return handleForm.status === '1' ? '处理反馈' : '忽略反馈'
})

function normalizeStatus(status?: number | string): string {
  if (status === 0 || status === '0' || status === 'pending') return '0'
  if (status === 1 || status === '1' || status === 'handled') return '1'
  if (status === 2 || status === '2' || status === 'ignored' || status === 'rejected') return '2'
  return ''
}

function getStatusLabel(row: FeedbackInfo): string {
  if (row.statusText) return row.statusText
  return statusOptions.find((item) => item.value === normalizeStatus(row.status))?.label || '未知状态'
}

function getStatusTagType(status?: number | string): 'warning' | 'success' | 'info' {
  const normalized = normalizeStatus(status)
  if (normalized === '0') return 'warning'
  if (normalized === '1') return 'success'
  return 'info'
}

function isPending(row: FeedbackInfo): boolean {
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

function getTypeLabel(row: FeedbackInfo): string {
  if (row.typeText) return row.typeText
  return typeOptions.find((item) => item.value === row.type)?.label || getDisplayText(row.type)
}

function getUserText(userId?: number | string, nickname?: string): string {
  const idText = getDisplayText(userId)
  const nicknameText = getDisplayText(nickname)
  return nicknameText === '-' ? idText : `${nicknameText} / ${idText}`
}

function getImageList(row: FeedbackInfo): string[] {
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

function getContentSummary(row: FeedbackInfo): string {
  const content = getDisplayText(row.content)
  return content.length > 48 ? `${content.slice(0, 48)}...` : content
}

function resetHandleForm(status: HandleStatus): void {
  handleForm.status = status
  handleForm.reason = ''
}

async function fetchList(): Promise<void> {
  const requestSeq = ++listRequestSeq
  loading.value = true
  try {
    const res = await getFeedbackList(query)
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
  query.type = ''
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

async function openHandleDialog(row: FeedbackInfo, status: HandleStatus): Promise<void> {
  dialogMode.value = 'handle'
  resetHandleForm(status)
  await openFeedbackDialog(row)
}

async function openDetailDialog(row: FeedbackInfo): Promise<void> {
  dialogMode.value = 'detail'
  await openFeedbackDialog(row)
}

async function openFeedbackDialog(row: FeedbackInfo): Promise<void> {
  currentFeedback.value = row
  dialogVisible.value = true
  dialogLoading.value = true
  try {
    const res = await getFeedbackDetail(row.id)
    currentFeedback.value = { ...row, ...res.data }
  } finally {
    dialogLoading.value = false
  }
}

async function submitHandle(): Promise<void> {
  if (!currentFeedback.value || submitLoading.value) return
  submitLoading.value = true
  try {
    await handleFeedback(currentFeedback.value.id, handleForm.status, handleForm.reason.trim() || undefined)
    ElMessage.success(handleForm.status === '1' ? '反馈处理成功' : '反馈已忽略')
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
  <div class="feedback-page">
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 150px">
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" clearable placeholder="全部" style="width: 170px">
            <el-option
              v-for="option in typeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户 ID">
          <el-input v-model="query.userId" clearable placeholder="请输入用户 ID" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="反馈 ID" width="96" align="center" />
      <el-table-column label="用户" min-width="170" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getUserText((row as FeedbackInfo).userId, (row as FeedbackInfo).userNickname) }}
        </template>
      </el-table-column>
      <el-table-column label="类型" width="130" align="center">
        <template #default="{ row }">
          {{ getTypeLabel(row as FeedbackInfo) }}
        </template>
      </el-table-column>
      <el-table-column label="内容摘要" min-width="260" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getContentSummary(row as FeedbackInfo) }}
        </template>
      </el-table-column>
      <el-table-column label="图片" width="90" align="center">
        <template #default="{ row }">
          {{ getImageList(row as FeedbackInfo).length }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType((row as FeedbackInfo).status)" size="small">
            {{ getStatusLabel(row as FeedbackInfo) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" min-width="168" />
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDetailDialog(row as FeedbackInfo)">
            详情
          </el-button>
          <template v-if="isPending(row as FeedbackInfo)">
            <el-button type="success" link size="small" @click="openHandleDialog(row as FeedbackInfo, '1')">
              处理
            </el-button>
            <el-button type="info" link size="small" @click="openHandleDialog(row as FeedbackInfo, '2')">
              忽略
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <div v-loading="dialogLoading" class="dialog-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">反馈 ID</span>
            <span>{{ getDisplayText(currentFeedback?.id) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">用户</span>
            <span>{{ getUserText(currentFeedback?.userId, currentFeedback?.userNickname) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">反馈类型</span>
            <span>{{ getTypeLabel(currentFeedback || { id: 0 }) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">处理状态</span>
            <el-tag :type="getStatusTagType(currentFeedback?.status)" size="small">
              {{ getStatusLabel(currentFeedback || { id: 0 }) }}
            </el-tag>
          </div>
          <div class="detail-item detail-item--full">
            <span class="detail-label">反馈内容</span>
            <span>{{ getDisplayText(currentFeedback?.content) }}</span>
          </div>
          <div class="detail-item detail-item--full">
            <span class="detail-label">联系方式</span>
            <span>{{ getDisplayText(currentFeedback?.contact) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">提交时间</span>
            <span>{{ getDisplayText(currentFeedback?.createdAt) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">处理时间</span>
            <span>{{ getDisplayText(currentFeedback?.handledAt) }}</span>
          </div>
          <div v-if="currentFeedback?.handleReason" class="detail-item detail-item--full">
            <span class="detail-label">处理备注</span>
            <div class="history-reason">{{ currentFeedback.handleReason }}</div>
          </div>
          <div v-if="getImageList(currentFeedback || { id: 0 }).length" class="detail-item detail-item--full">
            <span class="detail-label">反馈图片</span>
            <div class="image-list">
              <el-image
                v-for="url in getImageList(currentFeedback || { id: 0 })"
                :key="url"
                :src="url"
                :preview-src-list="getImageList(currentFeedback || { id: 0 })"
                preview-teleported
                fit="cover"
                class="feedback-image"
              />
            </div>
          </div>
        </div>

        <el-form v-if="dialogMode === 'handle'" label-width="88px" class="handle-form">
          <el-form-item label="处理结果">
            <el-select v-model="handleForm.status" style="width: 220px">
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
              maxlength="500"
              show-word-limit
              placeholder="请输入处理备注，可留空"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">
          {{ dialogMode === 'detail' ? '关闭' : '取消' }}
        </el-button>
        <el-button v-if="dialogMode === 'handle'" type="primary" :loading="submitLoading" @click="submitHandle">
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

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feedback-image {
  width: 64px;
  height: 64px;
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
