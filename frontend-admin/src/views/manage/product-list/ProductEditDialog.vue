<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { saveProduct, type Product } from '@/api/modules/product'
import { ProductStatus } from '@/constants/enums'

const props = defineProps<{ visible: boolean; data: Product | null }>()
const emit = defineEmits(['update:visible', 'success'])
const localVisible = computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })

const form = reactive({
  id: undefined as number | undefined,
  name: '',
  currval: 1,
  oldval: 1,
  status: ProductStatus.UP.value,
  recommended: '0',
})
const loading = ref(false)
const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  currval: [{ required: true, message: '请输入充值数量', trigger: 'blur' }],
  oldval: [{ required: true, message: '请输入支付金额', trigger: 'blur' }],
}
const formRef = ref()

watch(() => props.visible, (v) => {
  if (v) {
    form.id = props.data?.id
    form.name = props.data?.name || ''
    form.currval = props.data?.currval ?? 1
    form.oldval = props.data?.oldval ?? 1
    form.status = props.data?.status || ProductStatus.UP.value
    form.recommended = props.data?.recommended || '0'
  }
})

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await saveProduct({ ...form })
    ElMessage.success('保存成功')
    emit('success')
    localVisible.value = false
  } finally { loading.value = false }
}
</script>

<template>
  <el-dialog v-model="localVisible" :title="data ? '编辑产品' : '新增产品'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
      <el-form-item label="名称" prop="name"><el-input v-model="form.name"/></el-form-item>
      <el-form-item label="充值数量" prop="currval">
        <el-input-number v-model="form.currval" :min="1"/>
        <span class="form-unit">拍豆</span>
      </el-form-item>
      <el-form-item label="支付金额" prop="oldval">
        <el-input-number v-model="form.oldval" :min="1"/>
        <span class="form-unit">分，{{ (form.oldval / 100).toFixed(2) }} 元</span>
      </el-form-item>
      <el-form-item label="优先推荐">
        <el-switch v-model="form.recommended" active-value="1" inactive-value="0"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="localVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.form-unit {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
