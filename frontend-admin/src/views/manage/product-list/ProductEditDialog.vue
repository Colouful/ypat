<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { saveProduct, type Product } from '@/api/modules/product'

const props = defineProps<{ visible: boolean; data: Product | null }>()
const emit = defineEmits(['update:visible', 'success'])
const localVisible = computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })

const form = reactive({ id: undefined as number | undefined, name: '', currval: 0, oldval: 0 })
const loading = ref(false)
const rules = { name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }] }
const formRef = ref()

watch(() => props.visible, (v) => {
  if (v) {
    form.id = props.data?.id
    form.name = props.data?.name || ''
    form.currval = props.data?.currval ?? 0
    form.oldval = props.data?.oldval ?? 0
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
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="名称" prop="name"><el-input v-model="form.name"/></el-form-item>
      <el-form-item label="当前值"><el-input-number v-model="form.currval" :min="0"/></el-form-item>
      <el-form-item label="原值"><el-input-number v-model="form.oldval" :min="0"/></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="localVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>
