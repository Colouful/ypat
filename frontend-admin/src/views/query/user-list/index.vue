<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getUserList, type UserListQuery, type OauthQo } from '@/api/modules/user'
import StatusTag from '@/components/common/StatusTag.vue'
import { getGenderOptions, getUserStatusOptions } from '@/constants/enums'

const query = reactive<UserListQuery>({
  status: '',
  nickname: '',
  mobile: '',
  gender: '',
  regisdate: '',
  page: 0,
  size: 10,
})
const list = ref<OauthQo[]>([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const currentUser = ref<OauthQo | null>(null)
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
function reset() {
  query.id = undefined
  query.status = ''
  query.nickname = ''
  query.mobile = ''
  query.gender = ''
  query.regisdate = ''
  query.page = 0
  fetchList()
}
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
function getUserId(row?: OauthQo | null): number | undefined {
  return row?.id ?? row?.userid
}
function formatEmpty(value?: string | number | null): string | number {
  return value === 0 || value ? value : '-'
}
function getAreaText(row?: OauthQo | null): string {
  return [row?.province, row?.city, row?.area].filter(Boolean).join(' / ') || '-'
}
function getAvatar(row: OauthQo): string {
  return row.imgpath || row.avatarurl || ''
}
function getFlagText(value?: string): string {
  if (value === '1' || value === '2') return '是'
  if (value === '0' || value === '3') return '否'
  return '-'
}
function getStatusText(row: OauthQo): string {
  return row.statusTxt || '-'
}
function openDetail(row: OauthQo): void {
  currentUser.value = row
  detailVisible.value = true
}
onMounted(fetchList)
</script>

<template>
  <div class="query-user-page">
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="用户ID">
          <el-input v-model.number="query.id" clearable placeholder="请输入ID" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="query.nickname" clearable placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="query.mobile" clearable placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 160px">
            <el-option
              v-for="option in getUserStatusOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="query.gender" clearable placeholder="全部" style="width: 120px">
            <el-option
              v-for="option in getGenderOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="注册日期">
          <el-date-picker
            v-model="query.regisdate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="search">查询</el-button>
          <el-button :icon="'Refresh'" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="list" border stripe row-key="id" style="width: 100%">
      <el-table-column label="ID" width="90" align="center" fixed="left">
        <template #default="{ row }">{{ formatEmpty(getUserId(row as OauthQo)) }}</template>
      </el-table-column>
      <el-table-column label="头像" width="80" align="center">
        <template #default="{ row }">
          <el-avatar :size="36" :src="getAvatar(row as OauthQo)">
            {{ row.nickname?.slice(0, 1) || '用' }}
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ formatEmpty(row.nickname) }}</template>
      </el-table-column>
      <el-table-column prop="mobile" label="手机号" min-width="140">
        <template #default="{ row }">{{ formatEmpty(row.mobile) }}</template>
      </el-table-column>
      <el-table-column prop="genderTxt" label="性别" width="90" align="center">
        <template #default="{ row }">{{ formatEmpty(row.genderTxt) }}</template>
      </el-table-column>
      <el-table-column prop="professTxt" label="职业" min-width="110" show-overflow-tooltip>
        <template #default="{ row }">{{ formatEmpty(row.professTxt) }}</template>
      </el-table-column>
      <el-table-column label="地区" min-width="190" show-overflow-tooltip>
        <template #default="{ row }">{{ getAreaText(row as OauthQo) }}</template>
      </el-table-column>
      <el-table-column prop="name" label="实名姓名" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ formatEmpty(row.name) }}</template>
      </el-table-column>
      <el-table-column prop="certcode" label="证件号码" min-width="190" show-overflow-tooltip>
        <template #default="{ row }">{{ formatEmpty(row.certcode) }}</template>
      </el-table-column>
      <el-table-column prop="ppd" label="拍拍豆" width="100" align="center">
        <template #default="{ row }">{{ formatEmpty(row.ppd) }}</template>
      </el-table-column>
      <el-table-column prop="pubtimes" label="发布数" width="100" align="center">
        <template #default="{ row }">{{ formatEmpty(row.pubtimes) }}</template>
      </el-table-column>
      <el-table-column prop="rectimes" label="预约数" width="100" align="center">
        <template #default="{ row }">{{ formatEmpty(row.rectimes) }}</template>
      </el-table-column>
      <el-table-column prop="coltimes" label="收藏数" width="100" align="center">
        <template #default="{ row }">{{ formatEmpty(row.coltimes) }}</template>
      </el-table-column>
      <el-table-column prop="channelTxt" label="注册渠道" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ formatEmpty(row.channelTxt || row.channel) }}</template>
      </el-table-column>
      <el-table-column prop="regisdate" label="注册时间" min-width="170" show-overflow-tooltip>
        <template #default="{ row }">{{ formatEmpty(row.regisdate) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <StatusTag v-if="row.status" :status="row.status" />
          <span v-else>{{ getStatusText(row as OauthQo) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openDetail(row as OauthQo)">
            详情
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无数据" />
      </template>
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

    <el-drawer v-model="detailVisible" title="用户详情" size="720px">
      <div v-if="currentUser" class="detail-content">
        <div class="detail-header">
          <el-avatar :size="64" :src="getAvatar(currentUser)">
            {{ currentUser.nickname?.slice(0, 1) || '用' }}
          </el-avatar>
          <div>
            <div class="detail-name">{{ formatEmpty(currentUser.nickname) }}</div>
            <div class="detail-sub">ID：{{ formatEmpty(getUserId(currentUser)) }}</div>
          </div>
        </div>

        <el-descriptions title="基础信息" :column="2" border>
          <el-descriptions-item label="手机号">{{ formatEmpty(currentUser.mobile) }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ formatEmpty(currentUser.genderTxt) }}</el-descriptions-item>
          <el-descriptions-item label="职业">{{ formatEmpty(currentUser.professTxt) }}</el-descriptions-item>
          <el-descriptions-item label="地区">{{ getAreaText(currentUser) }}</el-descriptions-item>
          <el-descriptions-item label="生日">{{ formatEmpty(currentUser.birthday) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <StatusTag v-if="currentUser.status" :status="currentUser.status" />
            <span v-else>{{ getStatusText(currentUser) }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-descriptions title="实名与信用" :column="2" border>
          <el-descriptions-item label="实名姓名">{{ formatEmpty(currentUser.name) }}</el-descriptions-item>
          <el-descriptions-item label="证件号码">{{ formatEmpty(currentUser.certcode) }}</el-descriptions-item>
          <el-descriptions-item label="实名认证">{{ getFlagText(currentUser.realnameflag) }}</el-descriptions-item>
          <el-descriptions-item label="信用认证">{{ getFlagText(currentUser.creditflag) }}</el-descriptions-item>
        </el-descriptions>

        <el-descriptions title="账号与行为" :column="2" border>
          <el-descriptions-item label="拍拍豆">{{ formatEmpty(currentUser.ppd) }}</el-descriptions-item>
          <el-descriptions-item label="推荐人手机号">{{ formatEmpty(currentUser.recmobile) }}</el-descriptions-item>
          <el-descriptions-item label="发布次数">{{ formatEmpty(currentUser.pubtimes) }}</el-descriptions-item>
          <el-descriptions-item label="预约次数">{{ formatEmpty(currentUser.rectimes) }}</el-descriptions-item>
          <el-descriptions-item label="收藏次数">{{ formatEmpty(currentUser.coltimes) }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatEmpty(currentUser.regisdate) }}</el-descriptions-item>
          <el-descriptions-item label="注册渠道">{{ formatEmpty(currentUser.channelTxt || currentUser.channel) }}</el-descriptions-item>
          <el-descriptions-item label="内测批次">{{ formatEmpty(currentUser.internalBatchNo) }}</el-descriptions-item>
        </el-descriptions>

        <el-descriptions title="联系方式" :column="2" border>
          <el-descriptions-item label="微信">{{ formatEmpty(currentUser.wx) }}</el-descriptions-item>
          <el-descriptions-item label="QQ">{{ formatEmpty(currentUser.qq) }}</el-descriptions-item>
          <el-descriptions-item label="微博">{{ formatEmpty(currentUser.wb) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped lang="scss">
.query-user-page {
  min-width: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: $spacing-base;
  padding-bottom: $spacing-base;
  border-bottom: 1px solid $border-lighter;
}

.detail-name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.detail-sub {
  margin-top: $spacing-xs;
  color: $text-secondary;
}
</style>
