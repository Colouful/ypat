<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessList, type MessListQuery, type MessInfo } from '@/api/modules/mess'

const query = reactive<MessListQuery>({ ypatid: undefined, sendperid: undefined, recperid: undefined, page: 0, size: 10 })
const list = ref<MessInfo[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)

async function fetchList() {
  if (!query.ypatid && !query.sendperid && !query.recperid) { ElMessage.warning('请至少输入一个查询条件'); return }
  loading.value = true
  try {
    const res = await getMessList(query)
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
        <el-form-item label="约拍ID"><el-input-number v-model="query.ypatid" :min="0" controls-position="right"/></el-form-item>
        <el-form-item label="发送者ID"><el-input-number v-model="query.sendperid" :min="0" controls-position="right"/></el-form-item>
        <el-form-item label="接收者ID"><el-input-number v-model="query.recperid" :min="0" controls-position="right"/></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="ypatid" label="约拍ID" width="100" align="center"/>
      <el-table-column prop="sendperid" label="发送者ID" width="100" align="center"/>
      <el-table-column prop="recperid" label="接收者ID" width="100" align="center"/>
      <el-table-column prop="nickname" label="昵称" min-width="120"/>
      <el-table-column prop="credate" label="创建时间" min-width="160"/>
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip/>
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
