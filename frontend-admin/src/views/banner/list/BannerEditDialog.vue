<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { saveBanner, type Banner } from '@/api/modules/banner'
import { uploadFiles } from '@/api/modules/upload'

const props = defineProps<{ visible: boolean; data: Banner | null }>()
const emit = defineEmits(['update:visible', 'success'])
const localVisible = computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })

const form = reactive({
  id: undefined as number | undefined,
  title: '',
  imgpath: '',
  jumpflag: '0',
  jumptype: 'miniapp',
  jumpurl: '',
})
const loading = ref(false)
const uploadLoading = ref(false)
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }], imgpath: [{ required: true, message: '请上传图片', trigger: 'blur' }] }
const formRef = ref()

watch(() => props.visible, (v) => {
  if (v) {
    form.id = props.data?.id
    form.title = props.data?.title || ''
    form.imgpath = props.data?.imgpath || ''
    form.jumpflag = props.data?.jumpflag || '0'
    form.jumptype = props.data?.jumptype || 'miniapp'
    form.jumpurl = props.data?.jumpurl || ''
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

function validateJumpConfig(): boolean {
  if (form.jumpflag !== '1') {
    form.jumptype = 'miniapp'
    form.jumpurl = ''
    return true
  }
  const target = form.jumpurl.trim()
  if (!target) {
    ElMessage.warning('请输入跳转目标')
    return false
  }
  if (target.length > 500) {
    ElMessage.warning('跳转目标不能超过500个字符')
    return false
  }
  if (form.jumptype === 'miniapp' && !target.startsWith('/pages/') && !target.startsWith('/pages-sub/')) {
    ElMessage.warning('请输入 /pages 或 /pages-sub 开头的小程序页面路径')
    return false
  }
  if (form.jumptype === 'web' && !target.startsWith('http://') && !target.startsWith('https://')) {
    ElMessage.warning('请输入 http 或 https 开头的外部地址')
    return false
  }
  form.jumpurl = target
  return true
}

async function submit() {
  await formRef.value.validate()
  if (!validateJumpConfig()) return
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
      <el-form-item label="是否跳转">
        <el-switch v-model="form.jumpflag" active-value="1" inactive-value="0" />
      </el-form-item>
      <template v-if="form.jumpflag === '1'">
        <el-form-item label="跳转类型">
          <el-radio-group v-model="form.jumptype">
            <el-radio-button label="miniapp">小程序页面</el-radio-button>
            <el-radio-button label="web">外部地址</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="跳转目标">
          <el-input
            v-model="form.jumpurl"
            :maxlength="500"
            show-word-limit
            :placeholder="form.jumptype === 'miniapp' ? '/pages/work/index 或 /pages-sub/work/detail?id=1' : 'https://example.com/activity'"
          />
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <el-button @click="localVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>
