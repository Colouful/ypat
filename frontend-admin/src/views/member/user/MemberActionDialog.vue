<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { cancelMember, extendMember, grantMember } from '@/api/modules/member'
import type { MemberUser } from '@/api/types'

type ActionType = 'grant' | 'extend' | 'cancel'

const props = defineProps<{ visible: boolean; action: ActionType; user: MemberUser | null }>()
const emit = defineEmits(['update:visible', 'success'])
const localVisible = computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })
const formRef = ref()
const loading = ref(false)
const form = reactive({ days: 30, reason: '' })
const title = computed(() => ({ grant: '手动开通会员', extend: '延期会员', cancel: '取消会员' }[props.action]))
const needDays = computed(() => props.action !== 'cancel')
const rules = computed(() => ({
  days: needDays.value ? [{ required: true, message: '请输入天数', trigger: 'blur' }] : [],
  reason: [{ required: true, message: '请输入操作原因', trigger: 'blur' }],
}))

watch(() => props.visible, (visible) => {
  if (!visible) return
  form.days = 30
  form.reason = ''
})

async function submit() {
  if (!props.user) return
  await formRef.value.validate()
  loading.value = true
  try {
    if (props.action === 'grant') {
      await grantMember(props.user.userId, { days: form.days, reason: form.reason })
    } else if (props.action === 'extend') {
      await extendMember(props.user.userId, { days: form.days, reason: form.reason })
    } else {
      await cancelMember(props.user.userId, { reason: form.reason })
    }
    ElMessage.success('操作成功')
    emit('success')
    localVisible.value = false
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-dialog v-model="localVisible" :title="title" width="520px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="用户ID"><el-input :model-value="user?.userId" disabled/></el-form-item>
      <el-form-item v-if="needDays" label="天数" prop="days"><el-input-number v-model="form.days" :min="1"/></el-form-item>
      <el-form-item label="原因" prop="reason"><el-input v-model="form.reason" type="textarea" :rows="3"/></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="localVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">确认</el-button>
    </template>
  </el-dialog>
</template>
