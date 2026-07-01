<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getUserList, type UserListQuery, type OauthQo } from '@/api/modules/user'

const query = reactive<UserListQuery>({ status: '', page: 0, size: 10 })
const list = ref<OauthQo[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)

async function fetchList() {
  loading.value = true
  try {
    const res = await getUserList(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally { loading.value = false }
}
function search() { query.page = 0; fetchList() }
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="昵称"><el-input v-model="query.nickname" placeholder="请输入"/></el-form-item>
        <el-form-item label="手机号"><el-input v-model="query.mobile" placeholder="请输入"/></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="userid" label="ID" width="80" align="center"/>
      <el-table-column prop="name" label="姓名" min-width="120"/>
      <el-table-column prop="certcode" label="证件号码" min-width="180"/>
      <el-table-column prop="statusTxt" label="状态" width="120" align="center"/>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange"/></div>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: $spacing-lg; }
</style>
