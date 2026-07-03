<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { auditYpat, type YpatInfo } from '@/api/modules/ypat'
import { AuditFlag } from '@/constants/enums'

const props = defineProps<{ visible: boolean; data: YpatInfo | null }>()
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
      reason.value = props.data?.reason ?? ''
    }
  },
)

async function handleAudit(flag: string) {
  if (loading.value || !props.data) return
  const ypatId = props.data.id
  loading.value = true
  try {
    await auditYpat(ypatId, flag, reason.value.trim() || undefined)
    if (props.data?.id === ypatId && props.visible) {
      ElMessage.success(flag === AuditFlag.PASS ? '审核通过' : '审核不通过')
      emit('success')
      emit('update:visible', false)
    }
  } finally {
    loading.value = false
  }
}
function getAreaText(data: YpatInfo): string {
  return [data.province, data.city, data.area].filter(Boolean).join(' / ') || '-'
}
function getPatstyleList(data: YpatInfo): string[] {
  return (data.patstyleTxt || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}
</script>

<template>
  <el-dialog
    v-model="localVisible"
    title="约拍审核"
    width="680px"
    :close-on-click-modal="!loading"
    :close-on-press-escape="!loading"
    :show-close="!loading"
  >
    <div v-if="data">
      <p><strong>ID：</strong>{{ data.id }}</p>
      <p><strong>昵称：</strong>{{ data.nickname || '-' }}</p>
      <p><strong>约拍对象：</strong>{{ data.targetTxt || '-' }}</p>
      <p><strong>风格：</strong>{{ data.patstyleTxt || '-' }}</p>
      <p><strong>收费方式：</strong>{{ data.chargewayTxt || '-' }}</p>
      <p><strong>地区：</strong>{{ getAreaText(data) }}</p>
      <p><strong>关联作品ID：</strong>{{ data.workId || '-' }}</p>
      <p><strong>描述：</strong>{{ data.describ || '-' }}</p>
      <div v-if="getPatstyleList(data).length" class="style-list">
        <el-tag v-for="style in getPatstyleList(data)" :key="style" size="small">
          {{ style }}
        </el-tag>
      </div>
      <div v-if="data.pics?.length" class="image-list">
        <el-image
          v-for="pic in data.pics"
          :key="pic"
          :src="pic"
          fit="cover"
          class="preview-image"
          :preview-src-list="data.pics"
          preview-teleported
        />
      </div>
      <el-form label-width="80px">
        <el-form-item label="审核理由"><el-input v-model="reason" type="textarea" :rows="3" placeholder="审核不通过时填写"/></el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button :disabled="loading" @click="localVisible = false">取消</el-button>
      <el-button type="danger" :loading="loading" :disabled="loading" @click="handleAudit(AuditFlag.REJECT)">不通过</el-button>
      <el-button type="primary" :loading="loading" :disabled="loading" @click="handleAudit(AuditFlag.PASS)">通过</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.style-list {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
  margin: $spacing-base 0;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
  gap: $spacing-sm;
  margin: $spacing-base 0;
}

.preview-image {
  width: 96px;
  height: 96px;
  border-radius: $radius-sm;
}
</style>
