<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { auditWork, type WorkAdminInfo } from '@/api/modules/work'
import { AuditFlag } from '@/constants/enums'

const props = defineProps<{
  visible: boolean
  data: WorkAdminInfo | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

const localVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => {
    if (loading.value) return
    emit('update:visible', value)
  },
})

const reason = ref('')
const loading = ref(false)

watch(
  () => [props.visible, props.data?.id] as const,
  ([visible]) => {
    if (visible) {
      reason.value = props.data?.auditReason ?? ''
    }
  },
)

async function handleAudit(flag: string): Promise<void> {
  if (!props.data) return

  const workId = props.data.id
  loading.value = true
  try {
    await auditWork(workId, flag, reason.value.trim() || undefined)
    if (props.data?.id === workId && props.visible) {
      ElMessage.success(flag === AuditFlag.PASS ? '作品审核通过' : '作品审核不通过')
      emit('success')
      emit('update:visible', false)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-dialog
    v-model="localVisible"
    title="作品审核"
    width="600px"
    :close-on-click-modal="!loading"
    :close-on-press-escape="!loading"
    :show-close="!loading"
  >
    <div v-if="data">
      <p><strong>ID：</strong>{{ data.id }}</p>
      <p><strong>作者：</strong>{{ data.nickname || '-' }}</p>
      <p><strong>描述：</strong>{{ data.description || '-' }}</p>

      <el-form label-width="80px">
        <el-form-item label="审核理由">
          <el-input
            v-model="reason"
            type="textarea"
            :rows="4"
            placeholder="请输入审核理由，不通过时建议填写"
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button :disabled="loading" @click="localVisible = false">取消</el-button>
      <el-button
        type="danger"
        :loading="loading"
        :disabled="loading"
        @click="handleAudit(AuditFlag.REJECT)"
      >
        不通过
      </el-button>
      <el-button
        type="primary"
        :loading="loading"
        :disabled="loading"
        @click="handleAudit(AuditFlag.PASS)"
      >
        通过
      </el-button>
    </template>
  </el-dialog>
</template>
