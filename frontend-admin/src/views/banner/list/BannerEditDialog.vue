<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { saveBanner, type Banner } from '@/api/modules/banner'
import { uploadFiles } from '@/api/modules/upload'

const props = defineProps<{ visible: boolean; data: Banner | null }>()
const emit = defineEmits(['update:visible', 'success'])
const localVisible = computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })

const form = reactive({ id: undefined as number | undefined, title: '', imgpath: '' })
const loading = ref(false)
const uploadLoading = ref(false)
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }], imgpath: [{ required: true, message: '请上传图片', trigger: 'blur' }] }
const formRef = ref()

watch(() => props.visible, (v) => {
  if (v) {
    form.id = props.data?.id
    form.title = props.data?.title || ''
    form.imgpath = props.data?.imgpath || ''
  }
})

async function handleUpload(file: File) {
  uploadLoading.value = true
  try {
    const res = await uploadFiles([file])
    form.imgpath = res.data.urls[0]
    ElMessage.success('图片上传成功')
  } finally { uploadLoading.value = false }
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await saveBanner({ ...form })
    ElMessage.success('保存成功')
    emit('success')
    localVisible.value = false
  } finally { loading.value = false }
}
</script>

<template>
  <el-dialog v-model="localVisible" :title="data ? '编辑横幅' : '新增横幅'" width="500px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="标题" prop="title"><el-input v-model="form.title"/></el-form-item>
      <el-form-item label="图片" prop="imgpath">
        <el-upload :auto-upload="false" :show-file-list="false" :on-change="(f: any) => handleUpload(f.raw)">
          <el-button :loading="uploadLoading" type="primary">上传图片</el-button>
        </el-upload>
        <el-image v-if="form.imgpath" :src="form.imgpath" style="width:120px;margin-top:8px;" fit="cover"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="localVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>
