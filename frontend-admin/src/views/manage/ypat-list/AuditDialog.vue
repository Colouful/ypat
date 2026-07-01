<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { auditYpat, type YpatInfo } from '@/api/modules/ypat'
import { AuditFlag } from '@/constants/enums'

import { computed } from 'vue'

const props = defineProps<{ visible: boolean; data: YpatInfo | null }>()
const emit = defineEmits(['update:visible', 'success'])
const localVisible = computed({ get: () => props.visible, set: (v) => emit('update:visible', v) })

const reason = ref('')
const loading = ref(false)
watch(() => props.visible, (v) => { if (v) reason.value = '' })

async function handleAudit(flag: string) {
  if (!props.data) return
  loading.value = true
  try {
    await auditYpat(props.data.id, flag, reason.value)
    ElMessage.success(flag === AuditFlag.PASS ? '审核通过' : '审核不通过')
    emit('success')
    localVisible.value = false
  } finally { loading.value = false }
}
</script>

<template>
  <el-dialog v-model="localVisible" title="约拍审核" width="600px">
    <div v-if="data">
      <p><strong>ID：</strong>{{ data.id }}</p>
      <p><strong>昵称：</strong>{{ data.nickname }}</p>
      <p><strong>描述：</strong>{{ data.describ }}</p>
      <p><strong>城市：</strong>{{ data.city }}</p>
      <el-form label-width="80px">
        <el-form-item label="审核理由"><el-input v-model="reason" type="textarea" :rows="3" placeholder="审核不通过时填写"/></el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="localVisible = false">取消</el-button>
      <el-button type="danger" :loading="loading" @click="handleAudit(AuditFlag.REJECT)">不通过</el-button>
      <el-button type="primary" :loading="loading" @click="handleAudit(AuditFlag.PASS)">通过</el-button>
    </template>
  </el-dialog>
</template>
