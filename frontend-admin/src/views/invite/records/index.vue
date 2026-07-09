<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  getInviteRecords,
  type InviteRecord,
  type InviteRecordQuery,
} from '@/api/modules/invite'

const query = reactive<InviteRecordQuery>({
  inviterUserid: undefined,
  inviteeUserid: undefined,
  inviteCode: '',
  source: '',
  page: 0,
  size: 10,
})
const list = ref<InviteRecord[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)
const sourceOptions = [
  { label: '微信分享', value: 'share' },
  { label: '二维码', value: 'qr' },
  { label: '手动填写', value: 'manual' },
  { label: '手机号推荐', value: 'recmobile' },
  { label: '注册链路', value: 'register' },
]

async function fetchList() {
  loading.value = true
  try {
    const res = await getInviteRecords(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 0
  fetchList()
}

function reset() {
  Object.assign(query, {
    inviterUserid: undefined,
    inviteeUserid: undefined,
    inviteCode: '',
    source: '',
    page: 0,
  })
  fetchList()
}

function pageChange(page: number) {
  query.page = page - 1
  fetchList()
}

function sizeChange(size: number) {
  query.size = size
  query.page = 0
  fetchList()
}

function sourceText(source?: string) {
  const option = sourceOptions.find((item) => item.value === source)
  return option?.label || source || '-'
}

onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="邀请人ID">
          <el-input-number v-model="query.inviterUserid" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="被邀请人ID">
          <el-input-number v-model="query.inviteeUserid" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="邀请码">
          <el-input v-model="query.inviteCode" clearable placeholder="请输入邀请码" />
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="query.source" clearable placeholder="全部">
            <el-option v-for="item in sourceOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="inviterUserid" label="邀请人ID" width="110" align="center" />
      <el-table-column prop="inviteeUserid" label="被邀请人ID" width="120" align="center" />
      <el-table-column prop="inviteeNickname" label="被邀请人昵称" min-width="140">
        <template #default="{ row }">{{ row.inviteeNickname || '-' }}</template>
      </el-table-column>
      <el-table-column prop="inviteeMobileMask" label="脱敏手机号" width="130" align="center">
        <template #default="{ row }">{{ row.inviteeMobileMask || '-' }}</template>
      </el-table-column>
      <el-table-column prop="inviteCode" label="邀请码" min-width="130">
        <template #default="{ row }">{{ row.inviteCode || '-' }}</template>
      </el-table-column>
      <el-table-column label="来源" width="120" align="center">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ sourceText(row.source) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="奖励拍拍豆" width="120" align="center">
        <template #default="{ row }">{{ row.rewardPpd ?? 0 }}</template>
      </el-table-column>
      <el-table-column prop="credate" label="创建时间" min-width="170" />
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        :current-page="currentPage"
        :page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total,sizes,prev,pager,next,jumper"
        background
        @current-change="pageChange"
        @size-change="sizeChange"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
