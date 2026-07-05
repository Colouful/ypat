<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { getMemberLogs, type MemberLogQuery } from '@/api/modules/member'
import type { MemberOperationLog } from '@/api/types'

const query = reactive<MemberLogQuery>({ userId: undefined, operatorId: undefined, actionType: '', page: 0, size: 10 })
const list = ref<MemberOperationLog[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)
const actionOptions = [
  { label: '支付开通', value: 'PAY_GRANT' },
  { label: '后台开通', value: 'ADMIN_GRANT' },
  { label: '后台延期', value: 'ADMIN_EXTEND' },
  { label: '后台取消', value: 'ADMIN_CANCEL' },
]

async function fetchList() {
  loading.value = true
  try {
    const res = await getMemberLogs(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}
function search() { query.page = 0; fetchList() }
function reset() { query.userId = undefined; query.operatorId = undefined; query.actionType = ''; query.page = 0; fetchList() }
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
function actionText(value: string) {
  return actionOptions.find((o) => o.value === value)?.label || value
}
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="用户ID"><el-input-number v-model="query.userId" :min="1" controls-position="right"/></el-form-item>
        <el-form-item label="操作人ID"><el-input-number v-model="query.operatorId" :min="1" controls-position="right"/></el-form-item>
        <el-form-item label="动作"><el-select v-model="query.actionType" clearable placeholder="全部"><el-option v-for="o in actionOptions" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center"/>
      <el-table-column prop="userId" label="用户ID" width="100" align="center"/>
      <el-table-column prop="operatorId" label="操作人ID" width="110" align="center"/>
      <el-table-column label="动作" width="120" align="center"><template #default="{row}">{{ actionText(row.actionType) }}</template></el-table-column>
      <el-table-column prop="reason" label="原因" min-width="160"/>
      <el-table-column prop="beforeValue" label="变更前" min-width="160"/>
      <el-table-column prop="afterValue" label="变更后" min-width="160"/>
      <el-table-column prop="sourceOrderNo" label="来源订单" min-width="180"/>
      <el-table-column prop="createdAt" label="操作时间" min-width="160"/>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange"/></div>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
