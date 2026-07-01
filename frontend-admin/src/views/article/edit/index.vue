<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getArticleDetail, saveArticle, type Article } from '@/api/modules/article'
import { uploadFiles } from '@/api/modules/upload'

const route = useRoute()
const router = useRouter()
const id = Number(route.query.id) || 0
const form = ref<Partial<Article>>({ title: '', describ: '', content: '', imgpath: '' })
const loading = ref(false)
const uploadLoading = ref(false)
const formRef = ref()
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

async function fetchDetail() {
  if (!id) return
  const res = await getArticleDetail(id)
  form.value = res.data || {}
}

async function handleUpload(file: File) {
  uploadLoading.value = true
  try {
    const res = await uploadFiles([file])
    form.value.imgpath = res.data.urls[0]
    ElMessage.success('封面上传成功')
  } finally { uploadLoading.value = false }
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await saveArticle(form.value)
    ElMessage.success('保存成功')
    router.push('/article/index')
  } finally { loading.value = false }
}

onMounted(fetchDetail)
</script>

<template>
  <div class="article-edit-page">
    <el-card>
      <template #header>{{ id ? '编辑文章' : '新增文章' }}</template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title"/></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.describ" type="textarea"/></el-form-item>
        <el-form-item label="封面">
          <el-upload :auto-upload="false" :show-file-list="false" :on-change="(f: any) => handleUpload(f.raw)">
            <el-button :loading="uploadLoading" type="primary">上传封面</el-button>
          </el-upload>
          <el-image v-if="form.imgpath" :src="form.imgpath" style="width:120px;margin-top:8px;" fit="cover"/>
        </el-form-item>
        <el-form-item label="内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="15"/></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
          <el-button @click="router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
