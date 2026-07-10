<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMessList, type MessListQuery, type MessInfo } from '@/api/modules/mess'

const router = useRouter()
const query = reactive<MessListQuery>({ ypatid: undefined, sendperid: undefined, recperid: undefined, page: 0, size: 10 })
const list = ref<MessInfo[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)
const typeMap: Record<string, string> = { '1': '约拍申请', '4': '已查看联系方式' }

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
function typeText(type?: string) { return type ? typeMap[type] || type : '-' }
function readText(flag?: string) { return flag === '1' ? '已读' : flag === '0' ? '未读' : '-' }
function readType(flag?: string) { return flag === '0' ? 'warning' : 'info' }
function openPushLog(row: MessInfo) {
  router.push({
    path: '/manage/query/message-push-log',
    query: {
      messageId: row.id,
      ypatid: row.ypatid,
      sendperid: row.sendperid,
      recperid: row.recperid,
    },
  })
}
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
      <el-table-column prop="id" label="消息ID" width="90" align="center"/>
      <el-table-column prop="ypatid" label="约拍ID" width="100" align="center"/>
      <el-table-column prop="sendperid" label="发送者ID" width="100" align="center"/>
      <el-table-column prop="recperid" label="接收者ID" width="100" align="center"/>
      <el-table-column label="类型" width="130" align="center"><template #default="{row}">{{ typeText(row.type) }}</template></el-table-column>
      <el-table-column label="阅读" width="90" align="center"><template #default="{row}"><el-tag :type="readType(row.messviewflag)" size="small">{{ readText(row.messviewflag) }}</el-tag></template></el-table-column>
      <el-table-column prop="nickname" label="昵称" min-width="120"/>
      <el-table-column prop="credate" label="创建时间" min-width="160"/>
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip/>
      <el-table-column label="操作" width="110" fixed="right" align="center">
        <template #default="{ row }"><el-button type="primary" link @click="openPushLog(row as MessInfo)">推送记录</el-button></template>
      </el-table-column>
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
