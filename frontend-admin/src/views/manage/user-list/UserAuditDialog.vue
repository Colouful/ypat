<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getUserDetail, type OauthQo } from '@/api/modules/user'
import { AuditFlag, getUserStatusInfo } from '@/constants/enums'

const props = defineProps<{
  visible: boolean
  user: OauthQo | null
  loading: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  audit: [flag: string]
}>()

const detailLoading = ref(false)
const detail = ref<OauthQo | null>(null)

function getUserId(user?: OauthQo | null): number | undefined {
  return user?.userid ?? user?.id
}

function getPhotoLabel(index: number): string {
  const labels = ['身份证正面', '身份证反面', '手持身份证']
  return labels[index] ?? `证件照片${index + 1}`
}

// 是否已审核（已审核状态隐藏审核按钮）
const isAudited = computed(() => {
  const status = detail.value?.status
  return status === AuditFlag.PASS || status === AuditFlag.REJECT
})

// 图片预览
const previewVisible = ref(false)
const previewSrc = ref('')

/** 加载详情 */
async function loadDetail(): Promise<void> {
  if (!props.user) return

  detailLoading.value = true
  try {
    // 直接用列表传入的数据，或重新请求详情获取完整 pics
    detail.value = props.user

    // 列表接口返回 id，详情接口返回 userid；统一取可用的用户 ID。
    const userId = getUserId(props.user)
    if (userId) {
      const res = await getUserDetail(userId)
      if (res.data) {
        detail.value = res.data
      }
    }
  } catch {
    // 使用列表数据
  } finally {
    detailLoading.value = false
  }
}

/** 预览图片 */
function handlePreview(url: string): void {
  previewSrc.value = url
  previewVisible.value = true
}

/** 关闭弹窗 */
function handleClose(): void {
  emit('update:visible', false)
}

/** 审核操作 */
function handleAudit(flag: string): void {
  emit('audit', flag)
}

// 监听 visible 变化加载详情
watch(
  () => props.visible,
  (val) => {
    if (val) {
      loadDetail()
    }
  },
)
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="实名审核详情"
    width="750px"
    :close-on-click-modal="false"
    @update:model-value="handleClose"
  >
    <div v-loading="detailLoading" class="audit-dialog-content">
      <!-- 图片画廊 -->
      <div class="image-section">
        <h4 class="section-title">证件照片</h4>
        <div v-if="detail?.pics && detail.pics.length > 0" class="image-gallery">
          <div
            v-for="(pic, index) in detail.pics"
            :key="index"
            class="image-item"
            :aria-label="getPhotoLabel(index)"
            @click="handlePreview(pic)"
          >
            <div class="image-label">{{ getPhotoLabel(index) }}</div>
            <el-image
              :src="pic"
              fit="cover"
              style="width: 120px; height: 120px"
              :preview-src-list="detail.pics"
              :initial-index="index"
              preview-teleported
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>加载失败</span>
                </div>
              </template>
              <template #placeholder>
                <div class="image-placeholder">
                  <el-icon class="is-loading"><Loading /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
        </div>
        <el-empty v-else description="暂无照片" :image-size="60" />
      </div>

      <!-- 详细信息 -->
      <div class="info-section">
        <h4 class="section-title">基本信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">
            {{ getUserId(detail) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="姓名">
            {{ detail?.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="证件号码">
            {{ detail?.certcode || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag
              v-if="detail?.status"
              :type="getUserStatusInfo(detail.status).type"
              size="small"
            >
              {{ getUserStatusInfo(detail.status).name }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <template v-if="!isAudited">
          <el-button
            type="danger"
            :loading="loading"
            @click="handleAudit(AuditFlag.REJECT)"
          >
            审核不通过
          </el-button>
          <el-button
            type="success"
            :loading="loading"
            @click="handleAudit(AuditFlag.PASS)"
          >
            审核通过
          </el-button>
        </template>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.audit-dialog-content {
  max-height: 500px;
  overflow-y: auto;
}

.section-title {
  font-size: $font-size-base;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-base;
  padding-left: $spacing-sm;
  border-left: 3px solid $color-primary;
}

.image-section {
  margin-bottom: $spacing-xl;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-base;
}

.image-item {
  cursor: pointer;
  border-radius: $radius-sm;
  overflow: hidden;
  border: 1px solid $border-lighter;
  transition: all 0.2s;

  &:hover {
    border-color: $color-primary;
    box-shadow: $shadow-base;
  }
}

.image-label {
  width: 120px;
  padding: $spacing-xs;
  color: $text-regular;
  font-size: $font-size-sm;
  text-align: center;
  background-color: $bg-page;
  border-bottom: 1px solid $border-lighter;
}

.image-error,
.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  background-color: $bg-page;
  color: $text-secondary;
  font-size: $font-size-sm;
  gap: $spacing-xs;
}

.info-section {
  margin-bottom: $spacing-base;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}
</style>
