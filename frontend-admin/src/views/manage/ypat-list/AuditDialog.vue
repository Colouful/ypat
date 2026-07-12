<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, CloseBold, Picture, User } from '@element-plus/icons-vue'
import { auditYpat, getYpatDetail, type YpatInfo } from '@/api/modules/ypat'
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
const detailLoading = ref(false)
const detail = ref<YpatInfo | null>(null)
const displayData = computed(() => detail.value || props.data)
const styleTags = computed(() => (displayData.value ? getPatstyleList(displayData.value) : []))
const previewImages = computed(() => displayData.value?.pics || [])
const detailRows = computed(() => {
  if (!displayData.value) return []
  const data = displayData.value
  return [
    { label: '性别', value: data.genderTxt || '-' },
    { label: '职业', value: data.professTxt || '-' },
    { label: '约拍对象', value: data.targetTxt || '-' },
    { label: '收费方式', value: data.chargewayTxt || '-' },
    { label: '地区', value: getAreaText(data) },
    { label: '关联作品ID', value: data.workId || '-' },
  ]
})

async function loadDetail(id: number): Promise<void> {
  detail.value = null
  detailLoading.value = true
  try {
    const result = await getYpatDetail(id)
    if (props.visible && props.data?.id === id && result.data) {
      detail.value = result.data
      reason.value = result.data.reason || ''
    }
  } catch {
    // 详情失败时继续使用列表行数据，审核入口保持可用。
  } finally {
    if (props.data?.id === id) detailLoading.value = false
  }
}

watch(
  () => [props.visible, props.data?.id] as const,
  ([visible, id]) => {
    if (visible && id) {
      reason.value = props.data?.reason ?? ''
      void loadDetail(id)
    } else {
      detail.value = null
    }
  },
)

async function handleAudit(flag: string) {
  if (loading.value || !displayData.value) return
  const ypatId = displayData.value.id
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
    width="860px"
    class="ypat-audit-dialog"
    :close-on-click-modal="!loading"
    :close-on-press-escape="!loading"
    :show-close="!loading"
  >
    <div v-if="displayData" v-loading="detailLoading" class="audit-body">
      <div class="audit-profile">
        <div class="avatar">
          <el-icon><User /></el-icon>
        </div>
        <div class="profile-main">
          <div class="profile-title">
            <span class="nickname">{{ displayData.nickname || '-' }}</span>
            <el-tag size="small" type="warning" effect="light">ID {{ displayData.id }}</el-tag>
          </div>
          <div class="profile-sub">{{ displayData.pubdate || '暂无发布时间' }}</div>
        </div>
      </div>

      <div class="detail-grid">
        <div v-for="item in detailRows" :key="item.label" class="detail-item">
          <span class="detail-label">{{ item.label }}</span>
          <span class="detail-value">{{ item.value }}</span>
        </div>
      </div>

      <div class="section">
        <div class="section-title">风格</div>
        <div class="style-list">
          <el-tag v-for="style in styleTags" :key="style" size="small">
            {{ style }}
          </el-tag>
          <span v-if="!styleTags.length" class="empty-text">-</span>
        </div>
      </div>

      <div class="section">
        <div class="section-title">描述</div>
        <div class="description">{{ displayData.describ || '-' }}</div>
      </div>

      <div class="section">
        <div class="section-title">
          <el-icon><Picture /></el-icon>
          <span>图片</span>
        </div>
        <div v-if="previewImages.length" class="image-list">
          <el-image
            v-for="(pic, index) in previewImages"
            :key="pic"
            :src="pic"
            fit="cover"
            class="preview-image"
            :preview-src-list="previewImages"
            :initial-index="index"
            preview-teleported
          />
        </div>
        <span v-else class="empty-text">-</span>
      </div>

      <el-form label-position="top" class="audit-form">
        <el-form-item label="审核理由">
          <el-input
            v-model="reason"
            type="textarea"
            :rows="3"
            maxlength="120"
            show-word-limit
            placeholder="审核不通过时填写"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button :disabled="loading" @click="localVisible = false">取消</el-button>
      <el-button
        type="danger"
        :icon="CloseBold"
        :loading="loading"
        :disabled="loading"
        @click="handleAudit(AuditFlag.REJECT)"
      >
        不通过
      </el-button>
      <el-button
        type="primary"
        :icon="Check"
        :loading="loading"
        :disabled="loading"
        @click="handleAudit(AuditFlag.PASS)"
      >
        通过
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.audit-body {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
  max-height: 68vh;
  overflow-y: auto;
  padding-right: 4px;
}

:global(.ypat-audit-dialog) {
  max-width: calc(100vw - 32px);
}

.audit-profile {
  display: flex;
  align-items: center;
  gap: $spacing-base;
  padding: $spacing-base;
  background: #f7f9fc;
  border: 1px solid #e6ebf2;
  border-radius: $radius-base;
}

.avatar {
  display: inline-flex;
  flex: 0 0 44px;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  color: #2f7fd2;
  background: #eaf3ff;
  border-radius: 50%;
}

.profile-main {
  min-width: 0;
}

.profile-title {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
  align-items: center;
}

.nickname {
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.profile-sub {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border: 1px solid #e6ebf2;
  border-radius: $radius-base;
  overflow: hidden;
}

.detail-item {
  min-width: 0;
  padding: 12px 14px;
  border-right: 1px solid #e6ebf2;
  border-bottom: 1px solid #e6ebf2;
}

.detail-item:nth-child(3n) {
  border-right: 0;
}

.detail-item:nth-last-child(-n + 3) {
  border-bottom: 0;
}

.detail-label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: #909399;
}

.detail-value {
  display: block;
  overflow: hidden;
  font-size: 14px;
  color: #303133;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.section {
  min-width: 0;
}

.section-title {
  display: flex;
  gap: 6px;
  align-items: center;
  margin-bottom: $spacing-sm;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.style-list {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.description {
  padding: 12px 14px;
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
  background: #fafafa;
  border: 1px solid #e6ebf2;
  border-radius: $radius-base;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: $spacing-sm;
}

.preview-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: $radius-sm;
  overflow: hidden;
  border: 1px solid #e6ebf2;
}

.empty-text {
  color: #909399;
}

.audit-form {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

@media (max-width: 720px) {
  .detail-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .detail-item:nth-child(3n) {
    border-right: 1px solid #e6ebf2;
  }

  .detail-item:nth-child(2n) {
    border-right: 0;
  }

  .detail-item:nth-last-child(-n + 3) {
    border-bottom: 1px solid #e6ebf2;
  }

  .detail-item:nth-last-child(-n + 2) {
    border-bottom: 0;
  }
}
</style>
