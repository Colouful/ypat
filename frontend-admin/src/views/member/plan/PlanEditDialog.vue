<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { saveMemberPlan } from '@/api/modules/member'
import type { MemberPlan } from '@/api/types'

const props = defineProps<{ visible: boolean; data: MemberPlan | null }>()
const emit = defineEmits(['update:visible', 'success'])
const localVisible = computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })
const formRef = ref()
const loading = ref(false)
const form = reactive<Partial<MemberPlan>>({
  code: '',
  name: '',
  durationDays: 30,
  priceFen: 0,
  originPriceFen: 0,
  giftPpd: 0,
  levelCode: 'BASIC',
  recommended: '0',
  status: '1',
  sortNo: 0,
  benefits: '',
})

const rules = {
  code: [{ required: true, message: '请输入套餐编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  durationDays: [{ required: true, message: '请输入有效天数', trigger: 'blur' }],
  priceFen: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  giftPpd: [{ required: true, message: '请输入赠送拍拍豆', trigger: 'blur' }],
  levelCode: [{ required: true, message: '请输入会员等级', trigger: 'blur' }],
}

watch(() => props.visible, (visible) => {
  if (!visible) return
  Object.assign(form, {
    id: props.data?.id,
    code: props.data?.code || '',
    name: props.data?.name || '',
    durationDays: props.data?.durationDays ?? 30,
    priceFen: props.data?.priceFen ?? 0,
    originPriceFen: props.data?.originPriceFen ?? 0,
    giftPpd: props.data?.giftPpd ?? 0,
    levelCode: props.data?.levelCode || 'BASIC',
    recommended: props.data?.recommended || '0',
    status: props.data?.status || '1',
    sortNo: props.data?.sortNo ?? 0,
    benefits: props.data?.benefits || '',
  })
})

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await saveMemberPlan({ ...form })
    ElMessage.success('保存成功')
    emit('success')
    localVisible.value = false
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-dialog v-model="localVisible" :title="data ? '编辑套餐' : '新增套餐'" width="640px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="套餐编码" prop="code"><el-input v-model="form.code"/></el-form-item>
      <el-form-item label="套餐名称" prop="name"><el-input v-model="form.name"/></el-form-item>
      <el-form-item label="有效天数" prop="durationDays"><el-input-number v-model="form.durationDays" :min="0"/></el-form-item>
      <el-form-item label="售价(分)" prop="priceFen"><el-input-number v-model="form.priceFen" :min="0"/></el-form-item>
      <el-form-item label="原价(分)"><el-input-number v-model="form.originPriceFen" :min="0"/></el-form-item>
      <el-form-item label="赠送拍拍豆" prop="giftPpd"><el-input-number v-model="form.giftPpd" :min="0"/></el-form-item>
      <el-form-item label="会员等级" prop="levelCode"><el-input v-model="form.levelCode"/></el-form-item>
      <el-form-item label="是否推荐"><el-switch v-model="form.recommended" active-value="1" inactive-value="0"/></el-form-item>
      <el-form-item label="状态"><el-switch v-model="form.status" active-value="1" inactive-value="0"/></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sortNo" :min="0"/></el-form-item>
      <el-form-item label="权益说明"><el-input v-model="form.benefits" type="textarea" :rows="3"/></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="localVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>
