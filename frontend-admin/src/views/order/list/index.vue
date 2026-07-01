<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { getOrderList, type OrderListQuery, type Order } from '@/api/modules/order'
import { getOrderStatusOptions, getOrderTypeOptions } from '@/constants/enums'

const query = reactive<OrderListQuery>({ status: '', type: '', page: 0, size: 10 })
const list = ref<Order[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)

async function fetchList() {
  loading.value = true
  try {
    const res = await getOrderList(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally { loading.value = false }
}
function search() { query.page = 0; fetchList() }
function reset() { query.status = ''; query.type = ''; query.page = 0; fetchList() }
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="订单状态"><el-select v-model="query.status" clearable placeholder="全部"><el-option v-for="o in getOrderStatusOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="订单类型"><el-select v-model="query.type" clearable placeholder="全部"><el-option v-for="o in getOrderTypeOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center"/>
      <el-table-column prop="credate" label="创建时间" min-width="160"/>
      <el-table-column prop="userid" label="用户ID" width="100" align="center"/>
      <el-table-column prop="typeTxt" label="订单类型" min-width="120"/>
      <el-table-column prop="total_fee" label="金额" width="100" align="center"/>
      <el-table-column label="支付状态" width="120" align="center"><template #default="{row}"><StatusTag :status="row.status" type="order"/></template></el-table-column>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange"/></div>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: $spacing-lg; }
</style>
