<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getPubEventList, type PubEventListQuery, type PubEvent } from '@/api/modules/pubevent'

const query = reactive<PubEventListQuery>({ dateStrStart: '', dateStrEnd: '', eventKey: '', page: 0, size: 10 })
const list = ref<PubEvent[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)

async function fetchList() {
  loading.value = true
  try {
    const res = await getPubEventList(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally { loading.value = false }
}
function search() { query.page = 0; fetchList() }
function reset() { query.dateStrStart = ''; query.dateStrEnd = ''; query.eventKey = ''; query.page = 0; fetchList() }
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="开始日期"><el-date-picker v-model="query.dateStrStart" type="date" value-format="YYYY-MM-DD" placeholder="请选择"/></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="query.dateStrEnd" type="date" value-format="YYYY-MM-DD" placeholder="请选择"/></el-form-item>
        <el-form-item label="事件Key"><el-input v-model="query.eventKey" placeholder="请输入"/></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center"/>
      <el-table-column prop="dateStr" label="日期" min-width="120"/>
      <el-table-column prop="eventKey" label="事件Key" min-width="120"/>
      <el-table-column prop="eventKeyTxt" label="事件描述" min-width="180"/>
      <el-table-column prop="msgTimes" label="消息次数" width="100" align="center"/>
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
