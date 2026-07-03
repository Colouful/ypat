<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getYpatList, type YpatListQuery, type YpatInfo } from '@/api/modules/ypat'
import {
  getYpatTargetOptions,
  getYpatPatstyleOptions,
  getYpatChargeWayOptions,
} from '@/constants/enums'

const query = reactive<YpatListQuery>({
  status: '2',
  nickname: '',
  city: '',
  target: '',
  patstyle: '',
  chargeway: '',
  workId: '',
  page: 0,
  size: 10,
})
const list = ref<YpatInfo[]>([])
const total = ref(0)
const loading = ref(false)
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
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
function getAreaText(row: YpatInfo): string {
  return [row.province, row.city, row.area].filter(Boolean).join(' / ') || '-'
}
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="昵称"><el-input v-model="query.nickname" placeholder="请输入昵称"/></el-form-item>
        <el-form-item label="约拍对象"><el-select v-model="query.target" clearable placeholder="全部"><el-option v-for="o in getYpatTargetOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="风格"><el-select v-model="query.patstyle" clearable placeholder="全部"><el-option v-for="o in getYpatPatstyleOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="收费方式"><el-select v-model="query.chargeway" clearable placeholder="全部"><el-option v-for="o in getYpatChargeWayOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="城市"><el-input v-model="query.city" placeholder="请输入"/></el-form-item>
        <el-form-item label="作品ID"><el-input v-model="query.workId" placeholder="请输入关联作品ID"/></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center"/>
      <el-table-column prop="nickname" label="昵称" min-width="120"/>
      <el-table-column prop="mobile" label="手机号" min-width="120"/>
      <el-table-column prop="genderTxt" label="性别" width="80" align="center"/>
      <el-table-column prop="targetTxt" label="约拍对象" min-width="120"/>
      <el-table-column prop="patstyleTxt" label="风格" min-width="160" show-overflow-tooltip/>
      <el-table-column prop="chargewayTxt" label="收费方式" min-width="120"/>
      <el-table-column label="地区" min-width="160" show-overflow-tooltip><template #default="{row}">{{ getAreaText(row as YpatInfo) }}</template></el-table-column>
      <el-table-column prop="workId" label="关联作品ID" width="110" align="center"/>
      <el-table-column prop="pubdate" label="发布时间" min-width="160"/>
      <el-table-column prop="pattimes" label="拍摄次数" width="100" align="center"/>
      <el-table-column prop="readtimes" label="阅读次数" width="100" align="center"/>
      <el-table-column prop="coltimes" label="收藏次数" width="100" align="center"/>
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
