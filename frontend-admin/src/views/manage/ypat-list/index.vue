<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusTag from '@/components/common/StatusTag.vue'
import AuditDialog from './AuditDialog.vue'
import { getYpatList, recomYpat, type YpatInfo, type YpatListQuery } from '@/api/modules/ypat'
import {
  getYpatStatusOptions,
  getRecomOptions,
  getYpatTargetOptions,
  getYpatPatstyleOptions,
  getYpatChargeWayOptions,
  RecomFlag,
} from '@/constants/enums'

const query = reactive<YpatListQuery>({
  status: '',
  nickname: '',
  mobile: '',
  recomflag: '',
  target: '',
  patstyle: '',
  chargeway: '',
  city: '',
  workId: '',
  page: 0,
  size: 10,
})
const list = ref<YpatInfo[]>([])
const total = ref(0)
const loading = ref(false)
const recomLoadingIds = ref<Set<number>>(new Set())
const auditVisible = ref(false)
const current = ref<YpatInfo | null>(null)
const currentPage = computed(() => (query.page ?? 0) + 1)
let listRequestSeq = 0

async function fetchList() {
  const requestSeq = ++listRequestSeq
  loading.value = true
  try {
    const res = await getYpatList(query)
    if (requestSeq === listRequestSeq) {
      list.value = res.data.content || []
      total.value = res.data.totalElements || 0
    }
  } finally {
    if (requestSeq === listRequestSeq) {
      loading.value = false
    }
  }
}
function search() { query.page = 0; fetchList() }
function reset() {
  query.status = ''
  query.nickname = ''
  query.mobile = ''
  query.recomflag = ''
  query.target = ''
  query.patstyle = ''
  query.chargeway = ''
  query.city = ''
  query.workId = ''
  query.page = 0
  fetchList()
}
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
function openAudit(row: YpatInfo) { current.value = row; auditVisible.value = true }
async function doRecom(row: YpatInfo, flag: string) {
  if (recomLoadingIds.value.has(row.id)) return

  const text = flag === RecomFlag.YES.value ? '推荐' : '取消推荐'
  recomLoadingIds.value = new Set(recomLoadingIds.value).add(row.id)
  try {
    await ElMessageBox.confirm(`确定要${text}该约拍吗？`, '提示', { type: 'warning' })
    await recomYpat(row.id, flag)
    ElMessage.success(`${text}成功`)
    fetchList()
  } finally {
    const nextIds = new Set(recomLoadingIds.value)
    nextIds.delete(row.id)
    recomLoadingIds.value = nextIds
  }
}
function getAreaText(row: YpatInfo): string {
  return [row.province, row.city, row.area].filter(Boolean).join(' / ') || '-'
}
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="状态"><el-select v-model="query.status" clearable placeholder="全部"><el-option v-for="o in getYpatStatusOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="推荐"><el-select v-model="query.recomflag" clearable placeholder="全部"><el-option v-for="o in getRecomOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="约拍对象"><el-select v-model="query.target" clearable placeholder="全部"><el-option v-for="o in getYpatTargetOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="风格"><el-select v-model="query.patstyle" clearable placeholder="全部"><el-option v-for="o in getYpatPatstyleOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="收费方式"><el-select v-model="query.chargeway" clearable placeholder="全部"><el-option v-for="o in getYpatChargeWayOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="昵称"><el-input v-model="query.nickname" placeholder="请输入昵称"/></el-form-item>
        <el-form-item label="手机号"><el-input v-model="query.mobile" placeholder="请输入手机号"/></el-form-item>
        <el-form-item label="城市"><el-input v-model="query.city" placeholder="请输入城市"/></el-form-item>
        <el-form-item label="作品ID"><el-input v-model="query.workId" placeholder="请输入关联作品ID"/></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center"/>
      <el-table-column prop="genderTxt" label="性别" width="80" align="center"/>
      <el-table-column prop="nickname" label="昵称" min-width="120"/>
      <el-table-column prop="professTxt" label="职业" min-width="120"/>
      <el-table-column prop="targetTxt" label="约拍对象" min-width="120"/>
      <el-table-column prop="patstyleTxt" label="风格" min-width="160" show-overflow-tooltip/>
      <el-table-column prop="chargewayTxt" label="收费方式" min-width="120"/>
      <el-table-column label="地区" min-width="160" show-overflow-tooltip><template #default="{row}">{{ getAreaText(row as YpatInfo) }}</template></el-table-column>
      <el-table-column prop="workId" label="关联作品ID" width="110" align="center"/>
      <el-table-column prop="pubdate" label="发布时间" min-width="160"/>
      <el-table-column label="状态" width="120" align="center"><template #default="{row}"><StatusTag :status="row.status" type="ypat"/></template></el-table-column>
      <el-table-column prop="recomflag" label="推荐" width="100" align="center"><template #default="{row}">{{ row.recomflag === RecomFlag.YES.value ? '已推荐' : '未推荐' }}</template></el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{row}">
          <el-button type="primary" link size="small" @click="openAudit(row as unknown as YpatInfo)">审核</el-button>
          <el-button type="success" link size="small" v-if="row.recomflag !== RecomFlag.YES.value" :loading="recomLoadingIds.has(row.id)" :disabled="recomLoadingIds.has(row.id)" @click="doRecom(row as unknown as YpatInfo, RecomFlag.YES.value)">推荐</el-button>
          <el-button type="info" link size="small" v-else :loading="recomLoadingIds.has(row.id)" :disabled="recomLoadingIds.has(row.id)" @click="doRecom(row as unknown as YpatInfo, RecomFlag.NO.value)">取消推荐</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange"/></div>
    <AuditDialog v-model:visible="auditVisible" :data="current" @success="fetchList"/>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
