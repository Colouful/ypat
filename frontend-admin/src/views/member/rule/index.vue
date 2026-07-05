<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import {
  getMemberBenefitRules,
  saveMemberBenefitRule,
  type MemberBenefitRuleQuery,
} from '@/api/modules/member'
import type { MemberBenefitRule } from '@/api/types'

const query = reactive<MemberBenefitRuleQuery>({ levelCode: '', scene: '', status: '', page: 0, size: 10 })
const list = ref<MemberBenefitRule[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const editVisible = ref(false)
const currentPage = computed(() => (query.page ?? 0) + 1)
const formRef = ref()
const form = reactive<Partial<MemberBenefitRule>>({})
const statusOptions = [{ label: '启用', value: '1' }, { label: '停用', value: '0' }]
const yesNoOptions = [{ label: '生效', value: '1' }, { label: '未生效', value: '0' }]

const rules = {
  discountPpd: [{ required: true, message: '请输入优惠拍拍豆', trigger: 'blur' }],
  minActualPpd: [{ required: true, message: '请输入最低实扣', trigger: 'blur' }],
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getMemberBenefitRules(query)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}
function search() { query.page = 0; fetchList() }
function reset() { query.levelCode = ''; query.scene = ''; query.status = ''; query.page = 0; fetchList() }
function pageChange(page: number) { query.page = page - 1; fetchList() }
function sizeChange(size: number) { query.size = size; query.page = 0; fetchList() }
function openEdit(row: MemberBenefitRule) { Object.assign(form, row); editVisible.value = true }
async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    await saveMemberBenefitRule({ ...form })
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}
onMounted(fetchList)
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item label="等级"><el-input v-model="query.levelCode" clearable placeholder="BASIC"/></el-form-item>
        <el-form-item label="场景"><el-input v-model="query.scene" clearable placeholder="SUBMIT_YPAT"/></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" clearable placeholder="全部"><el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="levelCode" label="等级" width="100" align="center"/>
      <el-table-column prop="scene" label="场景" min-width="140"/>
      <el-table-column prop="benefitType" label="权益类型" min-width="140"/>
      <el-table-column prop="discountPpd" label="优惠拍拍豆" width="120" align="center"/>
      <el-table-column prop="minActualPpd" label="最低实扣" width="110" align="center"/>
      <el-table-column label="生效" width="90" align="center"><template #default="{row}"><el-tag :type="row.effective === '1' ? 'success' : 'info'" size="small">{{ row.effective === '1' ? '是' : '否' }}</el-tag></template></el-table-column>
      <el-table-column label="状态" width="90" align="center"><template #default="{row}"><el-tag :type="row.status === '1' ? 'success' : 'info'" size="small">{{ row.status === '1' ? '启用' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column prop="description" label="说明" min-width="180"/>
      <el-table-column label="操作" width="90" align="center" fixed="right"><template #default="{row}"><el-button type="primary" link size="small" @click="openEdit(row as MemberBenefitRule)">编辑</el-button></template></el-table-column>
    </el-table>
    <div class="pagination-wrapper"><el-pagination :current-page="currentPage" :page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next,jumper" background @current-change="pageChange" @size-change="sizeChange"/></div>

    <el-dialog v-model="editVisible" title="编辑权益规则" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="等级"><el-input v-model="form.levelCode" disabled/></el-form-item>
        <el-form-item label="场景"><el-input v-model="form.scene" disabled/></el-form-item>
        <el-form-item label="权益类型"><el-input v-model="form.benefitType" disabled/></el-form-item>
        <el-form-item label="优惠拍拍豆" prop="discountPpd"><el-input-number v-model="form.discountPpd" :min="0"/></el-form-item>
        <el-form-item label="最低实扣" prop="minActualPpd"><el-input-number v-model="form.minActualPpd" :min="0"/></el-form-item>
        <el-form-item label="是否生效"><el-select v-model="form.effective"><el-option v-for="o in yesNoOptions" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="3"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: $spacing-lg;
}
</style>
