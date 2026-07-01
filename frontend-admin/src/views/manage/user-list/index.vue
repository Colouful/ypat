<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/common/StatusTag.vue'
import UserAuditDialog from './UserAuditDialog.vue'
import { getUserList, auditUser, type OauthQo, type UserListQuery } from '@/api/modules/user'
import { getUserStatusOptions, AuditFlag } from '@/constants/enums'

// 查询参数
const queryParams = reactive<UserListQuery>({
  status: '',
  page: 0,
  size: 10,
})

// 表格数据
const tableData = ref<OauthQo[]>([])
const total = ref(0)
const loading = ref(false)

// 审核弹窗
const auditDialogVisible = ref(false)
const auditLoading = ref(false)
const currentUser = ref<OauthQo | null>(null)

// 状态选项
const statusOptions = getUserStatusOptions()

// 1-based 当前页（Element Plus pagination 使用 1-based，后端使用 0-based）
const currentPage = computed(() => (queryParams.page ?? 0) + 1)

/** 查询列表 */
async function fetchList(): Promise<void> {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    tableData.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

/** 查询按钮 */
function handleSearch(): void {
  queryParams.page = 0
  fetchList()
}

/** 重置按钮 */
function handleReset(): void {
  queryParams.status = ''
  queryParams.page = 0
  fetchList()
}

/** 分页变化 */
function handlePageChange(page: number): void {
  // Element Plus pagination 是 1-based，后端是 0-based
  queryParams.page = page - 1
  fetchList()
}

/** 每页条数变化 */
function handleSizeChange(size: number): void {
  queryParams.size = size
  queryParams.page = 0
  fetchList()
}

/** 打开审核弹窗 */
async function handleAudit(row: OauthQo): Promise<void> {
  currentUser.value = row
  auditDialogVisible.value = true
}

/** 审核操作（通过/不通过） */
async function handleAuditAction(flag: string): Promise<void> {
  if (!currentUser.value) return

  const actionText = flag === AuditFlag.PASS ? '通过' : '拒绝'

  try {
    await ElMessageBox.confirm(
      `确定要${actionText}该用户的实名认证申请吗？`,
      '审核确认',
      {
        confirmButtonText: `确认${actionText}`,
        cancelButtonText: '取消',
        type: flag === AuditFlag.PASS ? 'success' : 'warning',
      },
    )

    auditLoading.value = true
    await auditUser(currentUser.value.userid, flag)
    ElMessage.success(`审核${actionText}成功`)

    auditDialogVisible.value = false
    // 刷新列表
    fetchList()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      // 非用户取消的错误已在拦截器处理
    }
  } finally {
    auditLoading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="user-list-page">
    <!-- 查询区 -->
    <div class="search-bar">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="opt in statusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="handleSearch">
            查询
          </el-button>
          <el-button :icon="'Refresh'" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="userid" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="certcode" label="证件号码" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleAudit(row as unknown as OauthQo)"
            >
              审核
            </el-button>
          </template>
        </el-table-column>

        <!-- 空状态 -->
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
      </el-table>

      <!-- 分页 -->
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
    </div>

    <!-- 审核弹窗 -->
    <UserAuditDialog
      v-model:visible="auditDialogVisible"
      :user="currentUser"
      :loading="auditLoading"
      @audit="handleAuditAction"
    />
  </div>
</template>

<style scoped lang="scss">
.user-list-page {
  padding: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
