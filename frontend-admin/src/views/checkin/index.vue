<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCheckinRecords,
  getCheckinRule,
  saveCheckinRule,
  type CheckinRecordQuery,
} from '@/api/modules/checkin'
import type { CheckinRecord, CheckinRule } from '@/api/types'

const ruleLoading = ref(false)
const recordsLoading = ref(false)
const saving = ref(false)
const records = ref<CheckinRecord[]>([])
const total = ref(0)
const currentPage = computed(() => (query.page ?? 0) + 1)

const rule = reactive<CheckinRule>({
  enabled: '1',
  rewardPpd: 1,
  confirmTitle: '每日签到',
  confirmContent: '签到成功可获得 1 拍豆',
})

const query = reactive<CheckinRecordQuery>({
  userid: undefined,
  mobile: '',
  dateFrom: '',
  dateTo: '',
  page: 0,
  size: 10,
})

async function loadRule() {
  ruleLoading.value = true
  try {
    const res = await getCheckinRule()
    if (res.data) {
      Object.assign(rule, res.data)
    }
  } finally {
    ruleLoading.value = false
  }
}

async function submitRule() {
  if (rule.rewardPpd < 0) {
    ElMessage.warning('奖励拍豆数不能小于 0')
    return
  }

  saving.value = true
  try {
    const res = await saveCheckinRule({
      enabled: rule.enabled,
      rewardPpd: rule.rewardPpd,
      confirmTitle: rule.confirmTitle,
      confirmContent: rule.confirmContent,
    })
    if (res.data) {
      Object.assign(rule, res.data)
    }
    ElMessage.success('签到规则已保存')
  } finally {
    saving.value = false
  }
}

async function fetchRecords() {
  recordsLoading.value = true
  try {
    const res = await getCheckinRecords(query)
    const pageData = res.data ?? { content: [], totalElements: 0 }
    records.value = pageData.content ?? []
    total.value = pageData.totalElements ?? 0
  } finally {
    recordsLoading.value = false
  }
}

function search() {
  query.page = 0
  fetchRecords()
}

function reset() {
  Object.assign(query, {
    userid: undefined,
    mobile: '',
    dateFrom: '',
    dateTo: '',
    page: 0,
    size: 10,
  })
  fetchRecords()
}

function pageChange(page: number) {
  query.page = page - 1
  fetchRecords()
}

function sizeChange(size: number) {
  query.size = size
  query.page = 0
  fetchRecords()
}

onMounted(() => {
  loadRule()
  fetchRecords()
})
</script>

<template>
  <div class="checkin-page">
    <section class="panel" v-loading="ruleLoading">
      <div class="panel-title">签到规则</div>
      <el-form :model="rule" label-width="120px" class="rule-form">
        <el-form-item label="启用状态">
          <el-switch
            v-model="rule.enabled"
            active-value="1"
            inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        <el-form-item label="每日奖励拍豆">
          <el-input-number v-model="rule.rewardPpd" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="弹窗标题">
          <el-input
            v-model="rule.confirmTitle"
            maxlength="60"
            show-word-limit
            placeholder="请输入签到成功弹窗标题"
          />
        </el-form-item>
        <el-form-item label="弹窗内容">
          <el-input
            v-model="rule.confirmContent"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请输入签到成功弹窗内容"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submitRule">保存</el-button>
          <el-button @click="loadRule">刷新</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="panel">
      <div class="panel-title">签到记录</div>
      <div class="search-bar">
        <el-form :inline="true" :model="query" @submit.prevent>
          <el-form-item label="用户ID">
            <el-input-number v-model="query.userid" :min="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="query.mobile" clearable placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="query.dateFrom"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择开始日期"
            />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="query.dateTo"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择结束日期"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search">查询</el-button>
            <el-button @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table v-loading="recordsLoading" :data="records" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="userid" label="用户ID" width="100" align="center" />
        <el-table-column prop="nickname" label="昵称" min-width="120">
          <template #default="{ row }">{{ row.nickname || '-' }}</template>
        </el-table-column>
        <el-table-column prop="mobile" label="手机号" min-width="130" align="center">
          <template #default="{ row }">{{ row.mobile || '-' }}</template>
        </el-table-column>
        <el-table-column prop="checkinDate" label="签到日期" min-width="120" align="center" />
        <el-table-column prop="rewardPpd" label="奖励拍豆" width="100" align="center" />
        <el-table-column prop="recordId" label="流水ID" min-width="120" align="center">
          <template #default="{ row }">{{ row.recordId ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
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
    </section>
  </div>
</template>

<style scoped lang="scss">
.checkin-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.panel {
  padding: $spacing-lg;
  background: #fff;
  border: 1px solid $border-light;
  border-radius: $radius-base;
}

.panel-title {
  margin-bottom: $spacing-lg;
  color: $text-primary;
  font-size: 18px;
  font-weight: 600;
}

.rule-form {
  max-width: 720px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
