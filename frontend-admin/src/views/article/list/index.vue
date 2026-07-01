<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import StatusTag from '@/components/common/StatusTag.vue'
import { getArticleList, upDownArticle, type Article, type ArticleListQuery } from '@/api/modules/article'
import { getArticleStatusOptions, ArticleStatus } from '@/constants/enums'

const router = useRouter()
const query = reactive<ArticleListQuery>({ name: '', status: '', page: 0, size: 10 })
const list = ref<Article[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)

async function fetchList() {
  loading.value = true
  try {
    const res = await getArticleList(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally { loading.value = false }
}
function search() { query.page = 0; fetchList() }
function reset() { query.name = ''; query.status = ''; query.page = 0; fetchList() }
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
function edit(row?: Article) { router.push(`/article/edit${row ? '?id=' + row.id : ''}`) }
async function doUpDown(row: Article, status: string) {
  const text = status === ArticleStatus.YFB.value ? '发布' : '撤回'
  await ElMessageBox.confirm(`确定要${text}该文章吗？`, '提示', { type: 'warning' })
  await upDownArticle(row.id, status)
  ElMessage.success(`${text}成功`)
  fetchList()
}
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="标题"><el-input v-model="query.name" placeholder="请输入"/></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" clearable placeholder="全部"><el-option v-for="o in getArticleStatusOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button><el-button type="success" @click="edit()">新增</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" align="center"/>
      <el-table-column prop="title" label="标题" min-width="180"/>
      <el-table-column prop="describ" label="描述" min-width="180" show-overflow-tooltip/>
      <el-table-column prop="credate" label="创建时间" min-width="160"/>
      <el-table-column prop="readtimes" label="阅读数" width="100" align="center"/>
      <el-table-column label="状态" width="120" align="center"><template #default="{row}"><StatusTag :status="row.status" type="article"/></template></el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{row}">
          <el-button type="primary" link size="small" @click="edit(row)">编辑</el-button>
          <el-button type="success" link size="small" v-if="row.status !== ArticleStatus.YFB.value" @click="doUpDown(row, ArticleStatus.YFB.value)">发布</el-button>
          <el-button type="warning" link size="small" v-else @click="doUpDown(row, ArticleStatus.YCH.value)">撤回</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange"/></div>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: $spacing-lg; }
</style>
