<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/common/StatusTag.vue'
import ProductEditDialog from './ProductEditDialog.vue'
import { getProductList, upDownProduct, type Product, type ProductListQuery } from '@/api/modules/product'
import { getProductStatusOptions, ProductStatus } from '@/constants/enums'

const query = reactive<ProductListQuery>({ name: '', status: '', page: 0, size: 10 })
const list = ref<Product[]>([])
const total = ref(0)
const loading = ref(false)
const editVisible = ref(false)
const current = ref<Product | null>(null)
const currentPage = computed(() => (query.page ?? 0) + 1)

async function fetchList() {
  loading.value = true
  try {
    const res = await getProductList(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally { loading.value = false }
}
function search() { query.page = 0; fetchList() }
function reset() { query.name = ''; query.status = ''; query.page = 0; fetchList() }
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
function openEdit(row?: Product) { current.value = row || null; editVisible.value = true }
async function doUpDown(row: Product, status: string) {
  const text = status === ProductStatus.UP.value ? '上架' : '下架'
  await ElMessageBox.confirm(`确定要${text}该产品吗？`, '提示', { type: 'warning' })
  await upDownProduct(row.id, status)
  ElMessage.success(`${text}成功`)
  fetchList()
}
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="产品名称"><el-input v-model="query.name" placeholder="请输入"/></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" clearable placeholder="全部"><el-option v-for="o in getProductStatusOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button><el-button type="success" @click="openEdit()">新增</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center"/>
      <el-table-column prop="name" label="名称" min-width="150"/>
      <el-table-column prop="currval" label="当前值" width="100" align="center"/>
      <el-table-column prop="oldval" label="原值" width="100" align="center"/>
      <el-table-column label="状态" width="120" align="center"><template #default="{row}"><StatusTag :status="row.status" type="product"/></template></el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{row}">
          <el-button type="primary" link size="small" @click="openEdit(row as unknown as Product)">修改</el-button>
          <el-button type="success" link size="small" v-if="row.status !== ProductStatus.UP.value" @click="doUpDown(row as unknown as Product, ProductStatus.UP.value)">上架</el-button>
          <el-button type="info" link size="small" v-else @click="doUpDown(row as unknown as Product, ProductStatus.DOWN.value)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange"/></div>
    <ProductEditDialog v-model:visible="editVisible" :data="current" @success="fetchList"/>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: $spacing-lg; }
</style>
