<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  getMessagePushLogs,
  getMessagePushLogStats,
  type MessagePushLogQuery,
} from '@/api/modules/message-push-log'
import type { MessagePushLog, MessagePushLogStats } from '@/api/types'

const route = useRoute()
const query = reactive<MessagePushLogQuery>({
  eventType: '',
  businessType: '',
  success: '',
  messageId: undefined,
  ypatid: undefined,
  sendperid: undefined,
  recperid: undefined,
  touserOpenid: '',
  dateStart: '',
  dateEnd: '',
  page: 0,
  size: 10,
})
const list = ref<MessagePushLog[]>([])
const total = ref(0)
const loading = ref(false)
const stats = ref<MessagePushLogStats>({})
const currentPage = computed(() => (query.page ?? 0) + 1)

const eventOptions = [
  { label: '站内消息创建', value: 'IN_APP_CREATED' },
  { label: '微信订阅发送', value: 'WECHAT_SUBSCRIBE_SENT' },
]
const businessOptions = [
  { label: '约拍申请', value: '1' },
  { label: '实名认证审核', value: '2' },
  { label: '发布审核', value: '3' },
  { label: '已查看联系方式', value: '4' },
  { label: '订单通知', value: '5' },
]
const successOptions = [
  { label: '成功', value: '1' },
  { label: '失败', value: '0' },
]

function initFromRoute() {
  const routeQuery = route.query
  query.messageId = numberQuery(routeQuery.messageId)
  query.ypatid = numberQuery(routeQuery.ypatid)
  query.sendperid = numberQuery(routeQuery.sendperid)
  query.recperid = numberQuery(routeQuery.recperid)
}

function numberQuery(value: unknown): number | undefined {
  const raw = Array.isArray(value) ? value[0] : value
  const parsed = Number(raw)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined
}

function eventText(value?: string) {
  return eventOptions.find((item) => item.value === value)?.label || value || '-'
}

function businessText(value?: string) {
  return businessOptions.find((item) => item.value === value)?.label || value || '-'
}

function successType(value?: string) {
  return value === '1' ? 'success' : value === '0' ? 'danger' : 'info'
}

function successText(value?: string) {
  return value === '1' ? '成功' : value === '0' ? '失败' : '-'
}

async function fetchStats() {
  const res = await getMessagePushLogStats({ ...query, page: 0, size: 10 })
  stats.value = res.data || {}
}

async function fetchList() {
  loading.value = true
  try {
    const [listRes] = await Promise.all([
      getMessagePushLogs(query),
      fetchStats(),
    ])
    list.value = listRes.data.content || []
    total.value = listRes.data.totalElements || 0
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
    eventType: '',
    businessType: '',
    success: '',
    messageId: undefined,
    ypatid: undefined,
    sendperid: undefined,
    recperid: undefined,
    touserOpenid: '',
    dateStart: '',
    dateEnd: '',
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

onMounted(() => {
  initFromRoute()
  fetchList()
})
</script>

<template>
  <div class="message-push-log">
    <div class="summary-row">
      <div class="summary-item">
        <span>总记录</span>
        <strong>{{ stats.total ?? 0 }}</strong>
      </div>
      <div class="summary-item success">
        <span>成功</span>
        <strong>{{ stats.successCount ?? 0 }}</strong>
      </div>
      <div class="summary-item danger">
        <span>失败</span>
        <strong>{{ stats.failedCount ?? 0 }}</strong>
      </div>
      <div class="summary-item">
        <span>微信推送</span>
        <strong>{{ stats.wechatTotal ?? 0 }}</strong>
      </div>
      <div class="summary-item">
        <span>站内消息</span>
        <strong>{{ stats.inAppTotal ?? 0 }}</strong>
      </div>
      <div class="summary-item warning">
        <span>失败率</span>
        <strong>{{ stats.failedRate || '0%' }}</strong>
      </div>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="事件">
          <el-select v-model="query.eventType" clearable placeholder="全部">
            <el-option v-for="item in eventOptions" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="业务">
          <el-select v-model="query.businessType" clearable placeholder="全部">
            <el-option v-for="item in businessOptions" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.success" clearable placeholder="全部">
            <el-option v-for="item in successOptions" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="消息ID"><el-input-number v-model="query.messageId" :min="1" controls-position="right"/></el-form-item>
        <el-form-item label="约拍ID"><el-input-number v-model="query.ypatid" :min="1" controls-position="right"/></el-form-item>
        <el-form-item label="发送者ID"><el-input-number v-model="query.sendperid" :min="1" controls-position="right"/></el-form-item>
        <el-form-item label="接收者ID"><el-input-number v-model="query.recperid" :min="1" controls-position="right"/></el-form-item>
        <el-form-item label="OpenID"><el-input v-model="query.touserOpenid" clearable placeholder="接收者 OpenID"/></el-form-item>
        <el-form-item label="开始"><el-input v-model="query.dateStart" clearable placeholder="yyyy-MM-dd"/></el-form-item>
        <el-form-item label="结束"><el-input v-model="query.dateEnd" clearable placeholder="yyyy-MM-dd"/></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center"/>
      <el-table-column label="事件" min-width="140">
        <template #default="{ row }">{{ eventText(row.eventType) }}</template>
      </el-table-column>
      <el-table-column label="业务" min-width="120">
        <template #default="{ row }">{{ businessText(row.businessType) }}</template>
      </el-table-column>
      <el-table-column prop="messageId" label="消息ID" width="100" align="center"/>
      <el-table-column prop="ypatid" label="约拍ID" width="100" align="center"/>
      <el-table-column prop="sendperid" label="发送者ID" width="110" align="center"/>
      <el-table-column prop="recperid" label="接收者ID" width="110" align="center"/>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }"><el-tag :type="successType(row.success)" size="small">{{ successText(row.success) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="wechatErrcode" label="错误码" width="110" align="center"/>
      <el-table-column prop="wechatErrmsg" label="错误信息" min-width="160" show-overflow-tooltip/>
      <el-table-column prop="touserOpenid" label="OpenID" min-width="190" show-overflow-tooltip/>
      <el-table-column prop="templateId" label="模板ID" min-width="190" show-overflow-tooltip/>
      <el-table-column prop="pageUrl" label="跳转页" min-width="180" show-overflow-tooltip/>
      <el-table-column prop="createdAt" label="创建时间" min-width="160"/>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50,100]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange"/></div>
  </div>
</template>

<style scoped lang="scss">
.message-push-log {
  .summary-row {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(132px, 1fr));
    gap: $spacing-base;
    margin-bottom: $spacing-lg;
  }

  .summary-item {
    border: 1px solid $border-lighter;
    border-radius: 6px;
    padding: $spacing-base;
    background: $bg-card;

    span {
      display: block;
      color: $text-secondary;
      font-size: 13px;
      margin-bottom: $spacing-xs;
    }

    strong {
      color: $text-primary;
      font-size: 22px;
      line-height: 1.2;
    }

    &.success strong { color: $color-success; }
    &.danger strong { color: $color-danger; }
    &.warning strong { color: $color-warning; }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-lg;
  }
}
</style>
