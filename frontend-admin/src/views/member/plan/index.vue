<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import PlanEditDialog from './PlanEditDialog.vue'
import { getMemberPlans, type MemberPlanQuery } from '@/api/modules/member'
import type { MemberPlan } from '@/api/types'

const query = reactive<MemberPlanQuery>({ name: '', status: '', page: 0, size: 10 })
const list = ref<MemberPlan[]>([])
const total = ref(0)
const loading = ref(false)
const editVisible = ref(false)
const current = ref<MemberPlan | null>(null)
const currentPage = computed(() => (query.page ?? 0) + 1)
const statusOptions = [{ label: '启用', value: '1' }, { label: '停用', value: '0' }]

function fenText(value?: number) {
  return value == null ? '-' : `¥${(value / 100).toFixed(2)}`
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getMemberPlans(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}
function search() { query.page = 0; fetchList() }
function reset() { query.name = ''; query.status = ''; query.page = 0; fetchList() }
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
function openEdit(row?: MemberPlan) { current.value = row || null; editVisible.value = true }
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="套餐名称"><el-input v-model="query.name" clearable placeholder="请输入"/></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" clearable placeholder="全部"><el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button><el-button type="success" @click="openEdit()">新增</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center"/>
      <el-table-column prop="name" label="名称" min-width="130"/>
      <el-table-column prop="durationDays" label="天数" width="90" align="center"/>
      <el-table-column label="售价" width="110" align="center"><template #default="{row}">{{ fenText(row.priceFen) }}</template></el-table-column>
      <el-table-column label="原价" width="110" align="center"><template #default="{row}">{{ fenText(row.originPriceFen) }}</template></el-table-column>
      <el-table-column prop="giftPpd" label="赠送拍拍豆" width="120" align="center"/>
      <el-table-column prop="levelCode" label="等级" width="100" align="center"/>
      <el-table-column label="推荐" width="90" align="center"><template #default="{row}"><el-tag :type="row.recommended === '1' ? 'warning' : 'info'" size="small">{{ row.recommended === '1' ? '是' : '否' }}</el-tag></template></el-table-column>
      <el-table-column label="状态" width="90" align="center"><template #default="{row}"><el-tag :type="row.status === '1' ? 'success' : 'info'" size="small">{{ row.status === '1' ? '启用' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column prop="sortNo" label="排序" width="80" align="center"/>
      <el-table-column prop="updatedAt" label="更新时间" min-width="160"/>
      <el-table-column label="操作" width="90" align="center" fixed="right"><template #default="{row}"><el-button type="primary" link size="small" @click="openEdit(row as MemberPlan)">编辑</el-button></template></el-table-column>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange"/></div>
    <PlanEditDialog v-model:visible="editVisible" :data="current" @success="fetchList"/>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
