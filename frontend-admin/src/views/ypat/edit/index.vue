<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadFile, UploadFiles } from 'element-plus'
import { useRouter } from 'vue-router'
import { submitYpat, type YpatSubmitForm } from '@/api/modules/ypat'
import { getYpatTargetOptions, getYpatPatstyleOptions, getYpatChargeWayOptions, getGenderOptions, getProfessOptions } from '@/constants/enums'

const router = useRouter()
const form = ref<YpatSubmitForm & { patstyleList: string[] }>({
  describ: '', target: '', patdate: '', chargeway: '1', province: '', city: '', area: '', patstyle: '',
  nickname: '', gender: '', profess: '', patstyleList: [],
})
const avatar = ref('')
const avatarFile = ref<File>()
const workFiles = ref<File[]>([])
const loading = ref(false)
const formRef = ref()
const rules = {
  describ: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  target: [{ required: true, message: '请选择约拍对象', trigger: 'change' }],
  patdate: [{ required: true, message: '请选择约拍日期', trigger: 'change' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
}

function handleAvatarChange(file: UploadFile) {
  if (!file.raw) return
  avatarFile.value = file.raw
  avatar.value = URL.createObjectURL(file.raw)
}

function syncWorkFiles(_file: UploadFile, files: UploadFiles) {
  const selectedFiles: File[] = []
  files.forEach((item) => {
    if (item.raw) {
      selectedFiles.push(item.raw)
    }
  })
  workFiles.value = selectedFiles
}
async function submit() {
  if (loading.value) return

  loading.value = true
  try {
    await formRef.value.validate()
    if (!workFiles.value.length) {
      ElMessage.error('请至少上传一张作品图片')
      return
    }
    form.value.patstyle = form.value.patstyleList.join(',')
    await submitYpat(form.value, avatarFile.value, workFiles.value)
    ElMessage.success('代发约拍成功')
    router.push('/manage/ypat-list')
  } finally { loading.value = false }
}
</script>

<template>
  <div class="ypat-edit-page">
    <el-card>
      <template #header>后台代发约拍</template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="昵称" prop="nickname"><el-input v-model="form.nickname"/></el-form-item>
        <el-form-item label="性别"><el-radio-group v-model="form.gender"><el-radio v-for="o in getGenderOptions()" :key="o.value" :label="o.value">{{ o.label }}</el-radio></el-radio-group></el-form-item>
        <el-form-item label="职业"><el-select v-model="form.profess" clearable placeholder="请选择"><el-option v-for="o in getProfessOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="约拍对象" prop="target"><el-select v-model="form.target" clearable placeholder="请选择"><el-option v-for="o in getYpatTargetOptions()" :key="o.value" :label="o.label" :value="o.value"/></el-select></el-form-item>
        <el-form-item label="约拍日期" prop="patdate"><el-date-picker v-model="form.patdate" type="date" value-format="YYYY-MM-DD"/></el-form-item>
        <el-form-item label="城市" prop="city"><el-input v-model="form.city"/></el-form-item>
        <el-form-item label="收费方式"><el-radio-group v-model="form.chargeway"><el-radio v-for="o in getYpatChargeWayOptions()" :key="o.value" :label="o.value">{{ o.label }}</el-radio></el-radio-group></el-form-item>
        <el-form-item label="风格"><el-checkbox-group v-model="form.patstyleList"><el-checkbox v-for="o in getYpatPatstyleOptions()" :key="o.value" :label="o.value">{{ o.label }}</el-checkbox></el-checkbox-group></el-form-item>
        <el-form-item label="描述" prop="describ"><el-input v-model="form.describ" type="textarea" :rows="4"/></el-form-item>
        <el-form-item label="头像">
          <el-upload :auto-upload="false" :show-file-list="false" :limit="1" :on-change="handleAvatarChange"><el-button type="primary">选择头像</el-button></el-upload>
          <el-image v-if="avatar" :src="avatar" style="width:80px;margin-top:8px;" fit="cover"/>
        </el-form-item>
        <el-form-item label="作品图片">
          <el-upload :auto-upload="false" :on-change="syncWorkFiles" :on-remove="syncWorkFiles" list-type="picture-card" multiple><el-icon><Plus/></el-icon></el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">提交</el-button>
          <el-button @click="router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
