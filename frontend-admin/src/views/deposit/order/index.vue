<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { getDepositOrders, type DepositOrderQuery } from '@/api/modules/deposit'
import type { DepositOrder } from '@/api/types'

const query = reactive<DepositOrderQuery>({ userId: undefined, status: '', channel: '', outTradeNo: '', page: 0, size: 10 })
const list = ref<DepositOrder[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)
const statusOptions = [
  { label: '待支付', value: 'PENDING' },
  { label: '已支付', value: 'PAID' },
  { label: '已关闭', value: 'CLOSED' },
  { label: '已退款', value: 'REFUNDED' },
]

function fenText(value?: number) { return value == null ? '-' : `¥${(value / 100).toFixed(2)}` }
function statusType(status: string) { return status === 'PAID' ? 'success' : status === 'PENDING' ? 'warning' : 'info' }
function statusText(status: string) { return statusOptions.find((o) => o.value === status)?.label || status }

async function fetchList() {
  loading.value = true
  try {
    const res = await getDepositOrders(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}
function search() { query.page = 0; fetchList() }
function reset() { query.userId = undefined; query.status = ''; query.channel = ''; query.outTradeNo = ''; query.page = 0; fetchList() }
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="用户ID"><el-input-number v-model="query.userId" :min="1" controls-position="right"/></el-form-item>
        <el-form-item label="订单号"><el-input v-model="query.outTradeNo" clearable placeholder="请输入"/></el-form-item>
        <el-form-item label="渠道"><el-select v-model="query.channel" clearable placeholder="全部"><el-option label="小程序" value="MINIAPP"/><el-option label="H5" value="H5"/></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" clearable placeholder="全部"><el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center"/>
      <el-table-column prop="outTradeNo" label="订单号" min-width="190"/>
      <el-table-column prop="userId" label="用户ID" width="100" align="center"/>
      <el-table-column label="金额" width="110" align="center"><template #default="{row}">{{ fenText(row.amountFen) }}</template></el-table-column>
      <el-table-column prop="channel" label="渠道" width="100" align="center"/>
      <el-table-column label="状态" width="100" align="center"><template #default="{row}"><el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column prop="transactionId" label="微信交易号" min-width="180"/>
      <el-table-column prop="paidAt" label="支付时间" min-width="160"/>
      <el-table-column prop="createdAt" label="创建时间" min-width="160"/>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange"/></div>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: $spacing-lg; }
</style>
