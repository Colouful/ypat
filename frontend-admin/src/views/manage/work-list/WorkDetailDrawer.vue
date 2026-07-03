<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getWorkDetail, type WorkAdminInfo } from '@/api/modules/work'
import { WorkStatus } from '@/constants/enums'

const props = defineProps<{
  visible: boolean
  id?: number
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const localVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
})

const loading = ref(false)
const detail = ref<WorkAdminInfo | null>(null)
let detailRequestSeq = 0

function getRecordString(record: Record<string, unknown> | undefined, key: string): string {
  const value = record?.[key]
  return typeof value === 'string' && value.trim() ? value : ''
}

function getTextValue(...values: Array<string | undefined>): string {
  for (const value of values) {
    if (typeof value === 'string' && value.trim()) {
      return value
    }
  }
  return '-'
}

function getWorkStatusInfo(status?: string) {
  const statuses = Object.values(WorkStatus)
  return statuses.find((item) => item.value === status) || { name: '未知', type: 'info' as const }
}

function getMediaTypeLabel(value?: string): string {
  if (value === '1') return '图片'
  if (value === '2') return '视频'
  return value || '-'
}

function getReturnPhotoLabel(value?: string | number): string {
  if (String(value) === '1') return '是'
  if (String(value) === '0') return '否'
  return '-'
}

const tagList = computed(() =>
  Array.isArray(detail.value?.tags) ? detail.value.tags.filter(Boolean) : [],
)

const authorName = computed(() =>
  getTextValue(detail.value?.nickname, getRecordString(detail.value?.user, 'nickname')),
)

const authorMobile = computed(() =>
  getTextValue(detail.value?.mobile, getRecordString(detail.value?.user, 'mobile')),
)

const authorCity = computed(() =>
  getTextValue(detail.value?.city, getRecordString(detail.value?.user, 'city')),
)

const authorProfession = computed(() =>
  getTextValue(detail.value?.profession, getRecordString(detail.value?.user, 'profession')),
)

const authorGender = computed(() =>
  getTextValue(detail.value?.gender, getRecordString(detail.value?.user, 'gender')),
)

async function loadDetail(): Promise<void> {
  const requestedId = props.id
  const requestSeq = ++detailRequestSeq

  detail.value = null

  if (!requestedId) {
    loading.value = false
    return
  }

  loading.value = true
  try {
    const res = await getWorkDetail(requestedId)
    if (requestSeq === detailRequestSeq && props.visible && props.id === requestedId) {
      detail.value = res.data
    }
  } finally {
    if (requestSeq === detailRequestSeq) {
      loading.value = false
    }
  }
}

watch(
  () => [props.visible, props.id] as const,
  ([visible, id], previousValue) => {
    const [prevVisible, prevId] = previousValue ?? []
    if (visible && id) {
      if (!prevVisible || prevId !== id) {
        detail.value = null
      }
      loadDetail()
      return
    }

    detailRequestSeq += 1
    detail.value = null
    loading.value = false
  },
  { immediate: true },
)
</script>

<template>
  <el-drawer
    v-model="localVisible"
    title="作品详情"
    size="720px"
    :destroy-on-close="true"
  >
    <div v-loading="loading" class="work-detail-drawer">
      <template v-if="detail">
        <div class="section">
          <div class="section-title">基础信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="作品 ID">{{ detail.id }}</el-descriptions-item>
            <el-descriptions-item label="作者">{{ authorName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ authorMobile }}</el-descriptions-item>
            <el-descriptions-item label="城市">{{ authorCity }}</el-descriptions-item>
            <el-descriptions-item label="职业">{{ authorProfession }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ authorGender }}</el-descriptions-item>
            <el-descriptions-item label="设备">
              {{ detail.device || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="拍摄地点">
              {{ detail.shootLocation || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="约拍返片">
              {{ getReturnPhotoLabel(detail.returnPhotoFlag) }}
            </el-descriptions-item>
            <el-descriptions-item label="媒体类型">
              {{ getMediaTypeLabel(detail.mediaType) }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getWorkStatusInfo(detail.status).type" size="small">
                {{ getWorkStatusInfo(detail.status).name }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="发布时间">
              {{ detail.publishTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="审核理由">
              {{ detail.auditReason || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">作品描述</div>
          <div class="description-box">{{ detail.description || '-' }}</div>
        </div>

        <div class="section">
          <div class="section-title">作品标签</div>
          <div v-if="tagList.length" class="tag-list">
            <el-tag v-for="tag in tagList" :key="tag" size="small">
              {{ tag }}
            </el-tag>
          </div>
          <el-empty v-else description="暂无标签" :image-size="72" />
        </div>

        <div class="section">
          <div class="section-title">互动数据</div>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="浏览">
              {{ detail.readCount ?? 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="点赞">
              {{ detail.likeCount ?? 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="收藏">
              {{ detail.favoriteCount ?? 0 }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="section">
          <div class="section-title">媒体内容</div>
          <div v-if="detail.medias?.length" class="media-grid">
            <div v-for="media in detail.medias" :key="media.id" class="media-card">
              <template v-if="media.type === '2'">
                <video class="media-video" :src="media.url" controls preload="metadata" />
              </template>
              <template v-else>
                <el-image
                  class="media-image"
                  :src="media.url"
                  fit="cover"
                  :preview-src-list="[media.url]"
                  preview-teleported
                />
              </template>
              <div class="media-type">{{ getMediaTypeLabel(media.type) }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无媒体" :image-size="72" />
        </div>
      </template>

      <el-empty v-else-if="!loading" description="暂无详情" :image-size="72" />
    </div>
  </el-drawer>
</template>

<style scoped lang="scss">
.work-detail-drawer {
  padding-right: 12px;
}

.section + .section {
  margin-top: $spacing-xl;
}

.section-title {
  margin-bottom: $spacing-base;
  padding-left: $spacing-sm;
  border-left: 3px solid $color-primary;
  font-size: $font-size-base;
  font-weight: 600;
  color: $text-primary;
}

.description-box {
  min-height: 88px;
  padding: $spacing-base;
  border: 1px solid $border-lighter;
  border-radius: $radius-sm;
  background: $bg-page;
  color: $text-primary;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: $spacing-base;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.media-card {
  overflow: hidden;
  border: 1px solid $border-lighter;
  border-radius: $radius-sm;
  background: #fff;
}

.media-image,
.media-video {
  display: block;
  width: 100%;
  height: 180px;
  background: #000;
}

.media-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
}

.media-type {
  padding: $spacing-sm $spacing-base;
  color: $text-secondary;
  font-size: $font-size-sm;
  text-align: center;
}
</style>
