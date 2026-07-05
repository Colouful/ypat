<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import MemberActionDialog from './MemberActionDialog.vue'
import { getMemberUsers, type MemberUserQuery } from '@/api/modules/member'
import type { MemberUser } from '@/api/types'

type ActionType = 'grant' | 'extend' | 'cancel'

const query = reactive<MemberUserQuery & { expireRange: string[] }>({
  mobile: '',
  nickname: '',
  memberStatus: '',
  expireRange: [],
  page: 0,
  size: 10,
})
const list = ref<MemberUser[]>([])
const total = ref(0)
const loading = ref(false)
const actionVisible = ref(false)
const action = ref<ActionType>('grant')
const current = ref<MemberUser | null>(null)
const currentPage = computed(() => (query.page ?? 0) + 1)
const statusOptions = [{ label: '有效', value: 'ACTIVE' }, { label: '已过期', value: 'EXPIRED' }]

async function fetchList() {
  loading.value = true
  try {
    const res = await getMemberUsers(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}
function search() { query.page = 0; fetchList() }
function reset() {
  query.mobile = ''
  query.nickname = ''
  query.memberStatus = ''
  query.expireRange = []
  query.page = 0
  fetchList()
}
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
function openAction(row: MemberUser, type: ActionType) {
  current.value = row
  action.value = type
  actionVisible.value = true
}
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="手机号"><el-input v-model="query.mobile" clearable placeholder="请输入"/></el-form-item>
        <el-form-item label="昵称"><el-input v-model="query.nickname" clearable placeholder="请输入"/></el-form-item>
        <el-form-item label="会员状态"><el-select v-model="query.memberStatus" clearable placeholder="全部"><el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="到期时间"><el-date-picker v-model="query.expireRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD"/></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="userId" label="用户ID" width="100" align="center"/>
      <el-table-column prop="mobile" label="手机号" min-width="130"/>
      <el-table-column prop="nickname" label="昵称" min-width="130"/>
      <el-table-column prop="levelCode" label="等级" width="100" align="center"/>
      <el-table-column label="状态" width="100" align="center"><template #default="{row}"><el-tag :type="row.memberStatus === 'ACTIVE' ? 'success' : 'info'" size="small">{{ row.memberStatus === 'ACTIVE' ? '有效' : '已过期' }}</el-tag></template></el-table-column>
      <el-table-column prop="expireAt" label="到期时间" min-width="160"/>
      <el-table-column label="操作" width="210" align="center" fixed="right">
        <template #default="{row}">
          <el-button type="success" link size="small" @click="openAction(row as MemberUser, 'grant')">开通</el-button>
          <el-button type="primary" link size="small" @click="openAction(row as MemberUser, 'extend')">延期</el-button>
          <el-button type="danger" link size="small" @click="openAction(row as MemberUser, 'cancel')">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange"/></div>
    <MemberActionDialog v-model:visible="actionVisible" :action="action" :user="current" @success="fetchList"/>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
